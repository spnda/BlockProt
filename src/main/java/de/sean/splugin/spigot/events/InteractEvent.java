package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.SPlugin;
import de.sean.splugin.spigot.inventories.BlockLockInventory;
import de.sean.splugin.spigot.inventories.ChestLockInventory;
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.List;

/* Spigot */
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/* NBT API */
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
                        && !player.isSneaking()
                        && event.getItem() == null
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
                    arrow.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 255, false, false), true);
                    arrow.setInvulnerable(true);
                    arrow.addPassenger(player);
                }
                break;
            case CHEST:
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && player.hasPermission("spigot.lock")) {
                    // The user shift-left clicked the chest and is wanting to open the chest edit menu.
                    final BlockState chestState = event.getClickedBlock().getState();
                    final NBTCompound blockTile = new NBTTileEntity(chestState).getPersistentDataContainer();
                    final String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
                    final String playerUuid = player.getUniqueId().toString();
                    // Don't open the menu if the player is not the owner of this chest.
                    if ((owner == null || owner.isEmpty()) || owner.equals(playerUuid)) {
                        event.setCancelled(true);
                        SLockUtil.lock.put(playerUuid, chestState.getBlock());
                        final boolean redstone = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE);
                        Inventory inv = ChestLockInventory.inventory;
                        if (owner != null && owner.equals(playerUuid)) {
                            inv.setItem(0, SUtil.getItemStack(1, Material.CHEST, "Unlock"));
                            inv.setItem(1, SUtil.getItemStack(1, Material.REDSTONE, redstone ? "Activate Redstone" : "Deactivate Redstone"));
                            inv.setItem(2, SUtil.getItemStack(1, Material.PLAYER_HEAD, "Add Friends"));
                            inv.setItem(3, SUtil.getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"));
                        } else {
                            inv.setItem(0, SUtil.getItemStack(1, Material.CHEST, "Lock"));
                            for (int i = 1; i < 4; i++) inv.setItem(i, null);
                        }
                        inv.setItem(8, SUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"));
                        player.openInventory(inv);
                    }
                } else {
                    // The user right clicked and is trying to access the container
                    NBTTileEntity blockTileEntity = new NBTTileEntity(event.getClickedBlock().getState());
                    try {
                        String nbt = blockTileEntity.getPersistentDataContainer().getString(SLockUtil.LOCK_ATTRIBUTE);
                        if (nbt == null) break;
                        List<String> access = SUtil.parseStringList(nbt);
                        if (!access.isEmpty()) {
                            if (!access.contains(player.getUniqueId().toString())) {
                                event.setCancelled(true);
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("No permission."));
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
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && player.hasPermission("spigot.lock")) {
                    // The user shift-left clicked the chest and is wanting to open the chest edit menu.
                    final BlockState blockState = event.getClickedBlock().getState();
                    final NBTCompound blockTile = new NBTTileEntity(blockState).getPersistentDataContainer();
                    final String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
                    final String playerUuid = player.getUniqueId().toString();
                    // Don't open the menu if the player is not the owner of this chest.
                    if ((owner == null || owner.isEmpty()) || owner.equals(playerUuid)) {
                        event.setCancelled(true);
                        SLockUtil.lock.put(playerUuid, blockState.getBlock());
                        final boolean redstone = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE);
                        Inventory inv = BlockLockInventory.inventory;
                        if (owner != null && owner.equals(playerUuid)) {
                            inv.setItem(0, SUtil.getItemStack(1, blockState.getType(), "Unlock"));
                            inv.setItem(1, SUtil.getItemStack(1, Material.REDSTONE, redstone ? "Activate Redstone" : "Deactivate Redstone"));
                            inv.setItem(2, SUtil.getItemStack(1, Material.PLAYER_HEAD, "Add Friends"));
                            inv.setItem(3, SUtil.getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"));
                        } else {
                            inv.setItem(0, SUtil.getItemStack(1, blockState.getType(), "Lock"));
                            for (int i = 1; i < 4; i++) inv.setItem(i, null);
                        }
                        inv.setItem(8, SUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"));
                        player.openInventory(inv);
                    }
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
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("No permission."));
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
}
