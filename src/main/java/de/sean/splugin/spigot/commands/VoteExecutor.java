package de.sean.splugin.spigot.commands;

/* SPlugin */
import de.sean.splugin.util.SUtil;
import de.sean.splugin.util.SVote;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteExecutor implements CommandExecutor {
    private final SVote votingInstance;

    public VoteExecutor(SVote votingInstance) {
        this.votingInstance = votingInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // There's no current voting taking place or a vote has ended.
        if (!votingInstance.isCurrentlyVoting()) return false;
        // Only players can vote.
        if (sender instanceof Player) {
            Player player = (Player)sender;
            // If we have less than 2 arguments, this can't be valid.
            if (args.length < 2) return false;
            if (!player.hasPermission("SPlugin.vote")) return false;
            if (votingInstance.hasPlayerVoted(player.getUniqueId())) return false;
            // Now, check if any of the currently online players match the given UUID.
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // We'll test if the name of the player matches our command argument.
                if (SUtil.removePlayerTypeForString(onlinePlayer.getDisplayName()).equals(args[1])) {
                    // We'll set the player to having voted already. Then we add the vote and do some checks.
                    votingInstance.setPlayerHasVoted(player.getUniqueId());
                    votingInstance.addNewVoteForPlayer(onlinePlayer.getUniqueId());
                    votingInstance.checks();
                    return true;
                }
            }
            player.chat("Unbekannter Spieler.");
            return false;
        } else {
            return false;
        }
    }
}
