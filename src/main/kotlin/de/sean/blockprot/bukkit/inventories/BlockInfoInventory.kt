package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

object BlockInfoInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName = Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO)

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        if (item.type == Material.BLACK_STAINED_GLASS_PANE) {
            player.closeInventory()
            val block = InventoryState.get(player.uniqueId)?.block ?: return
            val handler = BlockLockHandler(block)
            val inv = BlockLockInventory.createInventoryAndFill(player, block.state.type, handler)
            player.openInventory(inv)
        }
        event.isCancelled = true
    }
}
