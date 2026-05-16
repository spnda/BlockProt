package de.sean.blockprot.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before BlockProt writes ownership NBT to a block.
 *
 * <p>External plugins may cancel this event to deny a lock, or inspect the
 * cause to synchronize their own region, economy, quest, or audit systems.</p>
 *
 * @since SP26
 */
public final class BlockProtLockEvent extends BlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Cause {
        MANUAL,
        LOCK_ON_PLACE,
        CLAIM_AUTO_LOCK,
        WORLDEDIT_PASTE,
        API
    }

    @NotNull
    private final Player player;
    @NotNull
    private final Cause cause;
    private boolean cancelled;

    public BlockProtLockEvent(@NotNull Block block, @NotNull Player player, @NotNull Cause cause) {
        super(block);
        this.player = player;
        this.cause = cause;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
