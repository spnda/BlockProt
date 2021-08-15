# BlockProt

[![CurseForge Downloads](http://cf.way2muchnoise.eu/full_440797_downloads.svg?badge_style=flat)](https://www.curseforge.com/minecraft/bukkit-plugins/blockprot)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/87829?style=flat-square&color=orange&label=spigot%20downloads)](https://www.spigotmc.org/resources/blockprot.87829/)
[![Spigot Rating](https://img.shields.io/spiget/rating/87829?style=flat-square&color=orange)](https://www.spigotmc.org/resources/blockprot.87829/)
[![BlockProt CI](https://img.shields.io/github/workflow/status/spnda/BlockProt/CI?style=flat-square&label=CI)](https://github.com/spnda/BlockProt)
[![](https://jitpack.io/v/spnda/BlockProt.svg)](https://jitpack.io/#spnda/BlockProt)


[![Servers](https://img.shields.io/bstats/servers/9999?style=flat-square)](https://bstats.org/plugin/bukkit/BlockProt/9999)
[![Players](https://img.shields.io/bstats/players/9999?style=flat-square)](https://bstats.org/plugin/bukkit/BlockProt/9999)

BlockProt is a lightweight Bukkit and Spigot plugin that gives players the ability to protect chests, furnaces and many more blocks.
The plugin features a modern GUI approach instead of commands, so that any player can easily understand how to use all of its features.

## Installing

To use this plugin, you can simply download it from [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/blockprot)
or [SpigotMC](https://www.spigotmc.org/resources/blockprot.87829/) and place it in your `plugins` directory.
You do not have to install anything else and any recent version will work with 1.14, 1.15, 1.16 and 1.17. It runs
on any Spigot and any fork thereof and does **not** run on CraftBukkit.

To get the latest updates the quickest, you can "watch" or "star" this repository or frequently check the
[Release tab](https://github.com/spnda/BlockProt/releases). Alternatively, it is also possible to
[watch the resource](https://www.spigotmc.org/resources/blockprot.87829/watch) on Spigot, which will also notify
you as soon as new builds get released.

---

If you want to build the plugin from source yourself, you can simply do so by installing a JDK 8 or newer, cloning this
repository and running `./gradlew build` in the main directory.

## Contact/Support

If you find bugs or any issues related to this plugin, please report them over on the
[GitHub issue tracker](https://github.com/spnda/BlockProt/issues). If you require more support or want to ask questions,
please use the [Discord server](https://discord.gg/WVy6DHScFb) for chatting.

## Developing Addons

This plugin offers a basic API which other developers can use to add new features and provide support and
compatibility with other plugins. BlockProt can be accessed via the [jitpack.io repository](https://jitpack.io/#spnda/BlockProt).

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
[here](https://github.com/spnda/BlockProt/tree/master/src/main/java/de/sean/blockprot/bukkit/events). You can
use these to block players from accessing some blocks based on custom conditions and much more. For even more
fine grain control over events, you can write a `PluginIntegration`. A `PluginIntegration` is designed to be
specific to a single other plugin and is only activated when the referenced plugin is actually loaded through
Bukkit. It also provides utilities to quickly load a single config file and register listeners. BlockProt 
natively includes a plugin integration for Towny, which you can find
[here](https://github.com/spnda/BlockProt/blob/master/src/main/java/de/sean/blockprot/bukkit/integrations/TownyIntegration.java).
You can freely use this as an example.

## License

BlockProt is licensed under GPLv3 license, view `LICENSE` to learn more.
