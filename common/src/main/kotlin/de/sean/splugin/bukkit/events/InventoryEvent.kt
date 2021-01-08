package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.inventories.*
import de.sean.splugin.bukkit.nbt.BlockLockHandler
import de.sean.splugin.bukkit.nbt.LockUtil
import de.sean.splugin.util.ItemUtil.getItemStack
import de.sean.splugin.util.ItemUtil.getPlayerSkull
import de.sean.splugin.util.Vector3f
import de.tr7zw.nbtapi.NBTTileEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class InventoryEvent : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent?) {
        //SLockUtil.lock.remove(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (event.view.title) {
            BlockLockInventory.INVENTORY_NAME -> {
                val inv: Inventory
                val playersCol = Bukkit.getOnlinePlayers()
                val block: Block?
                val handler: BlockLockHandler
                val owner: String
                when (item.type) {
                    in LockUtil.lockableBlocks -> {
                        block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString()))
                        if (block == null) return
                        handler = BlockLockHandler(NBTTileEntity(block.state))
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.lockBlock(player.uniqueId.toString(), player.isOp, if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                        if (ret.first) {
                            player.closeInventory()
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.second))
                        }
                        event.isCancelled = true
                    }
                    Material.REDSTONE, Material.GUNPOWDER -> {
                        block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString()))
                        if (block == null) return
                        handler = BlockLockHandler(NBTTileEntity(block.state))
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.lockRedstoneForBlock(player.uniqueId.toString(), if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                        if (ret.first) {
                            player.closeInventory()
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.second))
                        }
                        event.isCancelled = true
                    }
                    Material.PLAYER_HEAD -> {
                        player.closeInventory()
                        inv = FriendAddInventory.inventory
                        inv.clear()
                        block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString()))
                        if (block == null) return
                        handler = BlockLockHandler(NBTTileEntity(block.state))
                        owner = handler.getOwner()
                        val currentFriends = handler.getAccess() // All players that already have access
                        val possibleFriends = playersCol.toTypedArray() // All players online
                        val friendsToAdd: MutableList<Player> = ArrayList() // New list of everyone that doesn't have access *yet*
                        for (possibleFriend in possibleFriends) {
                            val possible = possibleFriend.uniqueId.toString()
                            if (!currentFriends.contains(possible) && possible != owner) friendsToAdd.add(possibleFriend)
                        }
                        var i = 0
                        while (i < 9 * 3 - 2 && i < friendsToAdd.size) {
                            inv.setItem(i, getPlayerSkull(friendsToAdd[i]))
                            i++
                        }
                        inv.setItem(9 * 3 - 1, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                        player.openInventory(inv)
                        event.isCancelled = true
                    }
                    Material.ZOMBIE_HEAD -> {
                        player.closeInventory()
                        inv = FriendRemoveInventory.inventory
                        inv.clear()
                        block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString()))
                        if (block == null) return
                        handler = BlockLockHandler(NBTTileEntity(block.state))
                        val friends = handler.getAccess()
                        var i = 0
                        while (i < 9 * 3 - 2 && i < friends.size) {
                            inv.setItem(i, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(friends[i]))))
                            i++
                        }
                        inv.setItem(9 * 3 - 1, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                        player.openInventory(inv)
                        event.isCancelled = true
                    }
                    Material.OAK_SIGN -> {
                        player.closeInventory()
                        inv = BlockInfoInventory.inventory
                        block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString()))
                        if (block == null) return
                        handler = BlockLockHandler(NBTTileEntity(block.state))
                        owner = handler.getOwner()
                        val access = handler.getAccess()
                        var i = 0
                        while (i < access.size && i < 9) {
                            inv.setItem(9 + i, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(access[i]))))
                            i++
                        }
                        inv.setItem(0, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner))))
                        inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                        player.openInventory(inv)
                    }
                    Material.BLACK_STAINED_GLASS_PANE -> player.closeInventory()
                    else -> player.closeInventory()
                }
            }
            FriendAddInventory.INVENTORY_NAME -> {
                val block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString())) ?: return
                val handler = BlockLockHandler(NBTTileEntity(block.state))
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val redstone = handler.getRedstone()
                    val inv = BlockLockInventory.inventory
                    inv.setItem(0, getItemStack(1, block.state.type, "Unlock"))
                    inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
                    inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                    inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                    if (player.isOp) inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                    inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                    player.openInventory(inv)
                } else if (item.type == Material.PLAYER_HEAD) {
                    val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                    val friend = skull.owningPlayer?.uniqueId.toString()
                    val doubleChest = getDoubleChest(block, player.world)
                    val ret = handler.addFriend(player.uniqueId.toString(), friend, if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                    if (ret.first) {
                        player.closeInventory()
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.second))
                    }
                }
                event.isCancelled = true
            }
            FriendRemoveInventory.INVENTORY_NAME -> {
                val block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString())) ?: return
                val handler = BlockLockHandler(NBTTileEntity(block.state))
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val redstone = handler.getRedstone()
                    val inv = BlockLockInventory.inventory
                    inv.setItem(0, getItemStack(1, block.state.type, "Unlock"))
                    inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
                    inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                    inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                    if (player.isOp) inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                    inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                    player.openInventory(inv)
                } else if (item.type == Material.PLAYER_HEAD) {
                    val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                    val friend = skull.owningPlayer?.uniqueId.toString()
                    val doubleChest = getDoubleChest(block, player.world)
                    val ret = handler.removeFriend(player.uniqueId.toString(), friend, if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                    if (ret.first) {
                        player.closeInventory()
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.second))
                    }
                }
                event.isCancelled = true
            }
            BlockInfoInventory.INVENTORY_NAME -> {
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val block = getBlockFromLocation(player, LockUtil.get(player.uniqueId.toString())) ?: return
                    val handler = BlockLockHandler(NBTTileEntity(block.state))
                    val redstone = handler.getRedstone()
                    val inv = BlockLockInventory.inventory
                    inv.setItem(0, getItemStack(1, block.state.type, "Unlock"))
                    inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
                    inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                    inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                    if (player.isOp) inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                    inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                    player.openInventory(inv)
                }
                event.isCancelled = true
            }
        }
    }

    private fun getBlockFromLocation(player: Player, location: Vector3f?): Block? {
        if (location == null) return null
        return player.world.getBlockAt(location.getXInt(), location.getYInt(), location.getZInt())
    }

    private fun getDoubleChest(block: Block, world: World): BlockState? {
        var doubleChest: DoubleChest? = null
        val chestState = block.state
        if (chestState is Chest) {
            val inventory = chestState.inventory
            if (inventory is DoubleChestInventory) {
                doubleChest = inventory.holder
            }
        }
        if (doubleChest == null) return null
        val second = doubleChest.location

        when {
            block.x > second.x -> second.subtract(.5, 0.0, 0.0)
            block.z > second.z -> second.subtract(0.0, 0.0, .5)
            else -> second.add(.5, 0.0, .5)
        }

        return world.getBlockAt(second).state
    }
}
