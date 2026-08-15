package dev.plex.itemizerx;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/** Bukkit API implementation; no CraftBukkit or Minecraft internals are required. */
public final class AttributeManager implements IAttributeManager
{
    public static final String[] OPERATIONS = {"add_number", "add_scalar", "multiply_scalar_1"};
    public static final String[] SLOT_GROUPS = {"any", "mainhand", "offhand", "hand", "feet", "legs", "chest", "head", "armor", "body", "saddle"};

    @Override
    public void addAttr(Player player, String[] args)
    {
        if (args.length < 4)
        {
            error(player, "/itemizer attr add <attribute> <amount> [operation] [slot-group] [modifier-key]");
            return;
        }

        Attribute attribute = Attributes.get(args[2]);
        if (attribute == null)
        {
            error(player, "Unknown attribute '" + args[2] + "'. Use /itemizer attr listall.");
            return;
        }

        double amount;
        try
        {
            amount = Double.parseDouble(args[3]);
            if (!Double.isFinite(amount))
            {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException ex)
        {
            error(player, "'" + args[3] + "' is not a finite number.");
            return;
        }

        int cursor = 4;
        Operation operation = Operation.ADD_NUMBER;
        if (args.length > cursor)
        {
            Operation parsed = parseOperation(args[cursor]);
            if (parsed != null)
            {
                operation = parsed;
                cursor++;
            }
        }

        EquipmentSlotGroup slot = EquipmentSlotGroup.ANY;
        if (args.length > cursor)
        {
            EquipmentSlotGroup parsed = parseSlot(args[cursor]);
            if (parsed != null)
            {
                slot = parsed;
                cursor++;
            }
        }

        NamespacedKey modifierKey;
        if (args.length > cursor)
        {
            modifierKey = parseKey(args[cursor]);
            if (modifierKey == null)
            {
                error(player, "Invalid modifier key. Use namespace:path or a simple name.");
                return;
            }
        }
        else
        {
            modifierKey = new NamespacedKey("itemizerx", attribute.getKey().getKey());
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
        {
            error(player, "This item cannot hold metadata.");
            return;
        }

        Collection<AttributeModifier> existing = meta.getAttributeModifiers(attribute);
        if (existing != null && existing.stream().anyMatch(modifier -> modifier.getKey().equals(modifierKey)))
        {
            error(player, "Modifier '" + modifierKey + "' already exists on " + attribute.getKey() + ".");
            return;
        }

        AttributeModifier modifier = new AttributeModifier(modifierKey, amount, operation, slot);
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
        player.sendMessage(Component.text("Added " + attribute.getKey() + " (" + amount + ", "
                + operation.name().toLowerCase(Locale.ROOT) + ", " + slot + ").", NamedTextColor.DARK_GREEN));
    }

    @Override
    public void editAttr(Player player, String[] args)
    {
        if (args.length < 3)
        {
            error(player, "/itemizer attr edit <attribute> <amount> [operation] [slot-group] [modifier-key]");
            return;
        }

        Attribute attribute = Attributes.get(args[2]);
        if (attribute == null)
        {
            error(player, "Unknown attribute '" + args[2] + "'. Use /itemizer attr listall.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        Collection<AttributeModifier> modifiers = meta == null ? null : meta.getAttributeModifiers(attribute);
        if (modifiers == null || modifiers.isEmpty())
        {
            error(player, "This item has no modifier for " + attribute.getKey() + ".");
            return;
        }
        if (args.length == 3)
        {
            player.sendMessage(Component.text("Modifiers for " + attribute.getKey() + ":", NamedTextColor.YELLOW));
            modifiers.forEach(modifier -> player.sendMessage(Component.text("- " + modifier.getKey() + " = "
                    + modifier.getAmount() + " (" + modifier.getOperation().name().toLowerCase(Locale.ROOT)
                    + ", " + modifier.getSlotGroup() + ")", NamedTextColor.YELLOW)));
            error(player, "/itemizer attr edit " + args[2] + " <amount> [operation] [slot-group] [modifier-key]");
            return;
        }

        double amount;
        try
        {
            amount = Double.parseDouble(args[3]);
            if (!Double.isFinite(amount))
            {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException ex)
        {
            error(player, "'" + args[3] + "' is not a finite number.");
            return;
        }

        int cursor = 4;
        Operation requestedOperation = null;
        EquipmentSlotGroup requestedSlot = null;
        if (args.length > cursor)
        {
            requestedOperation = parseOperation(args[cursor]);
            if (requestedOperation != null)
            {
                cursor++;
            }
        }
        if (args.length > cursor)
        {
            requestedSlot = parseSlot(args[cursor]);
            if (requestedSlot != null)
            {
                cursor++;
            }
        }

        NamespacedKey requestedKey = args.length > cursor ? parseKey(args[cursor]) : null;
        if (args.length > cursor && requestedKey == null)
        {
            error(player, "Invalid modifier key. Use namespace:path or a simple name.");
            return;
        }

        AttributeModifier original;
        if (requestedKey != null)
        {
            original = modifiers.stream().filter(modifier -> modifier.getKey().equals(requestedKey))
                    .findFirst().orElse(null);
            if (original == null)
            {
                error(player, "No modifier named '" + requestedKey + "' exists for " + attribute.getKey() + ".");
                return;
            }
        }
        else if (modifiers.size() == 1)
        {
            original = modifiers.iterator().next();
        }
        else
        {
            error(player, "This attribute has multiple modifiers; add the modifier key to select one.");
            return;
        }

        Operation operation = requestedOperation == null ? original.getOperation() : requestedOperation;
        EquipmentSlotGroup slot = requestedSlot == null ? original.getSlotGroup() : requestedSlot;
        AttributeModifier replacement = new AttributeModifier(original.getKey(), amount, operation, slot);
        meta.removeAttributeModifier(attribute, original);
        meta.addAttributeModifier(attribute, replacement);
        item.setItemMeta(meta);
        player.sendMessage(Component.text("Updated " + attribute.getKey() + " modifier " + original.getKey() + ".",
                NamedTextColor.DARK_GREEN));
    }

    @Override
    public void removeAttr(Player player, String[] args)
    {
        if (args.length < 3)
        {
            error(player, "/itemizer attr remove <attribute> [modifier-key]");
            return;
        }

        Attribute attribute = Attributes.get(args[2]);
        if (attribute == null)
        {
            error(player, "Unknown attribute '" + args[2] + "'.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
        {
            return;
        }

        boolean removed;
        if (args.length == 3)
        {
            removed = meta.removeAttributeModifier(attribute);
        }
        else
        {
            NamespacedKey key = parseKey(args[3]);
            Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
            AttributeModifier match = key == null || modifiers == null ? null : modifiers.stream()
                    .filter(modifier -> modifier.getKey().equals(key))
                    .findFirst().orElse(null);
            removed = match != null && meta.removeAttributeModifier(attribute, match);
        }

        if (!removed)
        {
            error(player, "No matching modifier exists for " + attribute.getKey() + ".");
            return;
        }

        item.setItemMeta(meta);
        player.sendMessage(Component.text("Attribute modifier removed.", NamedTextColor.DARK_GREEN));
    }

    @Override
    public void listAttr(Player player)
    {
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        Multimap<Attribute, AttributeModifier> modifiers = meta == null ? null : meta.getAttributeModifiers();
        if (modifiers == null || modifiers.isEmpty())
        {
            player.sendMessage(Component.text("This item has no custom attribute modifiers.", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Item attribute modifiers:", NamedTextColor.DARK_GREEN));
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries())
        {
            AttributeModifier modifier = entry.getValue();
            player.sendMessage(Component.text("- " + entry.getKey().getKey() + " | " + modifier.getKey()
                    + " | " + modifier.getAmount() + " | " + modifier.getOperation().name().toLowerCase(Locale.ROOT)
                    + " | " + modifier.getSlotGroup(), NamedTextColor.YELLOW));
        }
    }

    public static Operation parseOperation(String input)
    {
        return switch (input.toLowerCase(Locale.ROOT))
        {
            case "0", "add", "add_number" -> Operation.ADD_NUMBER;
            case "1", "base", "add_scalar", "multiply_base" -> Operation.ADD_SCALAR;
            case "2", "total", "multiply", "multiply_scalar_1", "multiply_total" -> Operation.MULTIPLY_SCALAR_1;
            default -> null;
        };
    }

    private static EquipmentSlotGroup parseSlot(String input)
    {
        return switch (input.toLowerCase(Locale.ROOT).replace("_", ""))
        {
            case "any" -> EquipmentSlotGroup.ANY;
            case "mainhand", "handmain" -> EquipmentSlotGroup.MAINHAND;
            case "offhand", "handoff" -> EquipmentSlotGroup.OFFHAND;
            case "hand" -> EquipmentSlotGroup.HAND;
            case "feet" -> EquipmentSlotGroup.FEET;
            case "legs" -> EquipmentSlotGroup.LEGS;
            case "chest" -> EquipmentSlotGroup.CHEST;
            case "head" -> EquipmentSlotGroup.HEAD;
            case "armor" -> EquipmentSlotGroup.ARMOR;
            case "body" -> EquipmentSlotGroup.BODY;
            case "saddle" -> EquipmentSlotGroup.SADDLE;
            default -> null;
        };
    }

    private static NamespacedKey parseKey(String input)
    {
        String normalized = input.toLowerCase(Locale.ROOT);
        return NamespacedKey.fromString(normalized.contains(":") ? normalized : "itemizerx:" + normalized);
    }

    private static void error(Player player, String message)
    {
        player.sendMessage(Component.text(message, NamedTextColor.DARK_RED));
    }
}
