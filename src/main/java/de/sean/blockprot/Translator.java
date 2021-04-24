package de.sean.blockprot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Helper to quickly obtain translations from a config by
 * a enum key.
 */
public class Translator {
    private static final HashMap<TranslationKey, String> values = new HashMap<>();

    /**
     * Initialize the translations from given configuration.
     * @param config the configuration to load translations from. See
     *               https://github.com/spnda/BlockProt/blob/master/src/main/resources/translations_en.yml
     *               for an example.
     */
    public static void init(@NotNull YamlConfiguration config) {
        TranslationKey[] translations = TranslationKey.values();
        for (TranslationKey translation : translations) {
            String translationKey = translation.toString();
            if (!config.contains(translationKey, true)) {
                continue;
            }
            Object value = config.get(translationKey);
            if (value instanceof String) {
                values.put(translation, (String)value);
            } else {
                values.put(translation, "Unknown Translation.");
            }
        }
    }

    /**
     * Get the translated String by key or an empty String
     * if none found.
     * @param key the translation key to search for
     * @return a translated String or an empty string if not found
     */
    @NotNull
    public static String get(@NotNull  TranslationKey key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }
}
