package de.sean.splugin.spigot.events

import de.sean.splugin.SPlugin
import de.sean.splugin.spigot.inventories.BlockLockInventory
import de.sean.splugin.util.SLockUtil
import de.sean.splugin.util.SMessages.unmarkPlayerAFK
import de.sean.splugin.util.SUtil
import de.sean.splugin.util.SUtil.getItemStack
import de.sean.splugin.util.SUtil.parseStringList
import de.tr7zw.nbtapi.NBTTileEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class InteractEvent : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun playerInteract(event: PlayerInteractEvent) {
        val player = event.player
        SUtil.playerLastActivity[player.uniqueId] =  System.currentTimeMillis()
        if (SUtil.afkPlayers[player.uniqueId]!!) {
            SUtil.afkPlayers[player.uniqueId] = false
            unmarkPlayerAFK(player)
        }
        if (event.clickedBlock == null) return
        when (event.clickedBlock!!.type) {
            Material.PURPUR_STAIRS, Material.OAK_STAIRS, Material.COBBLESTONE_STAIRS, Material.BRICK_STAIRS, Material.STONE_BRICK_STAIRS, Material.NETHER_BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS, Material.JUNGLE_STAIRS, Material.CRIMSON_STAIRS, Material.WARPED_STAIRS, Material.QUARTZ_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.DARK_PRISMARINE_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.POLISHED_GRANITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.POLISHED_DIORITE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS, Material.END_STONE_BRICK_STAIRS, Material.STONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS, Material.SMOOTH_QUARTZ_STAIRS, Material.GRANITE_STAIRS, Material.ANDESITE_STAIRS, Material.RED_NETHER_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.DIORITE_STAIRS, Material.BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_BRICK_STAIRS ->
                if (event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking
                    && event.item == null && !SPlugin.instance.config.getBoolean("DisableChairSitting")
                    && player.hasPermission("splugin.sit")) {
                val location = event.clickedBlock!!.location.clone()
                if (location.world == null) return
                // Check the above block
                val aboveLocation = location.clone().add(0.0, 1.0, 0.0)
                if (aboveLocation.block.type != Material.AIR) {
                    // if the above block is not a air block, prevent the player from sitting down.
                    return
                }
                location.add(.5, .075, .5)
                val arrow = location.world!!.spawnEntity(location, EntityType.ARROW) as Arrow
                arrow.addCustomEffect(PotionEffect(PotionEffectType.INVISIBILITY, 10000, 255, false, false), true)
                arrow.isInvulnerable = true
                arrow.addPassenger(player)
            }
            Material.CHEST, Material.FURNACE, Material.HOPPER, Material.BARREL, Material.SHULKER_BOX -> if ((event.action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking && player.hasPermission("splugin.lock")) {
                // The user shift-left clicked the chest and is wanting to open the chest edit menu.
                val blockState = event.clickedBlock!!.state
                val blockTile = NBTTileEntity(blockState).persistentDataContainer
                val owner = blockTile.getString(SLockUtil.OWNER_ATTRIBUTE)
                val playerUuid = player.uniqueId.toString()
                // Don't open the menu if the player is not the owner of this chest.
                if ((owner == null || owner.isEmpty()) || (owner == playerUuid)) {
                    event.isCancelled = true
                    SLockUtil.lock[playerUuid] = blockState.block
                    val redstone = blockTile.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)
                    val inv: Inventory = BlockLockInventory.inventory
                    if (owner != null && (owner == playerUuid)) {
                        inv.setItem(0, getItemStack(1, blockState.type, "Unlock"))
                        inv.setItem(1, getItemStack(1, if (redstone) Material.GUNPOWDER else Material.REDSTONE, if (redstone) "Activate Redstone" else "Deactivate Redstone"))
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
                // The user right clicked and is trying to access the container
                val nbtTileEntity = NBTTileEntity(event.clickedBlock?.state).persistentDataContainer
                val owner = nbtTileEntity.getString(SLockUtil.OWNER_ATTRIBUTE)
                if (owner.isNotEmpty()) {
                    val access = parseStringList(nbtTileEntity.getString(SLockUtil.LOCK_ATTRIBUTE))
                    val playerUuid = player.uniqueId.toString()
                    if (owner != playerUuid && !access.contains(playerUuid)) {
                        event.isCancelled = true
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("No permission."))
                    }
                }
            }
            else -> {
                // If it's a flint and steel or TNT being used, log it. This could be a griefing attempt.
                val item = event.item ?: return
                val location = player.location
                when (item.type) {
                    Material.FLINT_AND_STEEL -> SPlugin.instance.logger.info(player.name + " used flint and steel at " + location.x + ", " + location.y + ", " + location.z)
                    Material.TNT -> SPlugin.instance.logger.info(player.name + " placed TNT at " + location.x + ", " + location.y + ", " + location.z)
                    else -> {
                    }
                }
            }
        }
    }
}
