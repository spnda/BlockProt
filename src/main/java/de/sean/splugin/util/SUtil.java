package de.sean.splugin.util;

/* SPlugin */
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.sean.splugin.App;

/* Java */
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/* Spigot */
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class SUtil {
    private static HashMap<UUID, Long> playerLastActivity = new HashMap<>();
    private static final HashMap<UUID, Boolean> afkPlayers = new HashMap<>();

    public static String GUILD_ID;
    public static String CHANNEL_ID;

    public static void removePlayerAFK(UUID uuid)                       {           afkPlayers.remove(uuid); }
    public static void setPlayerAFK(UUID uuid, boolean afk)             {           afkPlayers.put(uuid, afk); }
    public static boolean isPlayerAFK(UUID uuid)                        { return    afkPlayers.get(uuid); }

    public static void removeActivityForPlayer(UUID uuid)               {           playerLastActivity.remove(uuid); }
    public static void setLastActivityForPlayer(UUID uuid, long time)   {           playerLastActivity.put(uuid, time); }
    public static long getLastActivityForPlayer(UUID uuid)              { return    playerLastActivity.get(uuid); }

    public static void saveConfigFile(FileConfiguration config) {
        try {
            config.save(App.getInstance().getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static String concatArrayRange(String[] arr, int begin, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = begin; i < end; i++) {
            builder.append(arr[i]).append(" ");
        }
        return builder.toString();
    }

    public static List<String> parseStringList(String str) {
        List<String> ret = new ArrayList<>(Arrays.asList(str.replaceAll("^\\[|]$", "").split(",")));
        ret.removeIf(String::isEmpty);
        return ret;
    }

    public static int randomInt(int min, int max)  {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static Color randomColor() {
        Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
        return colors[randomInt(0, colors.length - 1)];
    }

    public static String removePlayerTypeForString(String name) {
        return name.split("\\|")[1].trim();
    }

    public static String getStringForPlayerType(PlayerType playerType) {
        switch (playerType) {
            case MAYOR:             return "B\u00FCrgermeister";
            case DEPUTY_MAYOR:      return "Stellvtr. B\u00FCrgermeister";
            case OWNER:             return "Eigent\u00FCmer";
            case CITIZEN: default:  return "B\u00FCrger";
        }
    }

    public static ChatColor getChatColorForPlayerType(PlayerType playerType) {
        switch (playerType) {
            case MAYOR:             return ChatColor.GOLD;
            case DEPUTY_MAYOR:      return ChatColor.YELLOW;
            case OWNER:             return ChatColor.RED;
            case CITIZEN: default:  return ChatColor.GREEN;
        }
    }

    public static PlayerType getPlayerType(Player player) {
        FileConfiguration config = App.getInstance().getConfig();
        Object obj = config.get("Players." + player.getUniqueId() + ".Role");

        // There was no data found. We should put some default data there.
        // For now, return PlayerType.CITIZEN.
        if (obj == null) {
            config.set("Players." + player.getUniqueId() + ".Role", "CITIZEN");
            saveConfigFile(config);
            return PlayerType.CITIZEN;
        } else if (obj.equals("MAYOR")) {
            return PlayerType.MAYOR;
        } else if (obj.equals("OWNER")) {
            return PlayerType.OWNER;
        } else if (obj.equals("DEPUTY_MAYOR")) {
            return PlayerType.DEPUTY_MAYOR;
        } else {
            return PlayerType.CITIZEN;
        }
    }

    public enum PlayerType {
        CITIZEN,
        MAYOR,
        DEPUTY_MAYOR,
        OWNER,
    }
}
