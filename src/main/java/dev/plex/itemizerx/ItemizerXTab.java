package dev.plex.itemizerx;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

final class ItemizerXTab
{
    static final List<String> ROOT = List.of("help", "name", "id", "amount", "undo", "redo", "lore", "potion", "attr",
            "flag", "enchant", "property", "component", "data", "container", "preset", "gui", "dialog", "inventory", "title", "author", "head", "sign", "clearall");

    private final ItemizerX plugin;

    ItemizerXTab(ItemizerX plugin)
    {
        this.plugin = plugin;
    }

    public List<String> complete(CommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            return ROOT;
        }
        Stream<String> choices = switch (args.length)
        {
            case 1 -> ROOT.stream();
            case 2 -> secondArgument(args[0]);
            case 3 -> thirdArgument(sender, args[0], args[1]);
            case 4 -> fourthArgument(args[0], args[1]);
            case 5 -> args[0].equalsIgnoreCase("attr")
                    && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("edit"))
                    ? Arrays.stream(AttributeManager.OPERATIONS) : Stream.empty();
            case 6 -> args[0].equalsIgnoreCase("attr")
                    && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("edit"))
                    ? Arrays.stream(AttributeManager.SLOT_GROUPS) : Stream.empty();
            default -> Stream.empty();
        };

        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.filter(value -> value.toLowerCase(Locale.ROOT).startsWith(current)).sorted().toList();
    }

    private static Stream<String> secondArgument(String root)
    {
        return switch (root.toLowerCase(Locale.ROOT))
        {
            case "id" -> Arrays.stream(Material.values()).filter(Material::isItem).map(material -> friendly(material.getKey()));
            case "help" -> Stream.of("1", "2", "3");
            case "lore" -> Stream.of("add", "remove", "change", "clear");
            case "potion" -> Stream.of("add", "remove", "change", "color", "list");
            case "attr" -> Stream.of("add", "edit", "remove", "list", "listall");
            case "flag", "enchant" -> Stream.of("add", "remove", "list", "listall");
            case "component", "components" -> Stream.of("show", "nbt", "apply", "replace", "reset", "food",
                    "consumable", "equippable", "cooldown", "attack-range", "tool");
            case "property", "properties" -> Arrays.stream(ItemCommands.PROPERTIES);
            case "data", "pdc" -> Stream.of("list", "get", "set", "remove");
            case "container", "contents" -> Stream.of("list", "add", "remove", "clear");
            case "preset", "presets" -> Stream.of("list", "save", "load", "delete", "export", "import");
            default -> Stream.empty();
        };
    }

    private Stream<String> thirdArgument(CommandSender sender, String root, String action)
    {
        if ((root.equalsIgnoreCase("preset") || root.equalsIgnoreCase("presets"))
                && (action.equalsIgnoreCase("load") || action.equalsIgnoreCase("delete") || action.equalsIgnoreCase("export")))
        {
            return sender instanceof Player player ? plugin.presets.names(player.getUniqueId()).stream() : Stream.empty();
        }
        if ((root.equalsIgnoreCase("data") || root.equalsIgnoreCase("pdc"))
                && (action.equalsIgnoreCase("get") || action.equalsIgnoreCase("set") || action.equalsIgnoreCase("remove")))
        {
            return sender instanceof Player player
                    ? player.getInventory().getItemInMainHand().getPersistentDataContainer().getKeys().stream().map(ItemizerXTab::friendly)
                    : Stream.empty();
        }
        if ((action.equalsIgnoreCase("add") || action.equalsIgnoreCase("edit") || action.equalsIgnoreCase("remove")))
        {
            return switch (root.toLowerCase(Locale.ROOT))
            {
                case "potion" -> Registry.MOB_EFFECT.keyStream().map(ItemizerXTab::friendly);
                case "attr" -> Registry.ATTRIBUTE.keyStream().map(ItemizerXTab::friendly);
                case "enchant" -> RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                        .keyStream().map(ItemizerXTab::friendly);
                case "flag" -> Arrays.stream(ItemFlag.values()).map(flag -> flag.name().toLowerCase(Locale.ROOT));
                default -> Stream.empty();
            };
        }

        if (root.equalsIgnoreCase("property") || root.equalsIgnoreCase("properties"))
        {
            return switch (action.toLowerCase(Locale.ROOT))
            {
                case "unbreakable", "glider", "hide-tooltip" -> Stream.of("true", "false");
                case "glint" -> Stream.of("true", "false", "reset");
                case "damage", "max-damage", "max-stack", "enchantable", "item-model", "tooltip-style" -> Stream.of("reset");
                case "rarity" -> Stream.of("common", "uncommon", "rare", "epic", "reset");
                default -> Stream.empty();
            };
        }
        if (root.equalsIgnoreCase("component") || root.equalsIgnoreCase("components"))
        {
            return switch (action.toLowerCase(Locale.ROOT))
            {
                case "food", "consumable", "cooldown", "attack-range", "tool" -> Stream.of("reset", "remove");
                case "equippable" -> Stream.of("head", "chest", "legs", "feet", "hand", "offhand", "body", "saddle", "reset", "remove");
                default -> Stream.empty();
            };
        }
        return Stream.empty();
    }

    private static Stream<String> fourthArgument(String root, String action)
    {
        if ((root.equalsIgnoreCase("data") || root.equalsIgnoreCase("pdc"))
                && (action.equalsIgnoreCase("get") || action.equalsIgnoreCase("set")))
        {
            return Arrays.stream(ItemDataCommands.TYPES);
        }
        if ((root.equalsIgnoreCase("component") || root.equalsIgnoreCase("components"))
                && action.equalsIgnoreCase("consumable"))
        {
            return Stream.of("eat", "drink", "block", "bow", "trident", "crossbow", "spyglass",
                    "toot_horn", "brush", "bundle", "spear", "none");
        }
        return Stream.empty();
    }

    private static String friendly(NamespacedKey key)
    {
        return key.getNamespace().equals(NamespacedKey.MINECRAFT) ? key.getKey() : key.toString();
    }
}
