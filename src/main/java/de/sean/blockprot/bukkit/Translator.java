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
package de.sean.blockprot.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Helper to quickly obtain translations from a config by a enum key.
 *
 * @since 0.1.10
 */
public final class Translator {
    /**
     * A HashMap of all possible translation values by key, loaded through
     * {@link Translator#loadFromConfig(YamlConfiguration)}.
     *
     * @since 0.1.10
     */
    private static final HashMap<TranslationKey, String> values = new HashMap<>();

    /**
     * @since 0.2.3
     */
    private Translator() {
    }

    /**
     * Initialize the translations from given configuration.
     *
     * @param config the configuration to load translations from. See
     *               https://github.com/spnda/BlockProt/blob/master/src/main/resources/translations_en.yml for
     *               an example.
     * @since 0.2.3
     */
    public static void loadFromConfig(@NotNull final YamlConfiguration config) {
        TranslationKey[] translations = TranslationKey.values();
        for (TranslationKey translation : translations) {
            String translationKey = translation.toString();
            if (!config.contains(translationKey, true)) {
                continue;
            }
            Object value = config.get(translationKey);
            if (value instanceof String) {
                values.put(translation, (String) value);
            } else {
                values.put(translation, "Unknown Translation.");
            }
        }
    }

    /**
     * Get the translated String by key or an empty String if none found.
     *
     * @param key the translation key to search for
     * @return a translated String or an empty string if not found
     * @since 0.1.10
     */
    @NotNull
    public static String get(@NotNull final TranslationKey key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }
}
