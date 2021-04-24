package de.sean.blockprot.bukkit.nbt

import de.tr7zw.nbtapi.utils.MinecraftVersion
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.DoubleChestInventory

object LockUtil {
    const val OWNER_ATTRIBUTE = "splugin_owner"
    const val LOCK_ATTRIBUTE = "splugin_lock"
    const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"
    const val LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place"
    const val DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends"

    const val PERMISSION_LOCK = "blockprot.lock"
    const val PERMISSION_INFO = "blockprot.info"
    const val PERMISSION_ADMIN = "blockprot.admin"

    val lockableTileEntities: List<Material> = mutableListOf(
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.FURNACE,
        Material.SMOKER,
        Material.BLAST_FURNACE,
        Material.HOPPER,
        Material.BARREL,
        Material.BREWING_STAND,
        Material.BLACK_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX
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
        Material.ANVIL,
        Material.CHIPPED_ANVIL,
        Material.DAMAGED_ANVIL,
        Material.ACACIA_DOOR,
        Material.BIRCH_DOOR,
        Material.CRIMSON_DOOR,
        Material.DARK_OAK_DOOR,
        Material.JUNGLE_DOOR,
        Material.OAK_DOOR,
        Material.SPRUCE_DOOR,
        Material.WARPED_DOOR,
        Material.ACACIA_FENCE_GATE,
        Material.BIRCH_FENCE_GATE,
        Material.CRIMSON_FENCE_GATE,
        Material.DARK_OAK_FENCE_GATE,
        Material.JUNGLE_FENCE_GATE,
        Material.OAK_FENCE_GATE,
        Material.SPRUCE_FENCE_GATE,
        Material.WARPED_FENCE_GATE,
    ) else mutableListOf()

    fun isLockable(blockState: BlockState) = isLockableBlock(blockState) && isLockableTileEntity(blockState)
    fun isLockableBlock(blockState: BlockState) = blockState.type in lockableBlocks
    fun isLockableTileEntity(blockState: BlockState) = blockState.type in lockableTileEntities

    /**
     * Parse a comma-separated list from a String
     */
    fun parseStringList(str: String): List<String> {
        val ret: MutableList<String> =
            ArrayList(listOf(*str.replace("^\\[|]$".toRegex(), "").split(", ").toTypedArray()))
        ret.removeIf { obj: String -> obj.isEmpty() }
        return ret
    }

    /**
     * Copy the data over from the top/bottom door to the other half
     */
    fun applyToDoor(doorHandler: BlockLockHandler, block: Block) {
        if (block.type in listOf(
                Material.ACACIA_DOOR,
                Material.BIRCH_DOOR,
                Material.CRIMSON_DOOR,
                Material.DARK_OAK_DOOR,
                Material.JUNGLE_DOOR,
                Material.OAK_DOOR,
                Material.SPRUCE_DOOR,
                Material.WARPED_DOOR
            )
        ) {
            val blockState = block.state
            val door = blockState.blockData as Door
            var other = blockState.location
            other = if (door.half == Bisected.Half.TOP) other.subtract(0.0, 1.0, 0.0)
            else other.add(0.0, 1.0, 0.0)
            val otherDoor = blockState.world.getBlockAt(other)
            val otherDoorHandler = BlockLockHandler(otherDoor)
            otherDoorHandler.setOwner(doorHandler.getOwner())
            otherDoorHandler.setAccess(doorHandler.getAccess())
            otherDoorHandler.setRedstone(doorHandler.getRedstone())
        }
    }

    /**
     * Get the BlockState of the double chest of given [block].
     * @return The BlockState of the double chest, null if given [block] was not a chest.
     */
    fun getDoubleChest(block: Block, world: World): BlockState? {
        var doubleChest: DoubleChest? = null
        val chestState = block.state
        if (chestState is Chest) {
            val inventory = chestState.inventory
            if (inventory is DoubleChestInventory) {
                doubleChest = inventory.holder
            }
        }
        if (doubleChest == null) return null
        val second = doubleChest.location

        when {
            block.x > second.x -> second.subtract(.5, 0.0, 0.0)
            block.z > second.z -> second.subtract(0.0, 0.0, .5)
            else -> second.add(.5, 0.0, .5)
        }

        return world.getBlockAt(second).state
    }
}