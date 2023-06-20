package dev.plex.itemizerx.v1_20_R1;

import dev.plex.itemizerx.Attributes;
import dev.plex.itemizerx.IAttributeManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttributeManager implements IAttributeManager {

    @Override
    public NBTTagList getAttrList(final ItemStack item) {
        NBTTagList attrmod = item.w().c("AttributeModifiers", 10);
        if (attrmod == null) {
            item.v().a("AttributeModifiers", new NBTTagList());
        }
        return item.v().c("AttributeModifiers", 10);
    }

    @Override
    public void addAttr(final Player player, final String[] args) {
        int op;
        if (args.length < 4) {
            player.sendMessage(colorize("&b/itemizer attr add <&fname&b> <&fstrength&b> [&fslot&b] &c- "
                    + "&6Add an attribute"));
            return;
        }
        final Attributes a = Attributes.get(args[2]);
        if (a == null) {
            player.sendMessage(colorize("&4\"" + args[2] + "\" is not a valid attribute type."));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException ex) {
            player.sendMessage(colorize("&4\"" + args[3] + "\" is not a valid number."));
            return;
        }
        if (Double.isNaN(amount)) {
            player.sendMessage(colorize("&4Please do not use &f'NaN (Not a Number)'"));
            return;
        }
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final NBTTagList attrmod = getAttrList(nms);
        for (net.minecraft.nbt.NBTBase nbtBase : attrmod) {
            final NBTTagCompound c = (NBTTagCompound) nbtBase;
            if (c.l("Name").equals(args[2])) {
                player.sendMessage(colorize("&4An attribute with the name \"&f" + args[2] + "&4\" already exists!"));
                return;
            }
        }
        final NBTTagCompound c = new NBTTagCompound();
        c.a("Name", args[2]);
        c.a("AttributeName", a.mcName);
        c.a("Amount", amount);
        op = a.op;
        c.a("Operation", op);
        final Random random = new Random();
        c.a("UUID", new int[]
                {
                        random.nextInt(),
                        random.nextInt(),
                        random.nextInt(),
                        random.nextInt()
                });
        if (args.length == 5) {
            final List<String> options = new ArrayList<>();
            options.add("mainhand");
            options.add("offhand");
            options.add("head");
            options.add("chest");
            options.add("legs");
            options.add("feet");
            if (!options.contains(args[4].toLowerCase())) {
                player.sendMessage(colorize("&2Supported options:\n"
                        + "&e" + StringUtils.join(options, ", ")));
                return;
            }
            c.a("Slot", args[4].toLowerCase());
        }
        attrmod.add(c);
        nms.v().a("AttributeModifiers", attrmod);
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(colorize("&2Attribute added!"));
    }

    @Override
    public void removeAttr(final Player player, final String string) {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final NBTTagList attrmod = getAttrList(nms);
        final NBTTagList newList = new NBTTagList();
        boolean r = false;
        for (net.minecraft.nbt.NBTBase nbtBase : attrmod) {
            final NBTTagCompound c = (NBTTagCompound) nbtBase;
            if (!c.l("Name").equals(string)) {
                newList.add(nbtBase);
            } else {
                r = true;
            }
        }
        if (!r) {
            player.sendMessage(colorize("&4The attribute \"" + string + "\" doesn't exist!"));
            return;
        }
        nms.v().a("AttributeModifiers", newList);
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(colorize("&2Attribute removed!"));
    }

    @Override
    public void listAttr(final Player player) {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final NBTTagList attrmod = getAttrList(nms);
        if (attrmod.size() == 0) {
            player.sendMessage(colorize("&eThis item has no attributes."));
            return;
        }
        player.sendMessage(colorize("&2Item attributes: "));
        for (net.minecraft.nbt.NBTBase nbtBase : attrmod) {
            final NBTTagCompound c = (NBTTagCompound) nbtBase;
            player.sendMessage(colorize("&e" + Attributes.get(c.l("AttributeName")).mcName
                    + ", " + c.k("Amount")));
        }
    }

    @Override
    public String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
