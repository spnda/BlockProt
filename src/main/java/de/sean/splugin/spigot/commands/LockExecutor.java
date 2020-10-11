package de.sean.splugin.spigot.commands;

import de.sean.splugin.SPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
