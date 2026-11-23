package dev.plex.itemizerx;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class ItemizerXCommand
{
    private static final MiniMessage MM = MiniMessage.miniMessage();
    /** Formatting only: no click events, selectors, scores, or NBT, which a sign resolves at permission level 2. */
    private static final MiniMessage SAFE_MM = MiniMessage.builder().tags(TagResolver.resolver(
            StandardTags.color(), StandardTags.decorations(), StandardTags.gradient(), StandardTags.rainbow(),
            StandardTags.transition(), StandardTags.reset(), StandardTags.font(), StandardTags.hoverEvent(),
            StandardTags.keybind(), StandardTags.translatable(), StandardTags.insertion(), StandardTags.shadowColor(),
            StandardTags.pride())).build();
    private static final Set<Material> POTIONS = Set.of(Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION);
    private static final Pattern LEGACY_CODE = Pattern.compile("&[a-fk-or0-9]", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Map<String, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry("&0", "<black>"), Map.entry("&1", "<dark_blue>"), Map.entry("&2", "<dark_green>"),
            Map.entry("&3", "<dark_aqua>"), Map.entry("&4", "<dark_red>"), Map.entry("&5", "<dark_purple>"),
            Map.entry("&6", "<gold>"), Map.entry("&7", "<gray>"), Map.entry("&8", "<dark_gray>"),
            Map.entry("&9", "<blue>"), Map.entry("&a", "<green>"), Map.entry("&b", "<aqua>"),
            Map.entry("&c", "<red>"), Map.entry("&d", "<light_purple>"), Map.entry("&e", "<yellow>"),
            Map.entry("&f", "<white>"), Map.entry("&k", "<obfuscated>"), Map.entry("&l", "<bold>"),
            Map.entry("&m", "<strikethrough>"), Map.entry("&n", "<underlined>"), Map.entry("&o", "<italic>"),
            Map.entry("&r", "<reset>"));

    private static final int HELP_PAGE_SIZE = 8;
    private static final List<HelpEntry> HELP = List.of(
            new HelpEntry("name", " <<white>name<aqua>>", "Name your item", "itemizer.name"),
            new HelpEntry("id", " <<white>id<aqua>>", "Change the item's material", "itemizer.id"),
            new HelpEntry("amount", " <<white>1-99<aqua>>", "Change the stack amount", "itemizer.amount"),
            new HelpEntry("undo", "", "Undo your last item edit", "itemizer.use"),
            new HelpEntry("redo", "", "Redo an undone item edit", "itemizer.use"),
            new HelpEntry("gui", "", "Open the item dialog", "itemizer.gui"),
            new HelpEntry("inventory", "", "Open the inventory editor", "itemizer.gui"),
            new HelpEntry("lore", "", "Lore commands", "itemizer.lore.add", "itemizer.lore.remove", "itemizer.lore.change", "itemizer.lore.clear"),
            new HelpEntry("enchant", "", "Enchantment commands", "itemizer.enchant.add", "itemizer.enchant.remove", "itemizer.enchant.list", "itemizer.enchant.listall"),
            new HelpEntry("attr", "", "Attribute commands", "itemizer.attr.add", "itemizer.attr.edit", "itemizer.attr.remove", "itemizer.attr.list", "itemizer.attr.listall"),
            new HelpEntry("flag", "", "Item flag commands", "itemizer.flag.add", "itemizer.flag.remove", "itemizer.flag.list", "itemizer.flag.listall"),
            new HelpEntry("potion", "", "Potion commands", "itemizer.potion.add", "itemizer.potion.remove", "itemizer.potion.change", "itemizer.potion.color", "itemizer.potion.list"),
            new HelpEntry("property", "", "Edit item properties", "itemizer.property"),
            new HelpEntry("component", "", "Inspect or edit vanilla item components", "itemizer.component", "itemizer.component.edit", "itemizer.component.raw"),
            new HelpEntry("data", "", "Edit typed persistent custom data", "itemizer.data"),
            new HelpEntry("container", "", "Edit bundle and container contents", "itemizer.container"),
            new HelpEntry("preset", "", "Save, load, import, and export item presets", "itemizer.preset"),
            new HelpEntry("title", " <<white>name<aqua>>", "Set the book's title", "itemizer.title"),
            new HelpEntry("author", " <<white>name<aqua>>", "Set the book's author", "itemizer.author"),
            new HelpEntry("head", " <<white>name<aqua>>", "Set the player of the head", "itemizer.head"),
            new HelpEntry("sign", " <<white>line<aqua>> <<white>text<aqua>>", "Change a line of the sign you look at", "itemizer.sign"),
            new HelpEntry("clearall", "", "Clear all metadata from your item", "itemizer.clearall"));

    private final ItemizerX plugin;

    ItemizerXCommand(ItemizerX plugin)
    {
        this.plugin = plugin;
    }

    void execute(CommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            send(sender, "<aqua>ItemizerX <gold>v" + plugin.getPluginMeta().getVersion() + "<aqua> by <gold>"
                    + String.join(", ", plugin.getPluginMeta().getAuthors()));
            send(sender, "<aqua>Type <gold><click:run_command:/itemizer help>/itemizer help</click> <aqua>for help");
            return;
        }
        if (!(sender instanceof Player player))
        {
            send(sender, "<dark_red>You must be a player to execute this command!");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "help" -> help(player, args);
            case "name" -> name(player, item, args);
            case "id" -> id(player, item, args);
            case "amount" ->
            {
                if (holding(player, item))
                {
                    ItemCommands.amount(player, item, args);
                }
            }
            case "undo" -> plugin.history.undo(player);
            case "redo" -> plugin.history.redo(player);
            case "component", "components", "nbt" ->
            {
                if (args.length == 1 || holding(player, item))
                {
                    ItemCommands.component(player, item, args[0].equalsIgnoreCase("nbt") ? new String[]{"component", "nbt"} : args);
                }
            }
            case "property", "properties" ->
            {
                if (args.length == 1 || holding(player, item))
                {
                    ItemCommands.property(player, item, args);
                }
            }
            case "data", "pdc" ->
            {
                if (args.length == 1 || holding(player, item))
                {
                    ItemDataCommands.execute(player, item, args);
                }
            }
            case "container", "contents" ->
            {
                if (args.length == 1 || holding(player, item))
                {
                    ContainerCommands.execute(player, item, args);
                }
            }
            case "preset", "presets" -> plugin.presets.execute(player, args);
            case "gui", "editor", "dialog" -> plugin.dialog.open(player);
            case "inventory" -> plugin.editor.open(player);
            case "lore" -> lore(player, item, args);
            case "potion" -> potion(player, item, args);
            case "attr" -> attribute(player, item, args);
            case "flag" -> flag(player, item, args);
            case "enchant" -> enchant(player, item, args);
            case "title" -> title(player, item, args);
            case "author" -> author(player, item, args);
            case "head" -> head(player, item, args);
            case "sign" -> sign(player, args);
            case "clearall" ->
            {
                if (!denied(player, "itemizer.clearall") && holding(player, item))
                {
                    ItemCommands.clearComponents(player, item);
                    send(player, "<dark_green>All data cleared from your item");
                }
            }
            default -> unknown(player, "help");
        }
    }

    private void help(Player player, String[] args)
    {
        List<HelpEntry> entries = HELP.stream().filter(entry -> Arrays.stream(entry.permissions()).anyMatch(player::hasPermission)).toList();
        int pages = Math.max(1, (entries.size() + HELP_PAGE_SIZE - 1) / HELP_PAGE_SIZE);
        int page = 1;
        if (args.length > 1)
        {
            Integer requested = parseInt(player, args[1]);
            if (requested == null)
            {
                return;
            }
            page = Math.clamp(requested, 1, pages);
        }

        send(player, "<dark_aqua>=========<white>[<light_purple>ItemizerX Commands <white>" + page + "/" + pages + "]<dark_aqua>=========");
        entries.stream().skip((long) (page - 1) * HELP_PAGE_SIZE).limit(HELP_PAGE_SIZE)
                .forEach(entry -> send(player, "<aqua><click:suggest_command:/itemizer " + entry.command() + " >/itemizer "
                        + entry.command() + entry.arguments() + "</click> <red>- <gold>" + entry.description()));
        if (pages > 1)
        {
            String previous = page > 1
                    ? "<click:run_command:/itemizer help " + (page - 1) + "><aqua>« Previous</click>" : "<dark_gray>« Previous";
            String next = page < pages
                    ? "<click:run_command:/itemizer help " + (page + 1) + "><aqua>Next »</click>" : "<dark_gray>Next »";
            send(player, previous + " <dark_aqua>| " + next);
        }
    }

    private void name(Player player, ItemStack item, String[] args)
    {
        if (denied(player, "itemizer.name"))
        {
            return;
        }
        if (args.length == 1)
        {
            usage(player, "Name Commands", "<aqua>/itemizer name <<white>name<aqua>> <red>- <gold>Name your item");
            return;
        }
        if (!holding(player, item))
        {
            return;
        }
        Component name = colorize(StringUtils.join(args, " ", 1, args.length));
        item.editMeta(meta -> meta.displayName(name));
        send(player, "<dark_green>The name of the item in your hand has been set to <reset>'" + MM.serialize(name) + "<reset>'");
    }

    private void id(Player player, ItemStack item, String[] args)
    {
        if (denied(player, "itemizer.id"))
        {
            return;
        }
        if (args.length == 1)
        {
            usage(player, "ID Commands", "<aqua>/itemizer id <<white>id<aqua>> <red>- <gold>Change the item's material");
            return;
        }
        if (!holding(player, item))
        {
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null || !material.isItem() || material.isAir())
        {
            send(player, "<dark_red>The item <white>\"" + MM.escapeTags(args[1]) + "<white>\" <dark_red>does not exist!");
            return;
        }
        player.getInventory().setItemInMainHand(item.withType(material));
        send(player, "<dark_green>The material of the item has changed to <reset>'" + material.getKey() + "'");
    }

    private void lore(Player player, ItemStack item, String[] args)
    {
        String add = "<aqua>/itemizer lore add <<white>text<aqua>> <red>- <gold>Add a line of text to your item's lore";
        String remove = "<aqua>/itemizer lore remove <<white>index<aqua>> <red>- <gold>Remove a line of text from your item's lore";
        String change = "<aqua>/itemizer lore change <<white>index<aqua>> <<white>text<aqua>> <red>- <gold>Change a line of text in your item's lore";
        String clear = "<aqua>/itemizer lore clear <red>- <gold>Clear the item's lore";
        if (args.length == 1)
        {
            usage(player, "Lore Commands", add, remove, change, clear);
            return;
        }
        if (!holding(player, item))
        {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lines = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
        switch (args[1].toLowerCase(Locale.ROOT))
        {
            case "add" ->
            {
                if (denied(player, "itemizer.lore.add"))
                {
                    return;
                }
                if (args.length == 2)
                {
                    usage(player, "Lore Commands", add);
                    return;
                }
                Component line = colorize(StringUtils.join(args, " ", 2, args.length));
                lines.add(line);
                meta.lore(lines);
                item.setItemMeta(meta);
                send(player, "<dark_green>Line <white>'" + MM.serialize(line) + "<white>' <dark_green>added to the item's lore");
            }
            case "remove" ->
            {
                if (denied(player, "itemizer.lore.remove"))
                {
                    return;
                }
                if (args.length == 2)
                {
                    usage(player, "Lore Commands", remove);
                    return;
                }
                Integer index = loreIndex(player, lines, args[2]);
                if (index == null)
                {
                    return;
                }
                lines.remove(index - 1);
                meta.lore(lines);
                item.setItemMeta(meta);
                send(player, "<dark_green>Line <white>'" + index + "<white>' <dark_green>removed from the item's lore");
            }
            case "change" ->
            {
                if (denied(player, "itemizer.lore.change"))
                {
                    return;
                }
                if (args.length < 4)
                {
                    usage(player, "Lore Commands", change);
                    return;
                }
                Integer index = loreIndex(player, lines, args[2]);
                if (index == null)
                {
                    return;
                }
                Component line = colorize(StringUtils.join(args, " ", 3, args.length));
                lines.set(index - 1, line);
                meta.lore(lines);
                item.setItemMeta(meta);
                send(player, "<dark_green>Line <white>'" + index + "' <dark_green>has been changed to <white>'" + MM.serialize(line) + "<white>'");
            }
            case "clear" ->
            {
                if (denied(player, "itemizer.lore.clear"))
                {
                    return;
                }
                if (lines.isEmpty())
                {
                    send(player, "<dark_red>The item has no lore.");
                    return;
                }
                meta.lore(null);
                item.setItemMeta(meta);
                send(player, "<dark_green>The item's lore has been cleared!");
            }
            default -> unknown(player, "lore");
        }
    }

    private Integer loreIndex(Player player, List<Component> lines, String input)
    {
        if (lines.isEmpty())
        {
            send(player, "<yellow>This item has no lore.");
            return null;
        }
        Integer index = parseInt(player, input);
        if (index != null && (index < 1 || index > lines.size()))
        {
            send(player, "<dark_red>The item's lore doesn't have line <white>'" + index + "'");
            return null;
        }
        return index;
    }

    private void potion(Player player, ItemStack item, String[] args)
    {
        String add = "<aqua>/itemizer potion add <<white>effect<aqua>> <<white>level<aqua>> <<white>ticks<aqua>> <red>- <gold>Add a potion effect";
        String remove = "<aqua>/itemizer potion remove <<white>effect<aqua>> <red>- <gold>Remove a potion effect";
        String change = "<aqua>/itemizer potion change <<white>potion|splash_potion|lingering_potion<aqua>> <red>- <gold>Change the potion type";
        String color = "<aqua>/itemizer potion color <<white>#rrggbb<aqua>> <red>- <gold>Set the potion color";
        String list = "<aqua>/itemizer potion list <red>- <gold>List all potion effects";
        if (args.length == 1)
        {
            usage(player, "Potion Commands", add, remove, change, color, list);
            return;
        }
        if (!POTIONS.contains(item.getType()))
        {
            send(player, "<red>You do not have a potion in your hand.");
            return;
        }

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        switch (args[1].toLowerCase(Locale.ROOT))
        {
            case "add" ->
            {
                if (denied(player, "itemizer.potion.add"))
                {
                    return;
                }
                if (args.length < 5)
                {
                    usage(player, "Potion Commands", add);
                    return;
                }
                PotionEffectType type = findEffect(args[2]);
                if (type == null)
                {
                    send(player, "<dark_red>The potion effect <white>\"" + MM.escapeTags(args[2]) + "<white>\" <dark_red>does not exist!");
                    return;
                }
                Integer level = parseInt(player, args[3]);
                Integer ticks = parseInt(player, args[4]);
                if (level == null || ticks == null)
                {
                    return;
                }
                if (level < 1 || level > 256 || ticks < 1)
                {
                    send(player, "<dark_red>The level must be from 1 to 256 and the duration at least 1 tick.");
                    return;
                }
                boolean replacing = meta.hasCustomEffect(type);
                meta.addCustomEffect(new PotionEffect(type, ticks, level - 1), true);
                item.setItemMeta(meta);
                send(player, "<white>" + type.getKey() + " <dark_green>has been " + (replacing ? "changed on" : "added to") + " the potion");
            }
            case "remove" ->
            {
                if (denied(player, "itemizer.potion.remove"))
                {
                    return;
                }
                if (args.length == 2)
                {
                    usage(player, "Potion Commands", remove);
                    return;
                }
                PotionEffectType type = findEffect(args[2]);
                if (type == null)
                {
                    send(player, "<dark_red>The potion effect <white>\"" + MM.escapeTags(args[2]) + "<white>\" <dark_red>does not exist!");
                    return;
                }
                if (!meta.removeCustomEffect(type))
                {
                    send(player, "<dark_red>This potion does not have <white>" + type.getKey());
                    return;
                }
                item.setItemMeta(meta);
                send(player, "<white>" + type.getKey() + " <dark_green>has been removed from the potion");
            }
            case "change" ->
            {
                if (denied(player, "itemizer.potion.change"))
                {
                    return;
                }
                if (args.length == 2)
                {
                    usage(player, "Potion Commands", change);
                    return;
                }
                Material material = Material.matchMaterial(args[2]);
                if (material == null || !POTIONS.contains(material))
                {
                    send(player, "<white>'" + MM.escapeTags(args[2]) + "' <dark_red>is not a potion type!");
                    return;
                }
                player.getInventory().setItemInMainHand(item.withType(material));
                send(player, "<dark_green>The potion in hand has changed to <white>'" + material.getKey() + "'");
            }
            case "color" ->
            {
                if (denied(player, "itemizer.potion.color"))
                {
                    return;
                }
                if (args.length < 3)
                {
                    usage(player, "Potion Commands", color);
                    return;
                }
                Color parsed = parseColor(args[2]);
                if (parsed == null)
                {
                    send(player, "<dark_red>The color <white>\"" + MM.escapeTags(args[2]) + "<white>\" <dark_red>is not a hex color such as #ff8800!");
                    return;
                }
                meta.setColor(parsed);
                item.setItemMeta(meta);
                send(player, "<white>" + args[2] + " <dark_green>has been set as the potion color");
            }
            case "list" ->
            {
                if (!denied(player, "itemizer.potion.list"))
                {
                    send(player, "<dark_green>Available potion effects: <yellow>" + keys(Registry.MOB_EFFECT.stream()));
                }
            }
            default -> unknown(player, "potion");
        }
    }

    private void attribute(Player player, ItemStack item, String[] args)
    {
        if (args.length == 1)
        {
            usage(player, "Attribute Commands",
                    "<aqua>/itemizer attr add <<white>name<aqua>> <<white>amount<aqua>> [<white>operation<aqua>] [<white>slot group<aqua>] [<white>modifier key<aqua>] <red>- <gold>Add an attribute",
                    "<aqua>/itemizer attr edit <<white>name<aqua>> <<white>amount<aqua>> [<white>operation<aqua>] [<white>slot group<aqua>] [<white>modifier key<aqua>] <red>- <gold>Edit an attribute modifier",
                    "<aqua>/itemizer attr remove <<white>name<aqua>> [<white>modifier key<aqua>] <red>- <gold>Remove one or all matching modifiers",
                    "<aqua>/itemizer attr list <red>- <gold>List the item's attributes",
                    "<aqua>/itemizer attr listall <red>- <gold>List all supported attributes");
            return;
        }
        if (!holding(player, item))
        {
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT))
        {
            case "add" ->
            {
                if (!denied(player, "itemizer.attr.add"))
                {
                    plugin.attributes.addAttr(player, args);
                }
            }
            case "edit" ->
            {
                if (!denied(player, "itemizer.attr.edit"))
                {
                    plugin.attributes.editAttr(player, args);
                }
            }
            case "remove" ->
            {
                if (!denied(player, "itemizer.attr.remove"))
                {
                    plugin.attributes.removeAttr(player, args);
                }
            }
            case "list" ->
            {
                if (!denied(player, "itemizer.attr.list"))
                {
                    plugin.attributes.listAttr(player);
                }
            }
            case "listall" ->
            {
                if (!denied(player, "itemizer.attr.listall"))
                {
                    send(player, "<dark_green>Supported attributes: <yellow>" + Attributes.all());
                }
            }
            default -> unknown(player, "attr");
        }
    }

    private void flag(Player player, ItemStack item, String[] args)
    {
        String add = "<aqua>/itemizer flag add <<white>name<aqua>> <red>- <gold>Add a flag";
        String remove = "<aqua>/itemizer flag remove <<white>name<aqua>> <red>- <gold>Remove a flag";
        if (args.length == 1)
        {
            usage(player, "Flag Commands", add, remove,
                    "<aqua>/itemizer flag list <red>- <gold>List the item's flags",
                    "<aqua>/itemizer flag listall <red>- <gold>List all available flags");
            return;
        }
        if (!holding(player, item))
        {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        switch (args[1].toLowerCase(Locale.ROOT))
        {
            case "add", "remove" ->
            {
                boolean adding = args[1].equalsIgnoreCase("add");
                if (denied(player, adding ? "itemizer.flag.add" : "itemizer.flag.remove"))
                {
                    return;
                }
                if (args.length == 2)
                {
                    usage(player, "Flag Commands", adding ? add : remove);
                    return;
                }
                ItemFlag flag = parseFlag(args[2]);
                if (flag == null)
                {
                    send(player, "<dark_red>The flag <white>\"" + MM.escapeTags(args[2]) + "<white>\" <dark_red>does not exist!");
                    return;
                }
                String name = flag.name().toLowerCase(Locale.ROOT);
                if (meta.hasItemFlag(flag) == adding)
                {
                    send(player, adding
                            ? "<dark_red>The flag <white>'" + name + "' <dark_red>is already on the item!"
                            : "<dark_red>The flag <white>'" + name + "' <dark_red>is not on the item!");
                    return;
                }
                if (adding)
                {
                    meta.addItemFlags(flag);
                }
                else
                {
                    meta.removeItemFlags(flag);
                }
                item.setItemMeta(meta);
                send(player, "<dark_green>The flag <white>'" + name + "' <dark_green>has been " + (adding ? "added to" : "removed from") + " your item!");
            }
            case "list" ->
            {
                if (denied(player, "itemizer.flag.list"))
                {
                    return;
                }
                if (meta.getItemFlags().isEmpty())
                {
                    send(player, "<dark_red>The item in your hand does not have any flags");
                    return;
                }
                send(player, "<dark_green>Item flags: <yellow>" + flagNames(meta.getItemFlags().stream()));
            }
            case "listall" ->
            {
                if (!denied(player, "itemizer.flag.listall"))
                {
                    send(player, "<dark_green>Available item flags: <yellow>" + flagNames(Arrays.stream(ItemFlag.values())));
                }
            }
            default -> unknown(player, "flag");
        }
    }

    private void enchant(Player player, ItemStack item, String[] args)
    {
        if (args.length == 1)
        {
            usage(player, "Enchant Commands",
                    "<aqua>/itemizer enchant add <<white>name<aqua>> <<white>level<aqua>> <red>- <gold>Add an enchantment",
                    "<aqua>/itemizer enchant remove <<white>name<aqua>> <red>- <gold>Remove an enchantment",
                    "<aqua>/itemizer enchant list <red>- <gold>List the item's enchantments",
                    "<aqua>/itemizer enchant listall <red>- <gold>List all available enchantments");
            return;
        }
        if (!holding(player, item))
        {
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT))
        {
            case "add" ->
            {
                if (!denied(player, "itemizer.enchant.add"))
                {
                    plugin.enchantments.addEnchantment(player, item, args);
                }
            }
            case "remove" ->
            {
                if (!denied(player, "itemizer.enchant.remove"))
                {
                    plugin.enchantments.removeEnchantment(player, item, args);
                }
            }
            case "list" ->
            {
                if (denied(player, "itemizer.enchant.list"))
                {
                    return;
                }
                if (item.getEnchantments().isEmpty())
                {
                    send(player, "<dark_red>This item doesn't hold any enchantments");
                    return;
                }
                send(player, "<dark_green>Item enchantments: <yellow>" + item.getEnchantments().entrySet().stream()
                        .map(entry -> entry.getKey().getKey().getKey() + " " + entry.getValue())
                        .collect(Collectors.joining(", ")));
            }
            case "listall" ->
            {
                if (!denied(player, "itemizer.enchant.listall"))
                {
                    send(player, "<dark_green>Available enchantments: <yellow>"
                            + keys(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()));
                }
            }
            default -> unknown(player, "enchant");
        }
    }

    private void title(Player player, ItemStack item, String[] args)
    {
        if (denied(player, "itemizer.title"))
        {
            return;
        }
        if (args.length == 1)
        {
            usage(player, "Title Command", "<aqua>/itemizer title <<white>name<aqua>> <red>- <gold>Set the book's title");
            return;
        }
        if (item.getType() != Material.WRITTEN_BOOK)
        {
            send(player, "<red>You do not have a written book in your hand.");
            return;
        }
        Component title = colorize(StringUtils.join(args, " ", 1, args.length));
        item.editMeta(BookMeta.class, meta -> meta.title(title));
        send(player, "<dark_green>The title of the book has been set to <white>'" + MM.serialize(title) + "<white>'");
    }

    private void author(Player player, ItemStack item, String[] args)
    {
        if (denied(player, "itemizer.author"))
        {
            return;
        }
        if (args.length == 1)
        {
            usage(player, "Author Command", "<aqua>/itemizer author <<white>name<aqua>> <red>- <gold>Set the book's author");
            return;
        }
        if (item.getType() != Material.WRITTEN_BOOK)
        {
            send(player, "<red>You do not have a written book in your hand.");
            return;
        }
        Component author = colorize(StringUtils.join(args, " ", 1, args.length));
        item.editMeta(BookMeta.class, meta -> meta.author(author));
        send(player, "<dark_green>The author of the book has been set to <white>'" + MM.serialize(author) + "<white>'");
    }

    private void head(Player player, ItemStack item, String[] args)
    {
        if (denied(player, "itemizer.head"))
        {
            return;
        }
        if (args.length == 1)
        {
            usage(player, "Head Command", "<aqua>/itemizer head <<white>name<aqua>> <red>- <gold>Set the player of the head");
            return;
        }
        if (item.getType() != Material.PLAYER_HEAD)
        {
            send(player, "<red>You do not have a player head in your hand.");
            return;
        }
        String name = args[1];
        if (!PLAYER_NAME.matcher(name).matches())
        {
            send(player, "<dark_red>Player names use 1 to 16 letters, numbers, or underscores.");
            return;
        }
        // The profile holds only the name; the server fills in the skin without blocking this thread.
        item.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(Bukkit.createProfile(name)));
        send(player, "<dark_green>The player of the head has been set to <white>'" + name + "<white>'");
    }

    private void sign(Player player, String[] args)
    {
        if (denied(player, "itemizer.sign"))
        {
            return;
        }
        if (args.length < 3)
        {
            usage(player, "Sign Commands", "<aqua>/itemizer sign <<white>line<aqua>> <<white>text<aqua>> <red>- <gold>Change a line of the sign you look at");
            return;
        }
        Block block = player.getTargetBlockExact(20);
        if (block == null || !(block.getState() instanceof Sign sign))
        {
            send(player, "<red>Please look at a sign!");
            return;
        }
        Integer line = parseInt(player, args[1]);
        if (line == null)
        {
            return;
        }
        if (line < 1 || line > 4)
        {
            send(player, "<dark_red>Sign lines must be between 1 and 4");
            return;
        }

        if (sign.isWaxed())
        {
            send(player, "<dark_red>That sign is waxed, so its text can't be changed.");
            return;
        }

        // A click event on a sign runs its command at permission level 2 for whoever clicks it,
        // and selectors, scores, and NBT resolve at that level too.
        String input = StringUtils.join(args, " ", 2, args.length);
        Component text = player.hasPermission("itemizer.sign.interactive") ? colorize(input) : colorize(input, SAFE_MM);
        Side side = sign.getInteractableSideFor(player);
        List<Component> lines = new ArrayList<>(sign.getSide(side).lines());
        lines.set(line - 1, text);

        // Protection plugins can refuse the edit, and block loggers such as Oasis and CoreProtect record it.
        SignChangeEvent event = new SignChangeEvent(block, player, lines, side);
        if (!event.callEvent())
        {
            send(player, "<dark_red>You can't edit that sign.");
            return;
        }
        for (int index = 0; index < 4; index++)
        {
            sign.getSide(side).line(index, event.line(index));
        }
        sign.update();
        send(player, "<dark_green>Line <white>'" + line + "<white>'<dark_green> has been changed to <white>'"
                + MM.serialize(event.line(line - 1)) + "<white>'");
    }

    private static boolean denied(CommandSender sender, String permission)
    {
        if (sender.hasPermission(permission))
        {
            return false;
        }
        send(sender, "<dark_red>You don't have permission to use this command!");
        return true;
    }

    private static boolean holding(Player player, ItemStack item)
    {
        if (!item.getType().isAir())
        {
            return true;
        }
        send(player, "<red>You do not have an item in your hand.");
        return false;
    }

    private static void usage(CommandSender sender, String title, String... lines)
    {
        Messages.help(sender, title, lines);
    }

    private static void unknown(CommandSender sender, String command)
    {
        String path = command.equals("help") ? "/itemizer help" : "/itemizer " + command;
        send(sender, "<aqua>Unknown sub-command. Type <gold><click:run_command:" + path + ">" + path + "</click> <aqua>for help.");
    }

    private static void send(CommandSender sender, String miniMessage)
    {
        sender.sendMessage(MM.deserialize(miniMessage));
    }

    /** Reads {@code &} color codes and MiniMessage tags alike. */
    private static Component colorize(String input)
    {
        return colorize(input, MM);
    }

    private static Component colorize(String input, MiniMessage parser)
    {
        Matcher matcher = LEGACY_CODE.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find())
        {
            matcher.appendReplacement(result, Matcher.quoteReplacement(LEGACY_TAGS.get(matcher.group().toLowerCase(Locale.ROOT))));
        }
        matcher.appendTail(result);
        return parser.deserialize(result.toString());
    }

    private static Integer parseInt(CommandSender sender, String input)
    {
        try
        {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException ex)
        {
            send(sender, "<white>\"" + MM.escapeTags(input) + "<white>\"<dark_red> is not a valid number!");
            return null;
        }
    }

    private static Color parseColor(String input)
    {
        String hex = input.startsWith("#") ? input.substring(1) : input;
        if (!hex.matches("[0-9a-fA-F]{6}"))
        {
            return null;
        }
        return Color.fromRGB(Integer.parseInt(hex, 16));
    }

    private static ItemFlag parseFlag(String input)
    {
        try
        {
            return ItemFlag.valueOf(input.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    private static PotionEffectType findEffect(String input)
    {
        String value = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(value.contains(":") ? value : "minecraft:" + value);
        return key == null ? null : Registry.MOB_EFFECT.get(key);
    }

    private static String keys(java.util.stream.Stream<? extends Keyed> entries)
    {
        return entries.map(entry -> entry.getKey().getKey()).sorted().collect(Collectors.joining(", "));
    }

    private static String flagNames(java.util.stream.Stream<ItemFlag> flags)
    {
        return flags.map(flag -> flag.name().toLowerCase(Locale.ROOT)).sorted().collect(Collectors.joining(", "));
    }

    private record HelpEntry(String command, String arguments, String description, String... permissions)
    {
    }
}
