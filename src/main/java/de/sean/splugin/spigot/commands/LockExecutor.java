package de.sean.splugin.spigot.commands;

/* SPlugin */

import de.sean.splugin.App;
import de.sean.splugin.util.SLockUtil;

/* Java */
import de.sean.splugin.util.SUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LockExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = Bukkit.getPlayer(sender.getName());
        Player newPlayer;
        if (player == null) return false;
        if (args.length == 0 || args[0].equals("private")) {
            SLockUtil.addUserToAddLocking(player.getUniqueId());
            return true;
        }
        switch (args[0]) {
            case "add":
                if (args.length != 2) return false;
                newPlayer = Bukkit.getPlayer(args[1]);
                if (newPlayer == null) return false;
                SLockUtil.addUserToBeAddedFromLocking(player.getUniqueId(), newPlayer.getUniqueId());
                return true;
            case "remove":
                if (args.length != 2) return false;
                newPlayer = Bukkit.getPlayer(args[1]);
                if (newPlayer == null) return false;
                SLockUtil.addUserToBeRemovedFromLocking(player.getUniqueId(), newPlayer.getUniqueId());
                return true;
            case "public":
                SLockUtil.addUserToRemoveLocking(player.getUniqueId());
                return true;
            case "info":
                if (!sender.isOp()) return false;
                SLockUtil.addUserToInfo(player.getUniqueId());
                return true;
            case "clear":
                if (!sender.isOp()) return false;
                SLockUtil.addRemoveLockingForUser(player.getUniqueId());
                return true;
            case "place":
                FileConfiguration config = App.getInstance().getConfig();
                config.set("Players." + player.getUniqueId() + ".NotLockOnPlace", !config.getBoolean("Players." + player.getUniqueId() + ".NotLockOnPlace"));
                SUtil.saveConfigFile(config);
                break;
            default:
                return false;
        }
        return false;
    }
}
