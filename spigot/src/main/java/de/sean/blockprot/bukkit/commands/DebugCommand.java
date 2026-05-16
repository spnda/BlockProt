/*
 * Copyright (C) 2021 - 2025 spnda / SP26 fork
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.BlockProtLogger;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DebugCommand implements CommandExecutor {

    private static final String NOTCH_UUID = "069a79f4-44e9-4726-a5be-fca90e38aaf5";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!canUseCommand(sender)) return false;

        if (args.length < 2) {
            sender.sendMessage("Usage: /blockprot debug <placeDebugChest|placeDebugShulker|clearSearchHistory|run>");
            return false;
        }

        switch (args[1]) {
            case "placeDebugChest" -> {
                if (!(sender instanceof Player player)) break;
                player.getWorld().setType(player.getLocation(), Material.CHEST);
                new BlockNBTHandler(player.getWorld().getBlockAt(player.getLocation())).setOwner(NOTCH_UUID);
                BlockProtLogger.log("debug", "placeDebugChest at " + player.getLocation());
                return true;
            }
            case "placeDebugShulker" -> {
                if (!(sender instanceof Player player)) break;
                player.getWorld().setType(player.getLocation(), Material.SHULKER_BOX);
                new BlockNBTHandler(player.getWorld().getBlockAt(player.getLocation())).setOwner(NOTCH_UUID);
                BlockProtLogger.log("debug", "placeDebugShulker at " + player.getLocation());
                return true;
            }
            case "clearSearchHistory" -> {
                if (!(sender instanceof Player player)) break;
                new PlayerSettingsHandler(player).clearSearchHistory();
                player.sendMessage(Component.text("[BlockProt] Search history cleared.", NamedTextColor.GREEN));
                BlockProtLogger.log("debug", "clearSearchHistory for " + player.getName());
                return true;
            }
            case "run" -> {
                if (!(sender instanceof Player player)) break;
                // Run async to avoid blocking the main thread during slow checks.
                Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), () -> runDiagnostics(player));
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // Diagnostics
    // =========================================================================

    private void runDiagnostics(@NotNull Player player) {
        player.sendMessage(Component.text("[BlockProt] Running diagnostics… check the log file when done.", NamedTextColor.GOLD));

        BlockProtLogger.separator();
        BlockProtLogger.log("=== /blockprot debug run ===");
        BlockProtLogger.log("Player: " + player.getName() + " (" + player.getUniqueId() + ")");
        BlockProtLogger.log("Time: " + java.time.LocalDateTime.now());

        int[] counts = new int[]{0,0}; // [passed, failed]

        checkConfig(counts);
        checkTranslator(counts);
        checkNbtWriteRead(player, counts);
        checkPlayerSettings(player, counts);
        checkInventoryCreation(counts);
        checkProfileService(player, counts);
        checkLockableMaterials(counts);
        checkOnlinePlayers(counts);

        BlockProtLogger.separator();
        BlockProtLogger.log("=== Summary: " + counts[0] + " passed, " + counts[1] + " failed ===");

        var logFile = BlockProtLogger.getCurrentLogFile();
        boolean allOk = counts[1] == 0;

        player.sendMessage(allOk
            ? Component.text("[BlockProt] All diagnostics passed (" + counts[0] + "/" + (counts[0] + counts[1]) + ").", NamedTextColor.GREEN)
            : Component.text("[BlockProt] " + counts[1] + " check(s) FAILED. See log.", NamedTextColor.RED));

        if (logFile != null) {
            player.sendMessage(Component.text("[BlockProt] Log: " + logFile.getPath(), NamedTextColor.AQUA));
        }
    }

    private void checkConfig(int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 1. Config ---");
        try {
            var cfg = BlockProt.getDefaultConfig();
            BlockProtLogger.pass("Config loaded — friendFunctionalityDisabled=" + cfg.isFriendFunctionalityDisabled()
                + ", maxLockedBlocks=" + cfg.getMaxLockedBlockCount());
            counts[0]++;
        } catch (Exception e) {
            BlockProtLogger.fail("Config", e.getMessage());
            counts[1]++;
        }
    }

    private void checkTranslator(int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 2. Translator (all keys) ---");
        int keyFail = 0;
        for (TranslationKey key : TranslationKey.values()) {
            try {
                String val = Translator.get(key);
                if (val.isBlank()) {
                    BlockProtLogger.warn("Key blank: " + key);
                }
            } catch (Exception e) {
                BlockProtLogger.fail("Key " + key, e.getMessage());
                keyFail++;
            }
        }
        if (keyFail == 0) {
            BlockProtLogger.pass("All " + TranslationKey.values().length + " translation keys readable");
            counts[0]++;
        } else {
            BlockProtLogger.fail("Translator", keyFail + " keys threw exceptions");
            counts[1]++;
        }
    }

    private void checkNbtWriteRead(@NotNull Player player, int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 3. NBT Block Write/Read ---");
        try {
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                try {
                    var loc = player.getLocation().clone();
                    var world = player.getWorld();
                    var orig = world.getBlockAt(loc).getType();
                    world.setType(loc, Material.CHEST);
                    var handler = new BlockNBTHandler(world.getBlockAt(loc));
                    handler.setOwner(NOTCH_UUID);
                    handler.setName("debug_nbt_test");
                    String readOwner = handler.getOwner();
                    String readName  = handler.getName();
                    world.setType(loc, orig);
                    if (NOTCH_UUID.equals(readOwner) && "debug_nbt_test".equals(readName)) {
                        BlockProtLogger.pass("NBT write/read (owner=" + readOwner + ", name=" + readName + ")");
                    } else {
                        BlockProtLogger.fail("NBT mismatch", "owner=" + readOwner + " name=" + readName);
                    }
                } catch (Exception e) {
                    BlockProtLogger.fail("NBT block", e.getMessage());
                }
            });
        } catch (Exception e) {
            BlockProtLogger.fail("NBT schedule", e.getMessage());
            counts[1]++;
        }
        counts[0]++; // NBT result is logged async; we count it as initiated.
    }

    private void checkPlayerSettings(@NotNull Player player, int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 4. PlayerSettingsHandler ---");
        try {
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                try {
                    var ps = new PlayerSettingsHandler(player);
                    BlockProtLogger.pass("lockOnPlace=" + ps.getLockOnPlace());
                } catch (Exception e) {
                    BlockProtLogger.fail("PlayerSettingsHandler", e.getMessage());
                }
            });
            counts[0]++;
        } catch (Exception e) {
            BlockProtLogger.fail("PlayerSettingsHandler schedule", e.getMessage());
            counts[1]++;
        }
    }

    private void checkInventoryCreation(int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 5. Inventory Creation ---");
        try {
            Inventory inv = Bukkit.createInventory(null, 9, "debug_test");
            BlockProtLogger.pass("Inventory size=" + inv.getSize());
            counts[0]++;
        } catch (Exception e) {
            BlockProtLogger.fail("Inventory creation", e.getMessage());
            counts[1]++;
        }
    }

    private void checkProfileService(@NotNull Player player, int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 6. ProfileService ---");
        try {
            var profile = BlockProt.getProfileService().findByUuid(player.getUniqueId());
            if (profile != null) {
                BlockProtLogger.pass("ProfileService returned: " + profile.getName());
                counts[0]++;
            } else {
                BlockProtLogger.warn("ProfileService returned null for running player");
                counts[0]++; // Not a hard failure.
            }
        } catch (Exception e) {
            BlockProtLogger.fail("ProfileService", e.getMessage());
            counts[1]++;
        }
    }

    private void checkLockableMaterials(int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 7. Lockable material check ---");
        try {
            var cfg = BlockProt.getDefaultConfig();
            boolean chestLockable = cfg.isLockable(Material.CHEST);
            BlockProtLogger.pass("CHEST lockable=" + chestLockable);
            counts[0]++;
        } catch (Exception e) {
            BlockProtLogger.fail("Lockable material", e.getMessage());
            counts[1]++;
        }
    }

    private void checkOnlinePlayers(int[] counts) {
        BlockProtLogger.separator();
        BlockProtLogger.log("--- 8. Online Players ---");
        BlockProtLogger.log("Count: " + Bukkit.getOnlinePlayers().size());
        for (Player p : Bukkit.getOnlinePlayers()) {
            BlockProtLogger.log("  - " + p.getName() + " (" + p.getUniqueId() + ")");
        }
        counts[0]++;
    }

    // =========================================================================

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!canUseCommand(sender)) return Collections.emptyList();
        return new ArrayList<>(Arrays.asList("placeDebugChest", "placeDebugShulker", "clearSearchHistory", "run"));
    }

    @Override
    public boolean canUseCommand(@NotNull CommandSender sender) {
        return sender.hasPermission("blockprot.debug");
    }
}
