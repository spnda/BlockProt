package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.util.Vector3f
import de.tr7zw.nbtapi.utils.MinecraftVersion
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.event.inventory.InventoryType
import kotlin.collections.HashMap

object LockUtil {
    const val OWNER_ATTRIBUTE = "splugin_owner"
    const val LOCK_ATTRIBUTE = "splugin_lock"
    const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"

    val lockableTileEntities: List<Material> = mutableListOf(
        Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.HOPPER, Material.BARREL, Material.BREWING_STAND,
        Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    )

    val lockableInventories: List<InventoryType> = mutableListOf(
        InventoryType.CHEST, InventoryType.FURNACE, InventoryType.SMOKER, InventoryType.BLAST_FURNACE, InventoryType.HOPPER,
        InventoryType.BARREL, InventoryType.BREWING, InventoryType.SHULKER_BOX
    )

    /**
     * We can only lock normal blocks after 1.16.4. Therefore, in all versions prior this list will
     * be empty.
     */
    val lockableBlocks: List<Material> = if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) mutableListOf(
        Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
        Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.CRIMSON_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.WARPED_DOOR,
        Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.CRIMSON_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.WARPED_FENCE_GATE,
    ) else mutableListOf()

    fun isLockable(blockState: BlockState) = isLockableBlock(blockState) && isLockableTileEntity(blockState)
    fun isLockableBlock(blockState: BlockState) = blockState.type in lockableBlocks
    fun isLockableTileEntity(blockState: BlockState) = blockState.type in lockableTileEntities

    private val lock = HashMap<String, Vector3f>()

    fun add(player: String, location: Vector3f) {
        lock[player] = location
    }

    fun remove(player: String) {
        lock.remove(player)
    }

    fun get(player: String) = lock[player]
}
