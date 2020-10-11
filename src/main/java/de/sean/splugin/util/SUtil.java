package de.sean.splugin.util;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
        final StringBuilder builder = new StringBuilder();
        for (int i = begin; i < end; i++) {
            builder.append(arr[i]).append(" ");
        }
        return builder.toString();
    }

    public static List<String> parseStringList(String str) {
        final List<String> ret = new ArrayList<>(Arrays.asList(str.replaceAll("^\\[|]$", "").split(",")));
        ret.removeIf(String::isEmpty);
        return ret;
    }

    /**
     * Returns a pseudorandom {@code int} value between the specified
     * origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a pseudorandom {@code int} value between the origin
     *         (inclusive) and the bound (exclusive)
     * @throws IllegalArgumentException if {@code origin} is greater than
     *         or equal to {@code bound}
     */
    public static int randomInt(int min, int max) throws IllegalArgumentException {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static Color randomColor() {
        final Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
        return colors[randomInt(0, colors.length)];
    }

    public static ItemStack getItemStack(int num, Material material, String name) {
        return getItemStack(num, material, name, Collections.emptyList());
    }

    public static ItemStack getItemStack(int num, Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material, num);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack getPlayerSkull(OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(player.getName());
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(player.getDisplayName());
        skull.setItemMeta(meta);
        return skull;
    }
}
