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

import com.google.common.collect.Sets;
import de.sean.blockprot.util.BlockProtUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

/**
 * Helper to quickly obtain translations from a config by a enum key.
 *
 * @since 0.1.10
 */
public final class Translator {
    /**
     * The list of all included translation files. This is updated at compile
     * time from our gradle buildscript.
     */
    public static final HashSet<String> DEFAULT_TRANSLATION_FILES = Sets.newHashSet(BlockProtUtil.parseStringList("$TRANSLATION_FILES"));

    /**
     * The default locale we use for default translation
     * values.
     *
     * @since 0.4.6
     */
    @NotNull
    public static final Locale defaultLocale = Locale.UK;

    /**
     * A HashMap of all possible translation values by key, loaded through
     * {@link Translator#loadFromConfigs(YamlConfiguration, YamlConfiguration)}.
     *
     * @since 0.4.6
     */
    @NotNull
    private static final HashMap<TranslationKey, TranslationValue> values = new HashMap<>();

    /**
     * Represents the locale of the translated values. Defaults
     * to being {@link #defaultLocale}.
     *
     * @since 0.4.6
     */
    @NotNull
    private static Locale locale = defaultLocale;

    static String DEFAULT_FALLBACK = "";

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
     * @deprecated Use {@link #loadFromConfigs(YamlConfiguration, YamlConfiguration)}
     * instead, as we now expect default translation values.
     */
    @Deprecated
    public static void loadFromConfigs(@NotNull final YamlConfiguration config) {
        TranslationKey[] translations = TranslationKey.values();
        for (TranslationKey translation : translations) {
            String translationKey = translation.toString();
            if (!config.contains(translationKey, true)) {
                continue;
            }
            Object value = config.get(translationKey);
            if (value instanceof String) {
                values.put(translation, new TranslationValue("", (String) value));
            } else {
                values.put(translation, new TranslationValue(
                    TranslationValue.UNKNOWN_TRANSLATION, TranslationValue.UNKNOWN_TRANSLATION));
            }
        }
    }

    /**
     * Initialize the translations from given configuration and sets the
     * internal locale to a locale, which is read from the configs themselves.
     * If the configurations do not provide a locale, we instead use
     * {@link Locale#ROOT}.
     *
     * @param defaultConfig The default configuration we use to get the default
     *                      translation values.
     * @param config        the configuration to load translations from. See
     *                      https://github.com/spnda/BlockProt/blob/master/src/main/resources/translations_en.yml for
     *                      an example.
     * @since 0.4.6
     */
    public static void loadFromConfigs(@NotNull final YamlConfiguration defaultConfig, @NotNull final YamlConfiguration config) {
        String locale = config.getString("locale");
        Translator.locale = (locale == null)
            ? Locale.ROOT
            : new Locale(locale);

        TranslationKey[] translations = TranslationKey.values();
        for (TranslationKey translation : translations) {
            String translationKey = translation.toString();
            // Ignore keys that both configs do not contain.
            if (!defaultConfig.contains(translationKey, true) &&
                !config.contains(translationKey, true)) {
                continue;
            }

            // Get the default config value. Set it to
            // UNKNOWN_TRANSLATION if we don't have it.
            Object defaultValue = defaultConfig.get(translationKey);
            TranslationValue translationValue = new TranslationValue(
                (defaultValue instanceof String)
                    ? (String) defaultValue
                    : TranslationValue.UNKNOWN_TRANSLATION
            );

            Object translatedValue = config.get(translationKey);
            if (translatedValue instanceof String) {
                translationValue.setTranslatedValue((String) translatedValue);
            }
            values.put(translation, translationValue);
        }
    }

    /**
     * Get the translated String by translation key. This will use
     * {@link TranslationValue#getValue()}, so values that are not
     * translated still use their default value.
     *
     * @param key the translation key to search for.
     * @return A translated String or an empty string if not found.
     * @since 0.1.10
     */
    @NotNull
    public static String get(@NotNull final TranslationKey key) {
        TranslationValue value = values.get(key);
        return value == null
            ? DEFAULT_FALLBACK
            : value.getValue();
    }

    /**
     * Get the currently used for the current translator. This value
     * is used to identify the translation file and might be useful
     * for knowing the language used by this plugin.
     *
     * @return The locale of translations.
     * @since 0.4.6
     */
    @NotNull
    public static Locale getLocale() {
        return locale;
    }
}
