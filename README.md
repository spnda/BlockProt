# SPlugin

## 🤔 **What is it?**

**_SPlugin_** is a lightweight spigot plugin designed for a basic server infrastructure. 
Admins can define groups of users, players can lock chests/hoppers... and everything is configurable to the server.

## 🔧 Configuration

To further configure discord and in-game settings you will need to add a `config.yml` 
file into `server/plugins/SPlugin`. That yml file can have various data:
- Discord:
    - Token: `String`. Your discord bot token. You can get one from your application https://discord.com/developers
    - Guild: `String`. The guild ID you want to stream the chat data to.
    - Channel: `String`. The channel ID you want to stream the chat data to.
    - JoinMessage: `bool`. If true, the bot will send a message for each player that join to discord.
    - LeaveMessage: `bool`. If true, the bot will send a message for each player that leaves to discord.
    - Channels: `List of Guild/Channel`. You can have one channel per guild but multiple guilds.
- Messages:
    - Join: `List of String`. A list of different messages to post when a player joins. `[player]` will be replaced with the player name.
    - Leave: `List of String`. A list of different messages to post when a player leaves. `[player]` will be replaced with the player name.
    - Death: `List of String`. A list of different messages to post when a player dies. `[message]` will be replaced with the original death message. `[player]` will be replaced with the player name.
    - SleepEnter: `List of String`. A list of different messages to post when a player enters a bed. `[player]` will be replaced with the player name.
    - SleepLeave: `List of String`. A list of different messages to post when a player leaves a bed. `[player]` will be replaced with the player name.
- Players:
    - player uuid:
        - Role: `OWNER` | `CITIZEN`
        
An example config.yml file:
```yml
Discord:
    Token: 'DISCORD_BOT_TOKEN'
    Channels:    
        'GUILD_ID': 'CHANNEL_ID'
        'GUILD2_ID': 'CHANNEL2_ID'
Messages:
    Join:
    - '[player] has joined this server!'
    - '[player] has joined the server.'
```

## ▶️ Building yourself

Simply clone this repository and use `gradlew build` to build your JAR into `./build/libs/`.
All dependencies automatically get shadowed which means you won't need to put any dependency JARs anywhere you use this plugin.