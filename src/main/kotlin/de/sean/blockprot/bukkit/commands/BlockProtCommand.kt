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
package de.sean.blockprot.bukkit.commands

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.inventories.InventoryState
import de.sean.blockprot.bukkit.inventories.UserSettingsInventory
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.*

class BlockProtCommand : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args.size <= 1) {
            val list = mutableListOf("settings")
            if (sender.isOp) {
                list.add("update")
            }
            return list
        }

        return Collections.emptyList()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        when (args[0]) {
            "update" -> if (sender.isOp) {
                Bukkit.getScheduler()
                    .runTaskAsynchronously(BlockProt.getInstance()!!, UpdateChecker(Bukkit.getOnlinePlayers().toList(), BlockProt.getInstance()!!.description))
                return true
            }
            "settings" -> {
                val player = Bukkit.getPlayer(sender.name)

                return if (player != null) {
                    InventoryState.set(player.uniqueId, InventoryState(null))
                    player.openInventory(UserSettingsInventory().fill(player))
                    true
                } else false
            }
        }
        return false
    }
}
