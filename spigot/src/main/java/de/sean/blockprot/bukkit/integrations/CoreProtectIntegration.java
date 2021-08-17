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
import de.sean.blockprot.bukkit.events.BlockAccessEditMenuEvent;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class CoreProtectIntegration extends PluginIntegration implements Listener {
    private CoreProtectAPI coreProtect;

    public CoreProtectIntegration() {
        super("coreprotect.yml");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void load() {
        Plugin coreProtectPlugin = getPlugin();
        if (coreProtectPlugin == null || !coreProtectPlugin.isEnabled()) return;
        this.coreProtect = ((CoreProtect) coreProtectPlugin).getAPI();

        // We require at least API version 7 to work.
        if (!this.coreProtect.isEnabled() || this.coreProtect.APIVersion() < 7) return;

        registerListener(this);
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("CoreProtect");
    }

    public void onBlockAccess(final BlockAccessEvent event) {
        // Log any access to a block.
        this.coreProtect.logInteraction(event.getPlayer().getDisplayName(), event.getBlock().getLocation());
    }

    public void onBlockAccessEditMenu(final BlockAccessEditMenuEvent event) {
        // Log any user accessing the edit menu.
        this.coreProtect.logInteraction(event.getPlayer().getDisplayName(), event.getBlock().getLocation());
    }
}
