# SPlugin

## 🤔 **What is it?**

**_SPlugin_** is a lightweight spigot plugin designed for a basic server infrastructure. 
Admins can define groups of users, players can lock chests/hoppers... and everything is configurable to the server.

## 🔧 Configuration

To further configure discord and in-game settings you will need to add a `config.yml` 
file into `server/plugins/SPlugin`. When launching, there will be a default config there.
An example config.yml file:
```yml
discord:
    token: 'DISCORD_BOT_TOKEN'
    channels:    
        'GUILD_ID': 'CHANNEL_ID'
        'GUILD2_ID': 'CHANNEL2_ID'
messages:
    join:
    - '[player] has joined this server!'
    - '[player] has joined the server.'
players:
    - 7bf18cg3-e1c6-4c2f-98sb-0e63f2f4ec9f:
        role: CITIZEN
roles:
    CITIZEN:
        name: "Citizen"
        color: "YELLOW"
```
Example permissions.yml file:
```yml
groups:
    Default:
        default: true
        permissions:
            - splugin.sit
            - splugin.lock
```

## ▶️ Building yourself

Simply clone this repository and use `gradlew build` to build your JAR into `./build/libs/`.
All dependencies automatically get shadowed which means you won't need to put any dependency JARs anywhere you use this plugin.