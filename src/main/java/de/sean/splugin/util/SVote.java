package de.sean.splugin.util;

/* Java */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;

public class SVote {
    // We'll use a HashMap to store all the votes. 
    // The first UUID will be the UUID of the player to be voted for.
    // Each votable player then gets a HashMap linked to them with a few properties.
    private HashMap<UUID, HashMap<String, Object>> votes = new HashMap<UUID, HashMap<String, Object>>();
    // A HashMap to store all players, who have voted already.
    private HashMap<UUID, Boolean> playersVoted = new HashMap<UUID, Boolean>();
    // This will simply be a list of players available for voting.
    private ArrayList<UUID> players = new ArrayList<UUID>();

    private boolean currentlyVoting = true;

    public void setupVote(ArrayList<UUID> players) {
        this.players = players;
    }

    private void endVote() {
        SMessages.sendGlobalMessage(ChatColor.AQUA + "Die Wahl ist vorbei!");
        SMessages.sendGlobalMessage(ChatColor.AQUA + "Es haben " + playersVoted.size() + " Spieler abgestimmt.");
    }

    public void checks() {
        // If the length of players who've voted is the same length of online players,
        // All players seem to have voted. End the Vote.
        if (playersVoted.size() == players.size()) {
            currentlyVoting = false;
            endVote();
        }
    }

    public void setPlayerHasVoted(UUID player) {
        playersVoted.put(player, true);
    }

    public void addNewVoteForPlayer(UUID player) {
        HashMap<String, Object> v = votes.get(player);
        if (v.get("votes") == null) {
            v.put("votes", 1);
        } else {
            v.put("votes", (int)v.get("votes") + 1);
        }
    }

    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public boolean hasPlayerVoted(UUID uuid) {
        return playersVoted.get(uuid) == null ? false : playersVoted.get(uuid);
    }

    public boolean isCurrentlyVoting() {
        return currentlyVoting;
    }
}
