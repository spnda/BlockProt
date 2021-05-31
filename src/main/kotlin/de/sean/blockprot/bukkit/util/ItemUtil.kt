package de.sean.blockprot.bukkit.util

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object ItemUtil {
    fun getItemStack(num: Int, material: Material?, name: String?): ItemStack {
        return getItemStack(num, material, name, emptyList<String>())
    }

    fun getItemStack(num: Int, material: Material?, name: String?, lore: List<String?>?): ItemStack {
        val stack = ItemStack(material!!, num)
        val meta = stack.itemMeta
        meta!!.setDisplayName(name)
        meta.lore = lore
        stack.itemMeta = meta
        return stack
    }

    fun getPlayerSkull(player: OfflinePlayer): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta?
        meta!!.owningPlayer = player
        meta.setDisplayName(player.name)
        skull.itemMeta = meta
        return skull
    }

    fun getPlayerSkull(player: Player): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta?
        meta!!.owningPlayer = player
        meta.setDisplayName(player.displayName)
        skull.itemMeta = meta
        return skull
    }
}
