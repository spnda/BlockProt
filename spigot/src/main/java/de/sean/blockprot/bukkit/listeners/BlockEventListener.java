/*
 * Copyright (C) 2021 - 2024 spnda
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

package de.sean.blockprot.bukkit.listeners;

import com.google.common.collect.Iterables;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.Permissions;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.util.BlockUtil;
import de.sean.blockprot.nbt.LockReturnValue;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class BlockEventListener implements Listener {
    private final BlockProt blockProt;

    public BlockEventListener(@NotNull BlockProt blockProt) {
        this.blockProt = blockProt;
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) return;
        // The event hasn't been cancelled already, we'll check if need
        // to cancel it manually.
        BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
        // If the block is protected by any user, prevent it from burning down.
        if (handler.isProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) return; // We only want to check for lockable blocks.

        BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
        if (!handler.isOwner(event.getPlayer().getUniqueId().toString()) && handler.isProtected()) {
            // Prevent unauthorized players from breaking locked blocks.
            event.setCancelled(true);
        }

        // If access is not cancelled and the player is allowed to break the block.
        if (!event.isCancelled()) {
            StatHandler.removeContainer(event.getPlayer(), event.getBlock());

            // For blocks, we want to clear the NBT data, as that lives
            // independently of the actual block state.
            handler.clear();
        }
    }

    /**
     * We need to catch shulker box breaks separately with the lowest priority possible,
     * as otherwise other plugins might have cancelled it and a player could dupe the box.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShulkerBoxBreak(BlockBreakEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockableShulkerBox(event.getBlock().getType())) return;

        BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
        if (handler.isOwner(event.getPlayer().getUniqueId().toString()) && (!event.isCancelled() && event.isDropItems() && event.getPlayer().getGameMode() != GameMode.CREATIVE)) {
            // The player can break the block. We will now check if it's a shulker box,
            // so we can add NBT to the shulker box that it gets locked upon placing again.
            event.setDropItems(false); // Prevent the event from dropping items itself
            Collection<ItemStack> itemsToDrop = event.getBlock().getDrops();
            if (itemsToDrop.isEmpty()) return;

            var item = Iterables.getFirst(itemsToDrop, null); // Shulker blocks should only have a single drop anyway
            if (item == null) return;

            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                final var meta = item.getItemMeta();
                final var pdc = meta.getPersistentDataContainer();
                pdc.set(new NamespacedKey(BlockProt.getInstance(), "shulker_data"), PersistentDataType.STRING, "Hi!");
                item.setItemMeta(meta);

                // Since Minecraft 1.20.6 item stacks use components instead of NBT.
                // The block_entity_data tag works like the BlockEntityTag used to, but requires an "id" field for the type of block.
                final var nbt = NBT.itemStackToNBT(item);
                final var entityData = nbt.getOrCreateCompound("components").getOrCreateCompound("minecraft:block_entity_data");
                entityData.setString("id", item.getType().getKey().toString());
                entityData.getOrCreateCompound("PublicBukkitValues").mergeCompound(handler.getNbtCopy());
                item = Objects.requireNonNull(NBT.itemStackFromNBT(nbt));
            } else {
                NBT.modify(item, readWriteItemNBT -> {
                    readWriteItemNBT.getOrCreateCompound("BlockEntityTag").getOrCreateCompound("PublicBukkitValues").mergeCompound(handler.getNbtCopy());
                });
            }

            event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);

            event.getBlock().setType(Material.AIR);
            event.setCancelled(true); // So that other plugins don't fiddle with this.
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!event.getPlayer().hasPermission(Permissions.LOCK.key())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) return;

        Block block = event.getBlockPlaced();
        String playerUuid = event.getPlayer().getUniqueId().toString();
        BlockNBTHandler handler = new BlockNBTHandler(block);

        // We only try to lock the block if it isn't locked already.
        // Shulker boxes might already be locked, from previous placing.
        if (handler.isNotProtected()) {
            PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(event.getPlayer());

            // Lock the block instantly if the setting is enabled.
            if (settingsHandler.getLockOnPlace()) {
                BlockLockOnPlaceEvent lockOnPlaceEvent = new BlockLockOnPlaceEvent(event.getBlock(), event.getPlayer());

                Bukkit.getPluginManager().callEvent(lockOnPlaceEvent);
                if (!lockOnPlaceEvent.isCancelled()) {
                    LockReturnValue lock = handler.lockBlock(event.getPlayer());
                    if (!lock.success) {
                        event.setCancelled(true);
                        if (lock.reason != null) {
                            event.getPlayer().spigot().sendMessage(
                                    ChatMessageType.ACTION_BAR,
                                    TextComponent.fromLegacyText(Translator.get(lock.reason)));
                        }
                        return;
                    }

                    settingsHandler.getFriendsStream()
                        .filter(fh -> PluginIntegration.filterFriendByUuidForAll(UUID.fromString(fh.getName()), event.getPlayer(), block))
                        .forEach(handler::addFriend);
                }

                if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace()) {
                    handler.getRedstoneHandler().setAll(false);
                }
            }

            Bukkit.getScheduler().runTaskLater(
                this.blockProt,
                () -> {
                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                        // We cannot use BlockNBTHandler#applyToOtherContainer, because we want the
                        // data to be copied to this new chest, instead of the old chest being effectively
                        // cleared.
                        final BlockState doubleChestState = BlockUtil.getDoubleChest(block);
                        if (doubleChestState != null) {
                            final BlockNBTHandler doubleChestHandler = new BlockNBTHandler(doubleChestState.getBlock());
                            if (doubleChestHandler.isNotProtected() || doubleChestHandler.isOwner(playerUuid)) {
                                handler.mergeHandler(doubleChestHandler);
                            } else {
                                // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                                event.getPlayer().getWorld().getBlockAt(block.getLocation()).breakNaturally();
                            }

                            // Remove the container as we break it, but also remove it when successful to remove duplicates.
                            StatHandler.removeContainer(event.getPlayer(), block);
                        }
                    } else {
                        handler.setName(BlockUtil.getHumanReadableBlockName(block.getType()));
                        handler.applyToOtherContainer();
                    }
                },
                1
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(@NotNull final BlockPhysicsEvent event) {
        // We check for the type directly to immediately discard any other blocks we don't
        // want to handle here to minimize the slowdown of this event handler. We specifically
        // don't allow sand, gravel, or concrete powder to be lockable as those might severely
        // increase the amount of BlockNBTHandler checks we need to perform.
        if (event.getChangedType().toString().contains("ANVIL") &&
            BlockProt.getDefaultConfig().isLockableBlock(event.getChangedType())) {
            BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());

            if (handler.isProtected())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChanged(@NotNull final SignChangeEvent event) {
        final Block block = event.getBlock();
        if (!BlockProt.getDefaultConfig().isLockableBlock(block.getType())) return;

        final BlockNBTHandler handler = new BlockNBTHandler(block);
        if (!handler.isProtected()) return;

        final Player player = event.getPlayer();
        final String playerUuid = player.getUniqueId().toString();
        if (handler.isOwner(playerUuid)) return;

        final var friend = handler.getFriend(playerUuid);
        if (friend.isPresent() && friend.get().canWrite()) return;

        event.setCancelled(true);
    }
}
