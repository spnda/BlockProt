package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.setBackButton
import de.sean.blockprot.util.setItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.util.*

object BlockInfoInventory : BlockProtInventory {
    override fun getSize() = InventoryConstants.tripleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO)

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

    fun createInventoryAndFill(player: Player, handler: BlockLockHandler): Inventory {
        val inv = createInventory()
        val state = InventoryState.get(player.uniqueId) ?: return inv
        val owner = handler.getOwner()
        val access = handler.getAccess()
        val redstone = handler.getRedstone()

        inv.clear()
        state.friendResultCache.clear()
        for (i in 0..(access.size - 1).coerceAtMost(InventoryConstants.doubleLine)) { // Maximum of 2 lines of skulls
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(access[i]))
            inv.setItem(
                InventoryConstants.lineLength + i,
                ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name)
            )
            state.friendResultCache.add(offlinePlayer)
        }

        if (owner.isNotEmpty()) inv.setItem(
            0,
            ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner)))
        )
        inv.setItemStack(
            1,
            if (redstone) Material.REDSTONE else Material.GUNPOWDER,
            if (redstone) TranslationKey.INVENTORIES__REDSTONE__ALLOWED
            else TranslationKey.INVENTORIES__REDSTONE__DISALLOWED
        )
        inv.setBackButton(InventoryConstants.lineLength - 1)

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < InventoryConstants.doubleLine && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inv.setItem(InventoryConstants.lineLength + i, skull)
                i++
            }
        }
        return inv
    }
}
