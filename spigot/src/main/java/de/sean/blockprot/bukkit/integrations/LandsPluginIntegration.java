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

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import me.angeschossen.lands.api.exceptions.FlagConflictException;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.NaturalFlag;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class LandsPluginIntegration extends PluginIntegration implements Listener {
    @Nullable private Plugin landsPlugin = null;

    @Nullable private LandsIntegration integration = null;

    @Nullable private RoleFlag protectContainersFlag = null;

    @Nullable private NaturalFlag requireProtectForFriendFlag = null;

    // Apparently flag IDs can be no longer than 20 chars.
    private static final String LOCK_CONTAINER_FLAG_ID = "bp_lock_containers";

    private static final String REQUIRE_PROTECT_FOR_FRIEND_FLAG = "bp_friend_req_prot";

    private static final String ALLOW_PROTECTING_CONTAINERS_IN_WILDERNESS = "allow_protecting_containers_in_wilderness";

    private boolean enabled = false;

    public LandsPluginIntegration() {
        super("lands");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void load() {
        super.load();
        landsPlugin = getPlugin();
        if (landsPlugin == null)
            return;

        this.integration = LandsIntegration.of(BlockProt.getInstance());

        try {
            this.protectContainersFlag = RoleFlag.of(this.integration, FlagTarget.PLAYER, RoleFlagCategory.ACTION, LOCK_CONTAINER_FLAG_ID);
            this.requireProtectForFriendFlag = NaturalFlag.of(this.integration, FlagTarget.PLAYER, REQUIRE_PROTECT_FOR_FRIEND_FLAG);

            this.protectContainersFlag
                .setApplyInSubareas(true)
                .setAlwaysAllowInWilderness(allowProtectingContainersInWilderness())
                .setIcon(new ItemStack(Material.CHEST))
                .setDisplay(true)
                .setDescription(Translator.get(TranslationKey.INTEGRATIONS__LANDS__PROTECT_CONTAINERS_DESC))
                .setDisplayName(Translator.get(TranslationKey.INTEGRATIONS__LANDS__PROTECT_CONTAINERS_FLAG_NAME));

            this.requireProtectForFriendFlag
                .setIcon(new ItemStack(Material.ENDER_PEARL))
                .setDisplay(true)
                .setDescription(Translator.get(TranslationKey.INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_DESC))
                .setDisplayName(Translator.get(TranslationKey.INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_FLAG_NAME));
        } catch (FlagConflictException | IllegalArgumentException e) {
            BlockProt.getInstance().getLogger().warning("LandsIntegration: Failed to register flag(s).");
            e.printStackTrace();
        }
    }

    @Override
    public void enable() {
        if (landsPlugin == null || !landsPlugin.isEnabled())
            return;

        this.registerListener(this);

        enabled = true;
    }

    @Override
    public void reload() {
        super.reload();
        if (this.protectContainersFlag == null || this.requireProtectForFriendFlag == null)
            return;

        this.protectContainersFlag
            .setDescription(Translator.get(TranslationKey.INTEGRATIONS__LANDS__PROTECT_CONTAINERS_DESC))
            .setDisplayName(Translator.get(TranslationKey.INTEGRATIONS__LANDS__PROTECT_CONTAINERS_FLAG_NAME));

        this.requireProtectForFriendFlag
            .setDescription(Translator.get(TranslationKey.INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_DESC))
            .setDisplayName(Translator.get(TranslationKey.INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_FLAG_NAME));
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("Lands");
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void filterFriendsInternal(@NotNull ArrayList<OfflinePlayer> friends, @NotNull Player player, @NotNull Block block) {
        assert(this.integration != null);
        if (landsPlugin == null || requireProtectForFriendFlag == null)
            return;

        var area = this.integration.getArea(block.getLocation());
        if (area == null)
            // Anyone can lock in the wilderness.
            return;

        friends.removeIf(p -> {
            var role = area.getRole(p.getUniqueId());
            return role.isVisitorRole() || (area.hasNaturalFlag(requireProtectForFriendFlag) && role.hasFlag(protectContainersFlag));
        });
    }

    @Override
    protected boolean filterFriendByUuid(@NotNull UUID friend, @NotNull Player player, @NotNull Block block) {
        assert(this.integration != null);
        if (landsPlugin == null || requireProtectForFriendFlag == null)
            return true;

        var area = this.integration.getArea(block.getLocation());
        if (area == null)
            // Anyone can lock in the wilderness.
            return true;

        // TODO: This if-else abomination needs to be simplified
        var role = area.getRole(player.getUniqueId());
        if (role.isVisitorRole())
            return false;
        else if (area.hasNaturalFlag(requireProtectForFriendFlag))
            return role.hasFlag(protectContainersFlag);
        else
            return true;
    }

    private boolean allowProtectingContainersInWilderness() {
        return configuration.contains(ALLOW_PROTECTING_CONTAINERS_IN_WILDERNESS)
            && configuration.getBoolean(ALLOW_PROTECTING_CONTAINERS_IN_WILDERNESS);
    }

    @EventHandler
    public void onAccessEditMenu(@NotNull final BlockAccessMenuEvent event) {
        assert(this.integration != null);

        var area = this.integration.getArea(event.getBlock().getLocation());
        if (area == null) {
            if (!allowProtectingContainersInWilderness()) {
                event.removePermission(BlockAccessMenuEvent.MenuPermission.LOCK);
                event.removePermission(BlockAccessMenuEvent.MenuPermission.MANAGER);
            }
            return;
        }

        var role = area.getRole(event.getPlayer().getUniqueId());
        if (!role.hasFlag(this.protectContainersFlag) || role.isVisitorRole()) {
            event.removePermission(BlockAccessMenuEvent.MenuPermission.LOCK);
            event.removePermission(BlockAccessMenuEvent.MenuPermission.MANAGER);
        }
    }

    @EventHandler
    public void onLockOnPlace(@NotNull final BlockLockOnPlaceEvent event) {
        assert(this.integration != null);

        var area = this.integration.getArea(event.getBlock().getLocation());
        if (area == null) {
            if (!allowProtectingContainersInWilderness()) {
                event.setCancelled(true);
            }
            return;
        }

        var role = area.getRole(event.getPlayer().getUniqueId());
        if (!role.hasFlag(this.protectContainersFlag) || role.isVisitorRole()) {
            event.setCancelled(true);
        }
    }
}
