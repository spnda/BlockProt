package de.sean.blockprot.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before BlockProt clears ownership NBT from a protected block.
 *
 * @since SP26
 */
public final class BlockProtUnlockEvent extends BlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Cause {
        MANUAL,
        BREAK,
        INACTIVITY_CLEANUP,
        API
    }

    @NotNull
    private final Player player;
    @NotNull
    private final Cause cause;
    private boolean cancelled;

    public BlockProtUnlockEvent(@NotNull Block block, @NotNull Player player, @NotNull Cause cause) {
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
