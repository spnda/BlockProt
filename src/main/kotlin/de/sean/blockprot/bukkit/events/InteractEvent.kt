package de.sean.blockprot.bukkit.events

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.inventories.BlockLockInventory
import de.sean.blockprot.bukkit.inventories.InventoryConstants
import de.sean.blockprot.bukkit.inventories.InventoryState
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil.getItemStack
import de.sean.blockprot.util.setBackButton
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

open class InteractEvent : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    open fun playerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (event.clickedBlock == null) return
        when (event.clickedBlock!!.type) {
            in LockUtil.lockableTileEntities, in LockUtil.lockableBlocks -> {
                if (event.action == Action.RIGHT_CLICK_BLOCK &&
                    player.isSneaking &&
                    player.hasPermission(LockUtil.PERMISSION_LOCK)
                ) {
                    // The user shift-left clicked the block and is wanting to open the block edit menu.
                    val blockState = event.clickedBlock!!.state
                    val handler = BlockLockHandler(event.clickedBlock!!)
                    val owner = handler.getOwner()
                    val playerUuid = player.uniqueId.toString()
                    // Only open the menu if the player is the owner of this block
                    // or if this block is not protected
                    if (handler.isNotProtected() ||
                        owner == playerUuid ||
                        event.player.isOp ||
                        player.hasPermission(LockUtil.PERMISSION_INFO) ||
                        player.hasPermission(LockUtil.PERMISSION_ADMIN)
                    ) {
                        if (event.item == null) {
                            event.isCancelled = true
                            InventoryState.set(player.uniqueId, InventoryState(blockState.block))
                            InventoryState.get(player.uniqueId)?.friendSearchState = InventoryState.FriendSearchState.FRIEND_SEARCH
                            var inv: Inventory = BlockLockInventory.createInventory()
                            if (
                                (owner.isNotEmpty() && owner == playerUuid) ||
                                (owner.isNotEmpty() && (player.isOp || player.hasPermission(LockUtil.PERMISSION_INFO) || player.hasPermission(LockUtil.PERMISSION_ADMIN)))
                            ) {
                                inv = BlockLockInventory.createInventoryAndFill(player, blockState.type, handler)
                            } else {
                                inv.setItem(
                                    0,
                                    getItemStack(
                                        1,
                                        blockState.type,
                                        Translator.get(TranslationKey.INVENTORIES__LOCK)
                                    )
                                )
                                var i = 1
                                while (i < 5) {
                                    inv.setItem(i, null)
                                    i++
                                }
                            }
                            inv.setBackButton(InventoryConstants.lineLength - 1)
                            player.openInventory(inv)
                        }
                    } else {
                        event.isCancelled = true
                        player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            *TextComponent.fromLegacyText(
                                Translator.get(TranslationKey.MESSAGES__NO_PERMISSION)
                            )
                        )
                    }
                } else {
                    // The user right clicked and is trying to access the container
                    if (event.clickedBlock != null) {
                        val handler = BlockLockHandler(event.clickedBlock as Block)
                        if (!(handler.canAccess(player.uniqueId.toString()) || player.hasPermission(LockUtil.PERMISSION_BYPASS))) {
                            event.isCancelled = true
                            player.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                *TextComponent.fromLegacyText(
                                    Translator.get(TranslationKey.MESSAGES__NO_PERMISSION)
                                )
                            )
                        }
                    }
                }
            }
            else -> {
            }
        }
    }
}
