package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.claims.Claim;
import de.sean.blockprot.bukkit.claims.ClaimManager;
import de.sean.blockprot.bukkit.claims.ClaimWand;
import de.sean.blockprot.bukkit.listeners.ClaimEventListener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Central management hub for a single claim.
 * Opened via Shift + Right-Click on any block inside the owner's claim,
 * or programmatically from {@link de.sean.blockprot.bukkit.commands.ClaimCommand}.
 *
 * <h3>Layout (3 rows, 27 slots)</h3>
 * <pre>
 *  [INFO][FLG][ ][ ][ ][ ][WND][WLC][LST]   row 0
 *  [ ][ ][ ][ ][ ][ ][ ][ ][ ]              row 1
 *  [ ][ ][ ][ ][ ][ ][DEL][ ][ ]            row 2
 * </pre>
 *
 * <ul>
 *   <li>Slot  0 — Info   (PAPER)         : prints claim details to chat</li>
 *   <li>Slot  1 — Flags  (COMPARATOR)    : opens {@link ClaimFlagsInventory}</li>
 *   <li>Slot  6 — Wand   (BLAZE_ROD)     : gives selection wand</li>
 *   <li>Slot  7 — Welcome(WRITABLE_BOOK) : starts welcome-message chat flow</li>
 *   <li>Slot  8 — List   (MAP)           : lists all owner claims in chat</li>
 *   <li>Slot 24 — Delete (BARRIER)       : opens {@link ClaimDeleteConfirmInventory}</li>
 * </ul>
 *
 * @since SP26
 */
public final class ClaimManagementHub extends BlockProtInventory {

    // ─── Slot constants ───────────────────────────────────────────────────────
    private static final int SLOT_INFO      = 0;
    private static final int SLOT_FLAGS     = 1;
    private static final int SLOT_WAND      = 6;
    private static final int SLOT_WELCOME   = 7;
    private static final int SLOT_MY_CLAIMS = 8;
    private static final int SLOT_DELETE    = 24;

    // ─── BlockProtInventory contract ─────────────────────────────────────────

    @Override
    public int getSize() { return InventoryConstants.tripleLine; } // 27

    @Override
    public @Nullable String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.CLAIMS__HUB__TITLE);
    }

    // ─── fill ────────────────────────────────────────────────────────────────

    /**
     * Populates the hub for the claim stored in the player's {@link InventoryState}.
     *
     * @return The ready-to-open inventory, or {@code null} if no valid claim is found in state.
     */
    public @Nullable Inventory fill(@NotNull Player player) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null || state.currentClaimId == null) return null;

        Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
        if (claim == null) return null;

        inventory.clear();

        setItemStack(SLOT_INFO,      Material.PAPER,          TranslationKey.CLAIMS__HUB__INFO);
        setItemStack(SLOT_FLAGS,     Material.COMPARATOR,     TranslationKey.CLAIMS__HUB__FLAGS);
        setItemStack(SLOT_WAND,      Material.BLAZE_ROD,      TranslationKey.CLAIMS__HUB__WAND);
        setItemStack(SLOT_WELCOME,   Material.WRITABLE_BOOK,  TranslationKey.CLAIMS__HUB__WELCOME);
        setItemStack(SLOT_MY_CLAIMS, Material.MAP,            TranslationKey.CLAIMS__HUB__MY_CLAIMS);
        setItemStack(SLOT_DELETE,    Material.BARRIER,        TranslationKey.CLAIMS__HUB__DELETE);

        return inventory;
    }

    // ─── onClick ─────────────────────────────────────────────────────────────

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        event.setCancelled(true);
        if (state.currentClaimId == null) return;

        Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
        if (claim == null) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        switch (event.getSlot()) {

            case SLOT_INFO -> {
                player.closeInventory();
                printInfo(player, claim);
            }

            case SLOT_FLAGS -> {
                InventoryState flagState = new InventoryState(null);
                flagState.currentClaimId = claim.getId();
                InventoryState.set(player.getUniqueId(), flagState);
                closeAndOpen(player, new ClaimFlagsInventory().fill(player));
            }

            case SLOT_WAND -> {
                player.closeInventory();
                ClaimWand.giveWand(player);
            }

            case SLOT_WELCOME -> {
                player.closeInventory();
                ClaimEventListener.startWelcomePrompt(player, claim);
            }

            case SLOT_MY_CLAIMS -> {
                player.closeInventory();
                printClaimList(player);
            }

            case SLOT_DELETE -> {
                InventoryState delState = new InventoryState(null);
                delState.currentClaimId = claim.getId();
                InventoryState.set(player.getUniqueId(), delState);
                closeAndOpen(player, new ClaimDeleteConfirmInventory().fill(player));
            }
        }
    }

    // ─── onClose ─────────────────────────────────────────────────────────────

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        // Nothing to persist when the hub itself is closed.
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void printInfo(@NotNull Player player, @NotNull Claim claim) {
        send(player, "§6§l" + claim.getName());
        send(player, "§7World:  §f" + claim.getWorldName());
        send(player, String.format("§7From:   §f%d, %d, %d",
            claim.getMinX(), claim.getMinY(), claim.getMinZ()));
        send(player, String.format("§7To:     §f%d, %d, %d",
            claim.getMaxX(), claim.getMaxY(), claim.getMaxZ()));
        send(player, "§7Volume: §f" + claim.getVolume() + " §7blocks");
        String welcome = claim.getWelcomeMessage();
        if (welcome != null && !welcome.isEmpty())
            send(player, "§7Welcome: §f" + welcome);
    }

    private void printClaimList(@NotNull Player player) {
        var claims = ClaimManager.getInstance().getClaimsFor(player.getUniqueId().toString());
        if (claims.isEmpty()) {
            send(player, "§7You have no active claims.");
            return;
        }
        send(player, Translator.get(TranslationKey.CLAIMS__LIST_HEADER));
        for (Claim c : claims) {
            send(player, String.format("§7  §6%s §8│ §7%s §8(§f%d,%d,%d §8→ §f%d,%d,%d§8)",
                c.getName(), c.getWorldName(),
                c.getMinX(), c.getMinY(), c.getMinZ(),
                c.getMaxX(), c.getMaxY(), c.getMaxZ()));
        }
    }

    private void send(@NotNull Player player, @NotNull String text) {
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }
}
