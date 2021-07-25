/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.fabric.nbt;

import de.sean.blockprot.fabric.ext.BlockEntityNbtExtension;
import de.sean.blockprot.fabric.util.PlayerUtil;
import de.sean.blockprot.nbt.FriendModifyAction;
import de.sean.blockprot.nbt.IBlockNBTHandler;
import de.sean.blockprot.nbt.IFriendHandler;
import de.sean.blockprot.nbt.LockReturnValue;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public final class BlockNBTHandler extends IBlockNBTHandler<BlockEntityNbtExtension, PlayerEntity, FriendHandler> {
    public final BlockEntity block;

    public BlockNBTHandler(@NotNull final BlockEntity block) {
        this.block = block;

        this.container = (BlockEntityNbtExtension) block;
    }

    @Override
    public @NotNull String getOwner() {
        if (!container.contains(OWNER_ATTRIBUTE)) return "";
        else return container.getString(OWNER_ATTRIBUTE);
    }

    @Override
    public void setOwner(@NotNull String owner) {
        container.putString(OWNER_ATTRIBUTE, owner);
    }

    @Override
    public @NotNull Stream<FriendHandler> getFriendsStream() {
        if (!container.contains(LOCK_ATTRIBUTE)) return Stream.empty();

        final var compound = container.getCompound(LOCK_ATTRIBUTE);
        return compound
            .getKeys()
            .stream()
            .map((k) -> new FriendHandler(k, compound.getCompound(k)));
    }

    @Override
    public void setFriends(@NotNull List<FriendHandler> access) {
        final var compound = container.getCompound(LOCK_ATTRIBUTE);
        for (IFriendHandler<?, ?> handler : access) {
            var oldCompound = ((NbtCompound) handler.getContainer());
            var newCompound = new NbtCompound();
            for (var key : oldCompound.getKeys())
                newCompound.put(key, oldCompound);
            compound.put(handler.getName(), newCompound);
        }
    }

    @Override
    public void addFriend(@NotNull String friend) {
        var compound = new NbtCompound();
        compound.putString("id", friend);
        container.getCompound(LOCK_ATTRIBUTE).put(friend, compound);
    }

    @Override
    public void removeFriend(@NotNull String friend) {
        container.getCompound(LOCK_ATTRIBUTE).remove(friend);
    }

    @Override
    public boolean getRedstone() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.contains(REDSTONE_ATTRIBUTE)) {
            container.putBoolean(REDSTONE_ATTRIBUTE, DEFAULT_REDSTONE);
            return DEFAULT_REDSTONE;
        }
        return container.getBoolean(REDSTONE_ATTRIBUTE);
    }

    @Override
    public void setRedstone(boolean redstone) {
        container.putBoolean(REDSTONE_ATTRIBUTE, redstone);
    }

    @Override
    public void applyToOtherContainer() {
        // TODO: Properly apply to double chests.
    }

    @Override
    public @NotNull LockReturnValue lockBlock(@NotNull PlayerEntity player) {
        var owner = getOwner();
        final var playerUuid = player.getUuidAsString();

        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid;
            setOwner(owner);
            this.applyToOtherContainer();
            return new LockReturnValue(true);
        } else if (owner.equals(playerUuid) || PlayerUtil.isOp(player)) {
            this.clear();
            this.applyToOtherContainer();
            return new LockReturnValue(true);
        }
        return new LockReturnValue(false);
    }

    @Override
    public @NotNull LockReturnValue lockRedstoneForBlock(@NotNull String player, @Nullable Boolean value) {
        return null;
    }

    @Override
    public @NotNull LockReturnValue modifyFriends(@NotNull String player, @NotNull String friend, @NotNull FriendModifyAction action) {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "";
    }
}
