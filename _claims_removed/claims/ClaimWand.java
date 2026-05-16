package de.sean.blockprot.bukkit.claims;

import de.sean.blockprot.bukkit.BlockProt;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Helper class for the claim selection wand (Blaze Rod with a PDC tag).
 *
 * <p>Flow: Left-click → corner A · Right-click → corner B → wand is removed
 * automatically and the player is prompted in chat to enter a name.</p>
 *
 * @since SP26
 */
public final class ClaimWand {

    /** PDC key that marks a Blaze Rod as a claim wand. */
    public static final NamespacedKey WAND_KEY =
        new NamespacedKey(BlockProt.getInstance(), "claim_wand");

    private ClaimWand() {}

    /** Creates a new wand {@link ItemStack} with instructions in its lore. */
    @NotNull
    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lClaim Wand");
            meta.setLore(List.of(
                "§7Shift+Left-click  §f→ §bSet corner §lA",
                "§7Shift+Right-click §f→ §bSet corner §lB",
                "§8The wand is removed automatically",
                "§8once both corners are selected."
            ));
            meta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    /** Returns {@code true} if the given item stack is a claim wand. */
    public static boolean isWand(@Nullable ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(WAND_KEY, PersistentDataType.BYTE);
    }

    /**
     * Removes all wand items from the player's inventory (main + off hand).
     * Safe to call from any thread since it only modifies the inventory object.
     */
    public static void removeFromInventory(@NotNull Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isWand(inv.getItem(i))) inv.setItem(i, null);
        }
        if (isWand(inv.getItemInOffHand()))
            inv.setItemInOffHand(null);
    }

    /** Gives a wand to the player (adds to first available slot). */
    public static void giveWand(@NotNull Player player) {
        player.getInventory().addItem(createWand());
        player.sendMessage(
            "§aYou received the §6§lClaim Wand§a. §7Shift+Left-click corner §bA§7, Shift+Right-click corner §bB§7.");
    }
}
