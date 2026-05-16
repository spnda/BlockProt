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
 * Displays the live BlockProt command list using the active language file.
 *
 * <p>Archived claims commands are kept under {@code _claims_removed} and are
 * intentionally excluded from this help screen until that module is restored
 * and registered again.</p>
 *
 * @since SP26
 */
public final class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        send(sender, Translator.get(TranslationKey.HELP__HEADER));

        send(sender, Translator.get(TranslationKey.HELP__BLOCK_PROTECTION_TITLE));
        send(sender, Translator.get(TranslationKey.HELP__BLOCK_PROTECTION_SETTINGS));

        send(sender, Translator.get(TranslationKey.HELP__FRIENDS_TITLE));
        send(sender, Translator.get(TranslationKey.HELP__FRIENDS_GUI));
        send(sender, Translator.get(TranslationKey.HELP__FRIENDS_ADDALL));

        send(sender, Translator.get(TranslationKey.HELP__OTHER_TITLE));
        send(sender, Translator.get(TranslationKey.HELP__SETTINGS));
        send(sender, Translator.get(TranslationKey.HELP__STATS));
        send(sender, Translator.get(TranslationKey.HELP__ABOUT));
        send(sender, Translator.get(TranslationKey.HELP__DISABLE_HINTS));

        if (sender.isOp()) {
            send(sender, Translator.get(TranslationKey.HELP__INTEGRATIONS));
            send(sender, Translator.get(TranslationKey.HELP__UPDATE));
            send(sender, Translator.get(TranslationKey.HELP__RELOAD));
        }
        if (sender.hasPermission("blockprot.debug")) {
            send(sender, Translator.get(TranslationKey.HELP__DEBUG));
        }

        send(sender, Translator.get(TranslationKey.HELP__FOOTER));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }

    private void send(@NotNull CommandSender sender, @NotNull String text) {
        if (text.isBlank()) return;
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }
}
