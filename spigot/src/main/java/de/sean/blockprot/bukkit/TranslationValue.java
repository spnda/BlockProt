/*
 * Copyright (C) 2021 - 2024 spnda
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

package de.sean.blockprot.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Represents the default string value and a translated value
 * of it, as loaded through {@link Translator#loadFromConfigs(YamlConfiguration, YamlConfiguration)}.
 *
 * @since 0.4.6
 */
public final class TranslationValue {
    /**
     * The default value for a unknown translation, where both a
     * default value and a translated value are missing.
     *
     * @since 0.4.6
     */
    @NotNull
    public static final String UNKNOWN_TRANSLATION = "Unknown Translation";

    @NotNull
    public static final TranslationValue UNKNOWN_TRANSLATION_VALUE = new TranslationValue(UNKNOWN_TRANSLATION);

    @NotNull
    private final String defaultValue;

    @NotNull
    private String translatedValue;

    /**
     * Create a new translation value without an actual translation, which
     * can be added later.
     *
     * @param defaultValue The default value for this translation.
     * @see #setTranslatedValue(String)
     * @since 0.4.6
     */
    TranslationValue(@NotNull final String defaultValue) {
        this.defaultValue = defaultValue;
        this.translatedValue = UNKNOWN_TRANSLATION;
    }

    /**
     * Create a new translation value.
     *
     * @param defaultValue    The default, fallback value for this translation.
     * @param translatedValue The translated value.
     * @since 0.4.6
     */
    TranslationValue(@NotNull final String defaultValue, @Nullable final String translatedValue) {
        this.defaultValue = defaultValue;
        this.translatedValue = (translatedValue == null)
            ? UNKNOWN_TRANSLATION
            : translatedValue;
    }

    /**
     * Get the default value of this translation value as read
     * from the default config.
     *
     * @return Default, fallback value.
     * @since 0.4.6
     */
    @NotNull
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the translated value as read from the specified config.
     *
     * @return The translated value, or {@link #UNKNOWN_TRANSLATION}
     * if it was not readable.
     * @since 0.4.6
     */
    @NotNull
    public String getTranslatedValue() {
        return translatedValue;
    }

    /**
     * Sets a new value for the translation. Does not affect the
     * default value.
     *
     * @param value The new value for the translation. Shall not be
     *              null and will not be replaced with {@link #UNKNOWN_TRANSLATION}
     *              if it is invalid.
     * @since 0.4.6
     */
    void setTranslatedValue(@NotNull final String value) {
        this.translatedValue = value;
    }

    /**
     * Get the value of this translation. This checks whether or
     * not the translated value has been assigned a meaningful value
     * or is simply {@link #UNKNOWN_TRANSLATION}.
     *
     * @return The translated value or if no translated value exists,
     * the default value.
     * @since 0.4.6
     */
    @NotNull
    public String getValue() {
        return (translatedValue.equals(UNKNOWN_TRANSLATION))
            ? getDefaultValue()
            : translatedValue;
    }

    @Override
    @NotNull
    public String toString() {
        return new StringJoiner(
            " | ",
            TranslationValue.class.getSimpleName() + "[",
            "]"
        )
            .add("defaultValue=" + defaultValue)
            .add("translatedValue=" + translatedValue)
            .toString();
    }
}
