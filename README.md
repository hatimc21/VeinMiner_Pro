# VeinMiner_Pro

![Java CI with Maven](https://github.com/hatimc21/VeinMiner_Pro/actions/workflows/maven.yml/badge.svg)

A powerful and balanced Vein Miner and Tree Feller plugin for Spigot/PaperMC servers. This plugin allows players to mine entire ore veins and fell whole trees in one go, while respecting game balance through tool durability, enchantments, and hunger costs.

## Features
- **Smart Vein Mining:** Break one ore, and the entire connected vein breaks with it.
- **Efficient Tree Felling:** Break the bottom log of a tree to fell the entire trunk.
- **Player-Controlled:** Each player can toggle the ore and tree features on/off for themselves.
- **Balanced Gameplay:**
  - **Tool Durability:** Correctly consumes durability for every block broken.
  - **Enchantment Support:** Works perfectly with Fortune and Silk Touch.
  - **Hunger Cost:** A small exhaustion cost makes the feature feel integrated into survival.
- **Configurable:** Admins can customize the max vein size, block lists, and more via the `config.yml`.
- **Admin Reload:** Safely reload the configuration on-the-fly with `/vm reload`.

## Commands & Permissions
| Command                | Description                          | Permission               |
| ---------------------- | ------------------------------------ | ------------------------ |
| `/vm ores <on/off>`    | Toggles vein mining for ores.        | `(None)`                 |
| `/vm trees <on/off>`   | Toggles tree felling.                | `(None)`                 |
| `/vm reload`           | Reloads the plugin's configuration.  | `veinminerpro.reload`    |

## Installation
1. Download the latest `.jar` file from the [**Releases page**](https://github.com/hatimc21/VeinMiner_Pro/releases).
2. Place the `.jar` file into your server's `/plugins` folder.
3. Restart or reload your server.
4. (Optional) Configure the plugin by editing the `config.yml` file located in `/plugins/VeinMinerPro/`.

## How to Build from Source
If you want to compile the plugin yourself, you'll need Git and JDK 17 (or newer) installed.
```bash
# Clone the repository
git clone https://github.com/hatimc21/VeinMiner_Pro.git

# Navigate into the directory
cd VeinMiner_Pro

# Compile the plugin using Maven
mvn clean package
