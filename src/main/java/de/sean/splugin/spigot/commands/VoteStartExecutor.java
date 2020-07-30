package de.sean.splugin.spigot.commands;

/* SPlugin */
import de.sean.splugin.util.SVote;

import java.util.ArrayList;
import java.util.Arrays;
/* Java */
import java.util.UUID;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteStartExecutor implements CommandExecutor {
    private final SVote votingInstance;

    public VoteStartExecutor(SVote votingInstance) {
        this.votingInstance = votingInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // There's a vote currently running, we don't  want multiple votes at once.
        if (votingInstance.isCurrentlyVoting()) return false;
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (args.length < 2) return false;
            if (!player.hasPermission("splugin.startVotes")) return false;

            // No fucking clue how Streams work and if this line even works......
            //UUID[] uuids = Arrays.stream(args).map(UUID::fromString).toArray(UUID[]::new);

            ArrayList<UUID> uuids = new ArrayList<UUID>();
            for (String arg : args) {
                Player parg = Bukkit.getPlayer(arg);
                if (parg == null) return false;
                uuids.add(parg.getUniqueId());
            }

            votingInstance.setupVote(uuids);
            // The vote organizer is not allowed to vote. Mark him as voted but don't add a vote.
            votingInstance.setPlayerHasVoted(player.getUniqueId());

            Bukkit.broadcastMessage("Eine Wahl hat begonnen!");
            return true;
        } else {
            return false;
        }
    }
}
