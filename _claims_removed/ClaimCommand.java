package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.claims.Claim;
import de.sean.blockprot.bukkit.claims.ClaimManager;
import de.sean.blockprot.bukkit.claims.ClaimWand;
import de.sean.blockprot.bukkit.inventories.ClaimFlagsInventory;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.listeners.ClaimEventListener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles the {@code /blockprot claim} subcommand tree.
 *
 * <pre>
 * /bp claim wand           – give the selection wand (creation is automatic after naming)
 * /bp claim delete [name]  – delete the claim you are standing in (or by name)
 * /bp claim flags [name]   – open the flags GUI (for the claim you stand in, or by name)
 * /bp claim list           – list all your active claims
 * /bp claim info [name]    – show info about a claim
 * </pre>
 *
 * @since SP26
 */
public class ClaimCommand implements CommandExecutor {

    @Nullable
    private static ClaimEventListener listener;

    public static void setListener(@NotNull ClaimEventListener l) { listener = l; }

    private static final List<String> SUB_COMMANDS =
        List.of("wand", "delete", "flags", "list", "info", "welcome");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 2) { sendUsage(player); return true; }

        return switch (args[1].toLowerCase()) {

            // ── /bp claim wand ───────────────────────────────────────────────
            case "wand" -> {
                ClaimWand.giveWand(player);
                yield true;
            }

            // ── /bp claim delete [name] ──────────────────────────────────────
            case "delete" -> {
                Claim target = args.length >= 3
                    ? ClaimManager.getInstance().getClaimByName(
                        player.getUniqueId().toString(), args[2])
                    : ClaimManager.getInstance().getClaimAt(player.getLocation());

                if (target == null) {
                    send(player, args.length >= 3
                        ? "§cNo claim named §e'" + args[2] + "' §cfound."
                        : Translator.get(TranslationKey.CLAIMS__NO_CLAIM_HERE));
                } else if (!target.getOwnerUuid().equals(player.getUniqueId().toString())) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
                } else {
                    ClaimManager.getInstance().deleteClaim(
                        target.getId(), player.getUniqueId().toString());
                    send(player, Translator.get(TranslationKey.CLAIMS__DELETED)
                        .replace("{name}", target.getName()));
                }
                yield true;
            }

            // ── /bp claim flags [name] ───────────────────────────────────────
            case "flags" -> {
                Claim target = args.length >= 3
                    ? ClaimManager.getInstance().getClaimByName(
                        player.getUniqueId().toString(), args[2])
                    : ClaimManager.getInstance().getClaimAt(player.getLocation());

                if (target == null) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_CLAIM_HERE));
                    yield true;
                }
                if (!target.getOwnerUuid().equals(player.getUniqueId().toString())) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
                    yield true;
                }
                InventoryState state = new InventoryState(null);
                state.currentClaimId = target.getId();
                InventoryState.set(player.getUniqueId(), state);

                var inv = new ClaimFlagsInventory().fill(player);
                if (inv != null) player.openInventory(inv);
                yield true;
            }

            // ── /bp claim list ───────────────────────────────────────────────
            case "list" -> {
                List<Claim> mine =
                    ClaimManager.getInstance().getClaimsFor(player.getUniqueId().toString());

                if (mine.isEmpty()) {
                    send(player, "§7You have no active claims.");
                } else {
                    send(player, Translator.get(TranslationKey.CLAIMS__LIST_HEADER));
                    for (Claim c : mine) {
                        send(player, String.format(
                            "§7  §6%s §8│ §7%s §8(§f%d,%d,%d §8→ §f%d,%d,%d§8)",
                            c.getName(), c.getWorldName(),
                            c.getMinX(), c.getMinY(), c.getMinZ(),
                            c.getMaxX(), c.getMaxY(), c.getMaxZ()));
                    }
                }
                yield true;
            }

            // ── /bp claim info [name] ────────────────────────────────────────
            case "info" -> {
                Claim target = args.length >= 3
                    ? ClaimManager.getInstance().getClaimByName(
                        player.getUniqueId().toString(), args[2])
                    : ClaimManager.getInstance().getClaimAt(player.getLocation());

                if (target == null) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_CLAIM_HERE));
                    yield true;
                }
                send(player, "§6§lClaim: §f" + target.getName());
                send(player, "§7World: §f"  + target.getWorldName());
                send(player, String.format("§7From: §f%d, %d, %d",
                    target.getMinX(), target.getMinY(), target.getMinZ()));
                send(player, String.format("§7To:   §f%d, %d, %d",
                    target.getMaxX(), target.getMaxY(), target.getMaxZ()));
                send(player, "§7Volume: §f" + target.getVolume() + " §7blocks");
                send(player, "§7Flags:");
                for (var entry : target.getFlags().entrySet()) {
                    send(player, "§7  " + entry.getKey().name() + ": " +
                        (entry.getValue() ? "§aEnabled" : "§cDisabled"));
                }
                yield true;
            }

            // ── /bp claim welcome [message…] ──────────────────────────────────
            case "welcome" -> {
                Claim target = args.length >= 3
                    ? ClaimManager.getInstance().getClaimByName(
                        player.getUniqueId().toString(), args[2])
                    : ClaimManager.getInstance().getClaimAt(player.getLocation());

                if (target == null) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_CLAIM_HERE));
                    yield true;
                }
                if (!target.getOwnerUuid().equals(player.getUniqueId().toString())) {
                    send(player, Translator.get(TranslationKey.CLAIMS__NO_PERMISSION));
                    yield true;
                }

                if (args.length >= 3) {
                    // Inline: /bp claim welcome [name] <message...>
                    // If args[2] matches a claim name AND args.length > 3, use joined args[3..]
                    // Otherwise treat all args[2..] as the message for the current-location claim
                    String msg;
                    Claim byName = ClaimManager.getInstance()
                        .getClaimByName(player.getUniqueId().toString(), args[2]);
                    if (byName != null && args.length > 3) {
                        // args[2] is the claim name, args[3..] is the message
                        msg = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                        target = byName;
                    } else {
                        // args[2..] all form the message (for current-location claim)
                        msg = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                    }
                    if (msg.equalsIgnoreCase("clear")) msg = null;
                    target.setWelcomeMessage(msg);
                    ClaimManager.getInstance().saveAsync();
                    String key = msg == null
                        ? Translator.get(TranslationKey.CLAIMS__WELCOME_CLEARED)
                        : Translator.get(TranslationKey.CLAIMS__WELCOME_SET);
                    send(player, key.replace("{name}", target.getName()));
                } else {
                    // No message given — open chat prompt
                    if (listener != null) ClaimEventListener.startWelcomePrompt(player, target);
                }
                yield true;
            }

            default -> { sendUsage(player); yield true; }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 2) {
            return SUB_COMMANDS.stream()
                .filter(s -> s.startsWith(args[1].toLowerCase()))
                .toList();
        }

        // For delete/flags/info, suggest own claim names
        if (args.length == 3 && List.of("delete","flags","info").contains(args[1].toLowerCase())) {
            return ClaimManager.getInstance()
                .getClaimsFor(player.getUniqueId().toString())
                .stream()
                .map(Claim::getName)
                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                .toList();
        }

        return Collections.emptyList();
    }

    private void send(@NotNull Player player, @NotNull String text) {
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void sendUsage(@NotNull Player player) {
        send(player, "§6Usage: §f/bp claim §e<wand|delete|flags|list|info> [name]");
    }
}
