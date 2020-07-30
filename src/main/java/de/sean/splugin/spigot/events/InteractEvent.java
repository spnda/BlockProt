package de.sean.splugin.spigot.events;

/* Spigot */
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;
import de.tr7zw.nbtapi.NBTTileEntity;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.sean.splugin.App;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InteractEvent implements Listener {
    @EventHandler
    public void PlayerInteract(final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        switch (event.getClickedBlock().getType()) {
            case CHEST:
            case FURNACE:
            case HOPPER:
            case ANVIL:

            // Doors
            case OAK_DOOR:
            case BIRCH_DOOR:
            case SPRUCE_DOOR:
            case JUNGLE_DOOR:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case CRIMSON_DOOR:
            case WARPED_DOOR:
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    // The user left clicked to edit the block
                    DoubleChest doubleChest = null;
                    BlockState chestState = event.getClickedBlock().getState();
                    if (chestState instanceof Chest) {
                        Chest chest = (Chest) chestState;
                        Inventory inventory = chest.getInventory();
                        if (inventory instanceof DoubleChestInventory) {
                            doubleChest = (DoubleChest) inventory.getHolder();
                        }
                    }

                    NBTTileEntity blockTileEntity = new NBTTileEntity(chestState);
                    NBTCompound blockTile = blockTileEntity.getPersistentDataContainer();
                    String nbt = blockTile.getString(SLockUtil.LOCK_ATTRIBUTE);
                    List<String> access = SUtil.parseStringList(nbt);
                    Player player = event.getPlayer();
                    UUID playerUUID = player.getUniqueId();
                    String playerUUIDString = playerUUID.toString();

                    SLockUtil.LockData infoData = SLockUtil.info.get(playerUUID);
                    if (infoData != null) {
                        if (System.currentTimeMillis() - infoData.timeRequested <= 120000) {
                            player.sendMessage(ChatColor.GREEN + "Lock Info: \n" + ChatColor.RESET + access.toString());
                        }
                        SLockUtil.removeUserFromInfo(playerUUID);
                    }
                    if (access.contains(playerUUIDString)) {
                        // Permission granted. Do whatever you want.
                        SLockUtil.LockData data = SLockUtil.locking.get(playerUUID);
                        SLockUtil.GivePermData giveData = SLockUtil.givingPermission.get(playerUUID);
                        if (data != null) {
                            // The user requested private/public modification
                            if (!data.action) {
                                access.remove(playerUUIDString);
                                blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                                SLockUtil.removeUserFromLocking(playerUUID);
                                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Zugriff entfernt."));
                            } else {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Zugriff bereits gegeben."));
                            }
                        } else if (giveData != null) {
                            // The user requested add/remove modification
                            if (access.contains(giveData.uuidToGive.toString()) && giveData.action) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Zugriff ist dem Spieler bereits gegeben."));
                            } else {
                                if (giveData.action) {
                                    access.add(giveData.uuidToGive.toString());
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Neuem Spieler Zugriff gegeben."));
                                } else {
                                    access.remove(giveData.uuidToGive.toString());
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Spieler Zugriff entfernt."));
                                }
                                blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                                SLockUtil.removeUserFromGiving(playerUUID);
                            }
                        }
                    } else {
                        if (access.size() == 0) {
                            // Nobody owns this
                            SLockUtil.LockData data = SLockUtil.locking.get(playerUUID);
                            if (data != null) {
                                if (data.action) {
                                    access.add(playerUUIDString);
                                    blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                                    SLockUtil.removeUserFromLocking(playerUUID);
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Zugriff gegeben."));
                                }
                            }
                        } else {
                            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Kein Zugriff."));
                        }
                    }

                    // If we have a double chest we will have to add the NBT Tag to both TileEntities.
                    if (doubleChest != null) {
                        Location secChest = doubleChest.getLocation();
                        // If we are targeting the further away chest block, get the closer one
                        // (Closer/Further away from 0, 0, 0)
                        if (event.getClickedBlock().getLocation().getX() > secChest.getX()) secChest.subtract(.5, 0, .5);
                        else secChest.add(.5, 0, .5);
                        BlockState secChestState = event.getPlayer().getWorld().getBlockAt(secChest).getState();
                        NBTTileEntity secTileEntity = new NBTTileEntity(secChestState);

                        NBTCompound secTile = secTileEntity.getPersistentDataContainer();
                        secTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                    }
                } else {
                    // The user right clicked and is trying to access the container
                    NBTTileEntity blockTileEntity = new NBTTileEntity(event.getClickedBlock().getState());
                    NBTCompound blockTile = blockTileEntity.getPersistentDataContainer();
                    try {
                        String nbt = blockTile.getString(SLockUtil.LOCK_ATTRIBUTE);
                        if (nbt == null) break;
                        List<String> access = SUtil.parseStringList(nbt);
                        if (access.isEmpty()) {
                            event.setCancelled(false);
                        } else {
                            if (!access.contains(event.getPlayer().getUniqueId().toString())) {
                                event.setCancelled(true);
                                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Kein Zugriff."));
                            }
                        }
                    } catch (Exception e) {
                        App.getInstance().getLogger().severe("Tile NBT could not be read. " + e.toString());
                    }
                }
                break;
            default:
                // If it's a flint and steel or TNT being used, log it. This could be a griefing attempt.
                ItemStack item = event.getItem();
                if (item == null) return;
                Player p = event.getPlayer();
                Location location = p.getLocation();
                switch (item.getType()) {
                    case FLINT_AND_STEEL:
                        App.getInstance().getLogger().info(p.getName() + " used flint and steel at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                        break;
                    case TNT:
                        App.getInstance().getLogger().info(p.getName() + " placed TNT at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                    default:
                        break;
                }
                break;
        }
    }
}
