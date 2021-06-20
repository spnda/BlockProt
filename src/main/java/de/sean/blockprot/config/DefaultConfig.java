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
package de.sean.blockprot.config;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DefaultConfig extends BlockProtConfig {
    public DefaultConfig(@NotNull final FileConfiguration config) {
        super(config);
        this.loadBlocksFromConfig();
    }

    /**
     * A list of all lockable tile entities.
     */
    private final ArrayList<Material> lockableTileEntities = new ArrayList<>();

    /**
     * A list of all available shulker boxes, so we
     * can save the protection state even after breaking.
     */
    private final ArrayList<Material> shulkerBoxes = new ArrayList<>();

    /**
     * We can only lock normal blocks after 1.16.4. Therefore, in all versions prior this list will
     * be empty. Doors are separately listed inside of [lockableDoors].
     */
    private final ArrayList<Material> lockableBlocks = new ArrayList<>();

    /**
     * Doors are separate for LockUtil#applyToDoor and also only work after 1.16.4 Spigot.
     */
    private final ArrayList<Material> lockableDoors = new ArrayList<>();

    private final ArrayList<InventoryType> lockableInventories = new ArrayList<>(Arrays.asList(
        InventoryType.CHEST, InventoryType.FURNACE, InventoryType.SMOKER, InventoryType.BLAST_FURNACE, InventoryType.HOPPER,
        InventoryType.BARREL, InventoryType.BREWING, InventoryType.SHULKER_BOX
    ));

    private <T extends Enum<?>> void loadBlockListFromConfig(
        @NotNull String key, @NotNull final ArrayList<T> list, @NotNull final T[] enumValues) {
        List<?> configList = config.getList(key);
        if (configList == null) return;
        ArrayList<String> stringList = configList
            .stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(Collectors.toCollection(ArrayList::new));
        list.addAll(this.loadEnumValuesByName(enumValues, stringList));
    }

    /**
     * Loads all the different lists from the config.yml file and adds
     * them to the various lists in LockUtil.
     */
    private void loadBlocksFromConfig() {
        loadBlockListFromConfig("lockable_tile_entities", this.lockableTileEntities, Material.values());
        loadBlockListFromConfig("lockable_shulker_boxes", this.shulkerBoxes, Material.values());

        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            loadBlockListFromConfig("lockable_blocks", this.lockableBlocks, Material.values());
            loadBlockListFromConfig("lockable_doors", this.lockableDoors, Material.values());
            lockableBlocks.addAll(lockableDoors);
        }
    }

    /**
     * Get the filename of the language file we use.
     * This file should be located in /plugins/BlockProt/.
     */
    @Nullable
    public String getLanguageFile() {
        return config.getString("language_file");
    }

    /**
     * Whether or not to we should notify a OP player of any updates
     * when they join the server.
     */
    public boolean shouldNotifyOpOfUpdates() {
        if (!this.config.contains("notify_op_of_updates")) return false;
        return this.config.getBoolean("notify_op_of_updates");
    }

    /**
     * Checks the config if the "redstone_disallowed_by_default" key is
     * set to true. If it was not found, it defaults to false.
     */
    public boolean disallowRedstoneOnPlace() {
        if (this.config.contains("redstone_disallowed_by_default")) {
            return config.getBoolean("redstone_disallowed_by_default");
        } else {
            return true;
        }
    }

    /**
     * Whether the given [type] is either a lockable block or a lockable tile entity.
     */
    public boolean isLockable(Material type) {
        return isLockableBlock(type) || isLockableTileEntity(type);
    }

    public boolean isLockableBlock(Material type) {
        return lockableBlocks.contains(type);
    }

    public boolean isLockableTileEntity(Material type) {
        return lockableTileEntities.contains(type);
    }

    public boolean isLockableDoor(Material type) {
        return lockableDoors.contains(type);
    }

    public boolean isLockableShulkerBox(Material type) {
        return shulkerBoxes.contains(type);
    }

    public boolean isLockableInventory(InventoryType type) {
        return lockableInventories.contains(type);
    }
}
