/*
 * Copyright (C) 2021 - 2025 spnda / SP26 fork
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package de.sean.blockprot.bukkit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persistent session logger for BlockProt.
 * Creates one log file per plugin session (onEnable call) under
 * plugins/BlockProt/logs/. Each line is timestamped.
 *
 * Usage:
 *   BlockProtLogger.init(dataFolder);  // call from onEnable
 *   BlockProtLogger.log("message");
 *   BlockProtLogger.close();           // call from onDisable
 */
public final class BlockProtLogger {

    private static final DateTimeFormatter FILE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter LINE_FMT  = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Nullable private static PrintWriter writer = null;
    @Nullable private static File currentLogFile = null;

    // Secondary language for the console. Activated with secondary_language_file in config.yml.
    // When configured, each console log line shows the message in both languages.
    @Nullable private static java.util.function.Function<String, String> secondaryTranslator = null;

    public static void setSecondaryTranslator(@Nullable java.util.function.Function<String, String> fn) {
        secondaryTranslator = fn;
    }

    private BlockProtLogger() {}

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initialises the logger for this session. Creates the logs/ directory and
     * opens a new log file named after the current timestamp.
     */
    public static void init(@NotNull File dataFolder) {
        File logsDir = new File(dataFolder, "logs");
        if (!logsDir.exists()) logsDir.mkdirs();

        String name = "blockprot-" + LocalDateTime.now().format(FILE_FMT) + ".log";
        currentLogFile = new File(logsDir, name);

        try {
            writer = new PrintWriter(new FileWriter(currentLogFile, true));
            log("=== BlockProt Session Start ===");
        } catch (IOException e) {
            // Not critical — plugin still works, just without file logging.
            writer = null;
            currentLogFile = null;
        }
    }

    /** Flushes and closes the log file. Call from onDisable. */
    public static void close() {
        if (writer != null) {
            log("=== BlockProt Session End ===");
            writer.flush();
            writer.close();
            writer = null;
        }
    }

    // -------------------------------------------------------------------------
    // Write methods
    // -------------------------------------------------------------------------

    /** Writes a timestamped line. No-op if the logger was not initialised. */
    public static void log(@NotNull String message) {
        if (writer == null) return;
        String line = "[" + LocalDateTime.now().format(LINE_FMT) + "] " + message;
        if (secondaryTranslator != null) {
            String alt = secondaryTranslator.apply(message);
            if (alt != null && !alt.equals(message)) {
                line += "  |  " + alt;
            }
        }
        writer.println(line);
        writer.flush();
    }

    public static void log(@NotNull String section, @NotNull String message) {
        log("[" + section + "] " + message);
    }

    public static void pass(@NotNull String check) {
        log("PASS: " + check);
    }

    public static void fail(@NotNull String check, @Nullable String reason) {
        log("FAIL: " + check + (reason != null ? " — " + reason : ""));
    }

    public static void warn(@NotNull String message) {
        log("WARN: " + message);
    }

    public static void separator() {
        log("---");
    }

    /** Returns the path of the current log file, or null if not initialised. */
    @Nullable
    public static File getCurrentLogFile() {
        return currentLogFile;
    }
}
