package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.SPlugin;
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.List;
import java.util.UUID;

/* Spigot */
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/* NBT API */
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;

public class InteractEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        if (SUtil.isPlayerAFK(player.getUniqueId())) {
            SUtil.setPlayerAFK(player.getUniqueId(), false);
            SMessages.unmarkPlayerAFK(player);
        }

        if (event.getClickedBlock() == null) return;
        switch (event.getClickedBlock().getType()) {
            case PURPUR_STAIRS:
            case OAK_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case STONE_BRICK_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_STAIRS:
            case BIRCH_STAIRS:
            case JUNGLE_STAIRS:
            case CRIMSON_STAIRS:
            case WARPED_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case PRISMARINE_STAIRS:
            case PRISMARINE_BRICK_STAIRS:
            case DARK_PRISMARINE_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case POLISHED_GRANITE_STAIRS:
            case MOSSY_STONE_BRICK_STAIRS:
            case POLISHED_DIORITE_STAIRS:
            case MOSSY_COBBLESTONE_STAIRS:
            case END_STONE_BRICK_STAIRS:
            case STONE_STAIRS:
            case SMOOTH_SANDSTONE_STAIRS:
            case SMOOTH_QUARTZ_STAIRS:
            case GRANITE_STAIRS:
            case ANDESITE_STAIRS:
            case RED_NETHER_BRICK_STAIRS:
            case POLISHED_ANDESITE_STAIRS:
            case DIORITE_STAIRS:
            case BLACKSTONE_STAIRS:
            case POLISHED_BLACKSTONE_STAIRS:
            case POLISHED_BLACKSTONE_BRICK_STAIRS:
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                        && !SPlugin.instance.getConfig().getBoolean("DisableChairSitting")
                        && player.hasPermission("splugin.sit")) {
                    final Block block = event.getClickedBlock();
                    if (block == null) break;
                    final Location location = event.getClickedBlock().getLocation().clone();
                    if (location.getWorld() == null) break;
                    // Check the above block
                    final Location aboveLocation = location.clone().add(0, 1, 0);
                    if (!aboveLocation.getBlock().getType().equals(Material.AIR)) {
                        // if the above block is not a air block, prevent the player from sitting down.
                        break;
                    }
                    location.add(.5, .075, .4);
                    final Arrow arrow = (Arrow)location.getWorld().spawnEntity(location, EntityType.ARROW);
                    arrow.setInvulnerable(true);
                    arrow.addPassenger(player);
                }
                break;
            case CHEST:
                if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.hasPermission("spigot.lock")) {
                    // The user left clicked to edit the chest
                    DoubleChest doubleChest = null;
                    final BlockState chestState = event.getClickedBlock().getState();
                    if (chestState instanceof Chest) {
                        final Inventory inventory = ((Chest) chestState).getInventory();
                        if (inventory instanceof DoubleChestInventory) {
                            doubleChest = (DoubleChest) inventory.getHolder();
                        }
                    }

                    final NBTCompound blockTile = new NBTTileEntity(chestState).getPersistentDataContainer();
                    final List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));

                    List<String> newAccess = handleLock(access, event);
                    blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, newAccess.toString());

                    final SLockUtil.LockData redstoneData = SLockUtil.redstone.get(event.getPlayer().getUniqueId());
                    if (redstoneData != null) {
                        if (System.currentTimeMillis() - redstoneData.timeRequested < 120000) {
                            if (access.contains(event.getPlayer().getUniqueId().toString())) {
                                blockTile.setBoolean(SLockUtil.REDSTONE_ATTRIBUTE, !blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE));
                            }
                        }
                        SLockUtil.redstone.remove(event.getPlayer().getUniqueId());
                    }

                    // If we have a double chest we will have to add the NBT Tag to both TileEntities.
                    if (doubleChest != null) {
                        final Location secChest = doubleChest.getLocation();
                        // If we are targeting the further away chest block, get the closer one
                        // (Closer/Further away from 0, 0, 0)
                        if (event.getClickedBlock().getLocation().getX() > secChest.getX()) secChest.subtract(.5, 0, 0);
                        else if (event.getClickedBlock().getLocation().getZ() > secChest.getZ()) secChest.subtract(0, 0, .5);
                        else secChest.add(.5, 0, .5);
                        final NBTCompound secTile = new NBTTileEntity(player.getWorld().getBlockAt(secChest).getState()).getPersistentDataContainer();
                        secTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                    }
                } else {
                    // The user right clicked and is trying to access the container
                    NBTTileEntity blockTileEntity = new NBTTileEntity(event.getClickedBlock().getState());
                    try {
                        String nbt = blockTileEntity.getPersistentDataContainer().getString(SLockUtil.LOCK_ATTRIBUTE);
                        if (nbt == null) break;
                        List<String> access = SUtil.parseStringList(nbt);
                        if (access.isEmpty()) {
                            event.setCancelled(false);
                        } else {
                            if (!access.contains(player.getUniqueId().toString())) {
                                event.setCancelled(true);
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Kein Zugriff."));
                            }
                        }
                    } catch (Exception e) {
                        SPlugin.instance.getLogger().severe("Tile NBT could not be read. " + e.toString());
                    }
                }
                break;
            case FURNACE:
            case HOPPER:
            case BARREL:
            case SHULKER_BOX:
                if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.hasPermission("spigot.lock")) {
                    final NBTCompound blockTile = new NBTTileEntity(event.getClickedBlock().getState()).getPersistentDataContainer();

                    final String nbt = blockTile.getString(SLockUtil.LOCK_ATTRIBUTE);
                    final List<String> access = SUtil.parseStringList(nbt);

                    handleLock(access, event);
                    blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
                } else {
                    // The user right clicked and is trying to access the container
                    final String nbt = new NBTTileEntity(event.getClickedBlock().getState()).getString(SLockUtil.LOCK_ATTRIBUTE);
                    if (nbt == null) break;
                    final List<String> access = SUtil.parseStringList(nbt);
                    if (access.isEmpty()) {
                        event.setCancelled(false);
                    } else {
                        if (!access.contains(player.getUniqueId().toString())) {
                            event.setCancelled(true);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Kein Zugriff."));
                        }
                    }
                }
                break;
            default:
                // If it's a flint and steel or TNT being used, log it. This could be a griefing attempt.
                ItemStack item = event.getItem();
                if (item == null) return;
                Location location = player.getLocation();
                switch (item.getType()) {
                    case FLINT_AND_STEEL:
                        SPlugin.instance.getLogger().info(player.getName() + " used flint and steel at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                        break;
                    case TNT:
                        SPlugin.instance.getLogger().info(player.getName() + " placed TNT at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                    default:
                        break;
                }
                break;
        }
    }

    private List<String> handleLock(List<String> access, PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();
        final String playerUUIDString = playerUUID.toString();

        final SLockUtil.LockData infoData = SLockUtil.info.get(playerUUID);
        if (infoData != null) {
            if (System.currentTimeMillis() - infoData.timeRequested <= 120000) {
                player.sendMessage(ChatColor.GREEN + "Lock Info: \n" + ChatColor.RESET + access.toString());
            }
            SLockUtil.removeUserFromInfo(playerUUID);
        }
        final SLockUtil.LockData removeLockingdata = SLockUtil.removingLocking.get(playerUUID);
        if (removeLockingdata != null) {
            if (System.currentTimeMillis() - removeLockingdata.timeRequested < 120000) {
                access.clear();
            }
            SLockUtil.removeRemoveLockingForUser(playerUUID);
        }
        if (access.contains(playerUUIDString)) {
            // Permission granted. Do whatever you want.
            final SLockUtil.LockData data = SLockUtil.locking.get(playerUUID);
            final SLockUtil.GivePermData giveData = SLockUtil.givingPermission.get(playerUUID);
            if (data != null) {
                // The user requested private/public modification
                if (!data.action) {
                    access.remove(playerUUIDString);
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
                    SLockUtil.removeUserFromGiving(playerUUID);
                }
            }
        } else {
            if (access.size() == 0) {
                // Nobody owns this
                final SLockUtil.LockData data = SLockUtil.locking.get(playerUUID);
                if (data != null) {
                    if (data.action) {
                        access.add(playerUUIDString);
                        SLockUtil.removeUserFromLocking(playerUUID);
                        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Zugriff gegeben."));
                    }
                }
            } else {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Kein Zugriff."));
            }
        }

        return access;
    }
}
