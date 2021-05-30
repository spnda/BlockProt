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
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import java.util.*

class BlockInfoInventory : BlockProtInventory() {
    override fun getSize() = InventoryConstants.tripleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO)

    override fun onClick(event: InventoryClickEvent, state: InventoryState) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                player.closeInventory()
                if (state.block == null) return
                val handler = BlockLockHandler(state.block)
                val inv = BlockLockInventory().fill(player, state.block.state.type, handler)
                player.openInventory(inv)
            }
            Material.CYAN_STAINED_GLASS_PANE -> {
                if (state.friendPage >= 1) {
                    state.friendPage = state.friendPage - 1;

                    player.closeInventory();
                    if (state.block != null) player.openInventory(fill(player, BlockLockHandler(state.block)));
                }
            }
            Material.BLUE_STAINED_GLASS_PANE -> {
                val lastFriendInInventory = inventory.getItem(InventoryConstants.tripleLine - 1);
                if (lastFriendInInventory != null && lastFriendInInventory.amount == 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning we should go to the next page.
                    state.friendPage = state.friendPage + 1;

                    player.closeInventory();
                    if (state.block != null) player.openInventory(fill(player, BlockLockHandler(state.block)));
                }
            }
            else -> {
            }
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {}

    fun fill(player: Player, handler: BlockLockHandler): Inventory {
        val state = InventoryState.get(player.uniqueId) ?: return inventory
        val owner = handler.getOwner()
        val access = handler.getAccess()
        val redstone = handler.getRedstone()

        inventory.clear()
        state.friendResultCache.clear()
        for (i in 0..(access.size - 1).coerceAtMost(InventoryConstants.doubleLine)) { // Maximum of 2 lines of skulls
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(access[i]))
            inventory.setItem(
                InventoryConstants.lineLength + i,
                ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name)
            )
            state.friendResultCache.add(offlinePlayer)
        }

        if (owner.isNotEmpty()) inventory.setItem(
            0,
            ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner)))
        )
        if (state.friendPage == 0 && access.size >= InventoryConstants.doubleLine) {
            setItemStack(
                InventoryConstants.lineLength - 3,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE,
            )
            setItemStack(
                InventoryConstants.lineLength - 2,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE,
            )
        }
        setItemStack(
            1,
            if (redstone) Material.REDSTONE else Material.GUNPOWDER,
            if (redstone) TranslationKey.INVENTORIES__REDSTONE__ALLOWED
            else TranslationKey.INVENTORIES__REDSTONE__DISALLOWED
        )
        setBackButton(InventoryConstants.lineLength - 1)

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < InventoryConstants.doubleLine && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inventory.setItem(InventoryConstants.lineLength + i, skull)
                i++
            }
        }
        return inventory
    }
}
