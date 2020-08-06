package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Spigot */
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/* Discord */
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class DeathEvent implements Listener {
    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event) {
        // Some more formatting in the future?
        String message = SMessages.getRandomMessage("Messages.Death").replace("[message]", event.getDeathMessage()).replace("[player]", event.getEntity().getDisplayName());
        event.setDeathMessage(ChatColor.RED + message);
        
        /* Discord */
        JDA jda = App.getInstance().getDiscordInstance();
        Guild guild = jda.getGuildById(SUtil.GUILD_ID);
        if (guild == null) return;
        TextChannel channel = guild.getTextChannelById(SUtil.CHANNEL_ID);
        if (channel != null) channel.sendMessage(message).queue();
    }
}
