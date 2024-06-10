/*
 * Copyright (C) 2021 - 2023 spnda
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
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
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

    /**
     * As we differentiate between tile entities and blocks, it's best if we validate the values in the
     * config so that {@link #lockableTileEntities} actually only contains tile entities.
     */
    private final HashSet<Material> knownGoodTileEntities = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE,
            Material.HOPPER, Material.BARREL, Material.BREWING_STAND, Material.DISPENSER, Material.DROPPER,
            Material.LECTERN, Material.BEEHIVE, Material.BEE_NEST,

            Material.OAK_SIGN, Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN,
            Material.WARPED_SIGN, Material.WARPED_WALL_SIGN
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
        this.removeBlockDefaults();
        this.loadBlocksFromConfig();
    }

    private <T extends Enum<?>> void loadBlockListFromConfig(
            @NotNull String key, @NotNull final ArrayList<T> list, @NotNull final T[] enumValues, Function<T, Boolean> validateCallback) {
        List<?> configList = config.getList(key);
        if (configList == null) return;
        ArrayList<String> stringList = configList
            .stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(Collectors.toCollection(ArrayList::new));
        Set<T> newEnumValues = this.loadEnumValuesByName(enumValues, stringList);
        newEnumValues.removeIf((value) -> {
           if (!validateCallback.apply(value)) {
               BlockProt.getInstance().getLogger().warning("Caught invalid value passed to " + key + ": " + value.toString());
               return true;
           }
           return false;
        });
        list.addAll(newEnumValues);
    }

    /**
     * Loads all the different lists from the config.yml file and adds
     * them to the various lists in LockUtil.
     *
     * @since 0.3.3
     */
    private void loadBlocksFromConfig() {
        // Add some materials which are not valid in some versions
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_19_R1)) {
            this.knownGoodTileEntities.addAll(List.of(Material.MANGROVE_SIGN, Material.MANGROVE_WALL_SIGN));
        }
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R1)) {
            this.knownGoodTileEntities.add(Material.CHISELED_BOOKSHELF);
            this.knownGoodTileEntities.addAll(List.of(Material.OAK_WALL_HANGING_SIGN, Material.OAK_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.SPRUCE_WALL_HANGING_SIGN, Material.SPRUCE_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.BIRCH_WALL_HANGING_SIGN, Material.BIRCH_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.JUNGLE_WALL_HANGING_SIGN, Material.JUNGLE_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.ACACIA_WALL_HANGING_SIGN, Material.ACACIA_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.DARK_OAK_WALL_HANGING_SIGN, Material.DARK_OAK_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.CRIMSON_WALL_HANGING_SIGN, Material.CRIMSON_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.WARPED_WALL_HANGING_SIGN, Material.WARPED_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.MANGROVE_HANGING_SIGN, Material.MANGROVE_WALL_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.CHERRY_SIGN, Material.CHERRY_WALL_SIGN, Material.CHERRY_HANGING_SIGN, Material.CHERRY_WALL_HANGING_SIGN));
            this.knownGoodTileEntities.addAll(List.of(Material.BAMBOO_SIGN, Material.BAMBOO_WALL_SIGN, Material.BAMBOO_HANGING_SIGN, Material.BAMBOO_WALL_HANGING_SIGN));
        }

        loadBlockListFromConfig("lockable_tile_entities", this.lockableTileEntities, Material.values(),
                knownGoodTileEntities::contains);
        loadBlockListFromConfig("lockable_shulker_boxes", this.shulkerBoxes, Material.values(),
                material -> material.toString().contains("SHULKER_BOX"));

        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            loadBlockListFromConfig("lockable_blocks", this.lockableBlocks, Material.values(),
                    material -> !knownGoodTileEntities.contains(material));
            loadBlockListFromConfig("lockable_doors", this.lockableDoors, Material.values(),
                    material -> material.toString().contains("DOOR"));

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
     * Whether the lock on place setting should be enabled by default.
     *
     * @return Boolean for the default value of lock on place.
     * @since 0.4.11
     */
    public boolean lockOnPlaceByDefault() {
        if (!this.config.contains("lock_on_place_by_default")) return true;
        return this.config.getBoolean("lock_on_place_by_default");
    }

    /**
     * Whether the public should be a friend by default.
     *
     * @return Boolean for the default value of public is friend.
     * @since 1.1.15
     */
    public boolean publicIsFriendByDefault() {
        if (!this.config.contains("public_on_place_by_default")) return false;
        return this.config.getBoolean("public_on_place_by_default");
    }

    /**
     * 
     * @since 1.0.0
     */
    @Nullable
    public String getTranslationFallbackString() {
        if (!this.config.contains("fallback_string")) return "";
        return this.config.getString("fallback_string");
    }

    /**
     * Gets the maximum amount of blocks a player is allowed to
     * lock globally.
     * @return The value or if no limit is set, null.
     * @since 1.0.3
     */
    @Nullable
    public Integer getMaxLockedBlockCount() {
        if (!this.config.contains("player_max_locked_block_count"))
            return null;
        int val = this.config.getInt("player_max_locked_block_count");
        return val > 0 ? val : null;
    }

    /**
     * JavaPlugin#reloadConfig sets the default values to the config inside
     * the JAR, which are never edited by the player. We don't want this
     * for the lists.
     * 
     * @since 1.0.0
     */
    public void removeBlockDefaults() {
        Configuration defaults = this.config.getDefaults();
        if (defaults != null) {
            defaults.set("lockable_tile_entities", null);
            defaults.set("lockable_shulker_boxes", null);
            defaults.set("lockable_blocks", null);
            defaults.set("lockable_doors", null);
            this.config.setDefaults(defaults);
        }
    }

    public long getLockHintCooldown() {
        if (!config.contains("lock_hint_cooldown_in_seconds")) {
            return 10;
        }
        return config.getLong("lock_hint_cooldown_in_seconds");
    }

    /**
     * Gets the minimum percentage friend names have to match by the levenshtein distance.
     * @since 1.1.6
     */
    public double getFriendSearchSimilarityPercentage() {
        if (!config.contains("friend_search_similarity")) {
            return 0.5;
        }
        return config.getDouble("friend_search_similarity");
    }

    /**
     * Returns if the friend functionality is fully disabled. This will
     * no longer allow players to give other players access to their blocks, and
     * current settings are ignored until re-activated.
     * @since 1.1.16
     */
    public boolean isFriendFunctionalityDisabled() {
        if (!config.contains("disable_friend_functionality")) {
            return false;
        }
        return config.getBoolean("disable_friend_functionality");
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
