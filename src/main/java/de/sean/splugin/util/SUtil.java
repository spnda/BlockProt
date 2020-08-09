package de.sean.splugin.util;

/* SPlugin */
import de.sean.splugin.App;

/* Java */
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;

/* Spigot */
import org.bukkit.configuration.file.FileConfiguration;

public class SUtil {
    private static final HashMap<UUID, Long> playerLastActivity = new HashMap<>();
    private static final HashMap<UUID, Boolean> afkPlayers = new HashMap<>();

    public static void removePlayerAFK(UUID uuid)                       {           afkPlayers.remove(uuid); }
    public static void setPlayerAFK(UUID uuid, boolean afk)             {           afkPlayers.put(uuid, afk); }
    public static boolean isPlayerAFK(UUID uuid)                        { return    afkPlayers.get(uuid); }

    public static void removeActivityForPlayer(UUID uuid)               {           playerLastActivity.remove(uuid); }
    public static void setLastActivityForPlayer(UUID uuid, long time)   {           playerLastActivity.put(uuid, time); }
    public static long getLastActivityForPlayer(UUID uuid)              { return    playerLastActivity.get(uuid); }

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
}
