# <div align="center"><img src="https://via.placeholder.com/150/0055AA/FFFFFF?text=UJ%2B" width="128" alt="UltimateJoin+ Logo"></div>
# UltimateJoin+ 🚀
**The most advanced and modern Join/Quit manager for your Minecraft Server.**

![Java CI with Maven](https://github.com/tomas2193xd-arch/UltimateJoin-/actions/workflows/maven.yml/badge.svg)
![License](https://img.shields.io/github/license/tomas2193xd-arch/UltimateJoin-)
![Version](https://img.shields.io/github/v/release/tomas2193xd-arch/UltimateJoin-)

**UltimateJoin+** transforms the way players enter your server. Forget boring join messages; welcome your players with style using Hex colors, gradients, titles, bossbars, and interactive events.

---

## ✨ Features

- **🎨 Modern Formatting**: Full support for Hex Colors, Gradients, and MiniMessage syntax.
- **👑 Rank-Based Actions**: Custom messages, sounds, titles, and bossbars per rank (hooked with LuckPerms).
- **🌍 Multi-Language**: Built-in support for English, Spanish, German, French, Italian, Portuguese, and Chinese.
- **🎆 First Join Experience**: Custom spawn, fireworks, and command execution for new players.
- **📍 Spawn Management**: Smart handling of First Spawn, Join Spawn, and Respawn locations.
- **🚧 Maintenance Mode**: Native maintenance system with permission-based bypass and custom MOTD.
- **🎉 Welcomer Event**: Chat minigame to welcome new players and reward community interaction.
- **💾 Database Support**: MySQL and SQLite support to track player stats across resets.
- **🔌 Developer API**: Open API and PlaceholderAPI expansion included.

## 📥 Installation

1. Download the JAR file from the [Releases](https://github.com/tomas2193xd-arch/UltimateJoin-/releases) page.
2. Drop it into your server's `plugins` folder.
3. Restart your server.
4. (Optional) Configure `config.yml` and `messages_xx.yml` to your liking.

## 🛠 Commands & Permissions

| Command | Permission | Description |
|os|---|---|
| `/uj reload` | `uj.admin` | Reloads the configuration. |
| `/uj maintenance <on/off>` | `uj.maintenance` | Toggles maintenance mode. |
| `/setspawn` | `uj.setspawn` | Sets the main join/respawn point. |
| `/setfirstspawn` | `uj.setspawn` | Sets the dedicated safe spawn for new players. |
| `/spawn` | `uj.spawn` | Teleports you to the spawn point. |

## ⚙️ Configuration (Snippet)

```yaml
# Simple snippet of what you can do
ranks:
  vip:
    weight: 10
    permission: "group.vip"
    join-message: 
      - "<gradient:#ff0000:#ffff00>⭐ VIP %player% has joined the server!</gradient>"
    title:
      title: "<gold>Welcome Back!"
      subtitle: "<yellow>We missed you."
    sound: "ENTITY_PLAYER_LEVELUP"
```

## 🤝 Support & Contributing

Found a bug? Have a feature request?
Open an [Issue](https://github.com/tomas2193xd-arch/UltimateJoin-/issues) on our GitHub repository.

---
<div align="center">
  <sub>Made with ❤️ by Tomas Garcia. &copy; 2026 All Rights Reserved.</sub>
</div>
