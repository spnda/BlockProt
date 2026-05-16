/*
 * Copyright (C) 2021 - 2025 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit.inventories;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Chat-based text input. Closes the player's inventory, shows a prompt in chat,
 * provides tab-completion for online player names, and calls onConfirm on the
 * main thread. Typing the cancel word (from lang) aborts silently.
 *
 * <p>Requires Paper (not vanilla Spigot). On non-Paper servers, the plugin logs
 * a warning at startup via {@link de.sean.blockprot.bukkit.VersionCompat#isPaper()}
 * and this class simply won't receive events — the anvil-based fallback
 * ({@link AnvilInput}) is still available for callers that want it.
 */
public final class ChatInput implements Listener {

    private final UUID playerUuid;
    private final Plugin plugin;
    private final @Nullable Consumer<String> onConfirm;
    private final String cancelWord;
    private boolean consumed = false;

    private ChatInput(
            @NotNull Player player,
            @NotNull Plugin plugin,
            @Nullable Consumer<String> onConfirm
    ) {
        this.playerUuid = player.getUniqueId();
        this.plugin = plugin;
        this.onConfirm = onConfirm;
        this.cancelWord = Translator.get(TranslationKey.MESSAGES__CHAT_INPUT_CANCEL_WORD).trim().toLowerCase();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.closeInventory();

        // Build the prompt from lang, replacing {cancel} with the actual cancel word.
        String prompt = Translator.get(TranslationKey.MESSAGES__CHAT_INPUT_PROMPT)
            .replace("{cancel}", cancelWord);

        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(prompt));
    }

    /**
     * Opens a chat input session for {@code player}.
     *
     * @param player    The player who will type.
     * @param plugin    The owning plugin.
     * @param onConfirm Called on the main thread with the typed text, or not called if cancelled.
     */
    public static void open(
            @NotNull Player player,
            @NotNull Plugin plugin,
            @Nullable Consumer<String> onConfirm
    ) {
        new ChatInput(player, plugin, onConfirm);
    }

    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(playerUuid)) return;
        event.setCancelled(true); // Never shown in global chat.

        final String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        // If the player typed the localized cancel word, stop the session.
        if (text.equalsIgnoreCase(cancelWord)) {
            event.getPlayer().sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                Translator.get(TranslationKey.MESSAGES__CHAT_INPUT_CANCELLED)
            ));
            unregister();
            return;
        }

        unregister();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (onConfirm != null) {
                onConfirm.accept(text);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTabComplete(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;
        if (!player.getUniqueId().equals(playerUuid)) return;
        if (event.isCommand()) return;

        final String buffer = event.getBuffer();
        final List<String> names = Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(buffer.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());

        if (!names.isEmpty()) {
            event.setCompletions(names);
            event.setHandled(true);
        }
    }

    // -------------------------------------------------------------------------

    private void unregister() {
        if (!consumed) {
            consumed = true;
            HandlerList.unregisterAll(this);
        }
    }
}
