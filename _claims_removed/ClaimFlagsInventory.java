package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.claims.Claim;
import de.sean.blockprot.bukkit.claims.ClaimFlag;
import de.sean.blockprot.bukkit.claims.ClaimManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GUI for toggling all claim flags. Opens from the management hub or directly via
 * {@code /bp claim flags}.
 *
 * <p>Layout (6 rows, 54 slots):</p>
 * <pre>
 * Row 0 (env):      FIRE TNT REDSTONE PVP FLOW LEAF_DECAY MOB_SPAWNING
 * Row 1 (interact): CHEST DOOR LEVER_BUTTON USE_BUCKET
 * Row 2 (pets):     PET_SPAWNING PET_RIDING
 * Row 3 (special):  INVINCIBLE DENY_ENDERPEARL
 * Row 5:            [BACK at slot 53]
 * </pre>
 *
 * @since SP26
 */
public final class ClaimFlagsInventory extends BlockProtInventory {

    // ─── Slot map ────────────────────────────────────────────────────────
    // Row 0 – environmental
    private static final int SLOT_FIRE           = 1;
    private static final int SLOT_TNT            = 2;
    private static final int SLOT_REDSTONE       = 3;
    private static final int SLOT_PVP            = 4;
    private static final int SLOT_FLOW           = 5;
    private static final int SLOT_LEAF_DECAY     = 6;
    private static final int SLOT_MOB_SPAWNING   = 7;

    // Row 1 – interaction
    private static final int SLOT_CHEST          = 10;
    private static final int SLOT_DOOR           = 11;
    private static final int SLOT_LEVER_BUTTON   = 12;
    private static final int SLOT_USE_BUCKET     = 13;

    // Row 2 – pets
    private static final int SLOT_PET_SPAWNING   = 19;
    private static final int SLOT_PET_RIDING     = 20;
    private static final int SLOT_PET_MOVING     = 21;

    // Row 3 – special / admin
    private static final int SLOT_INVINCIBLE         = 28;
    private static final int SLOT_DENY_ENDERPEARL    = 29;

    // Back button at slot 53 (last slot of 6-row inventory)

    @Override
    public int getSize() { return InventoryConstants.sextupletLine; } // 54 slots

    @Override
    public @Nullable String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.CLAIMS__FLAGS__TITLE);
    }

    // ─── fill ───────────────────────────────────────────────────────────────────────

    /**
     * Populates the inventory with the claim's current flag states.
     *
     * @return The ready-to-open {@link Inventory}, or {@code null} if the claim was not found.
     */
    public @Nullable Inventory fill(@NotNull Player player) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null || state.currentClaimId == null) return null;

        Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
        if (claim == null) return null;

        inventory.clear();
        refreshAllFlags(claim);
        setBackButton(); // slot 53
        return inventory;
    }

    // ─── onClick ─────────────────────────────────────────────────────────────────

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        event.setCancelled(true);
        if (state.currentClaimId == null) return;

        Claim claim = ClaimManager.getInstance().getClaimById(state.currentClaimId);
        if (claim == null) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        // Back button → reopen hub
        if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
            InventoryState hubState = new InventoryState(null);
            hubState.currentClaimId = state.currentClaimId;
            InventoryState.set(player.getUniqueId(), hubState);
            closeAndOpen(player, new ClaimManagementHub().fill(player));
            return;
        }

        ClaimFlag toggled = slotToFlag(event.getSlot());
        if (toggled == null) return;

        claim.setFlag(toggled, !claim.getFlag(toggled));
        FlagDisplay display = flagToDisplay(toggled);
        if (display != null) {
            setEnchantedOptionItemStack(event.getSlot(), display.material(), display.key(),
                claim.getFlag(toggled));
        }
        ClaimManager.getInstance().saveAsync();
    }

    // ─── onClose ────────────────────────────────────────────────────────────────

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        ClaimManager.getInstance().saveAsync();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    private void refreshAllFlags(@NotNull Claim claim) {
        setEnchantedOptionItemStack(SLOT_FIRE,           Material.FLINT_AND_STEEL,TranslationKey.CLAIMS__FLAGS__FIRE,           claim.getFlag(ClaimFlag.FIRE));
        setEnchantedOptionItemStack(SLOT_TNT,            Material.TNT,            TranslationKey.CLAIMS__FLAGS__TNT,            claim.getFlag(ClaimFlag.TNT));
        setEnchantedOptionItemStack(SLOT_REDSTONE,       Material.REDSTONE,       TranslationKey.CLAIMS__FLAGS__REDSTONE,       claim.getFlag(ClaimFlag.REDSTONE));
        setEnchantedOptionItemStack(SLOT_PVP,            Material.IRON_SWORD,     TranslationKey.CLAIMS__FLAGS__PVP,            claim.getFlag(ClaimFlag.PVP));
        setEnchantedOptionItemStack(SLOT_FLOW,           Material.WATER_BUCKET,   TranslationKey.CLAIMS__FLAGS__FLOW,           claim.getFlag(ClaimFlag.FLOW));
        setEnchantedOptionItemStack(SLOT_LEAF_DECAY,     Material.OAK_LEAVES,     TranslationKey.CLAIMS__FLAGS__LEAF_DECAY,     claim.getFlag(ClaimFlag.LEAF_DECAY));
        setEnchantedOptionItemStack(SLOT_MOB_SPAWNING,   Material.ZOMBIE_HEAD,    TranslationKey.CLAIMS__FLAGS__MOB_SPAWNING,   claim.getFlag(ClaimFlag.MOB_SPAWNING));
        setEnchantedOptionItemStack(SLOT_CHEST,          Material.CHEST,          TranslationKey.CLAIMS__FLAGS__CHEST,          claim.getFlag(ClaimFlag.CHEST));
        setEnchantedOptionItemStack(SLOT_DOOR,           Material.OAK_DOOR,       TranslationKey.CLAIMS__FLAGS__DOOR,           claim.getFlag(ClaimFlag.DOOR));
        setEnchantedOptionItemStack(SLOT_LEVER_BUTTON,   Material.LEVER,          TranslationKey.CLAIMS__FLAGS__LEVER_BUTTON,   claim.getFlag(ClaimFlag.LEVER_BUTTON));
        setEnchantedOptionItemStack(SLOT_USE_BUCKET,     Material.BUCKET,         TranslationKey.CLAIMS__FLAGS__USE_BUCKET,     claim.getFlag(ClaimFlag.USE_BUCKET));
        setEnchantedOptionItemStack(SLOT_PET_SPAWNING,   Material.LEAD,           TranslationKey.CLAIMS__FLAGS__PET_SPAWNING,   claim.getFlag(ClaimFlag.PET_SPAWNING));
        setEnchantedOptionItemStack(SLOT_PET_RIDING,     Material.SADDLE,         TranslationKey.CLAIMS__FLAGS__PET_RIDING,     claim.getFlag(ClaimFlag.PET_RIDING));
        setEnchantedOptionItemStack(SLOT_PET_MOVING,     Material.HORSE_SPAWN_EGG,TranslationKey.CLAIMS__FLAGS__PET_MOVING,     claim.getFlag(ClaimFlag.PET_MOVING));
        setEnchantedOptionItemStack(SLOT_INVINCIBLE,     Material.GOLDEN_APPLE,   TranslationKey.CLAIMS__FLAGS__INVINCIBLE,     claim.getFlag(ClaimFlag.INVINCIBLE));
        setEnchantedOptionItemStack(SLOT_DENY_ENDERPEARL,Material.ENDER_PEARL,    TranslationKey.CLAIMS__FLAGS__DENY_ENDERPEARL,claim.getFlag(ClaimFlag.DENY_ENDERPEARL));
    }

    /** Maps a slot index back to a {@link ClaimFlag}, or {@code null} for non-flag slots. */
    @Nullable
    private static ClaimFlag slotToFlag(int slot) {
        return switch (slot) {
            case SLOT_FIRE            -> ClaimFlag.FIRE;
            case SLOT_TNT             -> ClaimFlag.TNT;
            case SLOT_REDSTONE        -> ClaimFlag.REDSTONE;
            case SLOT_PVP             -> ClaimFlag.PVP;
            case SLOT_FLOW            -> ClaimFlag.FLOW;
            case SLOT_LEAF_DECAY      -> ClaimFlag.LEAF_DECAY;
            case SLOT_MOB_SPAWNING    -> ClaimFlag.MOB_SPAWNING;
            case SLOT_CHEST           -> ClaimFlag.CHEST;
            case SLOT_DOOR            -> ClaimFlag.DOOR;
            case SLOT_LEVER_BUTTON    -> ClaimFlag.LEVER_BUTTON;
            case SLOT_USE_BUCKET      -> ClaimFlag.USE_BUCKET;
            case SLOT_PET_SPAWNING    -> ClaimFlag.PET_SPAWNING;
            case SLOT_PET_RIDING      -> ClaimFlag.PET_RIDING;
            case SLOT_PET_MOVING      -> ClaimFlag.PET_MOVING;
            case SLOT_INVINCIBLE      -> ClaimFlag.INVINCIBLE;
            case SLOT_DENY_ENDERPEARL -> ClaimFlag.DENY_ENDERPEARL;
            default -> null;
        };
    }

    private record FlagDisplay(Material material, TranslationKey key) {}

    @Nullable
    private static FlagDisplay flagToDisplay(@NotNull ClaimFlag flag) {
        return switch (flag) {
            case FIRE            -> new FlagDisplay(Material.FLINT_AND_STEEL, TranslationKey.CLAIMS__FLAGS__FIRE);
            case TNT             -> new FlagDisplay(Material.TNT,             TranslationKey.CLAIMS__FLAGS__TNT);
            case REDSTONE        -> new FlagDisplay(Material.REDSTONE,        TranslationKey.CLAIMS__FLAGS__REDSTONE);
            case PVP             -> new FlagDisplay(Material.IRON_SWORD,      TranslationKey.CLAIMS__FLAGS__PVP);
            case FLOW            -> new FlagDisplay(Material.WATER_BUCKET,    TranslationKey.CLAIMS__FLAGS__FLOW);
            case LEAF_DECAY      -> new FlagDisplay(Material.OAK_LEAVES,      TranslationKey.CLAIMS__FLAGS__LEAF_DECAY);
            case MOB_SPAWNING    -> new FlagDisplay(Material.ZOMBIE_HEAD,     TranslationKey.CLAIMS__FLAGS__MOB_SPAWNING);
            case CHEST           -> new FlagDisplay(Material.CHEST,           TranslationKey.CLAIMS__FLAGS__CHEST);
            case DOOR            -> new FlagDisplay(Material.OAK_DOOR,        TranslationKey.CLAIMS__FLAGS__DOOR);
            case LEVER_BUTTON    -> new FlagDisplay(Material.LEVER,           TranslationKey.CLAIMS__FLAGS__LEVER_BUTTON);
            case USE_BUCKET      -> new FlagDisplay(Material.BUCKET,          TranslationKey.CLAIMS__FLAGS__USE_BUCKET);
            case PET_SPAWNING    -> new FlagDisplay(Material.LEAD,            TranslationKey.CLAIMS__FLAGS__PET_SPAWNING);
            case PET_RIDING      -> new FlagDisplay(Material.SADDLE,          TranslationKey.CLAIMS__FLAGS__PET_RIDING);
            case PET_MOVING      -> new FlagDisplay(Material.HORSE_SPAWN_EGG, TranslationKey.CLAIMS__FLAGS__PET_MOVING);
            case INVINCIBLE      -> new FlagDisplay(Material.GOLDEN_APPLE,    TranslationKey.CLAIMS__FLAGS__INVINCIBLE);
            case DENY_ENDERPEARL -> new FlagDisplay(Material.ENDER_PEARL,     TranslationKey.CLAIMS__FLAGS__DENY_ENDERPEARL);
        };
    }
}


