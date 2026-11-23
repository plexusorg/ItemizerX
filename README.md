# ItemizerX

ItemizerX is a Paper plugin for editing the item in your hand from in game: names, lore,
enchantments, attributes, potions, item properties, vanilla data components, persistent data, and
container contents. Every edit can be undone, and items can be saved as presets and shared as
import tokens.

## Requirements

- Paper (or a fork) for Minecraft 1.21.11 or newer
- Java 25

## Installing

Put `ItemizerX-<version>.jar` in your server's `plugins` folder and restart. Everything is
op-only until you grant permissions. Grant players one of the
[permission packs](docs/commands.md#permission-packs): `itemizer.pack.cosmetic`,
`itemizer.pack.gameplay`, or `itemizer.pack.creation`. `itemizer.*` adds the
[sensitive permissions](docs/commands.md#sensitive-permissions) for trusted staff.

## Signs, protection, and logging

`/itemizer sign` edits a sign the way a player does, so protection plugins can refuse the edit
and block loggers such as [Oasis](https://oasis-protection.com) and CoreProtect record it.

## Commands

`/itemizer` (aliases `/ii` and `/it`). Type `/itemizer help` in game, or see
[docs/commands.md](docs/commands.md) for every command and the permission it needs.

## Building

```powershell
.\gradlew.bat build
```

The plugin jar is written to `build/libs/ItemizerX-<version>.jar`. To check the code against the
oldest supported API, build with `"-PpaperApiVersion=1.21.11-R0.1-SNAPSHOT"`.
