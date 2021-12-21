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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import de.sean.blockprot.bukkit.util.BlockUtil;
import de.sean.blockprot.nbt.FriendModifyAction;
import de.sean.blockprot.nbt.LockReturnValue;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A block handler to get values and settings from a single lockable
 * block.
 *
 * @since 0.2.3
 */
public final class BlockNBTHandler extends FriendSupportingHandler<NBTCompound> {
    static final String OWNER_ATTRIBUTE = "splugin_owner";

    static final String OLD_LOCK_ATTRIBUTE = "splugin_lock";
    static final String LOCK_ATTRIBUTE = "blockprot_friends";

    static final String OLD_REDSTONE_ATTRIBUTE = "splugin_lock_redstone";
    static final String REDSTONE_ATTRIBUTE = "blockprot_redstone";

    /**
     * The backing block this handler handles.
     *
     * @since 0.2.3
     */
    @NotNull
    public final Block block;

    /**
     * Create a new handler for given {@code block}.
     *
     * @param block The block we want to use and get the
     *              NBT container for.
     * @throws RuntimeException if {@code block} is not a lockable block
     *                          or lockable tile entity.
     * @since 0.2.3
     */
    public BlockNBTHandler(@NotNull final Block block) throws RuntimeException {
        super(LOCK_ATTRIBUTE);
        this.block = block;

        if (BlockProt.getDefaultConfig().isLockableBlock(this.block.getType())) {
            container = new NBTBlock(block).getData();
        } else if (BlockProt.getDefaultConfig().isLockableTileEntity(this.block.getType())) {
            container = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        } else {
            throw new RuntimeException("Given block " + block.getType() + " is not a lockable block/tile entity");
        }
    }

    /**
     * Reads the current owner from the NBT container.
     *
     * @return The owner as a UUID-String read from the container, or an empty String.
     * @since 0.2.3
     */
    @NotNull
    public String getOwner() {
        if (!container.hasKey(OWNER_ATTRIBUTE)) return "";
        else return container.getString(OWNER_ATTRIBUTE);
    }

    /**
     * Set the current owner of this block.
     *
     * @param owner The new owner for this block. Should
     *              be a valid UUID.
     * @since 0.2.3
     */
    public void setOwner(@NotNull final String owner) {
        container.setString(OWNER_ATTRIBUTE, owner);
    }

    /**
     * Gets the redstone settings handler for this block. Will remap
     * any legacy redstone settings to the new system.
     *
     * @return The redstone settings handler.
     * @since 0.4.13
     */
    public @NotNull RedstoneSettingsHandler getRedstoneHandler() {
        RedstoneSettingsHandler redstoneHandler = new RedstoneSettingsHandler(
            container.getOrCreateCompound(REDSTONE_ATTRIBUTE));

        // Used to port the old values from previous versions to the new
        // handler based redstone system.
        if (container.hasKey(OLD_REDSTONE_ATTRIBUTE)) {
            // We flip the value because its meaning has changed.
            boolean oldValue = !container.getBoolean(OLD_REDSTONE_ATTRIBUTE);
            redstoneHandler.setAll(oldValue);
            container.removeKey(OLD_REDSTONE_ATTRIBUTE);
        }
        return redstoneHandler;
    }

    /**
     * Whether or not this block is protected. This is evaluated by checking
     * if an owner exists and if any friends have been added to the block.
     *
     * @return True, if this block is not protected and there is no owner.
     * @since 0.2.3
     */
    public boolean isNotProtected() {
        return getOwner().isEmpty() && getFriends().isEmpty();
    }

    /**
     * @return True, if this block is protected.
     * @see #isNotProtected()
     * @since 0.2.3
     */
    public boolean isProtected() {
        return !isNotProtected();
    }

    /**
     * Checks whether or not given {@code player} is the owner of this block.
     *
     * @param player A String representing a players UUID.
     * @return Whether or not {@code player} is the owner of this block.
     * @since 0.2.3
     */
    public boolean isOwner(@NotNull final String player) {
        return getOwner().equals(player);
    }

    /**
     * Checks whether or not given {@code player} can access this block.
     *
     * @param player The player to check for.
     * @return True, if {@code player} can access this block.
     * @since 0.2.3
     */
    public boolean canAccess(@NotNull final String player) {
        Optional<FriendHandler> friend = getFriend(player);
        return !isProtected() || (getOwner().equals(player) || (friend.isPresent() && friend.get().canRead()));
    }

    /**
     * Locks this block for given {@code player} as the owner.
     *
     * @param player The player to set as an owner.
     * @return A {@link LockReturnValue} whether the block was successfully locked,
     * else there might have been issues with permissions.
     * @since 0.4.6
     */
    @NotNull
    public LockReturnValue lockBlock(@NotNull final Player player) {
        String owner = getOwner();
        final String playerUuid = player.getUniqueId().toString();

        if (owner.isEmpty()) {
            // Check if the player is not exceeding their max block count.
            Integer maxBlockCount = BlockProt.getDefaultConfig().getMaxLockedBlockCount();
            if (maxBlockCount != null) {
                PlayerBlocksStatistic playerBlocksStatistic = new PlayerBlocksStatistic();
                StatHandler.getStatistic(playerBlocksStatistic, player);
                if (playerBlocksStatistic.get().size() + 1 >= maxBlockCount) {
                    return new LockReturnValue(false,
                            LockReturnValue.Reason.EXCEEDED_MAX_BLOCK_COUNT);
                }
            }

            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid;
            setOwner(owner);
            this.applyToOtherContainer();
            StatHandler.addBlock(player, block.getLocation());
            return new LockReturnValue(true, null);
        } else if (owner.equals(playerUuid) || player.isOp() || player.hasPermission(PERMISSION_ADMIN)) {
            StatHandler.removeContainer(player, block.getLocation());
            this.clear();
            this.applyToOtherContainer();
            return new LockReturnValue(true, null);
        }

        return new LockReturnValue(false, LockReturnValue.Reason.NO_PERMISSION);
    }

    /**
     * Modifies the friends of this block for given {@code action}.
     *
     * @param player The player requesting this command, should be the owner.
     * @param friend The friend do to {@code action} with.
     * @param action The action we should perform with {@code friend} on this block.
     * @return A {@link LockReturnValue} whether or not the friends were modified
     * successfully.
     * @since 0.4.6
     */
    @NotNull
    public LockReturnValue modifyFriends(@NotNull final String player, @NotNull final String friend, @NotNull final FriendModifyAction action) {
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (!isOwner(player)) return new LockReturnValue(
            false
        );

        switch (action) {
            case ADD_FRIEND: {
                if (containsFriend(friend)) {
                    return new LockReturnValue(false, null);
                } else {
                    addFriend(friend);
                    this.applyToOtherContainer();
                    return new LockReturnValue(true, null);
                }
            }
            case REMOVE_FRIEND: {
                if (containsFriend(friend)) {
                    removeFriend(friend);
                    this.applyToOtherContainer();
                    return new LockReturnValue(true, null);
                } else {
                    return new LockReturnValue(false, null);
                }
            }
            default: {
                return new LockReturnValue(false, null);
            }
        }
    }

    /**
     * @see #applyToOtherContainer(Predicate, Consumer).
     * @since 0.4.6
     */
    public void applyToOtherContainer() {
        this.applyToOtherContainer(handler -> true, handler -> {
        });
    }

    /**
     * This applies any changes to this container to a possible other
     * half. For example doors consist from two blocks, as do double
     * chests. Without this call, all methods will modify only the local,
     * current block.
     * <p>
     * This method is specifically not called on each modification of NBT,
     * as this would be a massive, unnecessary performance penalty.
     *
     * @param condition A predicate defining whether the data should be merged
     *                  over from the given {@link BlockNBTHandler}.
     * @param orElse    If {@code condition} is not true, this function can be used
     *                  as a callback when applying fails.
     * @since 0.4.10
     */
    public void applyToOtherContainer(@NotNull Predicate<BlockNBTHandler> condition, @NotNull Consumer<BlockNBTHandler> orElse) {
        if (BlockProt.getDefaultConfig().isLockableDoor(block.getType())) {
            final Block otherDoor = BlockUtil.getOtherDoorHalf(block.getState());
            if (otherDoor == null) return;
            final BlockNBTHandler otherDoorHandler = new BlockNBTHandler(otherDoor);
            if (condition.test(otherDoorHandler)) {
                otherDoorHandler.mergeHandler(this);
            } else {
                orElse.accept(otherDoorHandler);
            }
        } else if (this.block.getType() == Material.CHEST || this.block.getType() == Material.TRAPPED_CHEST) {
            final BlockState doubleChestState = BlockUtil.getDoubleChest(this.block);
            if (doubleChestState != null) {
                final BlockNBTHandler doubleChestHandler = new BlockNBTHandler(doubleChestState.getBlock());
                if (condition.test(doubleChestHandler)) {
                    doubleChestHandler.mergeHandler(this);
                } else {
                    orElse.accept(doubleChestHandler);
                }
            }
        }
    }

    /**
     * Clears all values from this block and resets it to the
     * defaults.
     *
     * @since 0.3.2
     */
    public void clear() {
        this.setOwner("");
        this.setFriends(Collections.emptyList());
        this.getRedstoneHandler().reset();
    }

    /**
     * Merges this handler with another {@link NBTHandler}.
     *
     * @param handler The handler to merge with. If {@code handler} is not an instance
     *                of {@link BlockNBTHandler}, this will do nothing.
     * @since 0.3.2
     */
    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
        if (!(handler instanceof BlockNBTHandler)) return;
        final BlockNBTHandler blockNBTHandler = (BlockNBTHandler) handler;
        this.setOwner(blockNBTHandler.getOwner());
        this.setFriends(blockNBTHandler.getFriends());
        this.getRedstoneHandler().mergeHandler(blockNBTHandler.getRedstoneHandler());
    }

    @Override
    public void pasteNbt(@NotNull NBTContainer container) {
        // We remove the owner key for security reasons.
        container.removeKey(OWNER_ATTRIBUTE);
        super.pasteNbt(container);
    }
}
