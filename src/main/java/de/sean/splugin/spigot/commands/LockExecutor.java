package de.sean.splugin.spigot.commands;

/* SPlugin */
import de.sean.splugin.SPlugin;
import de.sean.splugin.util.SLockUtil;

/* Java */
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
        final Player player = Bukkit.getPlayer(sender.getName());
        if (player == null) return false;
        switch (args[0]) {
            case "place":
                final FileConfiguration config = SPlugin.instance.getConfig();
                config.set("Players." + player.getUniqueId() + ".NotLockOnPlace", !config.getBoolean("players." + player.getUniqueId() + ".notLockOnPlace"));
                SPlugin.instance.saveConfig();
                break;
            default:
                return false;
        }
        return false;
    }
}
