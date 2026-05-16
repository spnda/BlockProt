package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Displays a concise help reference for all BlockProt commands.
 * Registered under both {@code /bp help} and {@code /bp ayuda}.
 *
 * @since SP26
 */
public final class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        send(sender, Translator.get(TranslationKey.HELP__HEADER));

        // ── Block protection ──────────────────────────────────────────────────
        send(sender, "§6Block protection");
        send(sender, "§7  /bp §f— Opens your personal block protection settings.");

        // ── Friends ───────────────────────────────────────────────────────────
        send(sender, "§6Friends");
        send(sender, "§7  /bp friends add <player> §f— Add a default friend.");
        send(sender, "§7  /bp friends remove <player> §f— Remove a default friend.");

        // ── Claims ────────────────────────────────────────────────────────────
        send(sender, "§6Claims");
        send(sender, "§7  /bp claim wand §f— Get the selection wand.");
        send(sender, "§7  /bp claim list §f— List all your claims.");
        send(sender, "§7  /bp claim info [name] §f— Show claim details.");
        send(sender, "§7  /bp claim flags [name] §f— Open the flags GUI.");
        send(sender, "§7  /bp claim welcome [name] <msg> §f— Set a welcome message.");
        send(sender, "§7  /bp claim delete [name] §f— Delete a claim.");

        // ── Misc ──────────────────────────────────────────────────────────────
        send(sender, "§6Other");
        send(sender, "§7  /bp settings §f— Personal settings.");
        send(sender, "§7  /bp stats §f— View block statistics.");
        send(sender, "§7  /bp reload §f— Reload the plugin config. (admin)");

        send(sender, Translator.get(TranslationKey.HELP__FOOTER));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }

    private void send(@NotNull CommandSender sender, @NotNull String text) {
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }
}
