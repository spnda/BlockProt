package de.sean.splugin.paper.events

import de.sean.splugin.bukkit.events.InteractEvent
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PaperInteractEvent : InteractEvent() {
    companion object {
        val signs: List<Material> = mutableListOf(
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN, Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN, Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN, Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN, Material.OAK_SIGN, Material.OAK_WALL_SIGN, Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN, Material.WARPED_SIGN, Material.WARPED_WALL_SIGN
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun playerInteract(event: PlayerInteractEvent) {
        super.playerInteract(event)
        if (event.clickedBlock == null) return
        when (event.clickedBlock!!.type) {
            in signs -> {
                if (event.action == Action.RIGHT_CLICK_BLOCK)
                    event.player.openSign(event.clickedBlock?.state as Sign)
            }
            else -> {}
        }
    }
}
