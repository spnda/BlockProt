package de.sean.splugin.spigot.commands;

/* SPlugin */
import de.sean.splugin.util.SLockUtil;

/* Java */
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
        if (args.length > 0) {
            Player player = Bukkit.getPlayer(sender.getName());
            Player newPlayer;
            if (player == null) return false;
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
                case "private":
                    SLockUtil.addUserToAddLocking(player.getUniqueId());
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
                default:
                    // A unknown argument was passed.
                    return false;
            }
        }
        return false;
    }
}
