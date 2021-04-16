package de.sean.blockprot.bukkit.events

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.inventories.*
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.ItemUtil.getItemStack
import de.sean.blockprot.util.ItemUtil.getPlayerSkull
import de.sean.blockprot.util.Strings
import de.sean.blockprot.util.Vector3f
import de.tr7zw.nbtapi.NBTEntity
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
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
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
                val handler: BlockLockHandler
                val owner: String
                when (item.type) {
                    in LockUtil.lockableTileEntities, in LockUtil.lockableBlocks -> {
                        block = InventoryState.get(player.uniqueId)?.block
                        if (block == null) return
                        handler = BlockLockHandler(block)
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.lockBlock(player, player.isOp, if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                        if (ret.success) {
                            applyToDoor(handler, block)
                            player.closeInventory()
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                        }
                        event.isCancelled = true
                    }
                    Material.REDSTONE, Material.GUNPOWDER -> {
                        block = InventoryState.get(player.uniqueId)?.block
                        if (block == null) return
                        handler = BlockLockHandler(block)
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.lockRedstoneForBlock(player.uniqueId.toString(), if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                        if (ret.success) {
                            applyToDoor(handler, block)
                            player.closeInventory()
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                        }
                        event.isCancelled = true
                    }
                    Material.PLAYER_HEAD -> {
                        inv = FriendAddInventory.createInventory()
                        inv.clear()
                        block = InventoryState.get(player.uniqueId)?.block
                        if (block == null) return
                        handler = BlockLockHandler(block)
                        owner = handler.getOwner()
                        val currentFriends = handler.getAccess() // All players that already have access
                        val possibleFriends = playersCol.toTypedArray() // All players online
                        val friendsToAdd: MutableList<Player> = ArrayList() // New list of everyone that doesn't have access *yet*
                        for (possibleFriend in possibleFriends) {
                            val possible = possibleFriend.uniqueId.toString()
                            if (!currentFriends.contains(possible) && possible != owner) friendsToAdd.add(possibleFriend)
                        }
                        var i = 0
                        while (i < 9 * 3 - 3 && i < friendsToAdd.size) {
                            inv.setItem(i, getPlayerSkull(friendsToAdd[i]))
                            i++
                        }
                        inv.setItem(9 * 3 - 2, getItemStack(1, Material.MAP, Strings.SEARCH))
                        inv.setItem(9 * 3 - 1, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
                        player.closeInventory()
                        player.openInventory(inv)
                        event.isCancelled = true
                    }
                    Material.ZOMBIE_HEAD -> {
                        inv = FriendRemoveInventory.createInventory()
                        inv.clear()
                        block = InventoryState.get(player.uniqueId)?.block
                        if (block == null) return
                        handler = BlockLockHandler(block)
                        val friends = handler.getAccess()
                        // Get the skulls asynchronously and add them one after each other.
                        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
                            var i = 0
                            val skulls: MutableList<ItemStack> = ArrayList()
                            while (i < 9 * 3 - 2 && i < friends.size) {
                                val skull = getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(friends[i])))
                                skulls.add(skull)
                                i++
                            }
                            Bukkit.getScheduler().runTask(BlockProt.instance) { _ ->
                                for (skull in skulls.indices)
                                    inv.setItem(skull, skulls[skull])
                            }
                        }
                        inv.setItem(9 * 3 - 1, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
                        player.closeInventory()
                        player.openInventory(inv)
                        event.isCancelled = true
                    }
                    Material.OAK_SIGN -> {
                        player.closeInventory()
                        inv = BlockInfoInventory.createInventory()
                        block = InventoryState.get(player.uniqueId)?.block
                        if (block == null) return
                        handler = BlockLockHandler(block)
                        owner = handler.getOwner()
                        val access = handler.getAccess()
                        var i = 0
                        inv.clear() // If any items are still in the inventory from last request, clear them
                        while (i < access.size && i < 9) {
                            inv.setItem(9 + i, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(access[i]))))
                            i++
                        }
                        if (owner.isNotEmpty()) inv.setItem(0, getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner))))
                        inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
                        player.openInventory(inv)
                    }
                    Material.BLACK_STAINED_GLASS_PANE -> player.closeInventory()
                    else -> player.closeInventory()
                }
            }
            FriendAddInventory.INVENTORY_NAME -> {
                val block = InventoryState.get(player.uniqueId)?.block ?: return
                val handler = BlockLockHandler(block)
                when (item.type) {
                    Material.BLACK_STAINED_GLASS_PANE -> {
                        player.closeInventory()
                        val inv = BlockLockInventory.createInventoryAndFill(player, block.state.type, handler)
                        player.openInventory(inv)
                    }
                    Material.PLAYER_HEAD -> {
                        val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                        val friend = skull.owningPlayer?.uniqueId.toString()
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.addFriend(
                            player.uniqueId.toString(),
                            friend,
                            if (doubleChest != null) NBTTileEntity(doubleChest) else null
                        )
                        if (ret.success) {
                            applyToDoor(handler, block)
                            player.closeInventory()
                            player.spigot()
                                .sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                        }
                    }
                    Material.MAP -> {
                        FriendSearchInventory.openAnvilInventory(player)
                    }
                    else -> {}
                }
                event.isCancelled = true
            }
            FriendRemoveInventory.INVENTORY_NAME -> {
                val block = InventoryState.get(player.uniqueId)?.block ?: return
                val handler = BlockLockHandler(block)
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val inv = BlockLockInventory.createInventoryAndFill(player, block.state.type, handler)
                    player.openInventory(inv)
                } else if (item.type == Material.PLAYER_HEAD) {
                    val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                    val friend = skull.owningPlayer?.uniqueId.toString()
                    val doubleChest = getDoubleChest(block, player.world)
                    val ret = handler.removeFriend(player.uniqueId.toString(), friend, if (doubleChest != null) NBTTileEntity(doubleChest) else null)
                    if (ret.success) {
                        applyToDoor(handler, block)
                        player.closeInventory()
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                    }
                }
                event.isCancelled = true
            }
            BlockInfoInventory.INVENTORY_NAME -> {
                if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
                    player.closeInventory()
                    val block = InventoryState.get(player.uniqueId)?.block ?: return
                    val handler = BlockLockHandler(block)
                    val inv = BlockLockInventory.createInventoryAndFill(player, block.state.type, handler)
                    player.openInventory(inv)
                }
                event.isCancelled = true
            }
            FriendSearchResultInventory.INVENTORY_NAME -> {
                when (item.type) {
                    Material.BLACK_STAINED_GLASS_PANE -> {
                        // Go back to the search if nothing was found
                        FriendSearchInventory.openAnvilInventory(player)
                    }
                    Material.PLAYER_HEAD -> {
                        val block = InventoryState.get(player.uniqueId)?.block ?: return
                        val handler = BlockLockHandler(block)
                        val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                        val friend = skull.owningPlayer?.uniqueId.toString()
                        val doubleChest = getDoubleChest(block, player.world)
                        val ret = handler.addFriend(
                            player.uniqueId.toString(),
                            friend,
                            if (doubleChest != null) NBTTileEntity(doubleChest) else null
                        )
                        if (ret.success) {
                            applyToDoor(handler, block)
                            player.spigot()
                                .sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                        }
                        player.closeInventory()
                    }
                    Material.BARRIER -> {
                        player.closeInventory()
                    }
                    else -> {}
                }
                event.isCancelled = true
            }
            UserSettingsInventory.INVENTORY_NAME -> {
                when (item.type) {
                    Material.BARRIER -> {
                        // Lock on place button, default value is true
                        val nbtEntity = NBTEntity(player).persistentDataContainer
                        nbtEntity.setBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE, !nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE))
                        val lockOnPlace = nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE)
                        Bukkit.getLogger().info(lockOnPlace.toString())
                        event.inventory.setItem(0, getItemStack(
                            1,
                            Material.BARRIER,
                            if (lockOnPlace) Strings.USER_SETTINGS_LOCK_ON_PLACE_DEACTIVATE
                            else Strings.USER_SETTINGS_LOCK_ON_PLACE_ACTIVATE
                        ))
                        event.isCancelled = true
                    }
                    Material.BLACK_STAINED_GLASS_PANE -> player.closeInventory()
                    else -> {}
                }
            }
        }
    }

    /**
     * Copy the data over from the top/bottom door to the other half
     */
    private fun applyToDoor(doorHandler: BlockLockHandler, block: Block) {
        if (block.type in listOf(Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.CRIMSON_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.WARPED_DOOR)) {
            val blockState = block.state
            val door = blockState.blockData as Door
            var other = blockState.location
            other = if (door.half == Bisected.Half.TOP) other.subtract(0.0, 1.0, 0.0)
            else other.add(0.0, 1.0, 0.0)
            val otherDoor = blockState.world.getBlockAt(other)
            val otherDoorHandler = BlockLockHandler(otherDoor)
            otherDoorHandler.setOwner(doorHandler.getOwner())
            otherDoorHandler.setAccess(doorHandler.getAccess())
            otherDoorHandler.setRedstone(doorHandler.getRedstone())
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
