# SPlugin

SPlugin is a Spigot/Paper plugin designed for a basic survival server.
It is a lightweight and performant plugin, bringing you some essential features, including the ability to lock blocks 
and connect your server with Discord.

# 🔨️ Building yourself

SPlugin uses Gradle to handle dependencies as building.
Make sure you have Java 8 and Git installed, then run
```batch
git clone https://github.com/spnda/SPlugin.git
cd SPlugin/
gradlew build
```
All dependencies get shadowed automatically, meaning you won't need to install anything else on your server.
The compiled JARs for Spigot and Paper can be found in `splugin-spigot/build/libs` and `splugin-paper/build/libs` respectively.

# 📋 License

SPlugin is licensed under the MIT license, view `LICENSE` to learn more.
