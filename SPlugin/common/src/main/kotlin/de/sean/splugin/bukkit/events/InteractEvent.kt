package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.logging.Logger

open class InteractEvent : Listener {
    companion object {
        val sitableBlocks: List<Material> = mutableListOf(
            Material.PURPUR_STAIRS, Material.OAK_STAIRS, Material.COBBLESTONE_STAIRS, Material.BRICK_STAIRS, Material.STONE_BRICK_STAIRS, Material.NETHER_BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS, Material.JUNGLE_STAIRS, Material.CRIMSON_STAIRS, Material.WARPED_STAIRS, Material.QUARTZ_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.DARK_PRISMARINE_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.POLISHED_GRANITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.POLISHED_DIORITE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS, Material.END_STONE_BRICK_STAIRS, Material.STONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS, Material.SMOOTH_QUARTZ_STAIRS, Material.GRANITE_STAIRS, Material.ANDESITE_STAIRS, Material.RED_NETHER_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.DIORITE_STAIRS, Material.BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_BRICK_STAIRS
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    open fun playerInteract(event: PlayerInteractEvent) {
        val player = event.player
        AfkPlayerManager.setLastActivity(player.uniqueId, System.currentTimeMillis())
        if (AfkPlayerManager.isAfk(player.uniqueId)) {
            AfkPlayerManager.setAfk(player.uniqueId, false)
            AfkPlayerManager.unmarkPlayerAfk(player)
        }
        if (event.clickedBlock == null) return
        when (event.clickedBlock!!.type) {
            in sitableBlocks -> {
                if (event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking
                    && event.item == null
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
            }
            else -> {
                // Log suspicious activity. This could be a griefing attempt.
                val item = event.item
                if (item != null) {
                    val location = player.location
                    when (item.type) {
                        Material.FLINT_AND_STEEL -> Logger.getLogger(this.javaClass.simpleName).info(player.name + " used flint and steel at " + location.x + ", " + location.y + ", " + location.z)
                        Material.TNT -> Logger.getLogger(this.javaClass.simpleName).info(player.name + " placed TNT at " + location.x + ", " + location.y + ", " + location.z)
                        Material.LAVA -> Logger.getLogger(this.javaClass.simpleName).info(player.name + " placed lava at " + location.x + ", " + location.y + ", " + location.z)
                        else -> {}
                    }
                }
            }
        }
    }
}
