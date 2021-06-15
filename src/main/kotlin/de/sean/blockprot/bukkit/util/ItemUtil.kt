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
