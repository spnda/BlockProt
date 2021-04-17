package de.sean.blockprot.bukkit.commands

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.inventories.InventoryState
import de.sean.blockprot.bukkit.inventories.UserSettingsInventory
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import de.tr7zw.nbtapi.NBTEntity
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.*

class BlockProtCommand : TabExecutor {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size <= 1) {
            val list = mutableListOf<String>("settings")
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
                    .runTaskAsynchronously(BlockProt.instance, UpdateChecker(true, BlockProt.instance.description))
                return true
            }
            "settings" -> {
                val player = Bukkit.getPlayer(sender.name)

                return if (player != null) {
                    InventoryState.set(player.uniqueId, InventoryState(null))
                    player.openInventory(UserSettingsInventory.createInventoryAndFill(player))
                    true
                } else false
            }
        }
        return false
    }
}
