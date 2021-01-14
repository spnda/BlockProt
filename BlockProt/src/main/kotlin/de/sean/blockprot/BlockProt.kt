package de.sean.blockprot

import de.sean.blockprot.bukkit.events.*
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class BlockProt : JavaPlugin() {
    companion object {
        lateinit var instance: BlockProt
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        /* Register Events */
        val pm = Bukkit.getServer().pluginManager
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, InteractEvent())
        registerEvent(pm, InventoryEvent())

        super.onEnable()
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }
}
