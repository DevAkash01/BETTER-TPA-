# Better Teleportation (BETTER-TPA)

A modern, high-performance, standalone PaperMC teleportation plugin designed for Minecraft 1.21+ (built with Java 25 & 21, targeting the official Paper 1.21.x API).

---

## Features

- **Native Minecraft Dialogs**: Modal confirmation screens rendered using Paper's native Dialog API with player avatars and interactive buttons.
- **Directional Teleports**: Support for both `/tpa` (requester to target) and `/tpahere` (target to requester).
- **Personal Request Settings**: `/tpsetting <tpa|tpahere> <on|off|toggle>` allows players to individually control their incoming teleport requests.
- **Auto-Accept Mode**: `/tpauto` allows players to toggle automatic instant acceptance of incoming teleport requests.
- **Adventure & MiniMessage Support**: 100% component-based chat formatting with hover tooltips, click actions, gradients, and legacy color code support.
- **Warmup & Safety**: Configurable warmup delay, movement threshold cancellation, damage cancellation, and safe destination checks.
- **Customizable Audio Engine**: Full sound configuration for requests, accepts, denies, countdown ticks, and toggles.
- **Standalone Combat Protection**: Built-in combat tracking that blocks teleport requests and warmup when in combat.
- **Developer API & Events**: Custom Bukkit events for request lifecycle and teleportation hooks.

---

## Commands & Permissions

| Command | Usage | Description | Permission | Default |
| :--- | :--- | :--- | :--- | :--- |
| `/tpa` | `/tpa <player>` | Send a teleport request | `betterteleporation.tpa` | `true` |
| `/tpa reload` | `/tpa reload` | Reload configuration files | `betterteleporation.admin.reload` | `op` |
| `/tpahere` | `/tpahere <player>` | Request a player to teleport to you | `betterteleporation.tpahere` | `true` |
| `/tpauto` | `/tpauto [on\|off\|toggle]` | Toggle auto-accepting requests | `betterteleporation.tpauto` | `true` |
| `/tpsetting` | `/tpsetting <tpa\|tpahere> [on\|off\|toggle]` | Manage personal request settings | `betterteleporation.tpsetting` | `true` |
| `/tpaccept` | `/tpaccept [player]` | Accept a teleport request | `betterteleporation.tpaccept` | `true` |
| `/tpdeny` | `/tpdeny [player]` | Deny a teleport request | `betterteleporation.tpdeny` | `true` |

---

## Compilation & Building

```powershell
mvn clean package
```

The compiled plugin jar will be generated at `target/betterteleporation-1.0.0.jar`.
