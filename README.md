# BlockProt - SP26 Fork

[![BlockProt CI](https://img.shields.io/github/actions/workflow/status/spnda/BlockProt/ci.yml?branch=master&style=flat-square&label=CI)](https://github.com/spnda/BlockProt)
[![JitPack](https://img.shields.io/jitpack/version/com.github.spnda/BlockProt?style=flat-square)](https://jitpack.io/#spnda/BlockProt)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg?style=flat-square)](LICENSE)

> **This is the SP26 community fork** of [BlockProt by spnda](https://github.com/spnda/BlockProt).  
> It extends the original plugin with Java 25 / Paper 26.x compatibility, a hybrid MySQL index,
> per-world configuration, access auditing, automatic backups, and several quality-of-life features.  
> The upstream NBT block-protection core is preserved and untouched.

![Main menu](https://raw.githubusercontent.com/VictorGugug/BlockProt-SP26-U/master/images/main_menu.png)

---

## What is BlockProt?

BlockProt is a lightweight Bukkit/Spigot plugin that lets players protect chests, furnaces, and
many other blocks. It uses a modern GUI instead of commands, so any player can immediately
understand how to lock and share their blocks.

This fork targets Paper/Spigot-family servers on Minecraft 1.21+ and the new
year-based 26.x version family, including the 26.1.2 development target.

---

## Installing

### Pre-built JAR

Build the JAR yourself (see below) and place it in your `plugins/` directory. No other
dependencies are required for the base plugin. The produced JAR targets Java 25
class files, so servers must run JDK 25.

### Build from source

```bash
# Requires JDK 25
git clone https://github.com/VictorGugug/BlockProt-SP26-U.git
cd BlockProt-SP26-U
./gradlew :blockprot-spigot:shadowJar
# Output: spigot/build/libs/blockprot-spigot-*-all.jar
```

> The project compiles against `targetJavaVersion = 25`; the produced JAR uses
> Java class file version `69.0` and must be deployed on Java 25.

---

## SP26 Fork Features

All new features are **disabled by default** in `config.yml` so that existing servers are
never affected without an explicit admin decision. The base NBT block-protection system
works exactly as in the upstream plugin.

### 1. Java 25 / Paper 26.x Compatibility

- Compiles and runs on Java 25 (class file `69.0`).
- Detects both the classic `1.x` and the new year-based `26.x` version scheme at runtime.
- Validates Java version, Paper availability, and typed inventory view support on startup.
- Logs a one-line diagnostic to console and the session log file.

### 2. Persistent Session Logging

- Creates one log file per server session under `plugins/BlockProt/logs/`.
- Every line is timestamped. Logs plugin version, server version, and compatibility check results.
- Logs `PASS`, `FAIL`, or `WARN` for each startup check.
- Does **not** spam the console; detailed information stays in the log file.

### 3. 3D Claims System *(removed — implementation preserved)*

> **Status: removed from the active build.**  
> The full claims implementation was developed and compiled successfully, but was later removed
> from the plugin's runtime registration. All source files are kept in `_claims_removed/` as a
> reference implementation in case claims are re-enabled in the future.
>
> The system was originally inspired by RedProtect-style region protection. It added 3D bounding-box
> claims owned by individual players, with named regions, per-claim flag control, a wand-based
> selection flow, welcome messages, and a GUI management hub. A `ClaimManager` handled persistence
> via `claims.yml` and asynchronous load/save. The system was fully functional but was ultimately
> removed to keep the plugin scope focused.

**What was implemented (preserved in `_claims_removed/`):**

- Named 3D regions with min/max XYZ coordinates and per-world scope.
- Persistent storage in `claims.yml` with async load/save.
- Wand-based selection (left-click = pos A, right-click = pos B) with particle border preview.
- Chat prompt for naming a new claim with configurable timeout.
- Welcome message shown to players on claim entry.
- Overlap detection between claims.
- Per-claim behaviour flags (see below).
- GUI management hub (Shift + Right Click inside own claim).
- Archived claim command tree while the module was active. No claim commands are
  registered in the current build.

**Claim flags that were implemented:**

| Flag | Description |
|------|-------------|
| `TNT` | Allow TNT / creeper explosions to damage blocks |
| `FIRE` | Allow fire to spread |
| `PVP` | Allow PvP between players |
| `REDSTONE` | Allow non-members to trigger redstone |
| `FLOW` | Allow water and lava to flow |
| `LEAF_DECAY` | Allow leaf decay |
| `MOB_SPAWNING` | Allow natural mob spawning |
| `INVINCIBLE` | Make players inside the claim take no damage |
| `CHEST` | Allow non-members to open containers |
| `DOOR` | Allow non-members to use doors and trapdoors |
| `LEVER_BUTTON` | Allow non-members to use levers and buttons |
| `USE_BUCKET` | Allow non-members to use buckets |
| `PET_SPAWNING` | Allow taming new pets inside the claim |
| `PET_RIDING` | Allow non-members to mount pets |
| `PET_MOVING` | Allow non-members to lead animals |
| `DENY_ENDERPEARL` | Prevent ender pearl teleportation into / within the claim |

### 4. Hybrid MySQL / NBT Backend *(optional)*

NBT remains the source of truth for per-block ownership and friend lists. MySQL/MariaDB is
used as an optional index for fast lookups, auditing, and cross-server global trust.

- Connection pool via HikariCP (shaded to avoid conflicts).
- MySQL Connector/J bundled inside the shadow JAR.
- Tables: `blockprot_block_index`, `blockprot_global_trust`.
- All SQL operations are asynchronous.
- In-memory cache for global trust, loaded at startup.
- Disabled by default (`mysql.enabled: false`).

### 5. Master Friend List & `/bp friends addall`

- `/bp friends` - opens the global default-friends GUI.
- `/bp friends addall <player>` - adds the target player to **every** block the executor owns,
  and to their global default-friend list. Resolves offline players via Mojang API.
- When `localized_command_aliases: true` and Spanish is selected, `/bp amigos` and
  `/bp amigos agregartodos <player>` are also accepted. Canonical English commands
  always continue to work.
- Reports how many blocks were updated via an action bar message.

### 6. SQLite Access Audit Log

- Database: `plugins/BlockProt/blockprot_audit.sqlite`.
- Records `ACCESS_DENIED` and `ACCESS_GRANTED` events with player UUID, name, location, and timestamp.
- All writes are asynchronous (`CompletableFuture`).
- Indexed by location, player, and timestamp.
- Automatic pruning when the table exceeds 50 000 entries.
- In-game GUI (`AuditInventory`) shows access history per block.
- Admin teleport button inside the audit GUI.

### 7. Automatic Backup & Safe Migration

- On startup, detects pre-existing plugin data (`data.yml`, `claims.yml`, `blockprot.db`, etc.).
- If prior data is found, creates a ZIP backup under `plugins/BlockProt/backups/` **before** any
  migration step runs.
- Rotates backups; keeps a maximum of 10 ZIP files.
- `/bp reload` always triggers a forced backup first.
- Does not back up session log files or the `backups/` folder itself.

### 8. Inactivity Cleanup *(optional)*

- Configurable via `inactivity_cleanup_days` in `config.yml` (default `-1` = disabled).
- When enabled, runs once at startup and removes protections from blocks owned by players who
  have not logged in for the configured number of days.
- Notifies online admins with a summary message.

### 9. Per-World Configuration (`worlds.yml`)

- Each world can have its own lists of lockable tile entities, blocks, shulker boxes, and doors.
- `enabled: false` disables all block protection in that world.
- Worlds not listed in `worlds.yml` fall back to `config.yml` globals.
- At startup, missing worlds are added automatically with `enabled: true` and lists inherited
  from `config.yml` (non-destructive; existing entries are never overwritten).
- A broken `worlds.yml` is replaced with the bundled default and a warning is logged.

### 10. Config File Watcher

- Monitors `config.yml`, `worlds.yml`, and all `lang/*.yml` files for changes.
- Automatically reloads the plugin configuration when a change is detected.
- Debounced (2 s) to avoid duplicate reloads while an editor is still writing.

### 11. Hardened Security Options

| Config key | Default | Description |
|---|---|---|
| `protect_locked_blocks_from_explosions` | `true` | Prevents explosions from destroying NBT-protected blocks |
| `block_protected_block_piston_movement` | `true` | Prevents pistons from pushing or pulling NBT-protected blocks |

### 12. `/bp help` Command

- `/bp help` (also `/bp ayuda`) lists all available subcommands with a short description.

### 13. New Configuration Keys

```yaml
# ── Claims (implementation preserved in _claims_removed/) ─────────────────
claims_enabled: false
claims_hub_enabled: false
claims_advanced_enabled: false
claims_inherit_global_trust: false

# ── MySQL hybrid index ─────────────────────────────────────────────────────
mysql:
  enabled: false
  host: "127.0.0.1"
  port: 3306
  database: "blockprot"
  username: "blockprot"
  password: ""
  jdbc_url: ""          # Overrides host/port/database when set
  pool:
    maximum_pool_size: 10
    minimum_idle: 2
    connection_timeout_ms: 10000

# ── Security hardening ────────────────────────────────────────────────────
protect_locked_blocks_from_explosions: true
block_protected_block_piston_movement: true

# ── Claim flags (only relevant if claims_enabled: true) ───────────────────
claims_flag_flow_enabled: false
claims_flag_mob_spawning_enabled: false
claims_flag_invincible_enabled: false
claims_flag_deny_enderpearl_enabled: false
claims_flag_pets_enabled: false
claims_flag_chest_enabled: false
claims_flag_door_enabled: false
claims_flag_lever_button_enabled: false
claims_flag_use_bucket_enabled: false

# ── Inactivity cleanup ────────────────────────────────────────────────────
inactivity_cleanup_days: -1   # -1 = disabled
```

### 14. New Files Added vs. Upstream

| File | Description |
|------|-------------|
| `BlockProtLogger.java` | Persistent session log writer |
| `VersionCompat.java` | Runtime version detection (1.x and 26.x) |
| `VersionValidator.java` | Startup compatibility checks |
| `audit/AuditLogger.java` | SQLite access audit log |
| `commands/FriendsAddAllCommand.java` | `/bp friends addall` |
| `commands/HelpCommand.java` | `/bp help` |
| `config/WorldsConfig.java` | Per-world lockable-block configuration |
| `inventories/AuditInventory.java` | In-game audit log viewer |
| `inventories/ChatInput.java` | Chat-based player input helper |
| `inventories/AnvilInput.java` | Anvil GUI input fallback |
| `storage/HybridDatabase.java` | MySQL/MariaDB index with HikariCP |
| `tasks/BackupTask.java` | Pre-operation ZIP backup |
| `tasks/ConfigFileWatcher.java` | Auto-reload on config file change |
| `tasks/InactivityCleanupTask.java` | Remove protections of inactive players |
| `_claims_removed/` | Full claims implementation (preserved, not loaded) |

---

## Translating

If you know a language that is not yet supported, or you found a translation error, contributions
are welcome. Language files are in `spigot/src/main/resources/lang/`.

---

## Contact / Support

This fork is maintained by the SP26 community. For bugs or questions specific to this fork, open
an issue in this repository.

For upstream BlockProt issues, use the [original issue tracker](https://github.com/spnda/BlockProt/issues)
or the [Discord server](https://discord.gg/WVy6DHScFb).

---

## Developing Addons

The fork exposes the same `BlockProtAPI` as the upstream plugin. See the upstream documentation
for usage examples:

```java
// A BlockNBTHandler is an NBT handler designed to lock blocks, add friends, and edit settings.
// One handler exists per block.
BlockNBTHandler handler = BlockProtAPI.getInstance().getBlockHandler(block);

// The PlayerSettingsHandler manages player-level settings accessible via /blockprot settings.
PlayerSettingsHandler playerHandler = BlockProtAPI.getInstance().getPlayerSettings(player);
```

SP26-specific classes (`HybridDatabase`, `AuditLogger`, `WorldsConfig`, etc.) are internal and
not part of the public API.

---

## Dependencies Added by SP26

| Library | Version | Notes |
|---------|---------|-------|
| HikariCP | 7.0.2 | Shaded to `de.sean.blockprot.bukkit.shaded.hikari` |
| MySQL Connector/J | 9.7.0 | Bundled in shadow JAR |
| slf4j-api | runtime | Required by HikariCP |

---

## Build Verification

```powershell
.\gradlew.bat :blockprot-spigot:compileJava
.\gradlew.bat :blockprot-spigot:shadowJar
.\gradlew.bat :blockprot-spigot:build
```

Output JAR: `spigot/build/libs/blockprot-spigot-1.2.9-main-all.jar`

---

## bStats

[![bStats Graph](https://bstats.org/signatures/bukkit/BlockProt.svg)](https://bstats.org/plugin/bukkit/BlockProt)

---

## License

BlockProt is licensed under the **GNU General Public License v3**. See `LICENSE` for details.
