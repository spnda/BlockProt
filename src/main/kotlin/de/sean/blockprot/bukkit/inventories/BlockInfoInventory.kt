package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.util.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.util.*

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

    fun createInventoryAndFill(player: Player, handler: BlockLockHandler): Inventory {
        val inv = createInventory()
        val state = InventoryState.get(player.uniqueId) ?: return inv
        val owner = handler.getOwner()
        val access = handler.getAccess()
        val redstone = handler.getRedstone()

        inv.clear()
        state.friendResultCache.clear()
        for (i in 0..(access.size - 1).coerceAtMost(9 * 2)) { // Maximum of 2 lines of skulls
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(access[i]))
            inv.setItem(9 + i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name))
            state.friendResultCache.add(offlinePlayer)
        }

        if (owner.isNotEmpty()) inv.setItem(
            0,
            ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner)))
        )
        inv.setItem(
            1,
            ItemUtil.getItemStack(
                1,
                if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                if (redstone) Translator.get(TranslationKey.INVENTORIES__REDSTONE__ALLOWED)
                else Translator.get(TranslationKey.INVENTORIES__REDSTONE__DISALLOWED)
            )
        )
        inv.setItem(
            8,
            ItemUtil.getItemStack(
                1,
                Material.BLACK_STAINED_GLASS_PANE,
                Translator.get(TranslationKey.INVENTORIES__BACK)
            )
        )

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < 9 * 2 && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inv.setItem(9 + i, skull)
                i++
            }
        }
        return inv
    }
}
