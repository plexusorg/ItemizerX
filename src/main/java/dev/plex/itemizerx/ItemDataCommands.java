package dev.plex.itemizerx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.Set;

final class ItemDataCommands
{
    private static final int MAX_VALUE_LENGTH = 4096;
    static final String[] TYPES = {"string", "integer", "long", "double", "float", "short", "byte", "boolean"};

    private ItemDataCommands()
    {
    }

    static boolean execute(Player player, ItemStack item, String[] args)
    {
        if (!player.hasPermission("itemizer.data"))
        {
            Messages.error(player, "You don't have permission to edit custom item data.");
            return true;
        }
        if (args.length < 2)
        {
            help(player);
            return true;
        }

        try
        {
            switch (args[1].toLowerCase(Locale.ROOT))
            {
                case "list" -> list(player, item.getPersistentDataContainer());
                case "get" ->
                {
                    if (args.length < 4)
                    {
                        Messages.error(player, "/itemizer data get <key> <type>");
                        return true;
                    }
                    NamespacedKey key = key(player, args[2]);
                    Object value = get(item.getPersistentDataContainer(), key, args[3]);
                    player.sendMessage(Component.text(key + " = " + (value == null ? "<not set>" : value), NamedTextColor.YELLOW));
                }
                case "set" ->
                {
                    if (args.length < 5)
                    {
                        Messages.error(player, "/itemizer data set <key> <type> <value>");
                        return true;
                    }
                    NamespacedKey key = key(player, args[2]);
                    String value = StringUtils.join(args, " ", 4, args.length);
                    if (value.length() > MAX_VALUE_LENGTH)
                    {
                        throw new IllegalArgumentException("Custom data values are limited to " + MAX_VALUE_LENGTH + " characters.");
                    }
                    item.editPersistentDataContainer(container -> set(container, key, args[3], value));
                    Messages.success(player, "Custom data '" + key + "' updated.");
                }
                case "remove" ->
                {
                    if (args.length < 3)
                    {
                        Messages.error(player, "/itemizer data remove <key>");
                        return true;
                    }
                    NamespacedKey key = key(player, args[2]);
                    item.editPersistentDataContainer(container -> container.remove(key));
                    Messages.success(player, "Custom data '" + key + "' removed.");
                }
                default -> help(player);
            }
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid custom data value." : ex.getMessage());
        }
        return true;
    }

    private static NamespacedKey key(Player player, String input)
    {
        String normalized = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(normalized.contains(":") ? normalized : "itemizerx:" + normalized);
        if (key == null)
        {
            throw new IllegalArgumentException("Invalid namespaced key '" + input + "'.");
        }
        if (!key.getNamespace().equals("itemizerx") && !player.hasPermission("itemizer.data.foreign"))
        {
            throw new IllegalArgumentException("Editing another plugin's namespace requires itemizer.data.foreign.");
        }
        return key;
    }

    private static void list(Player player, PersistentDataContainerView container)
    {
        Set<NamespacedKey> keys = container.getKeys();
        if (keys.isEmpty())
        {
            player.sendMessage(Component.text("This item has no persistent custom data.", NamedTextColor.YELLOW));
            return;
        }
        player.sendMessage(Component.text("Persistent item data: " + String.join(", ", keys.stream().map(Object::toString).sorted().toList()), NamedTextColor.YELLOW));
    }

    private static Object get(PersistentDataContainerView container, NamespacedKey key, String type)
    {
        return switch (type.toLowerCase(Locale.ROOT))
        {
            case "string" -> container.get(key, PersistentDataType.STRING);
            case "int", "integer" -> container.get(key, PersistentDataType.INTEGER);
            case "long" -> container.get(key, PersistentDataType.LONG);
            case "double" -> container.get(key, PersistentDataType.DOUBLE);
            case "float" -> container.get(key, PersistentDataType.FLOAT);
            case "short" -> container.get(key, PersistentDataType.SHORT);
            case "byte" -> container.get(key, PersistentDataType.BYTE);
            case "bool", "boolean" -> container.get(key, PersistentDataType.BOOLEAN);
            default -> throw new IllegalArgumentException("Unknown type. Use: " + String.join(", ", TYPES));
        };
    }

    private static void set(PersistentDataContainer container, NamespacedKey key, String type, String value)
    {
        switch (type.toLowerCase(Locale.ROOT))
        {
            case "string" -> container.set(key, PersistentDataType.STRING, value);
            case "int", "integer" -> container.set(key, PersistentDataType.INTEGER, Integer.parseInt(value));
            case "long" -> container.set(key, PersistentDataType.LONG, Long.parseLong(value));
            case "double" -> container.set(key, PersistentDataType.DOUBLE, finiteDouble(value));
            case "float" -> container.set(key, PersistentDataType.FLOAT, finiteFloat(value));
            case "short" -> container.set(key, PersistentDataType.SHORT, Short.parseShort(value));
            case "byte" -> container.set(key, PersistentDataType.BYTE, Byte.parseByte(value));
            case "bool", "boolean" -> container.set(key, PersistentDataType.BOOLEAN, parseBoolean(value));
            default -> throw new IllegalArgumentException("Unknown type. Use: " + String.join(", ", TYPES));
        }
    }

    private static double finiteDouble(String value)
    {
        double parsed = Double.parseDouble(value);
        if (!Double.isFinite(parsed)) throw new IllegalArgumentException("Value must be finite.");
        return parsed;
    }

    private static float finiteFloat(String value)
    {
        float parsed = Float.parseFloat(value);
        if (!Float.isFinite(parsed)) throw new IllegalArgumentException("Value must be finite.");
        return parsed;
    }

    private static boolean parseBoolean(String value)
    {
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException("Boolean values must be true or false.");
    }

    private static void help(Player player)
    {
        player.sendMessage(Component.text("/itemizer data list|get <key> <type>|set <key> <type> <value>|remove <key>", NamedTextColor.AQUA));
    }


}
