package dev.plex.itemizerx.nms.attribute;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.item.ItemStack;

public interface NMSAttributeManager {
    NBTTagList getAttributeList(final ItemStack itemStack);
}
