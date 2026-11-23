package dev.plex.itemizerx;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/** Looks enchantments up in the server's registry, so datapack and new vanilla enchantments work without a release. */
final class EnchantmentManager
{
    void addEnchantment(Player player, ItemStack item, String[] args)
    {
        if (args.length < 4)
        {
            Messages.error(player, "/itemizer enchant add <enchantment> <level>");
            return;
        }

        Enchantment enchantment = find(args[2]);
        if (enchantment == null)
        {
            Messages.error(player, "Unknown enchantment '" + args[2] + "'. Use /itemizer enchant listall.");
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
            Messages.error(player, "The enchantment level must be a positive integer.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasEnchant(enchantment))
        {
            Messages.error(player, "This item already has " + enchantment.getKey() + ". Remove it first.");
            return;
        }

        item.addUnsafeEnchantment(enchantment, level);
        Messages.success(player, "Added " + enchantment.getKey() + " level " + level + ".");
    }

    void removeEnchantment(Player player, ItemStack item, String[] args)
    {
        if (args.length < 3)
        {
            Messages.error(player, "/itemizer enchant remove <enchantment>");
            return;
        }

        Enchantment enchantment = find(args[2]);
        if (enchantment == null)
        {
            Messages.error(player, "Unknown enchantment '" + args[2] + "'.");
            return;
        }
        if (item.removeEnchantment(enchantment) == 0)
        {
            Messages.error(player, "This item does not have " + enchantment.getKey() + ".");
            return;
        }
        Messages.success(player, "Removed " + enchantment.getKey() + ".");
    }

    private static Enchantment find(String input)
    {
        String name = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        return key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
    }

}
