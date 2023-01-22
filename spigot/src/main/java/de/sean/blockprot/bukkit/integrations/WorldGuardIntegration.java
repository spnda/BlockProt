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

package de.sean.blockprot.bukkit.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockProt integration with WorldGuard to provide flags to enable/disable
 * functionality.
 * 
 * @since 1.0.0
 */
public final class WorldGuardIntegration extends PluginIntegration implements Listener {
    private static final String FLAG_NAME = "allow-blockprot";
    private static final String CONFIG_ENABLE_FLAG_FUNCTIONALITY = "enable_flag_functionality";
    private boolean enabled = false;

    private @Nullable StateFlag allowFlag;

    public WorldGuardIntegration() {
        super("worldguard");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            allowFlag = new StateFlag(FLAG_NAME, true);
            registry.register(allowFlag);
        } catch (Exception e) {
            // Another plugin (possibly this plugin has been installed twice?)
            // has already registered our flag. Warn the admin.
            BlockProt.getInstance().getLogger().warning(
                    "Another plugin has already registered the " + FLAG_NAME + " flag.\n"
                            + "Functionality of this plugin might not be as expected due to flag conflicts.\n"
                            + "Please check if you accidentally installed BlockProt twice.");
        }
    }

    @Override
    public void load() {
        try {
            registerFlags();
        } catch (NoClassDefFoundError e) {
            // WorldGuard isn't loaded.
        }
    }

    @Override
    public void enable() {
        final Plugin wg = getPlugin();
        if (wg == null || !wg.isEnabled()) return;

        if (enableFlagFunctionality())
            this.registerListener(this);
        enabled = allowFlag != null;
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("WorldGuard");
    }

    /**
     * Whether we should enable functionality regarding the {@link #FLAG_NAME}
     * flag. Defaults to 'true'.
     */
    private boolean enableFlagFunctionality() {
        return !configuration.contains(CONFIG_ENABLE_FLAG_FUNCTIONALITY)
            || configuration.getBoolean(CONFIG_ENABLE_FLAG_FUNCTIONALITY);
    }

    private boolean checkIfDisallowedAtLocation(@NotNull final org.bukkit.World world, @NotNull final Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        World wgWorld = BukkitAdapter.adapt(world);
        RegionManager regions = container.get(wgWorld);

        if (regions != null) {
            BlockVector3 vec = BukkitAdapter.asBlockVector(location);
            ApplicableRegionSet applicableRegions = regions.getApplicableRegions(vec);
            for (ProtectedRegion r : applicableRegions) {
                if (r.getFlag(allowFlag) == StateFlag.State.DENY) {
                    return true;
                }
            }
        }

        return false;
    }

    @EventHandler
    public void onAccess(@NotNull final BlockAccessEvent event) {
        if (checkIfDisallowedAtLocation(event.getBlock().getWorld(), event.getBlock().getLocation())) {
            // If we cancel here now, nobody can access any chests.
            event.setBypassProtections(true);
        }
    }

    @EventHandler
    public void onAccessMenu(@NotNull final BlockAccessMenuEvent event) {
        if (checkIfDisallowedAtLocation(event.getBlock().getWorld(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLockOnPlace(@NotNull final BlockLockOnPlaceEvent event) {
        if (checkIfDisallowedAtLocation(event.getBlock().getWorld(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
