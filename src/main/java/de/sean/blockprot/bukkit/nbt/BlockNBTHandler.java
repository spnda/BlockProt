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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockNBTHandler extends NBTHandler<NBTCompound> {
    static final String OWNER_ATTRIBUTE = "splugin_owner";
    static final String LOCK_ATTRIBUTE = "splugin_lock";
    static final String REDSTONE_ATTRIBUTE = "splugin_lock_redstone";

    public final Block block;

    public BlockNBTHandler(@NotNull final Block block) {
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
     * As of 0.3.0 we expect a list of compounds, in which we can
     * store the access flags and other future settings.
     * Therefore we will remap the values here. This will possibly
     * be removed in a future version.
     */
    private void remapAccess(@NotNull final List<String> originalList) {
        NBTCompound access = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        for (String key : originalList) {
            access.getOrCreateCompound(key);
        }
    }

    /**
     * Gets a {@link Stream} of {@link FriendPlayer} for this block.
     */
    @NotNull
    private Stream<FriendPlayer> getFriendsStream() {
        if (!container.hasKey(LOCK_ATTRIBUTE)) return Stream.empty();
        final List<String> stringList = LockUtil.parseStringList(container.getString(LOCK_ATTRIBUTE));
        if (!stringList.isEmpty())
            remapAccess(stringList);

        return container.getCompoundList(LOCK_ATTRIBUTE)
            .stream()
            .map((c) -> (FriendPlayer)c);
    }

    /**
     * Gets the list of friends that are allowed to access the container.
     * @deprecated Use {@link #getFriends()} instead.
     * @return A list of UUID-Strings which each represent a player's UUID.
     */
    @NotNull
    @Deprecated
    public List<String> getAccess() {
        if (!container.hasKey(LOCK_ATTRIBUTE)) return new ArrayList<>();
        final List<String> stringList = LockUtil.parseStringList(container.getString(LOCK_ATTRIBUTE));
        if (!stringList.isEmpty())
            remapAccess(stringList);

        return container.getCompoundList(LOCK_ATTRIBUTE)
            .stream()
            .map(NBTCompound::getName)
            .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of friends for this block.
     */
    @NotNull
    public List<FriendPlayer> getFriends() {
        return getFriendsStream().collect(Collectors.toList());
    }

    /**
     * Filters the results of {@link #getFriends()} for any entry which
     * id qualifies for {@link String#equals(Object)}.
     * @param id The String ID to check for. Usually a UUID as a String as {@link UUID#toString()}.
     * @return The first {@link FriendPlayer} found, or none.
     */
    @NotNull
    public Optional<FriendPlayer> getFriend(@NotNull final String id) {
        return getFriendsStream()
            .filter((f) -> f.getName().equals(id))
            .findFirst();
    }

    /**
     * Set the current list of friends that have access to this block.
     */
    @Deprecated
    public void setAccess(@NotNull final List<String> access) {
        NBTCompound compound = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        for (String value : access) {
            compound.getOrCreateCompound(value);
        }
    }

    /**
     * Set a new list of PlayerNBTHandler for the friends list.
     */
    public void setFriends(@NotNull final List<FriendPlayer> access) {
        NBTCompound compound = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        for (FriendPlayer handler : access) {
            NBTCompound newCompound = compound.getOrCreateCompound(handler.getName());
            newCompound.mergeCompound(handler);
        }
    }

    /**
     * If true, redstone should be allowed for this block and should not be blocked.
     * If redstone has not been set for this block yet, the default value is true
     * @return Whether redstone should be allowed or not.
     */
    public boolean getRedstone() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(REDSTONE_ATTRIBUTE)) {
            container.setBoolean(REDSTONE_ATTRIBUTE, true);
            return true;
        }
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
    public LockReturnValue lockRedstoneForBlock(@NotNull final String player, @Nullable final NBTTileEntity doubleChest, @Nullable final Boolean value) {
        final String owner = getOwner();
        if (owner.equals(player)) { // Simpler than #isOwner
            boolean redstone = value == null ? !getRedstone() : value;
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
            final BlockNBTHandler otherDoorHandler = new BlockNBTHandler(otherDoor);
            otherDoorHandler.setOwner(this.getOwner());
            otherDoorHandler.setAccess(this.getAccess());
            otherDoorHandler.setRedstone(this.getRedstone());
        }
    }
}
