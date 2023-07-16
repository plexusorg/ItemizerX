package dev.plex.itemizerx.v1_17_R1;

import dev.plex.itemizerx.Attributes;
import dev.plex.itemizerx.IAttributeManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class AttributeManager implements IAttributeManager
{
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public ListTag getAttrList(final ItemStack item)
    {
        ListTag attrmod = item.getOrCreateTag().getList("AttributeModifiers", 10);
        if (attrmod == null)
        {
            item.getTag().put("AttributeModifiers", new CompoundTag());
        }
        return item.getTag().getList("AttributeModifiers", 10);
    }

    @Override
    public void addAttr(final Player player, final String[] args)
    {
        int op;
        if (args.length < 4)
        {
            player.sendMessage(mm.deserialize("<aqua>/itemizer attr add <<white>name<aqua>> <<white>strength<aqua>>" +
                    "[<white>slot<aqua>] <red>- <gold>Add an attribute"));
            return;
        }
        final Attributes a = Attributes.get(args[2]);
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
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final ListTag attrmod = getAttrList(nms);
        for (Tag nbtBase : attrmod)
        {
            final CompoundTag c = (CompoundTag)nbtBase;
            if (c.getString("Name").equals(args[2]))
            {
                player.sendMessage(mm.deserialize("<dark_red>An attribute with the name \"<white>" + args[2] + "<dark_red>\" already exists!"));
                return;
            }
        }
        final CompoundTag c = new CompoundTag();
        c.putString("Name", args[2]);
        c.putString("AttributeName", a.mcName);
        c.putDouble("Amount", amount);
        op = a.op;
        c.putInt("Operation", op);
        final Random random = new Random();
        c.putIntArray("UUID", new int[]
                {
                        random.nextInt(),
                        random.nextInt(),
                        random.nextInt(),
                        random.nextInt()
                });
        if (args.length == 5)
        {
            final List<String> options = new ArrayList<>();
            options.add("mainhand");
            options.add("offhand");
            options.add("head");
            options.add("chest");
            options.add("legs");
            options.add("feet");
            if (!options.contains(args[4].toLowerCase()))
            {
                player.sendMessage(mm.deserialize("<dark_green>Supported options:"));
                player.sendMessage(mm.deserialize("<yellow>" + StringUtils.join(options, ", ")));
                return;
            }
            c.putString("Slot", args[4].toLowerCase());
        }
        attrmod.add(c);
        nms.getTag().put("AttributeModifiers", attrmod);
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(mm.deserialize("<dark_aqua>Attribute added!"));
    }

    @Override
    public void removeAttr(final Player player, final String string)
    {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final ListTag attrmod = getAttrList(nms);
        final ListTag newList = new ListTag();
        boolean r = false;
        for (Tag nbtBase : attrmod)
        {
            final CompoundTag c = (CompoundTag)nbtBase;
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
        nms.getTag().put("AttributeModifiers", newList);
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(mm.deserialize("<dark_green>Attribute removed!"));
    }

    @Override
    public void listAttr(final Player player)
    {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final ListTag attrmod = getAttrList(nms);
        if (attrmod.size() == 0)
        {
            player.sendMessage(mm.deserialize("<yellow>This item has no attributes."));
            return;
        }
        player.sendMessage(mm.deserialize("<dark_green>Item attributes: "));
        for (Tag nbtBase : attrmod)
        {
            final CompoundTag c = (CompoundTag)nbtBase;
            player.sendMessage(mm.deserialize("<yellow>" + Attributes.get(c.getString("AttributeName")).mcName
                    + ", " + c.getDouble("Amount")));
        }
    }

    @Override
    public String colorize(String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
