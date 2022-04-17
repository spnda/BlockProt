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

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
import me.angeschossen.lands.api.flags.Flag;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.TrustedPlayer;
import me.angeschossen.lands.api.role.Role;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LandsPluginIntegration extends PluginIntegration implements Listener {
    @Nullable private LandsIntegration integration = null;

    private boolean enabled = false;

    public LandsPluginIntegration() {
        super("lands");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void enable() {
        final Plugin lands = getPlugin();
        if (lands == null || !lands.isEnabled())
            return;

        this.integration = new LandsIntegration(BlockProt.getInstance());
        this.registerListener(this);

        enabled = true;
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("Lands");
    }

    @EventHandler
    public void onAccess(@NotNull final BlockAccessEvent event) {
        if (!enabled || this.integration == null) return;

        Land land = this.integration.getLand(event.getBlock().getLocation());
        if (land == null) {
            // This is wilderness, we allow any block to be locked here.
            return;
        }

        Area area = this.integration.getAreaByLoc(event.getBlock().getLocation());
        if (area == null) {
            // ???
            return;
        }

        Role role = area.getRole(event.getPlayer().getUniqueId());
        if (!role.hasFlag(Flags.INTERACT_CONTAINER) || role.isVisitorRole()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAccessEditMenu(@NotNull final BlockAccessMenuEvent event) {
        if (!enabled || this.integration == null) return;

        Land land = this.integration.getLand(event.getBlock().getLocation());
        if (land == null) {
            // This is wilderness, we allow any block to be locked here.
            return;
        }

        Area area = this.integration.getAreaByLoc(event.getBlock().getLocation());
        if (area == null) {
            // ???
            return;
        }

        Role role = area.getRole(event.getPlayer().getUniqueId());
        if (!role.hasFlag(Flags.INTERACT_CONTAINER) || role.isVisitorRole()) {
            event.setCancelled(true);
        }
    }
}
