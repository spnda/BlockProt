package de.sean.blockprot.bukkit.listeners;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.Permissions;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.claims.Claim;
import de.sean.blockprot.bukkit.claims.ClaimFlag;
import de.sean.blockprot.bukkit.claims.ClaimManager;
import de.sean.blockprot.bukkit.claims.ClaimWand;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import de.sean.blockprot.bukkit.events.BlockProtLockEvent;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import de.sean.blockprot.bukkit.inventories.ClaimManagementHub;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Material;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central listener for the claims system (SP26).
 *
 * <h3>Wand flow</h3>
 * <ol>
 *   <li>Player receives wand via {@code /bp claim wand}.</li>
 *   <li>Left-click a block (or air with block in sight) → corner A, action-bar feedback.</li>
 *   <li>Right-click a block → corner B, wand removed, particle border spawned,
 *       chat prompt for name. 30-second timeout.</li>
 *   <li>Player types name → claim created. "cancel" aborts.</li>
 * </ol>
 *
 * <h3>Fixes in this revision</h3>
 * <ul>
 *   <li><b>Wand LEFT_CLICK_BLOCK</b>: {@code ignoreCancelled = false} so Paper's internal
 *       cancellation of block-interactions with non-tool items does not swallow the event.
 *       Also handles {@code LEFT_CLICK_AIR} as a fallback (common on Paper 1.20+ when
 *       the clicked block is just at reach distance with a non-tool item in hand).</li>
 *   <li><b>Chat input</b>: supports both the deprecated {@link AsyncPlayerChatEvent}
 *       (Spigot) and Paper's modern {@code io.papermc.paper.event.player.AsyncChatEvent}
 *       (Paper 1.19+). Detection is done at class-load via reflection; both are registered
 *       but a guard prevents double-processing on Paper.</li>
 *   <li><b>onInteractFlagCheck</b>: skips processing when the player holds the claim
 *       wand (those clicks belong to {@code onWandInteract}).</li>
 *   <li><b>onClaimHubOpen</b>: lowered to {@code NORMAL} priority so the HIGH-priority
 *       wand handler runs first and cancels the event; the hub handler uses
 *       {@code ignoreCancelled = true} and therefore never fires for wand clicks.</li>
 *   <li><b>onLeadAnimalIntoClaim</b>: replaced world-wide {@code getEntities()} scan
 *       with {@code getNearbyEntities(16,16,16)} — significantly cheaper on large worlds.</li>
 * </ul>
 *
 * @since SP26
 */
public class ClaimEventListener implements Listener {

    // ─── Paper AsyncChatEvent detection (reflection, compile-time safety) ─────

    /**
     * True when {@code io.papermc.paper.event.player.AsyncChatEvent} is present on the
     * classpath (i.e. the server is Paper 1.19+). When true a {@link PaperAsyncChatListener}
     * is created and registered separately; the legacy {@link AsyncPlayerChatEvent} handler
     * remains as a Spigot fallback but is skipped at runtime via {@link #chatHandledThisTick}.
     */
    static final boolean PAPER_CHAT_AVAILABLE;

    static {
        boolean available = false;
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            available = true;
        } catch (ClassNotFoundException ignored) {}
        PAPER_CHAT_AVAILABLE = available;
    }

    // ─── Per-player wand state ────────────────────────────────────────────────

    private final Map<UUID, Location> posA            = new ConcurrentHashMap<>();
    private final Map<UUID, Location> posB            = new ConcurrentHashMap<>();

    /** Players currently waiting to type a claim name in chat. */
    private final Set<UUID> pendingName               = ConcurrentHashMap.newKeySet();

    /** Running particle-border tasks keyed by player UUID. */
    private final Map<UUID, BukkitTask> borderTasks   = new ConcurrentHashMap<>();

    /** Running timeout tasks keyed by player UUID. */
    private final Map<UUID, BukkitTask> timeoutTasks  = new ConcurrentHashMap<>();

    // ─── Welcome-message chat flow ────────────────────────────────────────────

    @Nullable
    private static ClaimEventListener INSTANCE;

    /** Maps playerUUID → claimUUID for players waiting to type a welcome message. */
    private final Map<UUID, UUID> pendingWelcome         = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> welcomeTimeouts  = new ConcurrentHashMap<>();

    /** Which claim each player is currently inside (for welcome-on-enter). */
    private final Map<UUID, UUID> playerInClaim          = new ConcurrentHashMap<>();

    /**
     * Guard: UUIDs whose chat was already consumed by the Paper AsyncChatEvent handler
     * this async tick, so the legacy AsyncPlayerChatEvent handler skips them.
     */
    final Set<UUID> chatHandledThisTick                  = ConcurrentHashMap.newKeySet();

    public ClaimEventListener() {
        INSTANCE = this;
    }

    private static final double PARTICLE_STEP = 0.5;
    private static final long   BORDER_PERIOD = 10L;   // ticks
    private static final long   NAME_TIMEOUT  = 600L;  // ticks = 30 s
    private static final int    MAX_NAME_LEN  = 32;

    // ─── Claim Management Hub (Shift + Right-Click inside own claim) ──────────
    //
    // Priority NORMAL so the HIGH-priority wand handler runs first.
    // ignoreCancelled = true: if the wand handler cancelled the event (wand click),
    // this handler never fires for that interaction.

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClaimHubOpen(@NotNull PlayerInteractEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsHubEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (ClaimWand.isWand(player.getInventory().getItemInMainHand())) return;
        if (event.getClickedBlock() == null) return;

        Claim claim = ClaimManager.getInstance().getClaimAt(player.getLocation());
        if (claim == null) return;
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())) return;

        event.setCancelled(true);
        InventoryState state = new InventoryState(null);
        state.currentClaimId = claim.getId();
        InventoryState.set(player.getUniqueId(), state);

        var inv = new ClaimManagementHub().fill(player);
        if (inv != null) player.openInventory(inv);
    }

    // ─── Wand interaction ────────────────────────────────────────────────────
    //
    // FIX 1: ignoreCancelled = false
    //   Paper 1.20+ internally cancels PlayerInteractEvent for non-tool items
    //   (blaze rods are not tools). Without this the left-click is swallowed and
    //   posA is never recorded.
    //
    // FIX 2: LEFT_CLICK_AIR fallback
    //   On some Paper builds a left-click toward a block near the reach boundary
    //   arrives as LEFT_CLICK_AIR. We resolve the target block via getTargetBlockExact.

    // ─── Wand left-click via arm-swing (Paper-safe) ─────────────────────────
    //
    // PlayerAnimationEvent (arm swing) fires on EVERY left-click in Paper,
    // even when the server internally cancels PlayerInteractEvent for
    // non-tool items. We use this as the authoritative left-click trigger
    // for posA so the wand works reliably on all server variants.

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onWandSwing(@NotNull PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (!ClaimWand.isWand(player.getInventory().getItemInMainHand())) return;

        // Resolve the block the player is looking at (up to 5 blocks away)
        org.bukkit.block.Block target = player.getTargetBlockExact(5);
        if (target == null) {
            // Try non-exact raytrace as fallback
            @SuppressWarnings("deprecation")
            org.bukkit.block.Block fallback = player.getTargetBlock(null, 5);
            if (fallback == null || fallback.getType().isAir()) return;
            target = fallback;
        }
        handlePosA(player, target.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onWandInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!ClaimWand.isWand(player.getInventory().getItemInMainHand())) return;

        Action action = event.getAction();

        // LEFT_CLICK: handled by onWandSwing above; cancel here just in case
        // the event still fires (Spigot builds that do deliver it).
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            event.setCancelled(true);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (!player.isSneaking()) return;
            if (event.getClickedBlock() == null) return;
            event.setCancelled(true);
            handlePosB(player, event.getClickedBlock().getLocation());
        }
    }

    // ─── Corner A ────────────────────────────────────────────────────────────

    private void handlePosA(@NotNull Player player, @NotNull Location loc) {
        posA.put(player.getUniqueId(), loc);
        sendAction(player, Translator.get(TranslationKey.CLAIMS__WAND_POS_A)
            .replace("{x}", String.valueOf(loc.getBlockX()))
            .replace("{y}", String.valueOf(loc.getBlockY()))
            .replace("{z}", String.valueOf(loc.getBlockZ())));

        Location b = posB.get(player.getUniqueId());
        if (b != null) startOrRefreshBorder(player, loc, b);
    }

    // ─── Corner B ────────────────────────────────────────────────────────────

    private void handlePosB(@NotNull Player player, @NotNull Location loc) {
        Location a = posA.get(player.getUniqueId());

        if (a == null) {
            sendAction(player, "§cSet corner A first (left-click a block).");
            return;
        }
        if (a.getWorld() == null || !a.getWorld().equals(loc.getWorld())) {
            sendAction(player, "§cBoth corners must be in the same world.");
            return;
        }

        posB.put(player.getUniqueId(), loc);

        sendAction(player, Translator.get(TranslationKey.CLAIMS__WAND_POS_B)
            .replace("{x}", String.valueOf(loc.getBlockX()))
            .replace("{y}", String.valueOf(loc.getBlockY()))
            .replace("{z}", String.valueOf(loc.getBlockZ())));

        ClaimWand.removeFromInventory(player);
        startOrRefreshBorder(player, a, loc);
        startNamePrompt(player);
    }

    // ─── Name prompt ─────────────────────────────────────────────────────────

    private void startNamePrompt(@NotNull Player player) {
        pendingName.add(player.getUniqueId());

        send(player, Translator.get(TranslationKey.CLAIMS__NAME_PROMPT)
            .replace("{cancel}", Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD)));

        cancelTimeout(player.getUniqueId());
        BukkitTask timeout = Bukkit.getScheduler().runTaskLater(
            BlockProt.getInstance(), () -> {
                if (pendingName.remove(player.getUniqueId())) {
                    clearSelection(player.getUniqueId());
                    send(player, Translator.get(TranslationKey.CLAIMS__TIMEOUT));
                }
            }, NAME_TIMEOUT);
        timeoutTasks.put(player.getUniqueId(), timeout);
    }

    // ─── Chat input — legacy Spigot (AsyncPlayerChatEvent) ───────────────────
    //
    // When Paper's AsyncChatEvent handler already processed this player's message,
    // chatHandledThisTick contains their UUID and we skip processing here.

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (chatHandledThisTick.remove(player.getUniqueId())) return; // handled by Paper listener
        handleChatInput(player, event.getMessage(), () -> event.setCancelled(true));
    }

    /**
     * Core chat-input logic shared between the legacy and Paper chat handlers.
     *
     * @param player      Sender.
     * @param rawMessage  Plain text of the message.
     * @param cancelEvent Runnable that cancels the originating event.
     */
    void handleChatInput(@NotNull Player player, @NotNull String rawMessage,
                         @NotNull Runnable cancelEvent) {
        if (handleWelcomeFlow(player, rawMessage, cancelEvent)) return;
        if (handleClaimNameFlow(player, rawMessage, cancelEvent)) return;
    }

    private boolean handleWelcomeFlow(@NotNull Player player, @NotNull String rawMessage, @NotNull Runnable cancelEvent) {
        if (!pendingWelcome.containsKey(player.getUniqueId())) return false;
        cancelEvent.run();
        String input  = rawMessage.trim();
        UUID claimId  = pendingWelcome.remove(player.getUniqueId());
        BukkitTask t  = welcomeTimeouts.remove(player.getUniqueId());
        if (t != null) t.cancel();

        String cancelWord = Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD);
        if (input.equalsIgnoreCase(cancelWord)) {
            Bukkit.getScheduler().runTask(BlockProt.getInstance(),
                () -> send(player, Translator.get(TranslationKey.CLAIMS__NAME_CANCELLED)));
            return true;
        }

        Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
            Claim claim = ClaimManager.getInstance().getClaimById(claimId);
            if (claim == null) return;
            String msg = input.equalsIgnoreCase("clear") ? null : input;
            claim.setWelcomeMessage(msg);
            ClaimManager.getInstance().saveAsync();
            String response = (msg == null
                ? Translator.get(TranslationKey.CLAIMS__WELCOME_CLEARED)
                : Translator.get(TranslationKey.CLAIMS__WELCOME_SET))
                .replace("{name}", claim.getName());
            send(player, response);
        });
        return true;
    }

    private boolean handleClaimNameFlow(@NotNull Player player, @NotNull String rawMessage, @NotNull Runnable cancelEvent) {
        if (!pendingName.contains(player.getUniqueId())) return false;

        cancelEvent.run();

        String input      = rawMessage.trim();
        String cancelWord = Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD);

        if (input.equalsIgnoreCase(cancelWord)) {
            pendingName.remove(player.getUniqueId());
            cancelTimeout(player.getUniqueId());
            clearSelection(player.getUniqueId());
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () ->
                send(player, Translator.get(TranslationKey.CLAIMS__NAME_CANCELLED)));
            return true;
        }

        if (input.isEmpty() || input.length() > MAX_NAME_LEN) {
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                send(player, Translator.get(TranslationKey.CLAIMS__NAME_TOO_LONG)
                    .replace("{max}", String.valueOf(MAX_NAME_LEN)));
                send(player, Translator.get(TranslationKey.CLAIMS__NAME_PROMPT)
                    .replace("{cancel}", Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD)));
            });
            return true;
        }

        if (!input.matches("[\\w\\- ]+")) {
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                send(player, Translator.get(TranslationKey.CLAIMS__NAME_INVALID));
                send(player, Translator.get(TranslationKey.CLAIMS__NAME_PROMPT)
                    .replace("{cancel}", Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD)));
            });
            return true;
        }

        final String claimName = input;
        Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
            pendingName.remove(player.getUniqueId());
            cancelTimeout(player.getUniqueId());

            Location a = posA.get(player.getUniqueId());
            Location b = posB.get(player.getUniqueId());

            if (a == null || b == null) {
                send(player, "§cSelection expired. Please use the wand again.");
                clearSelection(player.getUniqueId());
                return;
            }

            if (ClaimManager.getInstance().nameExists(
                    player.getUniqueId().toString(), claimName)) {
                send(player, "§cYou already have a claim named §e'" + claimName + "'§c. Choose another name.");
                startNamePrompt(player);
                return;
            }

            Claim created = ClaimManager.getInstance().createClaim(
                player.getUniqueId().toString(), claimName, a, b);

            clearSelection(player.getUniqueId());

            if (created == null) {
                send(player, Translator.get(TranslationKey.CLAIMS__OVERLAP));
            } else {
                send(player, Translator.get(TranslationKey.CLAIMS__CREATED)
                    .replace("{name}", created.getName())
                    .replace("{world}", created.getWorldName())
                    .replace("{x1}", String.valueOf(created.getMinX()))
                    .replace("{y1}", String.valueOf(created.getMinY()))
                    .replace("{z1}", String.valueOf(created.getMinZ()))
                    .replace("{x2}", String.valueOf(created.getMaxX()))
                    .replace("{y2}", String.valueOf(created.getMaxY()))
                    .replace("{z2}", String.valueOf(created.getMaxZ())));
            }
        });
        return true;
    }

    // ─── Paper AsyncChatEvent inner listener ──────────────────────────────────
    //
    // This class references io.papermc.paper.event.player.AsyncChatEvent.
    // It is only instantiated when PAPER_CHAT_AVAILABLE is true (at runtime the
    // class is present). On Spigot builds the class is never loaded.

    /**
     * Creates and returns the Paper-specific chat listener if Paper is available,
     * or {@code null} on Spigot. Must be registered separately via
     * {@code PluginManager.registerEvents()}.
     */
    @Nullable
    public Listener createPaperChatListener() {
        if (!PAPER_CHAT_AVAILABLE) return null;
        return new PaperAsyncChatListener(this);
    }

    static final class PaperAsyncChatListener implements Listener {
        private final ClaimEventListener parent;

        PaperAsyncChatListener(@NotNull ClaimEventListener parent) {
            this.parent = parent;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPaperAsyncChat(
                @NotNull io.papermc.paper.event.player.AsyncChatEvent event) {
            Player player = event.getPlayer();
            if (!parent.needsChat(player.getUniqueId())) return;

            // Mark handled so the legacy AsyncPlayerChatEvent handler skips it
            parent.chatHandledThisTick.add(player.getUniqueId());

            String plain = net.kyori.adventure.text.serializer.plain
                .PlainTextComponentSerializer.plainText()
                .serialize(event.message());

            parent.handleChatInput(player, plain, () -> event.setCancelled(true));
        }
    }

    /** True if this player has a pending chat input (claim name or welcome message). */
    boolean needsChat(@NotNull UUID uuid) {
        return pendingName.contains(uuid) || pendingWelcome.containsKey(uuid);
    }

    // ─── Player disconnect cleanup ────────────────────────────────────────────

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        clearSelection(id);
        pendingName.remove(id);
        pendingWelcome.remove(id);
        chatHandledThisTick.remove(id);
        BukkitTask wt = welcomeTimeouts.remove(id);
        if (wt != null) wt.cancel();
        playerInClaim.remove(id);
    }

    // ─── Selection helpers ────────────────────────────────────────────────────

    public @Nullable Location getPosA(@NotNull UUID id) { return posA.get(id); }
    public @Nullable Location getPosB(@NotNull UUID id) { return posB.get(id); }

    public void clearSelection(@NotNull UUID id) {
        posA.remove(id);
        posB.remove(id);
        cancelTimeout(id);
        cancelBorder(id);
    }

    // ─── Particle border ─────────────────────────────────────────────────────

    private void startOrRefreshBorder(@NotNull Player player,
                                      @NotNull Location a, @NotNull Location b) {
        cancelBorder(player.getUniqueId());
        World world = a.getWorld();
        if (world == null) return;

        double minX = Math.min(a.getBlockX(), b.getBlockX());
        double minY = Math.min(a.getBlockY(), b.getBlockY());
        double minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        double maxX = Math.max(a.getBlockX(), b.getBlockX()) + 1;
        double maxY = Math.max(a.getBlockY(), b.getBlockY()) + 1;
        double maxZ = Math.max(a.getBlockZ(), b.getBlockZ()) + 1;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
            BlockProt.getInstance(), () -> {
                if (!player.isOnline()) {
                    cancelBorder(player.getUniqueId());
                    return;
                }
                drawCuboidEdges(world, player, minX, minY, minZ, maxX, maxY, maxZ);
            }, 0L, BORDER_PERIOD);

        borderTasks.put(player.getUniqueId(), task);
    }

    private void drawCuboidEdges(@NotNull World world, @NotNull Player player,
                                  double x0, double y0, double z0,
                                  double x1, double y1, double z1) {
        drawEdge(world, player, x0, y0, z0, x1, y0, z0);
        drawEdge(world, player, x1, y0, z0, x1, y0, z1);
        drawEdge(world, player, x1, y0, z1, x0, y0, z1);
        drawEdge(world, player, x0, y0, z1, x0, y0, z0);

        drawEdge(world, player, x0, y1, z0, x1, y1, z0);
        drawEdge(world, player, x1, y1, z0, x1, y1, z1);
        drawEdge(world, player, x1, y1, z1, x0, y1, z1);
        drawEdge(world, player, x0, y1, z1, x0, y1, z0);

        drawEdge(world, player, x0, y0, z0, x0, y1, z0);
        drawEdge(world, player, x1, y0, z0, x1, y1, z0);
        drawEdge(world, player, x1, y0, z1, x1, y1, z1);
        drawEdge(world, player, x0, y0, z1, x0, y1, z1);
    }

    private void drawEdge(@NotNull World world, @NotNull Player player,
                           double x1, double y1, double z1,
                           double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        int steps = (int) Math.ceil(len / PARTICLE_STEP);
        double sx = dx / steps, sy = dy / steps, sz = dz / steps;
        for (int i = 0; i <= steps; i++) {
            player.spawnParticle(Particle.FLAME,
                x1 + sx * i, y1 + sy * i, z1 + sz * i,
                1, 0, 0, 0, 0, null);
        }
    }

    private void cancelBorder(@NotNull UUID id) {
        BukkitTask t = borderTasks.remove(id);
        if (t != null) t.cancel();
    }

    private void cancelTimeout(@NotNull UUID id) {
        BukkitTask t = timeoutTasks.remove(id);
        if (t != null) t.cancel();
    }

    // ─── Protección de bloques dentro de parcela ──────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaceInClaim(@NotNull BlockPlaceEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        Player player = event.getPlayer();
        Claim claim   = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;

        boolean isOwner = claim.getOwnerUuid().equals(player.getUniqueId().toString());
        boolean isAdmin = player.hasPermission(Permissions.BYPASS.key());

        if (!isOwner && !isAdmin) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
            return;
        }

        if (isOwner
                && BlockProt.getDefaultConfig().isLockable(
                        event.getBlock().getType(), event.getBlock().getWorld())) {
            BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
            if (!handler.isNotProtected()) return;

            BlockLockOnPlaceEvent lockEvent = new BlockLockOnPlaceEvent(event.getBlock(), player);
            Bukkit.getPluginManager().callEvent(lockEvent);
            if (lockEvent.isCancelled()) return;

            var result = handler.lockBlock(player, BlockProtLockEvent.Cause.CLAIM_AUTO_LOCK);
            if (!result.success) return;

            if (BlockProt.getDefaultConfig().shouldInheritGlobalTrustInClaims()) {
                new PlayerSettingsHandler(player).getFriendsStream()
                    .filter(fh -> PluginIntegration.filterFriendByUuidForAll(
                        UUID.fromString(fh.getName()), player, event.getBlock()))
                    .forEach(handler::addFriend);
            }

            if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace())
                handler.getRedstoneHandler().setAll(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreakInClaim(@NotNull BlockBreakEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;
        Player player = event.getPlayer();
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.BYPASS.key())) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    // ─── Flag enforcement ────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvP(@NotNull EntityDamageByEntityEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player)) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(victim.getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.PVP)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRedstoneInClaim(@NotNull BlockRedstoneEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.REDSTONE)) event.setNewCurrent(0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(@NotNull BlockIgniteEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.FIRE)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsAdvancedEnabled()) return;
        event.blockList().removeIf(block -> {
            Claim claim = ClaimManager.getInstance().getClaimAt(block.getLocation());
            return claim != null && !claim.getFlag(ClaimFlag.TNT);
        });
    }

    // ─── Welcome-on-enter ────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerMoveWelcome(@NotNull PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player     = event.getPlayer();
        Claim  newClaim   = ClaimManager.getInstance().getClaimAt(event.getTo());
        UUID   newId      = newClaim != null ? newClaim.getId() : null;
        UUID   currentId  = playerInClaim.get(player.getUniqueId());

        if (newId != null && !newId.equals(currentId)) {
            playerInClaim.put(player.getUniqueId(), newId);
            String welcome = newClaim.getWelcomeMessage();
            if (welcome != null && !welcome.isEmpty())
                send(player, welcome.replace("&", "§"));
        } else if (newId == null && currentId != null) {
            playerInClaim.remove(player.getUniqueId());
        }
    }

    // ─── Extended flag enforcement ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFlowInClaim(@NotNull BlockFromToEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagFlowEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getToBlock().getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.FLOW)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeafDecayInClaim(@NotNull LeavesDecayEvent event) {
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.LEAF_DECAY)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawnInClaim(@NotNull CreatureSpawnEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagMobSpawningEnabled()) return;
        var reason = event.getSpawnReason();
        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
            && reason != CreatureSpawnEvent.SpawnReason.CHUNK_GEN) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getLocation());
        if (claim != null && !claim.getFlag(ClaimFlag.MOB_SPAWNING)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageInClaim(@NotNull EntityDamageEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagInvincibleEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getEntity().getLocation());
        if (claim != null && claim.getFlag(ClaimFlag.INVINCIBLE)) event.setCancelled(true);
    }

    // ─── Interaction flag checks ───────────────────────────────────────────────
    //
    // FIX: guard against wand in hand — wand right-clicks must not trigger these
    // checks; they are handled exclusively by onWandInteract.

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractFlagCheck(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        // Skip: wand interactions belong to onWandInteract
        if (ClaimWand.isWand(player.getInventory().getItemInMainHand())) return;

        Claim claim = ClaimManager.getInstance().getClaimAt(event.getClickedBlock().getLocation());
        if (claim == null) return;

        boolean isOwner = claim.getOwnerUuid().equals(player.getUniqueId().toString());
        boolean isAdmin = player.hasPermission(Permissions.BYPASS.key());
        if (isOwner || isAdmin) return;

        Material type = event.getClickedBlock().getType();
        String   name = type.name();

        if (BlockProt.getDefaultConfig().isClaimsFlagChestEnabled()
                && !claim.getFlag(ClaimFlag.CHEST) && isContainerMaterial(name)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
            return;
        }
        if (BlockProt.getDefaultConfig().isClaimsFlagDoorEnabled()
                && !claim.getFlag(ClaimFlag.DOOR) && isDoorMaterial(name)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
            return;
        }
        if (BlockProt.getDefaultConfig().isClaimsFlagLeverButtonEnabled()
                && !claim.getFlag(ClaimFlag.LEVER_BUTTON) && isLeverOrButton(type, name)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(@NotNull PlayerBucketFillEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagUseBucketEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;
        Player player = event.getPlayer();
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())
            && !player.hasPermission(Permissions.BYPASS.key())
            && !claim.getFlag(ClaimFlag.USE_BUCKET)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(@NotNull PlayerBucketEmptyEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagUseBucketEnabled()) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getBlock().getLocation());
        if (claim == null) return;
        Player player = event.getPlayer();
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())
            && !player.hasPermission(Permissions.BYPASS.key())
            && !claim.getFlag(ClaimFlag.USE_BUCKET)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    // ─── Pet flags ────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTame(@NotNull EntityTameEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagPetsEnabled()) return;
        if (!(event.getOwner() instanceof Player player)) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getEntity().getLocation());
        if (claim == null) return;
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.BYPASS.key())
                && !claim.getFlag(ClaimFlag.PET_SPAWNING)) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(@NotNull PlayerInteractEntityEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagPetsEnabled()) return;
        Player player = event.getPlayer();
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getRightClicked().getLocation());
        if (claim == null) return;
        if (!claim.getOwnerUuid().equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.BYPASS.key())
                && !claim.getFlag(ClaimFlag.PET_RIDING)
                && event.getRightClicked() instanceof org.bukkit.entity.Animals) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    // FIX: replaced world.getEntities() full-world scan with getNearbyEntities(16,16,16)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeadAnimalIntoClaim(@NotNull PlayerMoveEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagPetsEnabled()) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player  = event.getPlayer();
        Claim  toClaim = ClaimManager.getInstance().getClaimAt(event.getTo());
        if (toClaim == null) return;
        if (toClaim.getOwnerUuid().equals(player.getUniqueId().toString())) return;
        if (player.hasPermission(Permissions.BYPASS.key())) return;
        if (toClaim.getFlag(ClaimFlag.PET_MOVING)) return;

        boolean hasLeashed = player.getNearbyEntities(16, 16, 16).stream()
            .filter(e -> e instanceof org.bukkit.entity.Animals)
            .map(e -> (org.bukkit.entity.Animals) e)
            .anyMatch(a -> player.equals(a.getLeashHolder()));

        if (hasLeashed) {
            event.setCancelled(true);
            sendAction(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnderpearlTeleport(@NotNull PlayerTeleportEvent event) {
        if (!BlockProt.getDefaultConfig().isClaimsFlagDenyEnderpearlEnabled()) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        Claim claim = ClaimManager.getInstance().getClaimAt(event.getTo());
        if (claim != null && !claim.getFlag(ClaimFlag.DENY_ENDERPEARL)) {
            event.setCancelled(true);
            sendAction(event.getPlayer(), Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
        }
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private void sendAction(@NotNull Player player, @NotNull String text) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void send(@NotNull Player player, @NotNull String text) {
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    // ─── Static welcome-prompt entry-point ────────────────────────────────────

    public static void startWelcomePrompt(@NotNull Player player, @NotNull Claim claim) {
        if (INSTANCE == null) return;
        INSTANCE.doStartWelcomePrompt(player, claim);
    }

    private void doStartWelcomePrompt(@NotNull Player player, @NotNull Claim claim) {
        pendingWelcome.put(player.getUniqueId(), claim.getId());
        BukkitTask old = welcomeTimeouts.remove(player.getUniqueId());
        if (old != null) old.cancel();

        send(player, Translator.get(TranslationKey.CLAIMS__WELCOME_PROMPT)
            .replace("{cancel}", Translator.get(TranslationKey.CLAIMS__NAME_CANCEL_WORD)));

        BukkitTask timeout = Bukkit.getScheduler().runTaskLater(
            BlockProt.getInstance(), () -> {
                if (pendingWelcome.remove(player.getUniqueId()) != null) {
                    welcomeTimeouts.remove(player.getUniqueId());
                    send(player, Translator.get(TranslationKey.CLAIMS__TIMEOUT));
                }
            }, NAME_TIMEOUT);
        welcomeTimeouts.put(player.getUniqueId(), timeout);
    }

    // ─── Material helpers ─────────────────────────────────────────────────────

    private static boolean isContainerMaterial(@NotNull String name) {
        return name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER")
            || name.contains("FURNACE") || name.contains("SMOKER")
            || name.contains("HOPPER")  || name.contains("DROPPER") || name.contains("DISPENSER")
            || name.equals("BREWING_STAND") || name.equals("CRAFTING_TABLE") || name.equals("ANVIL")
            || name.equals("CHIPPED_ANVIL") || name.equals("DAMAGED_ANVIL");
    }

    private static boolean isDoorMaterial(@NotNull String name) {
        return name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_GATE");
    }

    private static boolean isLeverOrButton(@NotNull Material m, @NotNull String name) {
        return m == Material.LEVER || name.endsWith("_BUTTON");
    }
}
