package de.sean.splugin.spigot.events

import de.sean.splugin.spigot.inventories.*
import de.sean.splugin.util.SLockUtil
import de.sean.splugin.util.SUtil.getItemStack
import de.sean.splugin.util.SUtil.getPlayerSkull
import de.sean.splugin.util.SUtil.parseStringList
import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTTileEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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
                val blockTile: NBTCompound
                val owner: String
                when (item.type) {
                    Material.CHEST, Material.FURNACE, Material.HOPPER, Material.BARREL, Material.SHULKER_BOX -> {
                        lockBlock(player)
                        event.isCancelled = true
                    }
                    Material.REDSTONE, Material.GUNPOWDER -> {
                        lockRedstoneForBlock(player)
                        event.isCancelled = true
                    }
                    Material.PLAYER_HEAD -> {
                        player.closeInventory()
                        inv = FriendAddInventory.inventory
                        inv.clear()
                        block = SLockUtil.lock[player.uniqueId.toString()]
                        if (block == null) return
                        blockTile = NBTTileEntity(block.state).persistentDataContainer
                        owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
                        val currentFriends = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE)) // All players that already have access
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
                        block = SLockUtil.lock[player.uniqueId.toString()]
                        if (block == null) return
                        blockTile = NBTTileEntity(block.state).persistentDataContainer
                        val friends = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
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
                        block = SLockUtil.lock[player.uniqueId.toString()]
                        if (block == null) return
                        blockTile = NBTTileEntity(block.state).persistentDataContainer
                        owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
                        val access = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
                        var i = 0
                        while (i < access.size && i < 9) {
                            inv.setItem(9 + i, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(access[i]))))
                            i++
                        }
                        inv.setItem(0, getPlayerSkull(Bukkit.getPlayer(UUID.fromString(owner))!!))
                        inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                        player.openInventory(inv)
                    }
                    Material.BLACK_STAINED_GLASS_PANE -> player.closeInventory()
                    else -> player.closeInventory()
                }
            }
            FriendAddInventory.INVENTORY_NAME -> {
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
                    val blockTile = NBTTileEntity(block.state).persistentDataContainer
                    val redstone: Boolean = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)
                    val inv = BlockLockInventory.inventory
                    inv.setItem(0, getItemStack(1, block.state.type, "Unlock"))
                    inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
                    inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                    inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                    if (player.isOp) inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                    inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                    player.openInventory(inv)
                }
                addFriend(player, item)
                event.isCancelled = true
            }
            FriendRemoveInventory.INVENTORY_NAME -> {
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
                    val blockTile = NBTTileEntity(block.state).persistentDataContainer
                    val redstone: Boolean = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)
                    val inv = BlockLockInventory.inventory
                    inv.setItem(0, getItemStack(1, block.state.type, "Unlock"))
                    inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
                    inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                    inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                    if (player.isOp) inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                    inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                    player.openInventory(inv)
                }
                removeFriend(player, item)
                event.isCancelled = true
            }
            BlockInfoInventory.INVENTORY_NAME -> {
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
                    val blockTile = NBTTileEntity(block.state).persistentDataContainer
                    val redstone: Boolean = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)
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

    private fun lockRedstoneForBlock(player: Player) {
        val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
        val blockTile = NBTTileEntity(block.state).persistentDataContainer
        val owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
        // Allow the owner to deactivate redstone for this block.
        if (owner == player.uniqueId.toString()) {
            val redstone = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)
            blockTile.setBoolean(SLockUtil.REDSTONE_ATTRIBUTE, !redstone)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(if (redstone) "Redstone protection removed." else "Redstone protection added."))
        }
        player.closeInventory()
        SLockUtil.lock.remove(player.uniqueId.toString())
    }

    private fun lockBlock(player: Player) {
        val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
        val blockTile = NBTTileEntity(block.state).persistentDataContainer
        var owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
        if (owner == null || owner == "") {
            owner = player.uniqueId.toString()
            blockTile.setString(SLockUtil.OWNER_ATTRIBUTE, owner)
            applyToDoubleChest(block, player, owner, null)
            player.closeInventory()
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("Permission granted."))
            SLockUtil.lock.remove(player.uniqueId.toString())
        } else if (owner == player.uniqueId.toString()) {
            blockTile.setString(SLockUtil.OWNER_ATTRIBUTE, null)
            applyToDoubleChest(block, player, null, null)
            player.closeInventory()
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("Unlocked."))
        }
        SLockUtil.lock.remove(player.uniqueId.toString())
    }

    private fun addFriend(player: Player, itemStack: ItemStack) {
        // Get the player from the player head in itemStack.
        if (itemStack.type != Material.PLAYER_HEAD) return
        val skull = itemStack.itemMeta as SkullMeta? ?: return
        // Generic player head?
        val friend = skull.owningPlayer?.uniqueId.toString()
        val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
        val blockTile = NBTTileEntity(block.state).persistentDataContainer
        val owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
        // The requesting player is not the owner of this Chest. Ignore this request.
        // We'll still check for this. Though the InteractEvent Handler should already check this for us.
        if (owner != player.uniqueId.toString()) return
        var access = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
        if (!access.contains(friend)) {
            access = access.plus(friend)
            println(access)
            blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString())
            applyToDoubleChest(block, player, owner, access)
            player.closeInventory()
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("Permission granted."))
        }
        SLockUtil.lock.remove(player.uniqueId.toString())
    }

    private fun removeFriend(player: Player, itemStack: ItemStack) {
        // Get the player from the player head in itemStack.
        if (itemStack.type != Material.PLAYER_HEAD) return
        val skull = itemStack.itemMeta as SkullMeta? ?: return
        // Generic player head?
        val friend = skull.owningPlayer!!.uniqueId.toString()
        val block = SLockUtil.lock[player.uniqueId.toString()] ?: return
        val blockTile = NBTTileEntity(block.state).persistentDataContainer
        val owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
        // The requesting player is not the owner of this Chest. Ignore this request.
        // We'll still check for this. Though the InteractEvent Handler should already check this for us.
        if (owner != player.uniqueId.toString()) return
        var access = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
        if (access.contains(friend)) {
            access = access.minus(friend)
            println(access)
            blockTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString())
            applyToDoubleChest(block, player, owner, access)
            player.closeInventory()
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("Permission granted."))
        }
        SLockUtil.lock.remove(player.uniqueId.toString())
    }

    private fun applyToDoubleChest(block: Block, player: Player, owner: String?, access: List<String>?) {
        var doubleChest: DoubleChest? = null
        val chestState = block.state
        if (chestState is Chest) {
            val inventory = chestState.inventory
            if (inventory is DoubleChestInventory) {
                doubleChest = inventory.holder
            }
        }
        // If this wasn't a double chest, just ignore.
        if (doubleChest == null) return
        val secChest = doubleChest.location
        // If we are targeting the further away chest block, get the closer one
        // (Closer/Further away from 0, 0, 0)
        if (block.x > secChest.x) secChest.subtract(.5, 0.0, 0.0) else if (block.location.z > secChest.z) secChest.subtract(0.0, 0.0, .5) else secChest.add(.5, 0.0, .5)
        val secTile = NBTTileEntity(player.world.getBlockAt(secChest).state).persistentDataContainer
        secTile.setString(SLockUtil.OWNER_ATTRIBUTE, owner)
        if (access != null) secTile.setString(SLockUtil.LOCK_ATTRIBUTE, access.toString())
    }
}
