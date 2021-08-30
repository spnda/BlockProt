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

package de.sean.blockprot.bukkit.integrations;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class TownyIntegration extends PluginIntegration implements Listener {
    private static final String CLEANUP_PLOTS_AFTER_UNCLAIM = "cleanup_plots_after_unclaim";

    private static final String RESTRICT_ACCESS_TO_RESIDENTS = "restrict_access_to_residents";

    private static final String ALLOW_MAYOR_TO_SEE_BLOCK_INFO = "allow_mayor_to_see_block_info";

    private static final String BYPASS_PROTECTIONS_IN_RUINED_TOWNS = "bypass_protection_in_ruined_towns";

    /**
     * Our Towny plugin instance. Might be null if towny was not found.
     */
    @Nullable
    private Towny towny;

    public TownyIntegration() {
        super("towny.yml");
    }

    @Override
    public boolean isEnabled() {
        return towny != null;
    }

    @Override
    public void load() {
        final Plugin plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return;
        }

        towny = (Towny) plugin;

        this.registerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("Towny");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void filterFriendsInternal(@NotNull final ArrayList<OfflinePlayer> friends,
                                         @NotNull final Player player,
                                         @NotNull final Block block) {
        if (towny == null
            || TownyAPI.getInstance().isWilderness(block)
            || !shouldRestrictAccessToResidents()) {
            return;
        }

        Town town = TownyAPI.getInstance().getTown(block.getLocation());
        if (town == null) {
            // Shouldn't happen, as we already previously check
            // for the wilderness.
            return;
        }
        friends.removeIf(friend -> {
            // We remove all friends/players that are not part of this town.
            Resident resident = TownyAPI.getInstance().getResident(friend.getUniqueId());
            return resident == null || !town.hasResident(resident);
        });
    }

    /**
     * Restrict the access to residents only. If true, unlocked
     * blocks will be only accessible by town residents and only
     * residents can be added as friends.
     * Blocks however can always only be locked by residents.
     *
     * @return Boolean if we should restrict access to residents.
     */
    private boolean shouldRestrictAccessToResidents() {
        return configuration.contains(RESTRICT_ACCESS_TO_RESIDENTS)
            && configuration.getBoolean(RESTRICT_ACCESS_TO_RESIDENTS);
    }

    /**
     * This allows any Mayor to view the block info, similarly
     * to what the "blockprot.info" permission would do.
     *
     * @return Boolean if we should allow mayors to see block info.
     */
    private boolean shouldAllowMayorToSeeBlockInfo() {
        return configuration.contains(ALLOW_MAYOR_TO_SEE_BLOCK_INFO)
            && configuration.getBoolean(ALLOW_MAYOR_TO_SEE_BLOCK_INFO);
    }

    /**
     * When a plot is cleared, unclaimed or the town is unclaimed
     * or gets ruined, whether or not we should cleanup block protections.
     *
     * @return Boolean if we should cleanup plots.
     */
    private boolean shouldCleanupAfterUnclaim() {
        return configuration.contains(CLEANUP_PLOTS_AFTER_UNCLAIM)
            || configuration.getBoolean(CLEANUP_PLOTS_AFTER_UNCLAIM);
    }

    /**
     * Whether or not players are allowed to bypass protections in
     * ruined towns.
     *
     * @return Boolean if the protections should be bypassed.
     */
    private boolean shouldBypassProtectionsInRuinedTowns() {
        return configuration.contains(BYPASS_PROTECTIONS_IN_RUINED_TOWNS)
            || configuration.getBoolean(BYPASS_PROTECTIONS_IN_RUINED_TOWNS);
    }

    private boolean residentEqualsPlayer(@Nullable final Resident resident,
                                         @Nullable final Player player) {
        if (resident == null && player == null) {
            return true;
        }

        if (resident == null
            || resident.getPlayer() == null
            || player == null) {
            return false;
        }

        return resident.getPlayer().equals(player);
    }

    /**
     * Clear the protection of all blocks that are inside of the area
     * of {@code worldCoord}.
     *
     * @param worldCoord The region to clear protections for.
     */
    private void removeAllProtections(@Nullable final WorldCoord worldCoord) {
        if (worldCoord == null) {
            return;
        }

        World world = worldCoord.getBukkitWorld();
        int height = world.getMaxHeight() - 1;
        int size = TownySettings.getTownBlockSize();
        for (int x = 0; x < size; ++x) {
            for (int z = 0; z < size; ++z) {
                for (int y = height; y > 0; --y) {
                    int blockX = worldCoord.getX() * size + x;
                    int blockZ = worldCoord.getZ() * size + z;
                    Block block = world.getBlockAt(blockX, y, blockZ);
                    if (!BlockProt.getDefaultConfig()
                        .isLockable(block.getType())) {
                        continue;
                    }
                    BlockNBTHandler handler = new BlockNBTHandler(block);
                    handler.clear();
                }
            }
        }
    }

    @EventHandler
    public void onAccess(@NotNull final BlockAccessEvent event) {
        if (towny == null) {
            return;
        }

        Block block = event.getBlock();
        if (TownyAPI.getInstance().isWilderness(block)) {
            return; // We allow anyone to access blocks in the wilderness.
        }

        Town town = TownyAPI.getInstance().getTown(block.getLocation());
        if (town == null) {
            return;
        }

        if (shouldRestrictAccessToResidents()) {
            Resident resident = TownyAPI.getInstance().getResident(
                event.getPlayer().getUniqueId());
            // Only restrict the block if they're not a resident of this town.
            if (resident == null || !town.hasResident(resident)) {
                event.setCancelled(true);
            }
        } else if (shouldBypassProtectionsInRuinedTowns() && town.isRuined()) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onAccessEditMenu(
        @NotNull final BlockAccessMenuEvent event) {
        if (towny == null) {
            return;
        }

        Block block = event.getBlock();
        if (TownyAPI.getInstance().isWilderness(block)) {
            return; // Allow editing blocks that are in the wilderness.
        }

        if (!PlayerCacheUtil.getCachePermission(
            event.getPlayer(),
            block.getLocation(),
            block.getType(),
            TownyPermission.ActionType.DESTROY)) {
            // We do not want to allow players to edit this block if
            // they're not part of this town.
            event.setCancelled(true);
            return;
        }

        Town town = TownyAPI.getInstance().getTown(block.getLocation());
        if (town == null) {
            return;
        }

        if (residentEqualsPlayer(town.getMayor(), event.getPlayer())
            && shouldAllowMayorToSeeBlockInfo()) {
            event.addPermission(BlockAccessMenuEvent.MenuPermission.INFO);
        } else if (town.isRuined()) {
            // Cancel the event if we don't want to bypass protections
            // in ruined towns.
            event.setCancelled(!shouldBypassProtectionsInRuinedTowns());
        }
    }

    @EventHandler
    public void onLockOnPlace(@NotNull final BlockLockOnPlaceEvent event) {
        if (towny == null) {
            return;
        }

        Block block = event.getBlock();
        if (TownyAPI.getInstance().isWilderness(block)) {
            return;
        }

        if (shouldRestrictAccessToResidents()) {
            Town town = TownyAPI.getInstance().getTown(block.getLocation());
            if (town != null) {
                Resident resident =
                    TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId());
                if (resident == null || town.hasResident(resident)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlotClear(@NotNull final PlotClearEvent event) {
        if (!shouldCleanupAfterUnclaim() || event.getTownBlock() == null) {
            return;
        }
        removeAllProtections(event.getTownBlock().getWorldCoord());
    }

    @EventHandler
    public void onTownRuin(@NotNull final TownRuinedEvent event) {
        if (!shouldCleanupAfterUnclaim()) {
            return;
        }
        for (TownBlock townBlock : new ArrayList<>(event.getTown().getTownBlocks())) {
            removeAllProtections(townBlock.getWorldCoord());
        }
    }

    @EventHandler
    public void onTownUnclaim(@NotNull final TownUnclaimEvent event) {
        if (!shouldCleanupAfterUnclaim()) {
            return;
        }
        removeAllProtections(event.getWorldCoord());
    }
}
