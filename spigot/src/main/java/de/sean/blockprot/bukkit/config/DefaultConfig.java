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

package de.sean.blockprot.bukkit.config;

import de.sean.blockprot.bukkit.BlockProt;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The default config of the {@link BlockProt} plugin.
 */
public final class DefaultConfig extends BlockProtConfig {
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
        InventoryType.BARREL, InventoryType.BREWING, InventoryType.SHULKER_BOX, InventoryType.ANVIL, InventoryType.DISPENSER,
        InventoryType.DROPPER, InventoryType.LECTERN
    ));

    private final List<String> excludedWorlds;

    /**
     * Create a new default configuration from given {@code config}.
     *
     * @param config The yaml configuration, should be the {@code config.yml}.
     * @since 0.3.3
     */
    public DefaultConfig(@NotNull final FileConfiguration config) {
        super(config);
        this.excludedWorlds = config.getStringList("excluded_worlds");
        this.loadBlocksFromConfig();
    }

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
     *
     * @since 0.3.3
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
     *
     * @return The name of the language file.
     * @since 0.3.3
     */
    @Nullable
    public String getLanguageFile() {
        return config.getString("language_file");
    }

    /**
     * Whether we should replace the translation files on each startup
     * and therefore discard any potential changes made to the files
     * by the server admin.
     *
     * @return True if translation files should be replaced.
     */
    public boolean shouldReplaceTranslations() {
        if (!this.config.contains("replace_translations")) return true;
        return this.config.getBoolean("replace_translations");
    }

    /**
     * Whether or not to we should notify a OP player of any updates
     * when they join the server.
     *
     * @return True if a op should be notified of updates.
     * @since 0.3.3
     */
    public boolean shouldNotifyOpOfUpdates() {
        if (!this.config.contains("notify_op_of_updates")) return false;
        return this.config.getBoolean("notify_op_of_updates");
    }

    /**
     * Checks the config if the "redstone_disallowed_by_default" key is
     * set to true. If it was not found, it defaults to false.
     *
     * @return True if redstone should be automatically disabled when a
     * block is placed.
     * @since 0.3.3
     */
    public boolean disallowRedstoneOnPlace() {
        if (this.config.contains("redstone_disallowed_by_default")) {
            return config.getBoolean("redstone_disallowed_by_default");
        } else {
            return true;
        }
    }

    /**
     * Checks if given {@code world} should be excluded from any
     * block protection functionality.
     *
     * @param world The world to check for.
     * @return If true, we shall not allow players to own and protect
     * any blocks in given {@code world}.
     * @since 0.4.4
     */
    public boolean isWorldExcluded(World world) {
        return listContainsIgnoreCase(excludedWorlds, world.getName());
    }

    /**
     * Checks if the world of the block held by {@code inventory}
     * is excluded from any block protection functionality.
     *
     * @param holder The inventory we want to use. If it is not a known
     *               exception, we try to cast it to {@link BlockInventoryHolder}.
     * @return True, if the world is excluded or the {@code holder} was
     * unable to be cast to {@link BlockInventoryHolder} and we do not
     * know how to extract the World information from it.
     * This is done to prevent this plugin to, for example, interact
     * with other plugins' inventories.
     * @since 0.4.5
     */
    public boolean isWorldExcluded(InventoryHolder holder) {
        try {
            if (holder instanceof DoubleChest) {
                @Nullable World world = ((DoubleChest) holder).getWorld();
                if (world == null) return true;
                return listContainsIgnoreCase(excludedWorlds, world.getName());
            }
            return isWorldExcluded(((BlockInventoryHolder) holder).getBlock().getWorld());
        } catch (ClassCastException e) {
            return true;
        }
    }

    /**
     * <p> Whether the given {@code type} is either a lockable block or a lockable tile entity.
     *
     * <p> Keep in mind, that only tile entities are lockable through this plugin after Spigot 1.16_R3.
     *
     * <p> To add to this, this merely checks the material from the config. This means that a server author
     * might accidentally add a material which is not a block or tile entity.
     *
     * @param type The type to check for.
     * @return True, if {@code type} is lockable.
     * @since 0.3.3
     */
    public boolean isLockable(Material type) {
        return isLockableBlock(type) || isLockableTileEntity(type);
    }

    /**
     * Whether the given {@code type} is a lockable block. Be aware, this only
     * works after Spigot 1.16_R3 and the config might have some invalid values.
     *
     * @param type The material to check for.
     * @return True, if {@code type} is a lockable block.
     * @see #isLockable(Material)
     * @since 0.3.3
     */
    public boolean isLockableBlock(Material type) {
        return lockableBlocks.contains(type);
    }

    /**
     * Whether the given {@code type} is a lockable tile entity. Be aware,
     * the config might have some invalid values.
     *
     * @param type The material to check for.
     * @return True, if {@code type} is a lockable tile entity.
     * @see #isLockable(Material)
     * @since 0.3.3
     */
    public boolean isLockableTileEntity(Material type) {
        return lockableTileEntities.contains(type) || shulkerBoxes.contains(type);
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
