package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.util.Vector3f
import org.bukkit.Material
import kotlin.collections.HashMap

object LockUtil {
    const val OWNER_ATTRIBUTE = "splugin_owner"
    const val LOCK_ATTRIBUTE = "splugin_lock"
    const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"

    val lockableBlocks: List<Material> = mutableListOf(
        Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.HOPPER, Material.BARREL, Material.BREWING_STAND,
        Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    )

    private val lock = HashMap<String, Vector3f>()

    fun add(player: String, location: Vector3f) {
        lock[player] = location
    }

    fun get(player: String) = lock[player]
}
