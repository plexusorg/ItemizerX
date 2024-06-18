package dev.plex.itemizerx;

import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;

public interface IAttributeManager
{
    default ListTag getAttrList(final ItemStack item)
    {
        return null;
    }

    default List<AttributeModifier> getAttrList(final Item item)
    {
        return Collections.emptyList();
    }

    void addAttr(final Player player, final String[] args);

    void removeAttr(final Player player, final String string);

    void listAttr(final Player player);
}
