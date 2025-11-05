package dev.plex.itemizerx;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IEnchantmentManager {

    void addEnchantment(Player player, ItemStack item, String[] args);

    void removeEnchantment(Player player, ItemStack item, String[] args);
}
