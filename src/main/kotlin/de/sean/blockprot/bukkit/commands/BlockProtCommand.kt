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
                    .runTaskAsynchronously(BlockProt.instance, UpdateChecker(Bukkit.getOnlinePlayers().toList(), BlockProt.instance.description))
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
