package de.sean.blockprot.bukkit.commands

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.*

class BlockProtCommand : TabExecutor {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size <= 1) {
            return mutableListOf("update")
        }

        return Collections.emptyList()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        if (args[0] == "update" && sender.isOp) {
            Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance, UpdateChecker(true, BlockProt.instance.description))
            return true
        }
        return false
    }
}
