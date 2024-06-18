package dev.plex.itemizerx.v1_20_R4;

import dev.plex.itemizerx.IAttributeManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AttributeManager implements IAttributeManager
{
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public List<AttributeModifier> getAttrList(final Item item)
    {
        ItemAttributeModifiers attrmod = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (Entry modifier : attrmod.modifiers())
        {
            modifiers.add(modifier.modifier());
        }
        return modifiers;
    }

    @Override
    public void addAttr(final Player player, final String[] args)
    {
        if (args.length < 4)
        {
            player.sendMessage(mm.deserialize("<aqua>/itemizer attr add <<white>name<aqua>> <<white>strength<aqua>>" +
                    "[<white>slot<aqua>] <red>- <gold>Add an attribute"));
            return;
        }

        final Attributes_v1_20_R4 a = Attributes_v1_20_R4.get(args[2]);
        if (a == null)
        {
            player.sendMessage(mm.deserialize("<dark_red>\"" + args[2] + "\" is not a valid attribute type."));
            return;
        }

        double amount;
        try
        {
            amount = Double.parseDouble(args[3]);
        }
        catch (NumberFormatException ex)
        {
            player.sendMessage(mm.deserialize("<dark_red>\"" + args[3] + "\" is not a valid number."));
            return;
        }

        if (Double.isNaN(amount))
        {
            player.sendMessage(mm.deserialize("<dark_red>Please do not use <white>'NaN (Not a Number)'"));
            return;
        }

        ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final List<AttributeModifier> attrmod = getAttrList(nms.getItem());
        for (AttributeModifier modifier : attrmod)
        {
            if (modifier.name().equalsIgnoreCase(args[2]))
            {
                player.sendMessage(mm.deserialize("<dark_red>An attribute with the name \"<white>" + args[2] + "<dark_red>\" already exists!"));
                return;
            }
        }

        AtomicReference<EquipmentSlotGroup> group = new AtomicReference<>(EquipmentSlotGroup.ANY);
        if (args.length == 5)
        {
            EquipmentSlot slot;
            try
            {
                slot = EquipmentSlot.byName(args[4].toLowerCase());
            }
            catch (IllegalArgumentException ignored)
            {
                player.sendMessage(mm.deserialize("<dark_green>Supported options:"));
                player.sendMessage(mm.deserialize("<yellow>" + StringUtils.join(Arrays.stream(EquipmentSlot.values()).map(s -> s.getName().toLowerCase()).toArray(), ", ")));
                return;
            }

            group.set(EquipmentSlotGroup.bySlot(slot));
        }

        final AttributeModifier modifier = new AttributeModifier(a.mcName, amount, Operation.BY_ID.apply(a.op));
        nms.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, x ->
            x.withModifierAdded(a.attributeHolder, modifier, group.get()).withTooltip(true));
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(mm.deserialize("<dark_aqua>Attribute added!"));
    }

    @Override
    public void removeAttr(final Player player, final String string)
    {
        /*final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final ListTag attrmod = getAttrList(nms);
        final ListTag newList = new ListTag();
        boolean r = false;
        for (Tag nbtBase : attrmod)
        {
            final CompoundTag c = (CompoundTag) nbtBase;
            if (!c.getString("Name").equals(string))
            {
                newList.add(nbtBase);
            }
            else
            {
                r = true;
            }
        }
        if (!r)
        {
            player.sendMessage(mm.deserialize("<dark_red>The attribute \"" + string + "\" doesn't exist!"));
            return;
        }
        //nms.getTag().put("AttributeModifiers", newList);
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);*/
        player.sendMessage(mm.deserialize("<red>Removing attributes isn't supported at the moment!"));
    }

    @Override
    public void listAttr(final Player player)
    {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final List<AttributeModifier> attrmod = getAttrList(nms.getItem());
        if (attrmod.isEmpty())
        {
            player.sendMessage(mm.deserialize("<yellow>This item has no attributes."));
            return;
        }

        player.sendMessage(mm.deserialize("<dark_green>Item attributes: "));
        for (AttributeModifier modifier : attrmod)
        {
            player.sendMessage(mm.deserialize("<yellow>" + modifier.name() + ", " + modifier.amount()));
        }
    }
}