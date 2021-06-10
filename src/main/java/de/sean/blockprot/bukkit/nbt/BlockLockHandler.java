/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.TranslationKey;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class BlockLockHandler extends LockHandler<NBTCompound> {
    public final Block block;

    public BlockLockHandler(@NotNull final Block block) {
        super();
        this.block = block;

        if (LockUtil.INSTANCE.isLockableBlock(this.block.getType())) {
            container = new NBTBlock(block).getData();
        } else if (LockUtil.INSTANCE.isLockableTileEntity(this.block.getType())) {
            container = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        } else {
            throw new RuntimeException("Given block " + block.getType() + " is not a lockable block/tile entity");
        }
    }

    /**
     * Reads the current owner from the NBT container.
     * @return The owner as a UUID-String read from the container, or an empty String.
     */
    @NotNull
    public String getOwner() {
        if (!container.hasKey(OWNER_ATTRIBUTE)) return "";
        else return container.getString(OWNER_ATTRIBUTE);
    }

    /**
     * Set the current owner of this block.
     */
    public void setOwner(@NotNull final String owner) {
        container.setString(OWNER_ATTRIBUTE, owner);
    }

    /**
     * Gets the list of friends that are allowed to access the container.
     * @return A list of UUID-Strings which each represent a player's UUID.
     */
    @NotNull
    public List<String> getAccess() {
        if (!container.hasKey(LOCK_ATTRIBUTE)) return new ArrayList<>();
        return LockUtil.parseStringList(container.getString(LOCK_ATTRIBUTE));
    }

    /**
     * Set the current list of friends that have access to this block.
     */
    public void setAccess(@NotNull final List<String> access) {
        container.setString(LOCK_ATTRIBUTE, access.toString());
    }

    /**
     * Read the access flags of this block.
     */
    @NotNull
    public EnumSet<BlockAccessFlag> getBlockAccessFlags() {
        if (!container.hasKey(ACCESS_FLAGS_ATTRIBUTE)) return EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE);
        else return BlockAccessFlag.parseFlags(container.getInteger(ACCESS_FLAGS_ATTRIBUTE));
    }

    /**
     * Sets the access flags for this block. ORs all flags together to one integer, then
     * writes all of them to ACCESS_FLAGS_ATTRIBUTE.
     */
    public void setBlockAccessFlags(@NotNull final EnumSet<BlockAccessFlag> flags) {
        container.setInteger(ACCESS_FLAGS_ATTRIBUTE, flags.stream().mapToInt(BlockAccessFlag::getFlag).sum());
    }

    /**
     * If true, redstone should be allowed for this block and should not be blocked.
     * If redstone has not been set for this block yet, the default value is true
     * @return Whether redstone should be allowed or not.
     */
    public boolean getRedstone() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(REDSTONE_ATTRIBUTE)) return true;
        return container.getBoolean(REDSTONE_ATTRIBUTE);
    }

    /**
     * Set the new value for redstone. See {@link #getRedstone()} for more
     * details on the values.
     */
    public void setRedstone(final boolean redstone) {
        container.setBoolean(REDSTONE_ATTRIBUTE, redstone);
    }

    /**
     * Whether or not this block is protected. This is evaluated by checking
     * if an owner exists and if any friends have been added to the block.
     */
    public boolean isNotProtected() {
        return getOwner().isEmpty() && getAccess().isEmpty();
    }

    /**
     * See {@link #isNotProtected()}
     */
    public boolean isProtected() {
        return !isNotProtected();
    }

    /**
     * Checks whether or not given {@code player} is the owner of this block.
     * @param player A String representing a players UUID.
     */
    public boolean isOwner(@NotNull final String player) {
        return getOwner().equals(player);
    }

    /**
     * Checks whether or not given [player] can access this block.
     */
    public boolean canAccess(@NotNull final String player) {
        return !isProtected() || (getOwner().equals(player) || getAccess().contains(player));
    }

    @NotNull
    public LockReturnValue lockBlock(@NotNull final Player player, final boolean isOp, @Nullable final NBTTileEntity doubleChest) {
        String owner = getOwner();
        final String playerUuid = player.getUniqueId().toString();

        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid;
            setOwner(owner);
            if (doubleChest != null)
                doubleChest.getPersistentDataContainer().setString(OWNER_ATTRIBUTE, owner);
            return new LockReturnValue(true, TranslationKey.MESSAGES__PERMISSION_GRANTED);
        } else if (isOwner(playerUuid) || isOp || player.hasPermission(PERMISSION_ADMIN)) {
            setOwner(""); setAccess(Collections.emptyList());
            if (doubleChest != null) {
                doubleChest.getPersistentDataContainer().setString(OWNER_ATTRIBUTE, "");
                doubleChest.getPersistentDataContainer().setString(OWNER_ATTRIBUTE, "[]"); // Also clear the friends.
            }
            return new LockReturnValue(true, TranslationKey.MESSAGES__UNLOCKED);
        }
        return new LockReturnValue(false, TranslationKey.MESSAGES__NO_PERMISSION);
    }

    @NotNull
    public LockReturnValue lockRedstoneForBlock(@NotNull final String player, @Nullable final NBTTileEntity doubleChest) {
        final String owner = getOwner();
        if (owner.equals(player)) { // Simpler than #isOwner
            boolean redstone;
            if (!container.hasKey(REDSTONE_ATTRIBUTE)) {
                /* We assume that our current value is true, and we'll therefore change it off */
                redstone = false;
            } else {
                redstone = !getRedstone();
            }
            setRedstone(redstone);
            if (doubleChest != null)
                doubleChest.getPersistentDataContainer().setBoolean(REDSTONE_ATTRIBUTE, redstone);
            return new LockReturnValue(true, redstone ? TranslationKey.MESSAGES__REDSTONE_REMOVED : TranslationKey.MESSAGES__REDSTONE_ADDED);
        }
        return new LockReturnValue(false, TranslationKey.MESSAGES__NO_PERMISSION);
    }

    @NotNull
    public LockReturnValue modifyFriends(@NotNull final String player, @NotNull final String friend, @NotNull final FriendModifyAction action, @Nullable final NBTTileEntity doubleChest) {
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (!isOwner(player)) return new LockReturnValue(
            false,
            TranslationKey.MESSAGES__NO_PERMISSION
        );

        final List<String> access = getAccess();
        switch (action) {
            case ADD_FRIEND: {
                if (access.contains(friend)) {
                    return new LockReturnValue(false, TranslationKey.MESSAGES__FRIEND_ALREADY_ADDED);
                } else {
                    access.add(friend);
                    setAccess(access);
                    if (doubleChest != null)
                        doubleChest.getPersistentDataContainer().setString(LOCK_ATTRIBUTE, access.toString());
                    return new LockReturnValue(true, TranslationKey.MESSAGES__FRIEND_ADDED);
                }
            }
            case REMOVE_FRIEND: {
                if (access.contains(friend)) {
                    access.remove(friend);
                    setAccess(access);
                    if (doubleChest != null)
                        doubleChest.getPersistentDataContainer().setString(LOCK_ATTRIBUTE, access.toString());
                    return new LockReturnValue(true, TranslationKey.MESSAGES__FRIEND_REMOVED);
                } else {
                    return new LockReturnValue(false, TranslationKey.MESSAGES__FRIEND_CANT_BE_REMOVED);
                }
            }
            default: {
                return new LockReturnValue(false, "Unknown error occured.");
            }
        }
    }

    public void applyToDoor(@NotNull final Block block) {
        if (LockUtil.INSTANCE.isLockableDoor(block.getType())) {
            final Door door = (Door) block.getState();
            final Location other = block.getState().getLocation();
            if (door.getHalf() == Bisected.Half.TOP) {
                other.subtract(0f, 1f, 0f);
            } else {
                other.add(0f, 1f, 0f);
            }

            final Block otherDoor = block.getWorld().getBlockAt(other);
            final BlockLockHandler otherDoorHandler = new BlockLockHandler(otherDoor);
            otherDoorHandler.setOwner(this.getOwner());
            otherDoorHandler.setAccess(this.getAccess());
            otherDoorHandler.setRedstone(this.getRedstone());
        }
    }
}
