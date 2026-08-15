# ItemizerX 3

ItemizerX is a Paper plugin for editing items in game. Version 3 targets Minecraft **26.1 through
26.2** (including 26.1.1 and 26.1.2) and requires Java 25.

## What changed in 3.0

- Removed all versioned NMS/CraftBukkit code and the paperweight/reobfuscation build.
- Converted the project to a native Paper plugin and registered `/itemizer` through Paper's
  Brigadier lifecycle API instead of Bukkit's legacy command executor.
- Attributes and enchantments are read from the running server's registries, so new vanilla and
  datapack-provided entries appear automatically.
- Attribute modifiers support all three operations, every current equipment slot group, and custom
  namespaced modifier keys.
- Added direct vanilla item-component input using Paper's `ItemFactory#createItemStack` API.
- Added convenient editors for item metadata such as max stack size, max damage, glider,
  rarity, enchantability, item model, tooltip style, and enchantment glint override.
- Added structured editors for food, consumable, equippable, cooldown, attack-range, and tool
  components.
- Added bounded delta-based undo/redo, typed persistent data, item presets, bundle/container
  editing, and an inventory editor with a live preview.
- Updated Paper, CoreProtect, bStats, Gradle plugins, the Gradle wrapper, and the Java toolchain.

## New commands

```text
/itemizer amount <1-99>
/itemizer component show
/itemizer component nbt
/itemizer component apply <[component=value,...]>
/itemizer component replace <minecraft:item[component=value,...]>
/itemizer component reset
/itemizer component food <nutrition> <saturation> [always-eat]
/itemizer component consumable <seconds> <animation> [particles] [sound]
/itemizer component equippable <slot> [swappable] [dispensable] [damage-on-hurt]
/itemizer component cooldown <seconds> [group]
/itemizer component attack-range <min> <max> [creative-min] [creative-max] [margin] [mob-factor]
/itemizer component tool <default-speed> <damage-per-block> [creative-destroy]
/itemizer property list
/itemizer property <property> <value|reset>

/itemizer attr add <attribute> <amount> [operation] [slot-group] [modifier-key]
/itemizer attr edit <attribute> <amount> [operation] [slot-group] [modifier-key]
/itemizer attr remove <attribute> [modifier-key]
/itemizer attr list
/itemizer attr listall

/itemizer undo
/itemizer redo
/itemizer data list|get|set|remove
/itemizer container list|add|remove|clear
/itemizer preset list|save|load|delete|export|import
/itemizer gui
```

Attribute operations are `add_number`, `add_scalar`, and `multiply_scalar_1`. Slot groups are
`any`, `mainhand`, `offhand`, `hand`, `feet`, `legs`, `chest`, `head`, `armor`, `body`, and
`saddle`.

`component apply` accepts the component portion of vanilla `/give` syntax. `component replace`
accepts the complete item argument. These commands expose new data components without requiring an
ItemizerX release for each component Mojang adds.

Vanilla registry completions omit the redundant `minecraft:` namespace, so commands such as
`/itemizer attr edit scale ...` can use and complete the short key. Non-vanilla namespaces remain
fully qualified.

## Safety limits

- Undo history stores reversible component-string deltas rather than full item snapshots. It is
  limited to 50 entries and 256 KiB per player, with a 32 KiB maximum individual delta.
- Undo and redo verify a SHA-256 state fingerprint and refuse to modify a different held item.
- Presets are limited to 32 per player and 2,048 server-wide. Import tokens are size-limited,
  validated by Minecraft's item parser, rate-limited, and saved in a debounced batch.
- Persistent data values are limited to 4,096 characters. Players may only write to the
  `itemizerx` namespace unless explicitly granted `itemizer.data.foreign`.
- Raw component input and chat output are size-limited. The editor inventory cancels all movement,
  drag, hotbar, and creative click paths while it is open.
- Container `add` copies the off-hand item as an intentional administrative edit; it never
  consumes or moves inventory contents, which keeps undo/redo isolated to the held item.

## Build

```powershell
.\gradlew.bat clean build
```

The same sources can be checked against the oldest published supported API with:

```powershell
.\gradlew.bat clean build "-PpaperApiVersion=26.1.1.build.29-alpha"
```

The deployable jar is `build/libs/ItemizerX-3.0.0.jar`. Development server tasks are available as
`runServer-26.1.1`, `runServer-26.1.2`, and `runServer-26.2` (Paper did not publish a downloadable
26.1 server build).
