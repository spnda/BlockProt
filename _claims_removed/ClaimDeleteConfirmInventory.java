package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.claims.Claim;
import de.sean.blockprot.bukkit.claims.ClaimManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple yes/no confirmation dialog before deleting a claim.
 *
 * <h3>Layout (1 row, 9 slots)</h3>
 * <pre>
 *  [ ][ ][ ][YES][ ][NO][ ][ ][ ]
 * </pre>
 *
 * <ul>
 *   <li>Slot 3 — Yes (LIME_WOOL)  : deletes the claim and closes</li>
 *   <li>Slot 5 — No  (RED_WOOL)   : goes back to {@link ClaimManagementHub}</li>
 * </ul>
 *
 * @since SP26
 */
public final class ClaimDeleteConfirmInventory extends BlockProtInventory {

    private static final int SLOT_YES    = 3;
    private static final int SLOT_CANCEL = 5;

    @Override
    public int getSize() { return InventoryConstants.singleLine; } // 9

    @Override
    public @Nullable String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.CLAIMS__DELETE_CONFIRM_TITLE);
    }

    // ─── fill ────────────────────────────────────────────────────────────────

    public @Nullable Inventory fill(@NotNull Player player) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null || state.currentClaimId == null) return null;

        Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
        if (claim == null) return null;

        inventory.clear();
        setItemStack(SLOT_YES,    Material.LIME_WOOL, TranslationKey.CLAIMS__DELETE_YES);
        setItemStack(SLOT_CANCEL, Material.RED_WOOL,  TranslationKey.CLAIMS__DELETE_CANCEL);
        return inventory;
    }

    // ─── onClick ─────────────────────────────────────────────────────────────

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        event.setCancelled(true);
        if (state.currentClaimId == null) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        switch (event.getSlot()) {

            case SLOT_YES -> {
                Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
                String name = claim != null ? claim.getName() : "?";
                ClaimManager.getInstance().deleteClaim(
                    state.currentClaimId, player.getUniqueId().toString());
                player.closeInventory();
                InventoryState.remove(player.getUniqueId());
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                    Translator.get(TranslationKey.CLAIMS__DELETED)
                        .replace("{name}", name)));
            }

            case SLOT_CANCEL -> {
                // Return to hub
                InventoryState hubState = new InventoryState(null);
                hubState.currentClaimId = state.currentClaimId;
                InventoryState.set(player.getUniqueId(), hubState);
                closeAndOpen(player, new ClaimManagementHub().fill(player));
            }
        }
    }

    // ─── onClose ─────────────────────────────────────────────────────────────

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        // No data to persist.
    }
}
