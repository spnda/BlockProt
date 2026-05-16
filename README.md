# BlockProt

[![CurseForge Downloads](http://cf.way2muchnoise.eu/full_440797_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/bukkit-plugins/blockprot)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/uF5ePKuc?style=flat-square&color=green&label=Downloads&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGFyaWEtaGlkZGVuPSJ0cnVlIiBjbGFzcz0idGV4dC1sb2dvIiB3aWR0aD0iMTQyIiBoZWlnaHQ9IjE0MiI+PGc+PHBhdGggZD0iTTE1OS4wNyw4OS4yOUE3MC45NCw3MC45NCwwLDEsMCwyMCw2My41MkgzMkE1OC43OCw1OC43OCwwLDAsMSwxNDUuMjMsNDkuOTNsLTExLjY2LDMuMTJhNDYuNTQsNDYuNTQsMCwwLDAtMjktMjYuNTJsLTIuMTUsMTIuMTNhMzQuMzEsMzQuMzEsMCwwLDEsMi43Nyw2My4yNmwzLjE5LDExLjlhNDYuNTIsNDYuNTIsMCwwLDAsMjguMzMtNDlsMTEuNjItMy4xQTU3Ljk0LDU3Ljk0LDAsMCwxLDE0Ny4yNyw4NVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xOS43OSkiIGZpbGw9IiMxYmQ5NmEiIGZpbGwtcnVsZT0iZXZlbm9kZCI+PC9wYXRoPjxwYXRoIGQ9Ik0xMDguOTIsMTM5LjNBNzAuOTMsNzAuOTMsMCwwLDEsMTkuNzksNzZoMTJhNTkuNDgsNTkuNDgsMCwwLDAsMS43OCw5LjkxLDU4LjczLDU4LjczLDAsMCwwLDMuNjMsOS45MWwxMC42OC02LjQxYTQ2LjU4LDQ2LjU4LDAsMCwxLDQ0LjcyLTY1TDkwLjQzLDM2LjU0QTM0LjM4LDM0LjM4LDAsMCwwLDU3LjM2LDc5Ljc1QzU3LjY3LDgwLjg4LDU4LDgyLDU4LjQzLDgzbDEzLjY2LTguMTlMNjgsNjMuOTNsMTIuOS0xMy4yNSwxNi4zMS0zLjUxTDEwMS45LDUzbC03LjUyLDcuNjEtNi41NSwyLjA2LTQuNjksNC44MiwyLjMsNi4zOHM0LjY0LDQuOTQsNC42NSw0Ljk0bDYuNTctMS43NCw0LjY3LTUuMTMsMTAuMi0zLjI0LDMsNi44NEwxMDQuMDUsODguNDMsODYuNDEsOTRsLTcuOTItOC44MUw2NC43LDkzLjQ4YTM0LjQ0LDM0LjQ0LDAsMCwwLDI4LjcyLDExLjU5TDk2LjYxLDExN0E0Ni42LDQ2LjYsMCwwLDEsNTQuMTMsOTkuODNsLTEwLjY0LDYuMzhhNTguODEsNTguODEsMCwwLDAsOTkuNi05Ljc3bDExLjgsNC4yOUE3MC43Nyw3MC43NywwLDAsMSwxMDguOTIsMTM5LjNaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTkuNzkpIiBmaWxsPSIjMWJkOTZhIj48L3BhdGg+PC9nPjwvc3ZnPg==)](https://modrinth.com/plugin/blockprot)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/87829?style=flat-square&color=orange&label=Spigot%20Downloads)](https://www.spigotmc.org/resources/blockprot.87829/)
[![Spigot Rating](https://img.shields.io/spiget/stars/87829?style=flat-square&color=orange&label=Rating)](https://www.spigotmc.org/resources/blockprot.87829/)
[![Hangar Downloads](https://img.shields.io/hangar/dt/BlockProt?style=flat-square&label=Hangar%20Downloads)](https://hangar.papermc.io/spnda/BlockProt)
[![BlockProt CI](https://img.shields.io/github/actions/workflow/status/spnda/BlockProt/ci.yml?branch=master&style=flat-square&label=CI)](https://github.com/spnda/BlockProt)
[![gitlocalized](https://gitlocalize.com/repo/6775/whole_project/badge.svg)](https://gitlocalize.com/repo/6775)
[![JitPack](https://img.shields.io/jitpack/version/com.github.spnda/BlockProt?style=flat-square)](https://jitpack.io/#spnda/BlockProt)


[![Servers](https://img.shields.io/bstats/servers/9999?style=flat-square)](https://bstats.org/plugin/bukkit/BlockProt/9999)
[![Players](https://img.shields.io/bstats/players/9999?style=flat-square)](https://bstats.org/plugin/bukkit/BlockProt/9999)
[![Discord](https://img.shields.io/discord/860987230453891112?style=flat-square)](https://discord.gg/WVy6DHScFb)

BlockProt is a lightweight Bukkit and Spigot plugin that gives players the ability to protect
chests, furnaces and many more blocks. The plugin features a modern GUI approach instead of
commands, so that any player can easily understand how to use all of its features.

![image1](https://github.com/VictorGugug/BlockProt-SP26-U/blob/master/images/main_menu.png)

## SP26-U fork notes

This repository is a maintained SP26-U fork of the original
[spnda/BlockProt](https://github.com/spnda/BlockProt) project. The original
BlockProt core remains intact: NBT/Persistent Data Container ownership is still
the source of truth for individual protected blocks, and the default chest and
container protection flow remains enabled.

The SP26-U changes are layered around that original core:

- Java 25 build target, class file version 69.0.
- Runtime diagnostics for modern Spigot/Paper-family servers.
- Startup validation through `VersionValidator`.
- Compatibility helpers for classic `1.x` and year-based `26.x` server versions.
- Modern Bukkit/Paper API fixes, including the `LeavesDecayEvent` package move.
- Persistent per-session logs under `plugins/BlockProt/logs/`.
- Safe upgrade backup ZIPs under `plugins/BlockProt/backups/` when previous data is detected.
- Non-destructive config migration that adds missing keys without overwriting administrator values.
- Automatic config/language reload watcher with debouncing.
- Optional `worlds.yml` per-world lockable-block lists, disabled by default.
- Optional HikariCP/MySQL index while NBT remains authoritative.
- SQLite audit log and audit inventory for protected-block access history.
- `/bp friends addall <player>` for bulk friend sync across known protected blocks.
- Geyser/Floodgate-style Bedrock username prefix resolution for friend lookup.
- Optional localized subcommand aliases, disabled by default.
- `/bp help` command.
- Optional WorldEdit/FAWE paste watcher for NBT auto-locking pasted lockable blocks.
- Optional explosion and piston hardening for protected blocks.
- Optional inactivity cleanup for protections owned by inactive players.
- New cancellable API events: `BlockProtLockEvent` and `BlockProtUnlockEvent`.
- Expanded translation coverage for console messages, audit, help, chat input, and SP26 features.

High-impact SP26-U features are opt-in. Options such as MySQL, localized
subcommands, per-world lockable lists, WorldEdit paste auto-lock, and inactivity
cleanup are disabled by default. The original BlockProt protection behavior is
not disabled automatically during updates.

### Archived claims prototype

An earlier SP26 prototype included a RedProtect-like 3D claims system with
regions, flags, a wand, GUI screens, welcome messages, and claim event
listeners. That code is not active in this fork revision and is not compiled
from `spigot/src/main`.

The prototype is archived in `_claims_removed/` only as reference material for a
possible future implementation.

## Installing

To use the original public plugin, you can download it from [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/blockprot),
[Modrinth](https://modrinth.com/plugin/blockprot) or [SpigotMC](https://www.spigotmc.org/resources/blockprot.87829/)
and place it in your `plugins` directory. You do not have to install anything else and any recent
version will work with any recent Minecraft version, as long as you're running Java 17 or higher.
It runs on any Spigot and any fork thereof and does **not** run on CraftBukkit.

For this SP26-U fork, build the shaded JAR from source and place the generated
JAR in the server `plugins` directory. On existing servers, start once and then
review `plugins/BlockProt/config.yml`; the fork will create a safety backup when
old BlockProt data is detected.

To get the latest original upstream updates the quickest, you can "watch" or "star"
[spnda/BlockProt](https://github.com/spnda/BlockProt) or frequently check the
[Release tab](https://github.com/spnda/BlockProt/releases). Alternatively, it is also
possible to [watch the resource](https://www.spigotmc.org/resources/blockprot.87829/watch) on
Spigot, which will also notify you as soon as new builds get released.

---

If you want to build the plugin from source yourself, install the required JDK,
clone this repository and run:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat :blockprot-spigot:build
```

The SP26-U shaded JAR is generated at:

```text
spigot/build/libs/blockprot-spigot-1.2.9-main-all.jar
```

## Translating

If you know a language that isn't supported by this plugin (yet) or found some translation error
we'd love if you could translate for us!

You can do so easily by going to [gitlocalize](https://gitlocalize.com/repo/6775/) or sending me
the translations on Discord.

SP26-U also localizes console messages that are safe to translate after the
translation system has loaded. English remains the default language, and missing
keys fall back to the bundled English translation.

## Contact/Support

For the original project, report bugs or issues on the
[spnda/BlockProt GitHub issue tracker](https://github.com/spnda/BlockProt/issues).
If you require more support or want to ask questions, please use the
[Discord server](https://discord.gg/WVy6DHScFb) for chatting.

For SP26-U-specific changes, use this fork repository's issue tracker once the
fork is published on GitHub.

## Developing Addons

This plugin offers a basic API which other developers can use to add new features and provide
support and compatibility with other plugins. BlockProt can be accessed via the
[jitpack.io repository](https://jitpack.io/#spnda/BlockProt).

Your `build.gradle` file may look like this:
```groovy
repositories {
    maven 'https://jitpack.io'
}

dependencies {
    // This version might be slightly outdated. See https://github.com/spnda/BlockProt/releases.
    implementation 'com.github.spnda.BlockProt:blockprot-spigot:0.4.11'
}
```

From here on, you're good to go. We provide an easy to use `BlockProtAPI` class and other utility methods
to easily add new functionality, with low amount of boilerplate. In the following examples you can see how
to use that class.
```java
// A BlockNBTHandler is a NBT Handler designed to lock blocks, add friends and edit other settings.
// This handler exists on a per-block basis.
BlockNBTHandler handler = BlockProtAPI.getInstance().getBlockHandler(block);

// The PlayerSettingsHandler is made to handle settings that are commonly accessible
// through the "/blockprot settings" command.
PlayerSettingsHandler playerHandler = BlockProtAPI.getInstance().getPlayerSettings(player);
```

We also offer a variety of events that you can listen to. For an up-to-date and more detailed list, see
[here](https://github.com/spnda/BlockProt/tree/master/src/main/java/de/sean/blockprot/bukkit/events).
You can use these to block players from accessing some blocks based on custom conditions and much
more. For even more fine grain control over events, you can write a `PluginIntegration`. A
`PluginIntegration` is designed to be specific to a single other plugin and is only activated when
the referenced plugin is actually loaded through Bukkit. It also provides utilities to quickly load
a single config file and register listeners. BlockProt natively includes a plugin integration for
Towny, which you can find
[here](https://github.com/spnda/BlockProt/blob/master/src/main/java/de/sean/blockprot/bukkit/integrations/TownyIntegration.java).
You can freely use this as an example.

SP26-U additionally exposes `BlockProtLockEvent` and `BlockProtUnlockEvent` so
addons can react to or cancel lock/unlock operations.

## bStats

[![bStats Graph](https://bstats.org/signatures/bukkit/BlockProt.svg)](https://bstats.org/plugin/bukkit/BlockProt)

## License

BlockProt is licensed under GPLv3 license, view `LICENSE` to learn more.
