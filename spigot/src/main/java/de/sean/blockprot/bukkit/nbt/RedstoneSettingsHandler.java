/*
 * Copyright (C) 2021 - 2022 spnda
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

package de.sean.blockprot.bukkit.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.jetbrains.annotations.NotNull;

/**
 * A NBT handler specific to all redstone settings.
 * @since 0.4.13
 */
public class RedstoneSettingsHandler extends NBTHandler<NBTCompound> {
    public static final boolean DEFAULT_PROTECTION_VALUE = true;

    // Redstone-Current protection, so e.g. doors do not react to buttons etc.
    static final String CURRENT_PROTECTION_ATTRIBUTE = "current_protection";
    static final String PISTON_PROTECTION_ATTRIBUTE = "piston_protection";
    static final String HOPPER_PROTECTION_ATTRIBUTE = "hopper_protection";

    RedstoneSettingsHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    /**
     * If the redstone current protection is active or not.
     * @since 0.4.13
     */
    public boolean getCurrentProtection() {
        if (!container.hasKey(CURRENT_PROTECTION_ATTRIBUTE)) return DEFAULT_PROTECTION_VALUE;
        return container.getBoolean(CURRENT_PROTECTION_ATTRIBUTE);
    }

    /**
     * Set the redstone current protection to enabled (true) or disabled (false).
     * @since 0.4.13
     */
    public void setCurrentProtection(final boolean value) {
        container.setBoolean(CURRENT_PROTECTION_ATTRIBUTE, value);
    }

    /**
     * If the piston protection is active or not.
     * @since 0.4.13
     */
    public boolean getPistonProtection() {
        if (!container.hasKey(PISTON_PROTECTION_ATTRIBUTE)) return DEFAULT_PROTECTION_VALUE;
        return container.getBoolean(PISTON_PROTECTION_ATTRIBUTE);
    }

    /**
     * Set the piston protection to enabled (true) or disabled (false).
     * @since 0.4.13
     */
    public void setPistonProtection(final boolean value) {
        container.setBoolean(PISTON_PROTECTION_ATTRIBUTE, value);
    }

    /**
     * If the hopper protection is active or not.
     * @since 0.4.13
     */
    public boolean getHopperProtection() {
        if (!container.hasKey(HOPPER_PROTECTION_ATTRIBUTE)) return DEFAULT_PROTECTION_VALUE;
        return container.getBoolean(HOPPER_PROTECTION_ATTRIBUTE);
    }

    /**
     * Set the hopper protection to enabled (true) or disabled (false).
     * @since 0.4.13
     */
    public void setHopperProtection(final boolean value) {
        this.container.setBoolean(HOPPER_PROTECTION_ATTRIBUTE, value);
    }

    /**
     * Resets all the protections to their default value.
     * @since 0.4.13
     */
    public void reset() {
        this.setAll(DEFAULT_PROTECTION_VALUE);
    }

    /**
     * Resets all different redstone settings to given value.
     *
     * @param value The value.
     * @since 0.4.13
     */
    public void setAll(final boolean value) {
        this.setCurrentProtection(value);
        this.setPistonProtection(value);
        this.setHopperProtection(value);
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
        if (!(handler instanceof final RedstoneSettingsHandler redstoneHandler)) return;
        this.setCurrentProtection(redstoneHandler.getCurrentProtection());
        this.setPistonProtection(redstoneHandler.getPistonProtection());
        this.setHopperProtection(redstoneHandler.getHopperProtection());
    }
}
