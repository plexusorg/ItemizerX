package dev.plex.itemizerx;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.AttackRange;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

final class ItemCommands
{
    private static final int MAX_COMPONENT_INPUT = 16 * 1024;
    private static final int MAX_COMPONENT_OUTPUT = 12 * 1024;
    static final String[] PROPERTIES = {"list", "unbreakable", "glint", "glider", "hide-tooltip", "damage",
            "max-damage", "max-stack", "enchantable", "rarity", "item-model", "tooltip-style"};

    private ItemCommands()
    {
    }

    static boolean amount(Player player, ItemStack item, String[] args)
    {
        if (!player.hasPermission("itemizer.amount"))
        {
            return noPermission(player);
        }
        if (args.length < 2)
        {
            Messages.error(player, "/itemizer amount <1-99>");
            return true;
        }
        try
        {
            int amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 99)
            {
                throw new NumberFormatException();
            }
            item.setAmount(amount);
            Messages.success(player, "Item amount set to " + amount + ".");
        }
        catch (NumberFormatException ex)
        {
            Messages.error(player, "Amount must be an integer from 1 to 99.");
        }
        return true;
    }

    static boolean component(Player player, ItemStack item, String[] args)
    {
        if (!player.hasPermission("itemizer.component"))
        {
            return noPermission(player);
        }
        if (args.length < 2)
        {
            componentHelp(player);
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        try
        {
            switch (args[1].toLowerCase(Locale.ROOT))
            {
            case "show" ->
            {
                showComponents(player, item);
            }
            case "nbt" ->
            {
                showSerialized(player, meta == null ? "{}" : meta.getAsString());
            }
            case "apply" ->
            {
                if (args.length < 3)
                {
                    Messages.error(player, "/itemizer component apply <[component=value,...]>");
                    return true;
                }
                String components = StringUtils.join(args, " ", 2, args.length);
                if (!components.startsWith("["))
                {
                    Messages.error(player, "A component patch must begin with '['.");
                    return true;
                }
                replaceFromString(player, item, item.getType().getKey() + components);
            }
            case "replace" ->
            {
                if (args.length < 3)
                {
                    Messages.error(player, "/itemizer component replace <minecraft:item[components]>");
                    return true;
                }
                replaceFromString(player, item, StringUtils.join(args, " ", 2, args.length));
            }
            case "reset", "clear" ->
            {
                item.setItemMeta(null);
                Messages.success(player, "All custom item components were cleared.");
            }
            case "food" -> editFood(player, item, args);
            case "consumable" -> editConsumable(player, item, args);
            case "equippable" -> editEquippable(player, item, args);
            case "cooldown", "use-cooldown" -> editCooldown(player, item, args);
            case "attack-range", "range" -> editAttackRange(player, item, args);
            case "tool" -> editTool(player, item, args);
                default -> componentHelp(player);
            }
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid component value." : ex.getMessage());
        }
        return true;
    }

    static boolean property(Player player, ItemStack item, String[] args)
    {
        if (!player.hasPermission("itemizer.property"))
        {
            return noPermission(player);
        }
        if (args.length < 2)
        {
            propertyHelp(player);
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
        {
            Messages.error(player, "This item cannot hold metadata.");
            return true;
        }

        String property = args[1].toLowerCase(Locale.ROOT);
        if (property.equals("list"))
        {
            listProperties(player, item, meta);
            return true;
        }
        if (args.length < 3)
        {
            Messages.error(player, "/itemizer property <property> <value|reset>");
            return true;
        }

        String value = args[2];
        boolean reset = value.equalsIgnoreCase("reset") || value.equalsIgnoreCase("default");
        try
        {
            switch (property)
            {
                case "unbreakable" -> meta.setUnbreakable(parseBoolean(value));
                case "glint" -> meta.setEnchantmentGlintOverride(reset ? null : parseBoolean(value));
                case "glider" -> meta.setGlider(parseBoolean(value));
                case "hide-tooltip" -> meta.setHideTooltip(parseBoolean(value));
                case "max-stack" -> meta.setMaxStackSize(reset ? null : parseRange(value, 1, 99));
                case "enchantable" -> meta.setEnchantable(reset ? null : parseRange(value, 1, Integer.MAX_VALUE));
                case "rarity" -> meta.setRarity(reset ? null : ItemRarity.valueOf(value.toUpperCase(Locale.ROOT)));
                case "item-model" -> meta.setItemModel(reset ? null : requireKey(value));
                case "tooltip-style" -> meta.setTooltipStyle(reset ? null : requireKey(value));
                case "damage" ->
                {
                    Damageable damageable = requireDamageable(meta);
                    if (reset)
                    {
                        damageable.resetDamage();
                    }
                    else
                    {
                        damageable.setDamage(parseRange(value, 0, Integer.MAX_VALUE));
                    }
                }
                case "max-damage" -> requireDamageable(meta).setMaxDamage(reset ? null : parseRange(value, 1, Integer.MAX_VALUE));
                default ->
                {
                    propertyHelp(player);
                    return true;
                }
            }
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid value '" + value + "'." : ex.getMessage());
            return true;
        }

        item.setItemMeta(meta);
        Messages.success(player, "Property '" + property + "' updated.");
        return true;
    }

    private static void replaceFromString(Player player, ItemStack oldItem, String input)
    {
        if (input.length() > MAX_COMPONENT_INPUT)
        {
            Messages.error(player, "Item component input is limited to " + MAX_COMPONENT_INPUT + " characters.");
            return;
        }
        try
        {
            ItemStack replacement = Bukkit.getItemFactory().createItemStack(input);
            replacement.setAmount(oldItem.getAmount());
            player.getInventory().setItemInMainHand(replacement);
            Messages.success(player, "Item components updated.");
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, "Minecraft rejected that item syntax: " + ex.getMessage());
        }
    }

    private static void showSerialized(Player player, String value)
    {
        if (value.length() > MAX_COMPONENT_OUTPUT)
        {
            Messages.error(player, "This item is too large to display safely in chat.");
            return;
        }
        player.sendMessage(Component.text(value, NamedTextColor.YELLOW));
    }

    static void showComponents(Player player, ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        showSerialized(player, item.getType().getKey() + (meta == null ? "[]" : meta.getAsComponentString()));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editFood(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.FOOD, "food"))
        {
            return;
        }
        if (args.length < 4)
        {
            Messages.error(player, "/itemizer component food <nutrition> <saturation> [always-eat]");
            return;
        }
        int nutrition = parseRange(args[2], 0, 100);
        float saturation = parseFloat(args[3], 0, 100, "Saturation");
        FoodProperties existing = item.getData(DataComponentTypes.FOOD);
        FoodProperties.Builder builder = existing == null ? FoodProperties.food() : existing.toBuilder();
        builder.nutrition(nutrition).saturation(saturation);
        if (args.length > 4)
        {
            builder.canAlwaysEat(parseBoolean(args[4]));
        }
        item.setData(DataComponentTypes.FOOD, builder);
        Messages.success(player, "Food properties updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editConsumable(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.CONSUMABLE, "consumable"))
        {
            return;
        }
        if (args.length < 4)
        {
            Messages.error(player, "/itemizer component consumable <seconds> <animation> [particles] [sound]");
            return;
        }
        float seconds = parseFloat(args[2], 0, 3600, "Consume time");
        ItemUseAnimation animation = ItemUseAnimation.valueOf(args[3].toUpperCase(Locale.ROOT).replace('-', '_'));
        Consumable existing = item.getData(DataComponentTypes.CONSUMABLE);
        Consumable.Builder builder = existing == null ? Consumable.consumable() : existing.toBuilder();
        builder.consumeSeconds(seconds).animation(animation);
        if (args.length > 4)
        {
            builder.hasConsumeParticles(parseBoolean(args[4]));
        }
        if (args.length > 5)
        {
            builder.sound(Key.key(normalizeKey(args[5])));
        }
        item.setData(DataComponentTypes.CONSUMABLE, builder);
        Messages.success(player, "Consumable behavior updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editEquippable(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.EQUIPPABLE, "equippable"))
        {
            return;
        }
        if (args.length < 3)
        {
            Messages.error(player, "/itemizer component equippable <slot> [swappable] [dispensable] [damage-on-hurt]");
            return;
        }
        EquipmentSlot slot = parseEquipmentSlot(args[2]);
        Equippable.Builder builder = Equippable.equippable(slot);
        if (args.length > 3)
        {
            builder.swappable(parseBoolean(args[3]));
        }
        if (args.length > 4)
        {
            builder.dispensable(parseBoolean(args[4]));
        }
        if (args.length > 5)
        {
            builder.damageOnHurt(parseBoolean(args[5]));
        }
        item.setData(DataComponentTypes.EQUIPPABLE, builder);
        Messages.success(player, "Equippable behavior updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editCooldown(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.USE_COOLDOWN, "cooldown"))
        {
            return;
        }
        if (args.length < 3)
        {
            Messages.error(player, "/itemizer component cooldown <seconds> [group]");
            return;
        }
        UseCooldown.Builder builder = UseCooldown.useCooldown(parseFloat(args[2], 0, 3600, "Cooldown"));
        if (args.length > 3)
        {
            builder.cooldownGroup(Key.key(normalizeKey(args[3])));
        }
        item.setData(DataComponentTypes.USE_COOLDOWN, builder);
        Messages.success(player, "Use cooldown updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editAttackRange(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.ATTACK_RANGE, "attack-range"))
        {
            return;
        }
        if (args.length < 4)
        {
            Messages.error(player, "/itemizer component attack-range <min> <max> [creative-min] [creative-max] [margin] [mob-factor]");
            return;
        }
        float minimum = parseFloat(args[2], 0, 128, "Minimum range");
        float maximum = parseFloat(args[3], minimum, 128, "Maximum range");
        AttackRange.Builder builder = AttackRange.attackRange().minReach(minimum).maxReach(maximum);
        if (args.length > 4) builder.minCreativeReach(parseFloat(args[4], 0, 128, "Creative minimum"));
        if (args.length > 5) builder.maxCreativeReach(parseFloat(args[5], 0, 128, "Creative maximum"));
        if (args.length > 6) builder.hitboxMargin(parseFloat(args[6], 0, 16, "Hitbox margin"));
        if (args.length > 7) builder.mobFactor(parseFloat(args[7], 0, 16, "Mob factor"));
        item.setData(DataComponentTypes.ATTACK_RANGE, builder);
        Messages.success(player, "Attack range updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void editTool(Player player, ItemStack item, String[] args)
    {
        if (resetComponent(player, item, args, DataComponentTypes.TOOL, "tool"))
        {
            return;
        }
        if (args.length < 4)
        {
            Messages.error(player, "/itemizer component tool <default-speed> <damage-per-block> [creative-destroy]");
            return;
        }
        Tool.Builder builder = Tool.tool()
                .defaultMiningSpeed(parseFloat(args[2], 0, 1024, "Mining speed"))
                .damagePerBlock(parseRange(args[3], 0, 1000));
        if (args.length > 4)
        {
            builder.canDestroyBlocksInCreative(parseBoolean(args[4]));
        }
        item.setData(DataComponentTypes.TOOL, builder);
        Messages.success(player, "Tool behavior updated.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static boolean resetComponent(Player player, ItemStack item, String[] args,
                                          DataComponentType type, String name)
    {
        if (args.length < 3 || !(args[2].equalsIgnoreCase("reset") || args[2].equalsIgnoreCase("remove")))
        {
            return false;
        }
        if (args[2].equalsIgnoreCase("reset"))
        {
            item.resetData(type);
        }
        else
        {
            item.unsetData(type);
        }
        Messages.success(player, name + " component " + args[2].toLowerCase(Locale.ROOT) + ".");
        return true;
    }

    private static void listProperties(Player player, ItemStack item, ItemMeta meta)
    {
        player.sendMessage(Component.text("Properties for " + item.getType().getKey() + ":", NamedTextColor.DARK_GREEN));
        player.sendMessage(Component.text("unbreakable=" + meta.isUnbreakable()
                + ", glint=" + (meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : "default")
                + ", glider=" + meta.isGlider() + ", hide-tooltip=" + meta.isHideTooltip(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("max-stack=" + (meta.hasMaxStackSize() ? meta.getMaxStackSize() : "default")
                + ", enchantable=" + (meta.hasEnchantable() ? meta.getEnchantable() : "default")
                + ", rarity=" + (meta.hasRarity() ? meta.getRarity().name().toLowerCase(Locale.ROOT) : "default"), NamedTextColor.YELLOW));
        if (meta instanceof Damageable damageable)
        {
            player.sendMessage(Component.text("damage=" + (damageable.hasDamageValue() ? damageable.getDamage() : "default")
                    + ", max-damage=" + (damageable.hasMaxDamage() ? damageable.getMaxDamage() : "default"), NamedTextColor.YELLOW));
        }
        player.sendMessage(Component.text("item-model=" + (meta.hasItemModel() ? meta.getItemModel() : "default")
                + ", tooltip-style=" + (meta.hasTooltipStyle() ? meta.getTooltipStyle() : "default"), NamedTextColor.YELLOW));
    }

    /** Whether the item shows the glint: its override, or otherwise whether it is enchanted. */
    static boolean hasGlint(ItemMeta meta)
    {
        if (meta == null)
        {
            return false;
        }
        return meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : meta.hasEnchants();
    }

    private static Damageable requireDamageable(ItemMeta meta)
    {
        if (meta instanceof Damageable damageable)
        {
            return damageable;
        }
        throw new IllegalArgumentException("This item does not support durability.");
    }

    private static boolean parseBoolean(String value)
    {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on"))
        {
            return true;
        }
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off"))
        {
            return false;
        }
        throw new IllegalArgumentException("Use true or false.");
    }

    private static int parseRange(String value, int minimum, int maximum)
    {
        try
        {
            int parsed = Integer.parseInt(value);
            if (parsed < minimum || parsed > maximum)
            {
                throw new NumberFormatException();
            }
            return parsed;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Value must be an integer from " + minimum + " to " + maximum + ".");
        }
    }

    private static float parseFloat(String value, float minimum, float maximum, String label)
    {
        try
        {
            float parsed = Float.parseFloat(value);
            if (!Float.isFinite(parsed) || parsed < minimum || parsed > maximum)
            {
                throw new NumberFormatException();
            }
            return parsed;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException(label + " must be from " + minimum + " to " + maximum + ".");
        }
    }

    private static EquipmentSlot parseEquipmentSlot(String input)
    {
        String slot = input.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        return switch (slot)
        {
            case "hand", "mainhand" -> EquipmentSlot.HAND;
            case "offhand" -> EquipmentSlot.OFF_HAND;
            case "feet", "boots" -> EquipmentSlot.FEET;
            case "legs", "leggings" -> EquipmentSlot.LEGS;
            case "chest", "chestplate" -> EquipmentSlot.CHEST;
            case "head", "helmet" -> EquipmentSlot.HEAD;
            case "body" -> EquipmentSlot.BODY;
            case "saddle" -> EquipmentSlot.SADDLE;
            default -> throw new IllegalArgumentException("Unknown equipment slot '" + input + "'.");
        };
    }

    private static String normalizeKey(String value)
    {
        return value.contains(":") ? value.toLowerCase(Locale.ROOT) : "minecraft:" + value.toLowerCase(Locale.ROOT);
    }

    private static NamespacedKey requireKey(String value)
    {
        NamespacedKey key = NamespacedKey.fromString(value.contains(":") ? value.toLowerCase(Locale.ROOT) : "minecraft:" + value.toLowerCase(Locale.ROOT));
        if (key == null)
        {
            throw new IllegalArgumentException("Invalid namespaced key '" + value + "'.");
        }
        return key;
    }

    private static void componentHelp(CommandSender sender)
    {
        Messages.help(sender, "Component Commands",
                Messages.command("component show", "Show the item as /give syntax"),
                Messages.command("component nbt", "Show the item's data as SNBT"),
                Messages.command("component apply <[component=value,...]>", "Apply components"),
                Messages.command("component replace <item[component=value,...]>", "Replace the item"),
                Messages.command("component reset", "Clear every changed component"),
                Messages.command("component food <nutrition> <saturation> [always-eat]", "Make the item food"),
                Messages.command("component consumable <seconds> <animation> [particles] [sound]", "Set how it is consumed"),
                Messages.command("component equippable <slot> [swappable] [dispensable] [damage-on-hurt]", "Make it equippable"),
                Messages.command("component cooldown <seconds> [group]", "Set a use cooldown"),
                Messages.command("component attack-range <min> <max> [creative-min] [creative-max] [margin] [mob-factor]", "Set the attack reach"),
                Messages.command("component tool <speed> <damage-per-block> [creative-destroy]", "Make it a tool"),
                "<gray>The last six also take reset or remove.");
    }

    private static void propertyHelp(CommandSender sender)
    {
        Messages.help(sender, "Property Commands",
                Messages.command("property list", "Show the item's properties"),
                Messages.command("property <unbreakable|glider|hide-tooltip> <true|false>", "Toggle a property"),
                Messages.command("property glint <true|false|reset>", "Force or hide the enchantment glint"),
                Messages.command("property <damage|max-damage> <amount|reset>", "Set durability"),
                Messages.command("property max-stack <1-99|reset>", "Set the max stack size"),
                Messages.command("property enchantable <value|reset>", "Set the enchantability"),
                Messages.command("property rarity <common|uncommon|rare|epic|reset>", "Set the rarity"),
                Messages.command("property <item-model|tooltip-style> <key|reset>", "Set the model or tooltip style"));
    }

    private static boolean noPermission(CommandSender sender)
    {
        Messages.error(sender, "You don't have permission to use this command.");
        return true;
    }


}

