# Commands and permissions

Every command is `/itemizer` (or `/ii`, `/it`) and needs `itemizer.use` as well as the permission
listed. Commands edit the item in your main hand unless the table says otherwise. Run a command
with no arguments to see its usage in game.

Values in `<angle brackets>` are required and values in `[square brackets]` are optional. Text
arguments such as names and lore accept MiniMessage tags (`<red>`, `<bold>`) and `&` color codes.

## General

| Command | Does | Permission |
|---|---|---|
| `help [page]` | Lists the commands you have permission for | `itemizer.use` |
| `undo` | Undoes your last edit of the held item | `itemizer.use` |
| `redo` | Redoes the edit you last undid | `itemizer.use` |
| `name <name>` | Sets the item's name | `itemizer.name` |
| `id <item>` | Changes the item's material and keeps its data | `itemizer.id` |
| `amount <1-99>` | Sets the stack amount | `itemizer.amount` |
| `clearall` | Clears all of the item's metadata | `itemizer.clearall` |
| `gui` | Opens the dialog editor (aliases `dialog`, `editor`) | `itemizer.gui` |
| `inventory` | Opens the inventory editor | `itemizer.gui` |

Undo keeps up to 50 of your edits, made by command or in an editor, until you leave the server.
A command that would make the held item's data larger than 32 KiB is undone. Undo and redo refuse to run when the item in
your hand is not the one that was edited.

## Lore

| Command | Does | Permission |
|---|---|---|
| `lore add <text>` | Adds a line | `itemizer.lore.add` |
| `lore remove <line>` | Removes a line, counting from 1 | `itemizer.lore.remove` |
| `lore change <line> <text>` | Replaces a line | `itemizer.lore.change` |
| `lore clear` | Removes every line | `itemizer.lore.clear` |

## Enchantments

| Command | Does | Permission |
|---|---|---|
| `enchant add <enchantment> <1-255>` | Adds an enchantment, or changes the level of one the item has | `itemizer.enchant.add` |
| `enchant remove <enchantment>` | Removes an enchantment | `itemizer.enchant.remove` |
| `enchant list` | Lists the item's enchantments | `itemizer.enchant.list` |
| `enchant listall` | Lists every enchantment on the server, datapack ones included | `itemizer.enchant.listall` |

## Attributes

| Command | Does | Permission |
|---|---|---|
| `attr add <attribute> <amount> [operation] [slot] [key]` | Adds a modifier | `itemizer.attr.add` |
| `attr edit <attribute> <amount> [operation] [slot] [key]` | Changes a modifier; with only the attribute, lists its modifiers | `itemizer.attr.edit` |
| `attr remove <attribute> [key]` | Removes one modifier, or every modifier of the attribute | `itemizer.attr.remove` |
| `attr list` | Lists the item's modifiers | `itemizer.attr.list` |
| `attr listall` | Lists every attribute on the server | `itemizer.attr.listall` |

Operations are `add_number` (the default), `add_scalar`, and `multiply_scalar_1`. Slots are
`any` (the default), `mainhand`, `offhand`, `hand`, `feet`, `legs`, `chest`, `head`, `armor`,
`body`, and `saddle`. A key without a namespace goes in `itemizerx:`; `attr add` uses
`itemizerx:<attribute>` when you give none. An item that has several modifiers for one attribute
needs the key to `edit` one of them.

## Item flags

| Command | Does | Permission |
|---|---|---|
| `flag add <flag>` | Adds a flag, such as `hide_enchants` | `itemizer.flag.add` |
| `flag remove <flag>` | Removes a flag | `itemizer.flag.remove` |
| `flag list` | Lists the item's flags | `itemizer.flag.list` |
| `flag listall` | Lists every flag | `itemizer.flag.listall` |

## Potions

These need a potion, splash potion, or lingering potion in your hand.

| Command | Does | Permission |
|---|---|---|
| `potion add <effect> <level> <ticks>` | Adds an effect, or replaces one the potion has | `itemizer.potion.add` |
| `potion remove <effect>` | Removes an effect | `itemizer.potion.remove` |
| `potion change <potion\|splash_potion\|lingering_potion>` | Changes the potion type | `itemizer.potion.change` |
| `potion color <#rrggbb>` | Sets the color | `itemizer.potion.color` |
| `potion list` | Lists every effect on the server | `itemizer.potion.list` |

## Books, heads, and signs

| Command | Does | Permission |
|---|---|---|
| `title <title>` | Sets the title of a written book | `itemizer.title` |
| `author <author>` | Sets the author of a written book | `itemizer.author` |
| `head <player>` | Sets the player of a player head | `itemizer.head` |
| `sign <1-4> <text>` | Sets a line of the sign you look at, on the side facing you | `itemizer.sign` |

`sign` edits a block, not the held item, the way a player does: a protection plugin can refuse
it, a block logger such as Oasis or CoreProtect records it, and a waxed sign can't be changed.
Sign text keeps colors and formatting. Click events, selectors, scores, and NBT also need
`itemizer.sign.interactive`.

## Properties

All need `itemizer.property`. `property list` shows the item's values.
`property <property> <value>` sets one, and `reset` returns it to the item's default.

| Property | Value |
|---|---|
| `unbreakable`, `glider`, `hide-tooltip` | `true` or `false` |
| `glint` | `true`, `false`, or `reset` |
| `damage`, `max-damage` | a whole number; durable items only |
| `max-stack` | 1 to 99 |
| `enchantable` | the enchantability, 1 or more |
| `rarity` | `common`, `uncommon`, `rare`, or `epic` |
| `item-model`, `tooltip-style` | a namespaced key, such as `minecraft:diamond` |

## Data components

`show` and `nbt` need `itemizer.component`, `apply` and `replace` need `itemizer.component.raw`,
and the rest need `itemizer.component.edit`. An `apply` or `replace` that names `custom_data` also
needs `itemizer.data.foreign`. `nbt` on its own is short for `component nbt`.

| Command | Does |
|---|---|
| `component show` | Shows the item as `/give` syntax |
| `component nbt` | Shows the item's data as SNBT |
| `component apply <[component=value,...]>` | Sets or removes components in `/give` syntax, such as `[max_stack_size=1,!food]`; the item keeps the rest |
| `component replace <item[component=value,...]>` | Replaces the item with a complete `/give` item |
| `component reset` | Clears every component the item changed, except other plugins' data |
| `component food <nutrition> <saturation> [always-eat]` | Makes the item food |
| `component consumable <seconds> <animation> [particles] [sound]` | Sets how the item is consumed |
| `component equippable <slot> [swappable] [dispensable] [damage-on-hurt]` | Makes the item equippable |
| `component cooldown <seconds> [group]` | Sets a use cooldown |
| `component attack-range <min> <max> [creative-min] [creative-max] [margin] [mob-factor]` | Sets the attack reach |
| `component tool <speed> <damage-per-block> [creative-destroy]` | Makes the item a tool |

Each of `food`, `consumable`, `equippable`, `cooldown`, `attack-range`, and `tool` also takes
`reset`, which returns the component to the item's default, or `remove`, which removes it.

## Persistent data

All need `itemizer.data`. Keys without a namespace go in `itemizerx:`. Keys in any other
namespace also need `itemizer.data.foreign`, because they hold other plugins' data.

| Command | Does |
|---|---|
| `data list` | Lists the item's keys |
| `data get <key> <type>` | Shows a value |
| `data set <key> <type> <value>` | Sets a value, up to 4,096 characters |
| `data remove <key>` | Removes a key |

Types are `string`, `integer`, `long`, `double`, `float`, `short`, `byte`, and `boolean`.

## Containers

All need `itemizer.container` and a bundle, shulker box, or other container item in your hand.

| Command | Does |
|---|---|
| `container list` | Lists the contents by slot, from 0 |
| `container add [amount]` | Copies the item in your off hand into the container; your off hand keeps it |
| `container remove <slot>` | Empties a slot |
| `container clear` | Empties the container |

## Presets

All need `itemizer.preset`. Names use 1 to 32 letters, numbers, `_`, or `-`. You can keep 32
presets.

| Command | Does |
|---|---|
| `preset list` | Lists your presets |
| `preset save <name>` | Saves the held item |
| `preset load <name>` | Replaces the held item with the preset |
| `preset delete <name>` | Deletes a preset |
| `preset export <name>` | Gives you a token to copy; click the message to copy it |
| `preset import <name> [token]` | Saves a token from `export` as a preset; without a token, opens a box to paste it in |

## Permissions

Every permission is granted to ops by default. Each command checks only its own node, and a group
node grants every node in its group:

| Group | Grants |
|---|---|
| `itemizer.lore` | `itemizer.lore.add`, `.remove`, `.change`, `.clear` |
| `itemizer.enchant` | `itemizer.enchant.add`, `.remove`, `.list`, `.listall` |
| `itemizer.attr` | `itemizer.attr.add`, `.edit`, `.remove`, `.list`, `.listall` |
| `itemizer.flag` | `itemizer.flag.add`, `.remove`, `.list`, `.listall` |
| `itemizer.potion` | `itemizer.potion.add`, `.remove`, `.change`, `.color`, `.list` |

### Permission packs

Each pack grants the pack above it in this table and adds its own nodes. Pick the highest one you
trust a group of players with.

| Pack | Adds | What players can do with it |
|---|---|---|
| `itemizer.pack.cosmetic` | `itemizer.use`, `.name`, `.lore`, `.title`, `.author`, `.head`, `.flag`, `.sign`, `.clearall`, `.component`, and the read-only `enchant`, `attr`, and `potion` lists | Change how their items and signs look, and read an item's details. |
| `itemizer.pack.gameplay` | `itemizer.enchant`, `.attr`, `.potion`, `.property`, `.component.edit`, `.data` | Make the items they have as powerful as Minecraft allows, such as level 255 enchantments. |
| `itemizer.pack.creation` | `itemizer.id`, `.amount`, `.container`, `.preset`, `.gui` | Make any item, stack any item to 99, and copy items into containers and presets. |
| `itemizer.*` | the sensitive permissions below | Everything. |

`clearall` and `component reset` keep other plugins' data on the item unless the player also has
`itemizer.data.foreign`, so the cosmetic pack can't strip a tag such as soulbound.

### Sensitive permissions

These three are only in `itemizer.*`. Give them to staff you trust with operator powers.

| Permission | Why |
|---|---|
| `itemizer.sign.interactive` | A sign runs its click commands at permission level 2 for whoever clicks it, which covers `/give`, `/gamemode`, `/fill`, and `/kill`. |
| `itemizer.component.raw` | Builds any item the `/give` parser accepts, such as a book whose text runs a command as the staff member who clicks it, or a spawn egg that carries custom entity data. |
| `itemizer.data.foreign` | Writes other plugins' item data, which can forge their items, such as crate keys or vouchers, or strip tags they rely on. |