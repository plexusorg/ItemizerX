package dev.plex.itemizerx.nms.attribute.impl;

import dev.plex.itemizerx.nms.attribute.NMSAttributeManager;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.item.ItemStack;

public class v1_17_1_NMSAttributeManager implements NMSAttributeManager {
    @Override
    public NBTTagList getAttributeList(final ItemStack itemStack) {
        NBTTagList attrmod = itemStack.getOrCreateTag().getList("AttributeModifiers", 10);
        if (attrmod == null) {
            itemStack.getTag().set("AttributeModifiers", new NBTTagList());
        }
        return itemStack.getTag().getList("AttributeModifiers", 10);
    }
}
