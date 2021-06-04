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

package de.sean.blockprot

import de.sean.blockprot.bukkit.commands.BlockProtCommand
import de.sean.blockprot.bukkit.events.*
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import de.sean.blockprot.bukkit.util.LockUtil
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

open class BlockProt : JavaPlugin() {
    companion object {
        lateinit var instance: BlockProt
        protected lateinit var metrics: Metrics
        const val pluginId: Int = 9999
        const val defaultLanguageFile = "translations_en.yml"
    }

    private fun loadTranslation(fileName: String) {
        var file = File(dataFolder, fileName)
        /* Only save the resource if it exists inside the JAR and it has not
         * been saved to the dataFolder yet. */
        val resource = this.getResource(fileName)
        if (resource == null && !file.exists()) {
            Bukkit.getLogger().warning("Could not find language file: $fileName. Defaulting to $defaultLanguageFile")
            this.saveResource(defaultLanguageFile, true)
            file = File(dataFolder, defaultLanguageFile)
        } else if (resource != null && !file.exists()) {
            this.saveResource(fileName, true)
            if (!file.exists()) throw RuntimeException("Could not load language file: $fileName")
        }
        val config = YamlConfiguration.loadConfiguration(file)
        Translator.loadFromConfig(config)
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        LockUtil.loadBlocksFromConfig(config)

        /* Save all translation files into the plugin directory. */
        var languageFileName = config.get("language_file")
        if (languageFileName == null || languageFileName !is String) {
            languageFileName = defaultLanguageFile
        }
        loadTranslation(languageFileName)

        /* Check for updates */
        Bukkit.getScheduler().runTaskAsynchronously(this, UpdateChecker(emptyList(), description))

        /* bStats Metrics */
        metrics = Metrics(this, pluginId)

        /* Register Events */
        val pm = Bukkit.getServer().pluginManager
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, InteractEvent())
        registerEvent(pm, InventoryEvent())
        registerEvent(pm, JoinEvent())
        registerEvent(pm, RedstoneEvent())

        registerCommand("blockprot", BlockProtCommand())

        super.onEnable()
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }

    private fun registerCommand(name: String, executor: TabExecutor) {
        this.getCommand(name)?.setExecutor(executor)
    }
}
