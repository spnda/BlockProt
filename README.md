# SPlugin

> SPlugin is a lightweight spigot plugin designed for a basic server infrastructure. Users can define groups of users, lock chests/hoppers/anvil... and everything is configurable to the user.

## Usage

Put the `splugin-all.jar` into `server/plugins`.

To further configure discord and in-game settings you will need to add a `config.yml` 
file into `server/plugins/SPlugin`. That yml file can have various data:
- DiscordToken: `String`. Your discord bot token. You can get one from your application https://discord.com/developers
- DiscordGuild: `String`. The guild ID you want to stream the chat data to.
- DiscordChannel: `String`. The channel ID you want to stream the chat data to.
- Messages:
    - Join: `List of String`. A list of different messages to post when a player joins. `[player]` will be replaced with the player name.
    - Leave: `List of String`. A list of different messages to post when a player leaves. `[player]` will be replaced with the player name.
    - Death: `List of String`. A list of different messages to post when a player dies. `[message]` will be replaced with the original death message. `[player]` will be replaced with the player name.
    - SleepEnter: `List of String`. A list of different messages to post when a player enters a bed. `[player]` will be replaced with the player name.
    - SleepLeave: `List of String`. A list of different messages to post when a player leaves a bed. `[player]` will be replaced with the player name.
- Players:
    - player uuid:
        - Role: `OWNER` | `CITIZEN`
## Building yourself

Simply clone this repository and use `gradlew build` to build your JAR into `./build/libs/`.
All dependencies automatically get shadowed which means you won't need to put any dependency JARs anywhere you use this plugin.