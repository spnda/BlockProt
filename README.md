# BlockProt SP26-U

[![CurseForge Downloads](http://cf.way2muchnoise.eu/full_440797_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/bukkit-plugins/blockprot)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/uF5ePKuc?style=flat-square&color=green&label=Downloads)](https://modrinth.com/plugin/blockprot)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/87829?style=flat-square&color=orange&label=Spigot%20Downloads)](https://www.spigotmc.org/resources/blockprot.87829/)
[![bStats Servers](https://img.shields.io/bstats/servers/9999?style=flat-square)](https://bstats.org/plugin/bukkit/BlockProt/9999)

BlockProt SP26-U is a maintained fork of the original BlockProt plugin. The original core remains the same: players can protect chests, furnaces, shulker boxes, hoppers, barrels, doors, trapdoors, fence gates, anvils, and other configured blocks through NBT-backed ownership and friends.

This fork adds modern server compatibility, safer upgrade behavior, audit tooling, optional SQL indexing, command improvements, WorldEdit paste support, and several hardening features while keeping the original chest/container protection enabled by default.

## Compared Repositories

This README documents the functional differences reviewed between:

- Old upstream/base copy: `D:\A-TODO\CODIGOS\BlockProt-master\BlockProt-master`
- Current fork: `D:\A-TODO\CODIGOS\BlockProt-master\BlockProt-SP26-U`

The comparison focused on `spigot/src/main`, `spigot/build.gradle.kts`, root Gradle files, resources, and documentation. Generated folders such as `build/`, `.gradle/`, and `.git/` are not treated as plugin features.

## Runtime And Build Requirements

- Target runtime: modern Spigot/Paper-family servers.
- Java target: Java 25, class file version 69.0.
- The plugin does not support CraftBukkit.
- The original NBT-backed block protection remains the source of truth.
- MySQL is optional and disabled by default.
- Optional SP26 behavior is opt-in unless explicitly documented otherwise.

Build from source:

```powershell
.\gradlew.bat :blockprot-spigot:build
```

Main shaded output:

```text
spigot/build/libs/blockprot-spigot-1.2.9-main-all.jar
```

## Original Core Features Preserved

The following original BlockProt behavior is still present:

- NBT-backed block ownership.
- Lock-on-place support.
- GUI-first block management.
- Friend management per protected block.
- Public access support.
- Redstone, hopper, and piston protection settings.
- Block statistics.
- Player settings menu.
- Update checks.
- Integrations for WorldGuard, Towny, PlaceholderAPI, and Lands.
- Translation files with English fallback.

The fork does not replace the original NBT protection model with SQL. NBT/Persistent Data Container remains authoritative for individual block ownership and friends.

## SP26-U Feature Summary

### Java 25 And Modern Server Compatibility

Added compatibility work for modern server stacks:

- Java 25 build target.
- Runtime diagnostics via `VersionCompat`.
- Startup validation via `VersionValidator`.
- Detection for classic `1.x` version strings and year-based `26.x` version strings.
- Paper typed inventory view checks.
- Modern Bukkit/Paper API fixes, including the `LeavesDecayEvent` package change.
- Explicit `Material` imports where modern APIs require them.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/VersionCompat.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/VersionValidator.java`
- `spigot/build.gradle.kts`
- `gradle.properties`

### Session Logs

SP26-U adds persistent session logging:

- Creates log files under `plugins/BlockProt/logs/`.
- Records plugin version, server version, runtime, and compatibility checks.
- Keeps verbose details out of console while preserving diagnostics in files.
- Used by backup, migration, world scanning, debug checks, and translation fallback reporting.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/BlockProtLogger.java`

### Safe Upgrade And Backup Protocol

The fork detects existing BlockProt data and creates a safety ZIP before applying migration-style config merges.

Backup behavior:

- Creates ZIP backups in `plugins/BlockProt/backups/`.
- Keeps a maximum of 10 backup ZIPs.
- Skips the `backups/` folder itself.
- Skips session log files.
- Only prints console backup notices when existing data is detected.
- Writes detailed information to the session log.

Detected prior-data signals include:

- `data.yml`
- `claims.yml`
- `blockprot.db`
- `blockprot_audit.sqlite`
- `stats.yml`
- Legacy `.yml`, `.db`, `.sqlite`, or `.dat` files in old data folders
- `DIM-1` and `DIM1` dimension folders

Important: SP26-U detects dimension folders for safety backup, but it does not physically move Nether or End region files. Dimension consolidation remains a manual administrator task.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/BackupTask.java`

### Non-Destructive Config Migration

The fork merges missing keys from the bundled `config.yml` into the server's existing `plugins/BlockProt/config.yml`.

Rules:

- Existing administrator values are never overwritten.
- Missing new keys are added during startup and reload.
- The admin is asked to review new options before enabling SP26 features.
- New high-impact options are disabled by default.
- Core chest/container protection is not disabled automatically.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/BlockProt.java`

### Configuration File Watcher

SP26-U watches configuration files and reloads the plugin configuration automatically.

Behavior:

- Watches `config.yml`, `worlds.yml`, and language `.yml` files.
- Debounces rapid editor writes to avoid reload spam.
- Reloads config and translations on the server thread.
- Console messages use the configured language file.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/ConfigFileWatcher.java`

### Per-World Lockable Lists

SP26-U adds optional `worlds.yml` support for per-world lockable block lists.

Default behavior:

- `worlds_config_enabled: false`
- The core plugin uses global `config.yml` lockable lists.
- Generated `worlds.yml` entries cannot disable protection by accident.

When enabled:

- A world with `enabled: false` disables protection in that world.
- A world with `enabled: true` uses the lists defined in `worlds.yml`.
- An unlisted world falls back to global `config.yml` lists.
- Newly detected worlds are added with `enabled: true`.

Key files:

- `spigot/src/main/resources/worlds.yml`
- `spigot/src/main/java/de/sean/blockprot/bukkit/config/WorldsConfig.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/config/DefaultConfig.java`

### Hybrid MySQL + NBT Backend

SP26-U adds an optional SQL index while keeping NBT as the source of truth.

Design:

- NBT/Persistent Data Container stores block owner and per-block friends.
- MySQL/MariaDB stores fast lookup indexes for auditing and global trust.
- HikariCP provides connection pooling.
- SQL operations run asynchronously through Bukkit scheduler tasks.
- A local RAM cache is used for global trust checks.

Database tables:

- `blockprot_global_trust`
- `blockprot_block_index`

Configuration:

```yaml
mysql:
  enabled: false
```

MySQL is intentionally disabled by default because it requires real administrator-provided credentials.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/storage/HybridDatabase.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/nbt/BlockNBTHandler.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/nbt/PlayerSettingsHandler.java`

### SQLite Audit Log

SP26-U adds a local audit database.

Features:

- SQLite database: `blockprot_audit.sqlite`
- Stores access attempts by player, action, world, coordinates, and timestamp.
- Records `ACCESS_DENIED` and `ACCESS_GRANTED`.
- Writes asynchronously.
- Provides lookup by block and by player.
- Prunes old rows when the configured limit is exceeded.
- Adds an audit GUI for protected block history.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/audit/AuditLogger.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/AuditInventory.java`

### Friends Add-All And Global Trust Sync

SP26-U adds bulk friend management.

Commands:

```text
/bp friends
/bp friends addall <player>
```

Behavior:

- `/bp friends` opens the default/global friends GUI.
- `/bp friends addall <player>` adds the target UUID to all known protected blocks owned by the command sender.
- Updates the sender's global/default trust list.
- Syncs global trust to MySQL when MySQL is enabled.
- Performs player lookup asynchronously.
- Supports Geyser/Floodgate-style Bedrock username prefixes through config.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/FriendsAddAllCommand.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/util/PlayerNameResolver.java`

### Localized Commands

The root Bukkit command remains static because `plugin.yml` command registration is static:

```text
/blockprot
/bp
```

SP26-U adds optional localized subcommand aliases inside the command dispatcher.

Default:

```yaml
localized_commands:
  enabled: false
  keep_english_aliases: true
```

When enabled, aliases such as `/bp ayuda` or `/bp estadisticas` can route to the same internal command as `/bp help` or `/bp stats`.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/BlockProtCommand.java`

### Help Command

SP26-U adds an internal help command:

```text
/bp help
```

It lists available subcommands based on sender permissions.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/HelpCommand.java`

### WorldEdit Paste Watcher

SP26-U adds an optional watcher for WorldEdit/FAWE paste commands.

Configuration:

```yaml
worldedit_paste_autolock:
  enabled: false
  radius: 24
  max_blocks_per_paste: 5000
  delay_ticks: 20
  require_claim_owner: false
```

Behavior:

- Watches paste commands.
- Runs a bounded delayed scan near the player.
- Locks lockable blocks with NBT.
- Can optionally require the block to be inside a claim owned by the player.

This feature is disabled by default because it changes bulk-build behavior.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/WorldEditPasteListener.java`

### Piston And Explosion Hardening

SP26-U hardens non-standard theft paths.

Options:

```yaml
protect_locked_blocks_from_explosions: true
block_protected_block_piston_movement: true
```

Behavior:

- Protected NBT blocks can be removed from explosion block lists.
- Protected NBT blocks can be made immovable by pistons.
- Hopper and hopper-minecart protection from the original plugin is preserved.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/ExplodeEventListener.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/PistonEventListener.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/HopperEventListener.java`

### Inactivity Cleanup

SP26-U adds optional cleanup for protections owned by inactive players.

Configuration:

```yaml
inactivity_cleanup_days: -1
```

Behavior:

- Disabled by default.
- If set to a positive number, runs asynchronously.
- Checks known protected block statistics.
- Clears protections for players inactive beyond the configured threshold.
- Reports the number of freed blocks through localized messages.

Key file:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/InactivityCleanupTask.java`

### Improved Input Helpers

SP26-U adds reusable input helpers:

- `ChatInput` for chat-based prompts.
- `AnvilInput` for GUI text entry.
- Inventory state additions for audit and friend-search flows.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/ChatInput.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/AnvilInput.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/InventoryState.java`

### API Expansion

SP26-U expands the addon API.

New events:

- `BlockProtLockEvent`
- `BlockProtUnlockEvent`

New API behavior:

- API lock and unlock calls fire cancellable events.
- Lock causes identify whether the lock came from manual action, lock-on-place, WorldEdit paste scan, or API usage.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/BlockProtAPI.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/events/BlockProtLockEvent.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/events/BlockProtUnlockEvent.java`

### Translation And Console Localization

SP26-U extends translation coverage.

Changes:

- New translation keys for console messages, audit, help, friends add-all, chat input, and SP26 features.
- Console messages for config reloads and world scanning use the configured `language_file`.
- English remains the default language.
- Missing active-language keys fall back to bundled English.
- Missing keys are merged into disk language files where possible.
- Detailed fallback information goes to the session log instead of spamming console.

Key files:

- `spigot/src/main/java/de/sean/blockprot/bukkit/TranslationKey.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/Translator.java`
- `spigot/src/main/resources/lang/*.yml`

## Archived Claims System

Earlier SP26 work included a RedProtect-like 3D claims system with:

- Claim regions.
- Claim flags.
- Claim wand.
- Claim manager.
- Claim GUI screens.
- Claim event listeners.
- Welcome messages.
- Block place/break restrictions inside claims.
- Auto-lock inside claims.

That system is not part of the active plugin source in `spigot/src/main` in this repository revision. It was removed from the runtime code path and archived under:

```text
_claims_removed/
```

The archived code is kept as a reference for a future reimplementation. It should not be documented as an active server feature unless it is moved back into `spigot/src/main` and wired into startup again.

## Main New Configuration Keys

```yaml
worlds_config_enabled: false
optional_features_enable_all: false

localized_commands:
  enabled: false
  keep_english_aliases: true

mysql:
  enabled: false
  host: "127.0.0.1"
  port: 3306
  database: "blockprot"
  username: "blockprot"
  password: ""
  jdbc_url: ""
  pool:
    maximum_pool_size: 10
    minimum_idle: 2
    connection_timeout_ms: 10000

protect_locked_blocks_from_explosions: true
block_protected_block_piston_movement: true

worldedit_paste_autolock:
  enabled: false
  radius: 24
  max_blocks_per_paste: 5000
  delay_ticks: 20
  require_claim_owner: false

bedrock_username_prefixes:
  - "."
  - "*"
  - "_"

inactivity_cleanup_days: -1
```

## Dependencies Added By The Fork

- HikariCP
- MySQL Connector/J
- SLF4J API for Hikari runtime support

The shaded JAR includes the SQL dependencies needed by the optional MySQL backend.

## Installation

1. Stop the server.
2. Put the built JAR in the server `plugins/` directory.
3. Start the server once.
4. If existing BlockProt data is detected, SP26-U creates a ZIP backup automatically.
5. Review `plugins/BlockProt/config.yml`.
6. Enable only the optional modules you actually want.

For existing servers, always review the generated/merged config after the first startup.

## Development Notes

- Source comments and JavaDocs should be written in English.
- Player-facing messages belong in translation files.
- Console messages should use `Translator` when translations are already loaded.
- New optional features should be disabled by default unless they are part of the original BlockProt core.
- The original NBT ownership model must remain authoritative for individual block protection.

## License

BlockProt is licensed under GPLv3. See `LICENSE` for details.
