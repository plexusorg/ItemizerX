package dev.plex.itemizerx;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/** Registry-backed enchantment editing, including new and datapack-provided enchantments. */
public final class EnchantmentManager implements IEnchantmentManager
{
    @Override
    public void addEnchantment(Player player, ItemStack item, String[] args)
    {
        if (args.length < 4)
        {
            error(player, "/itemizer enchant add <enchantment> <level>");
            return;
        }

        Enchantment enchantment = find(args[2]);
        if (enchantment == null)
        {
            error(player, "Unknown enchantment '" + args[2] + "'. Use /itemizer enchant listall.");
            return;
        }

        int level;
        try
        {
            level = Integer.parseInt(args[3]);
            if (level < 1)
            {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException ex)
        {
            error(player, "The enchantment level must be a positive integer.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasEnchant(enchantment))
        {
            error(player, "This item already has " + enchantment.getKey() + ". Remove it first.");
            return;
        }

        item.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(Component.text("Added " + enchantment.getKey() + " level " + level + ".", NamedTextColor.DARK_GREEN));
    }

    @Override
    public void removeEnchantment(Player player, ItemStack item, String[] args)
    {
        if (args.length < 3)
        {
            error(player, "/itemizer enchant remove <enchantment>");
            return;
        }

        Enchantment enchantment = find(args[2]);
        if (enchantment == null)
        {
            error(player, "Unknown enchantment '" + args[2] + "'.");
            return;
        }
        if (item.removeEnchantment(enchantment) == 0)
        {
            error(player, "This item does not have " + enchantment.getKey() + ".");
            return;
        }
        player.sendMessage(Component.text("Removed " + enchantment.getKey() + ".", NamedTextColor.DARK_GREEN));
    }

    private static Enchantment find(String input)
    {
        String name = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        return key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
    }

    private static void error(Player player, String message)
    {
        player.sendMessage(Component.text(message, NamedTextColor.DARK_RED));
    }
}
