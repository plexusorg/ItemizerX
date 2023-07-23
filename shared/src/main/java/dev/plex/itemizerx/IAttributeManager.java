package dev.plex.itemizerx;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;

public interface IAttributeManager
{
    ListTag getAttrList(final ItemStack item);

    void addAttr(final Player player, final String[] args);

    void removeAttr(final Player player, final String string);

    void listAttr(final Player player);
}
