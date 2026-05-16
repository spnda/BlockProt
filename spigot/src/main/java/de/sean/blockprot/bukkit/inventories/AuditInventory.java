package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.Permissions;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.audit.AuditLogger;
import de.sean.blockprot.bukkit.audit.AuditLogger.AuditEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// GUI that shows the access history for a protected block.
// Owners see denied attempts. Admins also get a teleport button.
public final class AuditInventory extends BlockProtInventory {

    private static final int PAGE_SIZE = 45; // Five rows for entries, last row for controls.
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM HH:mm");

    private List<AuditEntry> entries = new ArrayList<>();
    private String blockWorld;
    private int blockX, blockY, blockZ;

    @Override
    int getSize() { return InventoryConstants.sextupletLine; }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__AUDIT__TITLE);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) {
            event.setCancelled(true);
            return;
        }

        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE -> {
                // Return to the block menu.
                if (state.getBlock() != null) {
                    var handler = getNbtHandlerOrNull(state.getBlock());
                    closeAndOpen(player, handler == null ? null
                        : new BlockLockInventory().fill(player, state.getBlock().getType(), handler));
                } else {
                    closeAndOpen(player, null);
                }
            }
            case CYAN_STAINED_GLASS_PANE -> {
                if (state.currentPageIndex > 0) {
                    state.currentPageIndex--;
                    closeAndOpen(player, fill(player));
                }
            }
            case BLUE_STAINED_GLASS_PANE -> {
                int maxPage = (int) Math.ceil(entries.size() / (double) PAGE_SIZE);
                if (state.currentPageIndex < maxPage - 1) {
                    state.currentPageIndex++;
                    closeAndOpen(player, fill(player));
                }
            }
            case COMPASS -> {
                // Teleport to the block. Admins only.
                if (player.hasPermission(Permissions.ADMIN.key())) {
                    var world = Bukkit.getWorld(blockWorld);
                    if (world != null) {
                        player.closeInventory();
                        player.teleport(new Location(world, blockX + 0.5, blockY + 1, blockZ + 0.5));
                    }
                }
            }
            default -> {}
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {}

    @Nullable
    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return null;

        AuditLogger audit = BlockProt.getAuditLogger();
        Block block = state.getBlock();

        inventory.clear();

        if (audit == null || block == null) {
            setItemStack(22, Material.BARRIER, Translator.get(TranslationKey.INVENTORIES__AUDIT__NO_ENTRIES));
            setBackButton();
            return inventory;
        }

        blockWorld = block.getWorld().getName();
        blockX = block.getX();
        blockY = block.getY();
        blockZ = block.getZ();

        // Load entries once; page changes reuse the cached list.
        if (entries.isEmpty()) {
            entries = audit.getEntriesForBlock(blockWorld, blockX, blockY, blockZ, 500);
        }

        if (entries.isEmpty()) {
            setItemStack(22, Material.PAPER, Translator.get(TranslationKey.INVENTORIES__AUDIT__NO_ENTRIES));
            setBackButton();
            return inventory;
        }

        int offset = state.currentPageIndex * PAGE_SIZE;
        int count  = Math.min(PAGE_SIZE, entries.size() - offset);

        for (int i = 0; i < count; i++) {
            AuditEntry e = entries.get(offset + i);
            ItemStack skull = new ItemStack(Material.SKELETON_SKULL, 1);
            ItemMeta meta = skull.getItemMeta();
            if (meta != null) {
                String actionLabel = e.action() == AuditLogger.Action.ACCESS_DENIED ? "§cX" : "§aOK";
                meta.setDisplayName(actionLabel + " §f" + (e.playerName() != null ? e.playerName() : e.playerUuid()));
                List<String> lore = new ArrayList<>();
                lore.add("§7" + DATE_FMT.format(new Date(e.timestamp())));
                lore.add("§8" + e.world() + " " + e.x() + "," + e.y() + "," + e.z());
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            inventory.setItem(i, skull);
        }

        // Controls on the last row.
        setItemStack(45, Material.CYAN_STAINED_GLASS_PANE,  TranslationKey.INVENTORIES__LAST_PAGE);
        setItemStack(46, Material.BLUE_STAINED_GLASS_PANE,  TranslationKey.INVENTORIES__NEXT_PAGE);

        // Teleport button for admins only.
        if (player.hasPermission(Permissions.ADMIN.key())) {
            setItemStack(49, Material.COMPASS, TranslationKey.INVENTORIES__AUDIT__TELEPORT);
        }

        setBackButton(53);
        return inventory;
    }
}
