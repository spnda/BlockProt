package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.inventories.BlockLockInventory
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil.getItemStack
import de.sean.blockprot.util.Vector3f
import de.tr7zw.nbtapi.NBTTileEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
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
            in LockUtil.lockableBlocks -> {
                if ((event.action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking && player.hasPermission("blockprot.lock")) {
                    // The user shift-left clicked the block and is wanting to open the block edit menu.
                    val blockState = event.clickedBlock!!.state
                    val handler = BlockLockHandler(NBTTileEntity(blockState))
                    val owner = handler.getOwner()
                    val playerUuid = player.uniqueId.toString()
                    // Only open the menu if the player is the owner of this block
                    // or if this block is not protected
                    if (handler.isNotProtected() || owner == playerUuid || event.player.isOp) {
                        if (event.item == null) {
                            event.isCancelled = true
                            LockUtil.add(playerUuid, Vector3f.fromDouble(blockState.block.location.x, blockState.block.location.y, blockState.block.location.z))
                            val redstone = handler.getRedstone()
                            val inv: Inventory = BlockLockInventory.inventory
                            if ((owner.isNotEmpty() && owner == playerUuid) || (player.isOp)) {
                                inv.setItem(0, getItemStack(1, blockState.type, "Unlock"))
                                inv.setItem(
                                    1,
                                    getItemStack(
                                        1,
                                        if (redstone) Material.GUNPOWDER else Material.REDSTONE,
                                        if (redstone) "Activate Redstone" else "Deactivate Redstone"
                                    )
                                )
                                inv.setItem(2, getItemStack(1, Material.PLAYER_HEAD, "Add Friends"))
                                inv.setItem(3, getItemStack(1, Material.ZOMBIE_HEAD, "Remove Friends"))
                                if (player.isOp) {
                                    inv.setItem(4, getItemStack(1, Material.OAK_SIGN, "Info"))
                                }
                            } else {
                                inv.setItem(0, getItemStack(1, blockState.type, "Lock"))
                                var i = 1
                                while (i < 5) {
                                    inv.setItem(i, null)
                                    i++
                                }
                            }
                            inv.setItem(8, getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, "Back"))
                            player.openInventory(inv)
                        }
                    } else {
                        event.isCancelled = true
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("No permission."))
                    }
                } else {
                    // The user right clicked and is trying to access the container
                    val handler = BlockLockHandler(NBTTileEntity(event.clickedBlock?.state))
                    if (!handler.canAccess(player.uniqueId.toString())) {
                        event.isCancelled = true
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("No permission."))
                    }
                }
            }
            else -> {}
        }
    }
}
