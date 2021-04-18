package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.tr7zw.nbtapi.NBTEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

interface BlockProtInventory {
    val size: Int
    val inventoryName: String
    fun createInventory(): Inventory = Bukkit.createInventory(null, size, inventoryName)
    fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?)

    fun modifyFriends(player: Player, modify: (MutableList<String>) -> Unit) {
        val playerNBT = NBTEntity(player).persistentDataContainer
        val currentFriendList = LockUtil.parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
        modify(currentFriendList.toMutableList())
        playerNBT.setString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE, currentFriendList.toString())
    }

    fun applyChangesAndExit(handler: BlockLockHandler, player: Player, func: () -> BlockLockHandler.LockReturnValue) {
        val ret = func()
        if (ret.success) {
            LockUtil.applyToDoor(handler, handler.block)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
        }
        player.closeInventory()
    }
}
