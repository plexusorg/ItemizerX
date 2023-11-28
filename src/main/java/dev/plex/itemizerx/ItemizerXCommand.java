package dev.plex.itemizerx;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
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
import java.util.Objects;

public class ItemizerXCommand implements CommandExecutor, ItemizerXBase
{
    final List<Material> POTIONS = Arrays.asList(Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION);
    final Map<String, String> COLOR_TRANSLATION = new HashMap<>(){{
        put("&a", "<green>");
        put("&b", "<aqua>");
        put("&c", "<red>");
        put("&d", "<light_purple>");
        put("&e", "<yellow>");
        put("&f", "<white>");
        put("&k", "<obfuscated>");
        put("&l", "<bold>");
        put("&m", "<strikethrough>");
        put("&n", "<underlined>");
        put("&o", "<italic>");
        put("&r", "<reset>");
        put("&1", "<dark_blue>");
        put("&2", "<dark_green>");
        put("&3", "<dark_aqua>");
        put("&4", "<dark_red>");
        put("&5", "<dark_purple>");
        put("&6", "<gold>");
        put("&7", "<gray>");
        put("&8", "<dark_gray>");
        put("&9", "<blue>");
        put("&0", "<black>");
    }};
    CoreProtectBridge cpb = new CoreProtectBridge();
    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args)
    {
        if (!sender.hasPermission("itemizer.use"))
        {
            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(mm.deserialize("<aqua>ItemizerX <gold>v" + plugin.getDescription().getVersion() + "<aqua> by <gold>" + StringUtils.join(plugin.getDescription().getAuthors(), ", ")));
            sender.sendMessage(mm.deserialize("<aqua>Type <gold><click:run_command:/itemizer help>/itemizer help</click> <aqua>for help"));
            return true;
        }

        if (!(sender instanceof final Player player))
        {
            sender.sendMessage(mm.deserialize("<dark_red>You must be a player to execute this command!"));
            return true;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        final boolean hasItem = item.getType() != Material.AIR;
        final boolean hasPotion = POTIONS.contains(item.getType());
        final boolean hasBook = item.getType() == Material.WRITTEN_BOOK;
        final ItemMeta meta = item.getItemMeta();

        switch (args[0])
        {
            case "help" ->
            {
                sender.sendMessage(mm.deserialize("""
                        <dark_aqua>=============<white>[<light_purple>ItemizerX Commands<white>]<dark_aqua>=============
                        <aqua>/itemizer name <<white>name<aqua>> <red>- <gold>Name your item
                        <aqua>/itemizer id <<white>id<aqua>> <red>- <gold>Change the item's material
                        <aqua>/itemizer lore <red>- <gold>Lore editing command
                        <aqua>/itemizer potion <red>- <gold>Potion editing command
                        <aqua>/itemizer attr <red>- <gold>Attribute editing command
                        <aqua>/itemizer flag <red>- <gold>Flag editing command
                        <aqua>/itemizer enchant <red>- <gold>Enchant editing command
                        <aqua>/itemizer title <<white>name<aqua>> <red>- <gold>Set the book's title
                        <aqua>/itemizer author <<white>name<aqua>> <red>- <gold>Set the book's author
                        <aqua>/itemizer head <<white>name<aqua>> <red>- <gold>Set the player of the head
                        <aqua>/itemizer sign <<white>line<aqua>> <<white>text<aqua>> <red>- <gold>Change the line on the sign
                        <aqua>/itemizer clearall <red>- <gold>Clears all metadata from your item"""));
                return true;
            }
            case "name" ->
            {
                if (!sender.hasPermission("itemizer.name"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Name Commands<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer name <<white>name<aqua>> <red>- <gold>Name your item"));
                }
                else
                {
                    if (!hasItem)
                    {
                        sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                        return true;
                    }
                    Component name = colorize(StringUtils.join(args, " ", 1, args.length));
                    assert meta != null;
                    meta.displayName(name);
                    item.setItemMeta(meta);
                    sender.sendMessage(mm.deserialize("<dark_green>The name of the item in your hand has been set to <reset>'" + mm.serialize(name) + "<reset>'"));
                }
                return true;
            }
            case "id" ->
            {
                if (!sender.hasPermission("itemizer.id"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>ID Commands<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer id <<white>id<aqua>> <red>- <gold>Change the item's material"));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                Material material = Material.matchMaterial(args[1].toUpperCase());
                if (material == null)
                {
                    sender.sendMessage(mm.deserialize("<dark_red>The material <white>\"" + args[1] + "<white>\" <dark_red>does not exist!"));
                    return true;
                }
                item.setType(material);
                sender.sendMessage(mm.deserialize("<dark_green>The material of the item has changed to <reset>'" + material.name() + "'"));
                return true;
            }
            case "lore" ->
            {
                if (!sender.hasPermission("itemizer.lore"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("""
                            <dark_aqua>===============<white>[<light_purple>Lore Commands<white>]<dark_aqua>===============
                            <aqua>/itemizer lore add <<white>text<aqua>> <red>- <gold>Add a line of text to your item's lore
                            <aqua>/itemizer lore remove <<white>index<aqua>> <red>- <gold>Remove a line of text from your item's lore
                            <aqua>/itemizer lore change <<white>index<aqua>> <<white>text<aqua>> <red>- <gold>Change a line of text in your item's lore
                            <aqua>/itemizer lore clear <red>- <gold>Clear the item's lore"""));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                else
                {
                    switch (args[1])
                    {
                        case "add" ->
                        {
                            if (!sender.hasPermission("itemizer.lore.add"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length == 2)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Lore Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer lore add <<white>text<aqua>> <red>- <gold>Add a line of text to your item's lore"));
                                return true;
                            }
                            Component lore = colorize(StringUtils.join(args, " ", 2, args.length));
                            assert meta != null;
                            List<Component> lores = new ArrayList<>();
                            if (meta.lore() != null)
                            {
                                lores = meta.lore();
                            }
                            lores.add(lore);
                            meta.lore(lores);
                            item.setItemMeta(meta);
                            sender.sendMessage(mm.deserialize("<dark_green>Line <white>'" + mm.serialize(lore) + "<white>' <dark_green>added to the item's lore"));
                            return true;
                        }
                        case "remove" ->
                        {
                            if (!sender.hasPermission("itemizer.lore.remove"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length == 2)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Lore Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer lore remove <<white>index<aqua>> <red>- <gold>Remove a line of text from your item's lore"));
                                return true;
                            }
                            Integer index = parseInt(sender, args[2]);
                            if (index == null)
                            {
                                return true;
                            }
                            assert meta != null;
                            List<Component> lores;
                            if (meta.lore() != null)
                            {
                                lores = meta.lore();
                            }
                            else
                            {
                                sender.sendMessage(mm.deserialize("<yellow>This item has no lores."));
                                return true;
                            }
                            if (index > lores.size())
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The item's lore doesn't have line <white>'" + index + "'"));
                                return true;
                            }
                            lores.remove(index - 1);
                            meta.lore(lores);
                            item.setItemMeta(meta);
                            sender.sendMessage(mm.deserialize("<dark_green>Line <white>'" + index + "<white>' <dark_green>removed from the item's lore"));
                            return true;
                        }
                        case "change" ->
                        {
                            if (!sender.hasPermission("itemizer.lore.change"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length < 4)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Lore Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer lore change <<white>index<aqua>> <<white>text<aqua>> <red>- <gold>Change a line of text in your item's lore"));
                                return true;
                            }
                            Integer index = parseInt(sender, args[2]);
                            if (index == null)
                            {
                                return true;
                            }
                            Component lore = colorize(StringUtils.join(args, " ", 3, args.length));
                            assert meta != null;
                            List<Component> lores;
                            if (meta.lore() != null)
                            {
                                lores = meta.lore();
                            }
                            else
                            {
                                sender.sendMessage(mm.deserialize("<yellow>This item has no lores."));
                                return true;
                            }
                            if (index > lores.size())
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The item's lore doesn't have line <white>'" + index + "'"));
                                return true;
                            }
                            lores.set(index - 1, lore);
                            meta.lore(lores);
                            item.setItemMeta(meta);
                            sender.sendMessage(mm.deserialize("<dark_green>Line <white>'" + index + "' <dark_green>has been changed to <white>'" + mm.serialize(lore) + "<white>'"));
                            return true;
                        }
                        case "clear" ->
                        {
                            if (!sender.hasPermission("itemizer.lore.clear"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            assert meta != null;
                            if (meta.lore() == null || meta.lore().isEmpty())
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The item has no lores."));
                                return true;
                            }
                            meta.lore(null);
                            item.setItemMeta(meta);
                            sender.sendMessage(mm.deserialize("<dark_green>The item's lore has been cleared!"));
                            return true;
                        }
                        default ->
                        {
                            sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer lore</click> <aqua>for help."));
                            return true;
                        }
                    }
                }
            }
            case "potion" ->
            {
                if (!sender.hasPermission("itemizer.potion"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("""
                            <dark_aqua>===============<white>[<light_purple>Potion Commands<white>]<dark_aqua>===============
                            <aqua>/itemizer potion add <<white>effect<aqua>> <<white>level<aqua>> <<white>time[tick]<aqua>> <red>- <gold>Add a potion effect
                            <aqua>/itemizer potion remove <<white>effect<aqua>> <red>- <gold>Remove a potion effect
                            <aqua>/itemizer potion change <<white>name<aqua>> <red>- <gold>Change the potion type
                            <aqua>/itemizer potion color <<white>hexcolor<aqua>> <red>- <gold>Set the potion color
                            <aqua>/itemizer potion list <red>- <gold>List all potion effects"""));
                    return true;
                }
                if (!hasPotion)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have a potion in your hand."));
                    return true;
                }
                else
                {
                    switch (args[1])
                    {
                        case "add" ->
                        {
                            if (!sender.hasPermission("itemizer.potion.add"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length < 5)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Potion Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer potion add <<white>effect<aqua>> <<white>level<aqua>> <<white>time[tick]<aqua>> <red>- <gold>Add a potion effect"));
                                return true;
                            }
                            PotionEffectType potType = PotionEffectType.getByName(args[2].toUpperCase());
                            if (potType == null)
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The potion <white>\"" + args[2] + "<white>\" <dark_red>does not exist!"));
                                return true;
                            }
                            Integer level = parseInt(sender, args[3]);
                            Integer tick = parseInt(sender, args[4]);
                            if (level == null || tick == null)
                            {
                                return true;
                            }
                            final PotionEffect pot = new PotionEffect(potType, tick, level);
                            final PotionMeta potionMeta = (PotionMeta) meta;
                            assert potionMeta != null;
                            if (potionMeta.hasCustomEffect(pot.getType()))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>This potion already has <white>" + pot.getType().getName()));
                                return true;
                            }
                            potionMeta.addCustomEffect(pot, false);
                            item.setItemMeta(potionMeta);
                            sender.sendMessage(mm.deserialize(pot.getType().getName() + " <dark_green>has been added to the potion"));
                            return true;
                        }
                        case "remove" ->
                        {
                            if (!sender.hasPermission("itemizer.potion.remove"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length == 2)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Potion Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer potion remove <<white>effect<aqua>> <red>- <gold>Remove a potion effect"));
                                return true;
                            }
                            PotionEffectType potType = PotionEffectType.getByName(args[2].toUpperCase());
                            if (potType == null)
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The potion effect <white>\"" + args[2] + "<white>\" <dark_red>does not exist!"));
                                return true;
                            }
                            final PotionMeta potionMeta = (PotionMeta) meta;
                            assert potionMeta != null;
                            if (!potionMeta.hasCustomEffect(potType))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>This potion does not have <white>" + potType.getName()));
                                return true;
                            }
                            potionMeta.removeCustomEffect(potType);
                            item.setItemMeta(potionMeta);
                            sender.sendMessage(mm.deserialize(potType.getName() + " <dark_green>has been removed from the potion"));
                            return true;
                        }
                        case "change" ->
                        {
                            if (!sender.hasPermission("itemizer.potion.change"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length == 2)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Potion Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer potion change <<white>name<aqua>> <red>- <gold>Change the potion type"));
                                return true;
                            }
                            Material material = Material.matchMaterial(args[2]);
                            if (material == null || !POTIONS.contains(material))
                            {
                                sender.sendMessage(mm.deserialize(material != null ? "<white>'" + material.name() + "' <dark_red>is not a potion type!" : "<dark_red>That material doesn't exist!"));
                                return true;
                            }
                            item.setType(material);
                            sender.sendMessage(mm.deserialize("<dark_green>The potion in hand has changed to <white>'" + material.name() + "'"));
                            return true;
                        }
                        case "color" ->
                        {
                            if (!sender.hasPermission("itemizer.potion.color"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            if (args.length < 3)
                            {
                                sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Potion Commands<white>]<dark_aqua>==============="));
                                sender.sendMessage(mm.deserialize("<aqua>/itemizer potion color <<white>hexcolor<aqua>> <red>- <gold>Set the potion color"));
                                return true;
                            }
                            final PotionMeta potionMeta = (PotionMeta) meta;
                            assert potionMeta != null;
                            try
                            {
                                java.awt.Color awtColor = java.awt.Color.decode(args[2]);
                                Color color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
                                potionMeta.setColor(color);
                                item.setItemMeta(potionMeta);
                                sender.sendMessage(mm.deserialize(args[2] + " <dark_green>has been set as potion color"));
                            }
                            catch (NumberFormatException ignored)
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>The hex <white>\"" + args[2] + "<white>\" <dark_red>is invalid!"));
                                return true;
                            }
                            return true;
                        }
                        case "list" ->
                        {
                            if (!sender.hasPermission("itemizer.potion.list"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            StringBuilder sb = new StringBuilder();
                            PotionEffectType[] effects;
                            for (int i = 0; i < (effects = PotionEffectType.values()).length; i++)
                            {
                                sb.append(", ").append(effects[i].getName());
                            }
                            sender.sendMessage(mm.deserialize("<dark_green>Available potion effects: <yellow>" + sb.toString().replaceFirst(", ", "")));
                            return true;
                        }
                        default ->
                        {
                            sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer potion>/itemizer potion</click> <aqua>for help."));
                            return true;
                        }
                    }
                }
            }
            case "attr" ->
            {
                if (!sender.hasPermission("itemizer.attr"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("""
                            <dark_aqua>===============<white>[<light_purple>Attribute Commands<white>]<dark_aqua>===============
                            <aqua>/itemizer attr add <<white>name<aqua>> <<white>strength<aqua>> [<white>slot<aqua>] <red>- <gold>Add an attribute
                            <aqua>/itemizer attr remove <<white>name<aqua>> <red>- <gold>Remove an attribute
                            <aqua>/itemizer attr list <red>- <gold>List all item's attributes
                            <aqua>/itemizer attr listall <red>- <gold>List all supported attributes"""));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                else
                {
                    switch (args[1])
                    {
                        case "add" ->
                        {
                            if (!sender.hasPermission("itemizer.attr.add"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            plugin.attr.addAttr(player, args);
                            return true;
                        }
                        case "remove" ->
                        {
                            if (!sender.hasPermission("itemizer.attr.remove"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            // NPE to fix here
                            plugin.attr.removeAttr(player, args[2]);
                            return true;
                        }
                        case "list" ->
                        {
                            if (!sender.hasPermission("itemizer.attr.list"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            plugin.attr.listAttr(player);
                            return true;
                        }
                        case "listall" ->
                        {
                            if (!sender.hasPermission("itemizer.attr.listall"))
                            {
                                sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                                return true;
                            }
                            sender.sendMessage(mm.deserialize("<dark_green>Supported attributes: <yellow>" + Attributes.getAttributes()));
                            return true;
                        }
                        default ->
                        {
                            sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer attr>/itemizer attr</click> <aqua>for help."));
                            return true;
                        }
                    }
                }
            }
            case "flag" ->
            {
                if (!sender.hasPermission("itemizer.flag"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("""
                            <dark_aqua>===============<white>[<light_purple>Flag Commands<white>]<dark_aqua>===============
                            <aqua>/itemizer flag add <<white>name<aqua> <red>- <gold>Add a flag
                            <aqua>/itemizer flag remove <<white>name<aqua> <red>- <gold>Remove a flag
                            <aqua>/itemizer flag list <red>- <gold>List all item's flags
                            <aqua>/itemizer flag listall <red>- <gold>List all available flags"""));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                switch (args[1])
                {
                    case "add" ->
                    {
                        if (!sender.hasPermission("itemizer.flag.add"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        if (args.length == 2)
                        {
                            sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Flag Commands<white>]<dark_aqua>==============="));
                            sender.sendMessage(mm.deserialize("<aqua>/itemizer flag add <<white>name<aqua> <red>- <gold>Add a flag"));
                            return true;
                        }
                        ItemFlag flag = null;
                        try
                        {
                            flag = ItemFlag.valueOf(args[2].toUpperCase());
                        }
                        catch (Exception ignored)
                        {
                        }
                        if (flag == null)
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The flag <white>\"" + args[2] + "<white>\" does not exist!"));
                            return true;
                        }
                        assert meta != null;
                        if (meta.getItemFlags().contains(flag))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The flag <white>'" + args[2].toUpperCase() + "' <dark_red>is already added to the item!"));
                            return true;
                        }
                        meta.addItemFlags(flag);
                        item.setItemMeta(meta);
                        sender.sendMessage(mm.deserialize("<dark_green>The flag <white>'" + args[2].toUpperCase() + "' <dark_green>has been added to your item!"));
                        return true;
                    }
                    case "remove" ->
                    {
                        if (!sender.hasPermission("itemizer.flag.remove"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        if (args.length == 2)
                        {
                            sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Flag Commands<white>]<dark_aqua>==============="));
                            sender.sendMessage(mm.deserialize("<aqua>/itemizer flag remove <<white>name<aqua> <red>- <gold>Remove a flag"));
                            return true;
                        }
                        ItemFlag flag = null;
                        try
                        {
                            flag = ItemFlag.valueOf(args[2].toUpperCase());
                        }
                        catch (Exception ignored)
                        {
                        }
                        if (flag == null)
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The flag <white>\"" + args[2] + "<white>\" does not exist!"));
                            return true;
                        }
                        assert meta != null;
                        if (!meta.getItemFlags().contains(flag))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The flag <white>'" + args[2].toUpperCase() + "<white>' <dark_red>has not been added to the item!"));
                            return true;
                        }
                        meta.removeItemFlags(flag);
                        item.setItemMeta(meta);
                        sender.sendMessage(mm.deserialize("<dark_green>The flag <white>'" + args[2].toUpperCase() + "<white>' <dark_green>has been removed from your item!"));
                        return true;
                    }
                    case "list" ->
                    {
                        if (!sender.hasPermission("itemizer.flag.list"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        assert meta != null;
                        if (Objects.requireNonNull(meta.getItemFlags()).isEmpty())
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The item in your hand does not have any flags"));
                            return true;
                        }
                        sender.sendMessage(mm.deserialize("<dark_green>Item flags: <yellow>" + StringUtils.join(meta.getItemFlags(), ", ")));
                        return true;
                    }
                    case "listall" ->
                    {
                        if (!sender.hasPermission("itemizer.flag.listall"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        sender.sendMessage(mm.deserialize("<dark_green>Available item flags: <yellow>" + StringUtils.join(ItemFlag.values(), ", ")));
                        return true;
                    }
                    default ->
                    {
                        sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer flag>/itemizer flag</click> <aqua>for help."));
                        return true;
                    }
                }
            }
            case "enchant" ->
            {
                if (!sender.hasPermission("itemizer.enchant"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("""
                            <dark_aqua>===============<white>[<light_purple>Enchant Commands<white>]<dark_aqua>===============
                            <aqua>/itemizer enchant add <<white>name<aqua>> <<white>level<aqua>> <red>- <gold>Add an enchant
                            <aqua>/itemizer enchant remove <<white>name<aqua>> <red>- <gold>Remove an enchant
                            <aqua>/itemizer enchant list <red>- <gold>List all item's enchants
                            <aqua>/itemizer enchant listall <red>- <gold>List all available enchants"""));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                switch (args[1])
                {
                    case "add" ->
                    {
                        if (!sender.hasPermission("itemizer.enchant.add"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        if (args.length < 4)
                        {
                            sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Enchant Commands<white>]<dark_aqua>==============="));
                            sender.sendMessage(mm.deserialize("<aqua>/itemizer enchant add <<white>name<aqua>> <<white>level<aqua>> <red>- <gold>Add an enchant"));
                            return true;
                        }
                        final Enchantment ench = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                        if (ench == null)
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The enchantment <white>'" + args[2] + "<white>' <dark_red>does not exist!"));
                            return true;
                        }
                        Integer level = parseInt(sender, args[3]);
                        if (level == null)
                        {
                            return true;
                        }
                        item.addUnsafeEnchantment(ench, level);
                        sender.sendMessage(mm.deserialize("<dark_green>The enchant <white>'" + ench.getKey().getKey() + "' <dark_green>has been added to your item"));
                        return true;
                    }
                    case "remove" ->
                    {
                        if (!sender.hasPermission("itemizer.enchant.remove"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        if (args.length == 2)
                        {
                            sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Enchant Commands<white>]<dark_aqua>==============="));
                            sender.sendMessage(mm.deserialize("<aqua>/itemizer enchant remove <<white>name<aqua>> <red>- <gold>Remove an enchant"));
                            return true;
                        }
                        final Enchantment ench = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                        if (ench == null)
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>The enchantment <white>'" + args[2] + "<white>' <dark_red>does not exist!"));
                            return true;
                        }
                        assert meta != null;
                        if (Objects.requireNonNull(meta.getEnchants()).isEmpty())
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>This item doesn't hold any enchants"));
                            return true;
                        }
                        if (!meta.getEnchants().containsKey(ench))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>This item doesn't have <white<'" + ench.getKey().getKey() + "' <dark_red>enchant!"));
                            return true;
                        }
                        item.removeEnchantment(ench);
                        sender.sendMessage(mm.deserialize("<dark_green>The enchant <white>'" + ench.getKey().getKey() + "' <dark_green>has been removed from your item"));
                        return true;
                    }
                    case "list" ->
                    {
                        if (!sender.hasPermission("itemizer.enchant.list"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        assert meta != null;
                        if (Objects.requireNonNull(meta.getEnchants()).isEmpty())
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>This item doesn't hold any enchants"));
                            return true;
                        }
                        sender.sendMessage(mm.deserialize("<dark_green>Item enchants: <yellow>" + StringUtils.join(meta.getEnchants().keySet(), ", ")));
                        return true;
                    }
                    case "listall" ->
                    {
                        if (!sender.hasPermission("itemizer.enchant.listall"))
                        {
                            sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                            return true;
                        }
                        StringBuilder sb = new StringBuilder();
                        Enchantment[] enchantments;
                        for (int i = 0; i < (enchantments = Enchantment.values()).length; i++)
                        {
                            sb.append(", ").append(enchantments[i].getKey().getKey());
                        }
                        sender.sendMessage(mm.deserialize("<dark_green>Available item enchants: <yellow>" + sb.toString().replaceFirst(", ", "")));
                        return true;
                    }
                    default ->
                    {
                        sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer enchant>/itemizer enchant</click> <aqua>for help."));
                        return true;
                    }
                }
            }
            case "title" ->
            {
                if (!sender.hasPermission("itemizer.title"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Title Command<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer title <<white>name<aqua>> <red>- <gold>Set the book's title"));
                    return true;
                }
                if (!hasBook)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have a Written Book in your hand."));
                    return true;
                }
                Component name = colorize(StringUtils.join(args, " ", 1, args.length));
                final BookMeta bookMeta = (BookMeta) meta;
                assert bookMeta != null;
                bookMeta.title(name);
                item.setItemMeta(bookMeta);
                sender.sendMessage(mm.deserialize("<dark_green>The title of the book has been set to <white>'" + mm.serialize(name) + "<white>'"));
                return true;
            }
            case "author" ->
            {
                if (!sender.hasPermission("itemizer.author"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Author Command<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer author <<white>name<aqua>> <red>- <gold>Set the book's author"));
                    return true;
                }
                if (!hasBook)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have a Written Book in your hand."));
                    return true;
                }
                Component name = colorize(args[1]);
                final BookMeta bookMeta = (BookMeta) meta;
                assert bookMeta != null;
                bookMeta.author(name);
                item.setItemMeta(bookMeta);
                sender.sendMessage(mm.deserialize("<dark_green>The author of the book has been set to <white>'" + mm.serialize(name) + "<white>'"));
                return true;
            }
            case "head" ->
            {
                if (!sender.hasPermission("itemizer.head"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length == 1)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Head Command<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer head <<white>name<aqua>> <red>- <gold>Set the player of the head"));
                    return true;
                }
                if (item.getType() != Material.PLAYER_HEAD)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have a Skull in your hand."));
                    return true;
                }
                String name = args[1];
                if (name.length() > 16)
                {
                    name = name.substring(0, 16);
                }
                final SkullMeta skullMeta = (SkullMeta) meta;
                assert skullMeta != null;
                skullMeta.setOwner(name);
                item.setItemMeta(skullMeta);
                sender.sendMessage(mm.deserialize("<dark_green>The player of the head has been set to <white>'" + name + "<white>'"));
                return true;
            }
            case "sign" ->
            {
                if (!sender.hasPermission("itemizer.sign"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (args.length < 3)
                {
                    sender.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Sign Commands<white>]<dark_aqua>==============="));
                    sender.sendMessage(mm.deserialize("<aqua>/itemizer sign <<white>line<aqua>> <<white>text<aqua>> <red>- <gold>Change the line on the sign"));
                    return true;
                }
                final Block block = player.getTargetBlockExact(20);
                if (block == null || block.getType() == Material.AIR || !block.getType().toString().contains("SIGN"))
                {
                    sender.sendMessage(mm.deserialize("<red>Please look at a sign!"));
                    return true;
                }
                Integer line = parseInt(sender, args[1]);
                if (line == null)
                {
                    return true;
                }
                else if (line > 4)
                {
                    sender.sendMessage(mm.deserialize("<dark_red>There's a maximum of 4 lines on a sign"));
                    return true;
                }
                Component text = colorize(StringUtils.join(args, " ", 2, args.length));
                if (cpb.getAPI() != null)
                {
                    cpb.getAPI().logRemoval(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
                }
                Sign sign = (Sign) block.getState();
                sign.line(line - 1, text);
                sign.update();
                if (cpb.getAPI() != null)
                {
                    cpb.getAPI().logPlacement(player.getName(), sign.getLocation(), sign.getType(), sign.getBlockData());
                }
                sender.sendMessage(mm.deserialize("<dark_green>Line <white>'" + line + "<white>'<dark_green> has successfully changed to <white>'" + mm.serialize(text) + "<white>'"));
                return true;
            }
            case "clearall" ->
            {
                if (!sender.hasPermission("itemizer.clearall"))
                {
                    sender.sendMessage(mm.deserialize("<dark_red>You don't have permission to use this command!"));
                    return true;
                }
                if (!hasItem)
                {
                    sender.sendMessage(mm.deserialize("<red>You do not have an item in your hand."));
                    return true;
                }
                item.setItemMeta(null);
                sender.sendMessage(mm.deserialize("<dark_green>All data cleared from your item"));
                return true;
            }
            default ->
            {
                sender.sendMessage(mm.deserialize("<aqua>Unknown sub-command. Type <gold><click:run_command:/itemizer help>/itemizer help</click> <aqua>for help."));
                return true;
            }
        }
    }

    private Component colorize(String string)
    {
        Matcher matcher = Pattern.compile("&[a-fk-or0-9]", Pattern.CASE_INSENSITIVE).matcher(string);
        while (matcher.find())
        {
            String color = matcher.group();
            string = string.replace(color, COLOR_TRANSLATION.getOrDefault(color.toLowerCase(), color));
        }
        return mm.deserialize(string);
    }

    private Integer parseInt(CommandSender sender, String string)
    {
        try
        {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException ex)
        {
            sender.sendMessage(mm.deserialize("<white>\"" + string + "<white>\"<dark_red> is not a valid number!"));
        }
        return null;
    }
}
