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
