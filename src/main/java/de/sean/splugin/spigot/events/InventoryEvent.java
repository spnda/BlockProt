package de.sean.splugin.spigot.events;

import de.sean.splugin.spigot.inventories.*;
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class InventoryEvent implements Listener {
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        //SLockUtil.lock.remove(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (event.getView().getTitle()) {
            case ChestLockInventory.INVENTORY_NAME:
            case BlockLockInventory.INVENTORY_NAME:
                Inventory inv;
                Collection<? extends Player> playersCol = Bukkit.getOnlinePlayers();
                Block block;
                NBTCompound blockTile;
                String owner;
                switch (item.getType()) {
                    case CHEST:
                    case FURNACE:
                    case HOPPER:
                    case BARREL:
                    case SHULKER_BOX:
                        lockBlock(player);
                        event.setCancelled(true);
                        break;
                    case REDSTONE:
                        lockRedstoneForBlock(player);
                        event.setCancelled(true);
                        break;
                    case PLAYER_HEAD:
                        player.closeInventory();
                        inv = FriendAddInventory.inventory;
                        inv.clear();
                        block = SLockUtil.lock.get(player.getUniqueId().toString());
                        if (block == null) return;
                        blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
                        owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
                        List<String> currentFriends = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE)); // All players that already have access
                        Player[] possibleFriends = playersCol.toArray(new Player[0]); // All players online
                        List<Player> friendsToAdd = new ArrayList<>(); // New list of everyone that doesn't have access *yet*
                        for (Player possibleFriend : possibleFriends) {
                            String possible = possibleFriend.getUniqueId().toString();
                            if (!currentFriends.contains(possible) && !possible.equals(owner)) friendsToAdd.add(possibleFriend);
                        }
                        for (int i = 0; i < 9 * 3 - 2 && i < friendsToAdd.size(); i++) {
                            inv.setItem(i, SUtil.getPlayerSkull(friendsToAdd.get(i)));
                        }
                        inv.setItem(9 * 3 - 1, SUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"));
                        player.openInventory(inv);
                        event.setCancelled(true);
                        break;
                    case ZOMBIE_HEAD:
                        player.closeInventory();
                        inv = FriendRemoveInventory.inventory;
                        inv.clear();
                        block = SLockUtil.lock.get(player.getUniqueId().toString());
                        if (block == null) return;
                        blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
                        List<String> friends = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
                        for (int i = 0; i < 9 * 3 - 2 && i < friends.size(); i++) {
                            inv.setItem(i, SUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(friends.get(i)))));
                        }
                        inv.setItem(9 * 3 - 1, SUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"));
                        player.openInventory(inv);
                        event.setCancelled(true);
                        break;
                    case OAK_SIGN:
                        player.closeInventory();
                        inv = BlockInfoInventory.inventory;
                        block = SLockUtil.lock.get(player.getUniqueId().toString());
                        if (block == null) return;
                        blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
                        owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
                        inv.setItem(0, SUtil.getPlayerSkull(Bukkit.getPlayer(UUID.fromString(owner))));
                        List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
                        for (int i = 0; i < access.size() && i < 9; i++) {
                            inv.setItem(9 + i, SUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(access.get(i)))));
                        }
                        player.openInventory(inv);
                    case BLACK_STAINED_GLASS_PANE:
                        player.closeInventory();
                        break;
                }
                break;
            case FriendAddInventory.INVENTORY_NAME:
                if (item.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                    player.closeInventory();
                    player.openInventory(ChestLockInventory.inventory);
                    break;
                }
                addFriend(player, item);
                event.setCancelled(true);
                break;
            case FriendRemoveInventory.INVENTORY_NAME:
                if (item.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                    player.closeInventory();
                    player.openInventory(ChestLockInventory.inventory);
                    break;
                }
                removeFriend(player, item);
                event.setCancelled(true);
        }
    }

    private void lockRedstoneForBlock(Player player) {
        final Block block = SLockUtil.lock.get(player.getUniqueId().toString());
        if (block == null) return;
        final NBTCompound blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
        // Allow the owner to deactivate redstone for this block.
        if (owner.equals(player.getUniqueId().toString())) {
            boolean redstone = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE);
            blockTile.setBoolean(SLockUtil.REDSTONE_ATTRIBUTE, !redstone);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(redstone ? "Redstone protection removed." : "Redstone protection added."));
        }
        player.closeInventory();
        SLockUtil.lock.remove(player.getUniqueId().toString());
    }

    private void lockBlock(Player player) {
        final Block block = SLockUtil.lock.get(player.getUniqueId().toString());
        if (block == null) return;
        final BlockState blockState = block.getState();
        final NBTCompound blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
        if (owner == null || owner.equals("")) {
            owner = player.getUniqueId().toString();
            blockTile.setString(SLockUtil.OWNER_ATTRIBUTE, owner);
            applyToDoubleChest(block, player, owner, null);

            player.closeInventory();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Permission granted."));
            SLockUtil.lock.remove(player.getUniqueId().toString());
        } else if (owner.equals(player.getUniqueId().toString())) {
            blockTile.setString(SLockUtil.OWNER_ATTRIBUTE, null);
            applyToDoubleChest(block, player, null, null);
            player.closeInventory();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Unlocked."));
        }
        SLockUtil.lock.remove(player.getUniqueId().toString());
    }

    private void addFriend(Player player, ItemStack itemStack) {
        // Get the player from the player head in itemStack.
        if (itemStack.getType() != Material.PLAYER_HEAD) return;
        SkullMeta skull = (SkullMeta)itemStack.getItemMeta();
        // Generic player head?
        if (skull == null) return;
        String friend = skull.getOwningPlayer().getUniqueId().toString();

        final Block block = SLockUtil.lock.get(player.getUniqueId().toString());
        if (block == null) return;
        final NBTCompound blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
        // The requesting player is not the owner of this Chest. Ignore this request.
        // We'll still check for this. Though the InteractEvent Handler should already check this for us.
        if (!owner.equals(player.getUniqueId().toString())) return;
        final List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
        if (!access.contains(friend)) {
            access.add(friend);
            System.out.println(access);
            blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
            applyToDoubleChest(block, player, owner, access);
            player.closeInventory();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Permission granted."));
        }
        SLockUtil.lock.remove(player.getUniqueId().toString());
    }

    private void removeFriend(Player player, ItemStack itemStack) {
        // Get the player from the player head in itemStack.
        if (itemStack.getType() != Material.PLAYER_HEAD) return;
        SkullMeta skull = (SkullMeta) itemStack.getItemMeta();
        // Generic player head?
        if (skull == null) return;
        String friend = skull.getOwningPlayer().getUniqueId().toString();

        final Block block = SLockUtil.lock.get(player.getUniqueId().toString());
        if (block == null) return;
        final NBTCompound blockTile = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        String owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE);
        // The requesting player is not the owner of this Chest. Ignore this request.
        // We'll still check for this. Though the InteractEvent Handler should already check this for us.
        if (!owner.equals(player.getUniqueId().toString())) return;
        final List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
        if (access.contains(friend)) {
            access.remove(friend);
            System.out.println(access);
            blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
            applyToDoubleChest(block, player, owner, access);
            player.closeInventory();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Permission granted."));
        }
        SLockUtil.lock.remove(player.getUniqueId().toString());
    }

    private void applyToDoubleChest(Block block, Player player, String owner, List<String> access) {
        DoubleChest doubleChest = null;
        final BlockState chestState = block.getState();
        if (chestState instanceof Chest) {
            final Inventory inventory = ((Chest) chestState).getInventory();
            if (inventory instanceof DoubleChestInventory) {
                doubleChest = (DoubleChest) inventory.getHolder();
            }
        }
        // If this wasn't a double chest, just ignore.
        if (doubleChest == null) return;
        final Location secChest = doubleChest.getLocation();
        // If we are targeting the further away chest block, get the closer one
        // (Closer/Further away from 0, 0, 0)
        if (block.getX() > secChest.getX()) secChest.subtract(.5, 0, 0);
        else if (block.getLocation().getZ() > secChest.getZ()) secChest.subtract(0, 0, .5);
        else secChest.add(.5, 0, .5);
        final NBTCompound secTile = new NBTTileEntity(player.getWorld().getBlockAt(secChest).getState()).getPersistentDataContainer();
        secTile.setString(SLockUtil.OWNER_ATTRIBUTE, owner);
        if (access != null) secTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString());
    }
}
