/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.sean.blockprot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Helper to quickly obtain translations from a config by a enum key.
 */
public final class Translator {
    /**
     * A HashMap of all possible translation values by key, loaded through
     * {@link Translator#loadFromConfig(YamlConfiguration)}.
     */
    private static final HashMap<TranslationKey, String> values = new HashMap<>();

    private Translator() {
    }

    /**
     * Initialize the translations from given configuration.
     *
     * @param config the configuration to load translations from. See
     *               https://github.com/spnda/BlockProt/blob/master/src/main/resources/translations_en.yml for
     *               an example.
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
     */
    @NotNull
    public static String get(@NotNull final TranslationKey key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }
}
