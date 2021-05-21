package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockReturnValue
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.tr7zw.changeme.nbtapi.NBTEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface BlockProtInventory {
    val size: Int
    val inventoryName: String
    fun createInventory(): Inventory = Bukkit.createInventory(null, size, inventoryName)
    fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?)

    /**
     * Modify the friends list in the [player]'s NBT at [LockUtil.DEFAULT_FRIENDS_ATTRIBUTE].
     */
    fun modifyFriends(player: Player, exit: Boolean, modify: (MutableList<String>) -> Unit) {
        val playerNBT = NBTEntity(player).persistentDataContainer
        val currentFriendList = LockUtil.parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE)).toMutableList()
        modify(currentFriendList)
        playerNBT.setString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE, currentFriendList.toString())
        if (exit) {
            player.closeInventory()
            InventoryState.remove(player.uniqueId)
        }
    }

    /**
     * Apply changes and create a handler with a callback in which the caller can freely use
     * the given handler. If [exit] is true, the players inventory will be closed.
     */
    fun applyChanges(block: Block, player: Player, exit: Boolean, func: (handler: BlockLockHandler) -> LockReturnValue) {
        val handler = BlockLockHandler(block)
        val ret = func(handler)
        if (ret.success) {
            LockUtil.applyToDoor(handler, handler.block)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
        }
        if (exit) {
            player.closeInventory()
            InventoryState.remove(player.uniqueId)
        }
    }

    /**
     * Finds the index of the first item in [inventory] where [ItemStack.equals(item)]
     * results in true. Returns -1 if no item was found.
     */
    fun findItemIndex(inventory: Inventory, item: ItemStack): Int {
        for (i in inventory.contents.indices) {
            if (inventory.contents[i].equals(item)) {
                return i
            }
        }
        return -1
    }
}
