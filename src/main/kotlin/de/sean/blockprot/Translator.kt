package de.sean.blockprot

import org.bukkit.configuration.file.YamlConfiguration

class Translator(config: YamlConfiguration) {
    private val values = HashMap<TranslationKey, String>()

    init {
        val translations = enumValues<TranslationKey>()
        for (translation in translations) {
            val key = translation.toString()
            if (!config.contains(key, true)) {
                continue
            }
            val value = config.get(key)
            if (value is String) {
                values[translation] = value
            } else {
                values[translation] = "Unknown Translation."
            }
        }
    }

    fun get(key: TranslationKey) = values[key] ?: ""
}
