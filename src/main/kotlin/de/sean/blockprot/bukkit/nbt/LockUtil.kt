/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.BlockProt
import de.tr7zw.changeme.nbtapi.NBTEntity
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.DoubleChestInventory

object LockUtil {
    /**
     * A list of *all* lockable tile entities.
     */
    private val lockableTileEntities: MutableList<Material> = mutableListOf()

    /**
     * A list of all available shulker boxes, so we
     * can save the protection state even after breaking.
     */
    private val shulkerBoxes: MutableList<Material> = mutableListOf()

    private val lockableInventories: List<InventoryType> = mutableListOf(
        InventoryType.CHEST, InventoryType.FURNACE, InventoryType.SMOKER, InventoryType.BLAST_FURNACE, InventoryType.HOPPER,
        InventoryType.BARREL, InventoryType.BREWING, InventoryType.SHULKER_BOX
    )

    /**
     * We can only lock normal blocks after 1.16.4. Therefore, in all versions prior this list will
     * be empty. Doors are separately listed inside of [lockableDoors].
     */
    private val lockableBlocks: MutableList<Material> = mutableListOf()

    /**
     * Doors are separate for LockUtil#applyToDoor and also only work after 1.16.4 Spigot.
     */
    private val lockableDoors: MutableList<Material> = mutableListOf()

    private fun List<String>.containsIgnoreCase(value: String): Boolean {
        for (item in this) {
            if (item.equals(value, ignoreCase = true)) return true
        }
        return false
    }

    /**
     * Get a list of [Material]s for a list of [String]s. This checks all values
     * in the [Material] enum and returns the materials which name is included in
     * the list of strings. If some values in [strings] were not found in [values],
     * a warning will be printed afterwards.
     */
    private fun <T : Enum<*>> loadEnumValuesFromStrings(values: Array<T>, strings: MutableList<String>): Set<T> {
        val ret = mutableSetOf<T>()
        for (value in values) {
            if (strings.containsIgnoreCase(value.name)) {
                ret.add(value)
                strings.remove(value.name)
            }
        }
        if (strings.isNotEmpty()) Bukkit.getLogger().warning("Could not map these values to enum: $strings")
        return ret
    }

    /**
     * Loads all the different lists from the config.yml file and adds
     * them to the various lists in LockUtil.
     */
    fun loadBlocksFromConfig(config: FileConfiguration) {
        if (config.contains("lockable_tile_entities")) {
            var tileEntities = config.getList("lockable_tile_entities")!!
            tileEntities = tileEntities.filterIsInstance<String>()
            val materials = loadEnumValuesFromStrings(Material.values(), tileEntities.toMutableList())
            lockableTileEntities.addAll(materials)
        }

        if (config.contains("lockable_shulker_boxes")) {
            var shulkerBoxes = config.getList("lockable_shulker_boxes")!!
            shulkerBoxes = shulkerBoxes.filterIsInstance<String>()
            val materials = loadEnumValuesFromStrings(Material.values(), shulkerBoxes.toMutableList())
            LockUtil.shulkerBoxes.addAll(materials)
        }

        lockableTileEntities.addAll(shulkerBoxes)

        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            if (config.contains("lockable_blocks")) {
                var lockableBlocks = config.getList("lockable_blocks")!!
                lockableBlocks = lockableBlocks.filterIsInstance<String>()
                val materials = loadEnumValuesFromStrings(Material.values(), lockableBlocks.toMutableList())
                LockUtil.lockableBlocks.addAll(materials)
            }

            if (config.contains("lockable_doors")) {
                var lockableDoors = config.getList("lockable_doors")!!
                lockableDoors = lockableDoors.filterIsInstance<String>()
                val materials = loadEnumValuesFromStrings(Material.values(), lockableDoors.toMutableList())
                LockUtil.lockableDoors.addAll(materials)
            }

            lockableBlocks.addAll(lockableDoors)
        }
    }

    /**
     * Whether the given [type] is either a lockable block or a lockable tile entity.
     */
    @JvmStatic fun isLockable(type: Material) = isLockableBlock(type) || isLockableTileEntity(type)
    @JvmStatic fun isLockableBlock(type: Material) = type in lockableBlocks
    @JvmStatic fun isLockableTileEntity(type: Material) = type in lockableTileEntities
    @JvmStatic fun isLockableDoor(type: Material) = type in lockableDoors
    @JvmStatic fun isLockableShulkerBox(type: Material) = type in shulkerBoxes
    @JvmStatic fun isLockableInventory(type: InventoryType) = type in lockableInventories

    /**
     * Parse a comma-separated list from a String
     */
    @JvmStatic
    fun parseStringList(str: String?): List<String> {
        if (str == null) return emptyList()
        val ret: MutableList<String> =
            ArrayList(listOf(*str.replace("^\\[|]$".toRegex(), "").split(", ").toTypedArray()))
        ret.removeIf { obj: String -> obj.isEmpty() }
        return ret
    }

    /**
     * Get the BlockState of the double chest of given [block].
     * @return The BlockState of the double chest, null if given [block] was not a chest.
     */
    @JvmStatic
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

    /**
     * Check if the given [player] wants their blocks to be locked when
     * placed.
     */
    @JvmStatic
    fun shouldLockOnPlace(player: Player): Boolean {
        val nbtEntity = NBTEntity(player).persistentDataContainer
        return if (nbtEntity.hasKey(PlayerSettingsHandler.LOCK_ON_PLACE_ATTRIBUTE)) {
            nbtEntity.getBoolean(PlayerSettingsHandler.LOCK_ON_PLACE_ATTRIBUTE) != false
        } else {
            true
        }
    }

    /**
     * Checks the config if the "redstone_disallowed_by_default" key is
     * set to true. If it was not found, it defaults to false.
     */
    @JvmStatic
    fun disallowRedstoneOnPlace(): Boolean {
        val config = BlockProt.instance.config
        return if (config.contains("redstone_disallowed_by_default")) {
            config.getBoolean("redstone_disallowed_by_default")
        } else {
            true
        }
    }
}
