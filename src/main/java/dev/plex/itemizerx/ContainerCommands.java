package dev.plex.itemizerx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ContainerCommands
{
    private ContainerCommands()
    {
    }

    static boolean execute(Player player, ItemStack item, String[] args)
    {
        if (!player.hasPermission("itemizer.container"))
        {
            Messages.error(player, "You don't have permission to edit item containers.");
            return true;
        }
        if (args.length < 2)
        {
            help(player);
            return true;
        }

        try
        {
            String action = args[1].toLowerCase(Locale.ROOT);
            if (action.equals("list"))
            {
                list(player, contents(item));
                return true;
            }
            if (action.equals("add"))
            {
                ItemStack source = player.getInventory().getItemInOffHand();
                if (source.getType() == Material.AIR)
                {
                    throw new IllegalArgumentException("Hold the item to copy in your off hand.");
                }
                ItemStack addition = source.clone();
                if (args.length > 2)
                {
                    int amount = Integer.parseInt(args[2]);
                    if (amount < 1 || amount > addition.getMaxStackSize())
                    {
                        throw new IllegalArgumentException("Amount must be from 1 to " + addition.getMaxStackSize() + ".");
                    }
                    addition.setAmount(amount);
                }
                add(item, addition);
                Messages.success(player, "Copied " + addition.getAmount() + " " + addition.getType().getKey() + " into the container.");
                return true;
            }
            if (action.equals("remove"))
            {
                if (args.length < 3) throw new IllegalArgumentException("/itemizer container remove <slot>");
                remove(item, Integer.parseInt(args[2]));
                Messages.success(player, "Container slot cleared.");
                return true;
            }
            if (action.equals("clear"))
            {
                clear(item);
                Messages.success(player, "Container contents cleared.");
                return true;
            }
            help(player);
        }
        catch (IllegalArgumentException | IllegalStateException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid container edit." : ex.getMessage());
        }
        return true;
    }

    private static List<ItemStack> contents(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BundleMeta bundle)
        {
            return bundle.getItems();
        }
        if (meta instanceof BlockStateMeta blockMeta && blockMeta.getBlockState() instanceof Container container)
        {
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack content : container.getInventory().getContents()) items.add(content);
            return items;
        }
        throw new IllegalArgumentException("Hold a bundle, shulker box, or other container item.");
    }

    private static void add(ItemStack item, ItemStack addition)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BundleMeta bundle)
        {
            bundle.addItem(addition);
            item.setItemMeta(bundle);
            return;
        }
        mutateInventory(item, inventory -> {
            if (!inventory.addItem(addition).isEmpty())
            {
                throw new IllegalStateException("The container does not have enough space.");
            }
        });
    }

    private static void remove(ItemStack item, int slot)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BundleMeta bundle)
        {
            List<ItemStack> values = new ArrayList<>(bundle.getItems());
            if (slot < 0 || slot >= values.size()) throw new IllegalArgumentException("Bundle slot is out of range.");
            values.remove(slot);
            bundle.setItems(values);
            item.setItemMeta(bundle);
            return;
        }
        mutateInventory(item, inventory -> {
            if (slot < 0 || slot >= inventory.getSize()) throw new IllegalArgumentException("Container slot is out of range.");
            inventory.clear(slot);
        });
    }

    private static void clear(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BundleMeta bundle)
        {
            bundle.setItems(List.of());
            item.setItemMeta(bundle);
            return;
        }
        mutateInventory(item, Inventory::clear);
    }

    private static void mutateInventory(ItemStack item, java.util.function.Consumer<Inventory> edit)
    {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockMeta) || !(blockMeta.getBlockState() instanceof Container container))
        {
            throw new IllegalArgumentException("Hold a bundle, shulker box, or other container item.");
        }
        edit.accept(container.getInventory());
        blockMeta.setBlockState(container);
        item.setItemMeta(blockMeta);
    }

    private static void list(Player player, List<ItemStack> items)
    {
        boolean any = false;
        for (int slot = 0; slot < items.size(); slot++)
        {
            ItemStack value = items.get(slot);
            if (value != null && value.getType() != Material.AIR)
            {
                any = true;
                player.sendMessage(Component.text(slot + ": " + value.getAmount() + "x " + value.getType().getKey(), NamedTextColor.YELLOW));
            }
        }
        if (!any) player.sendMessage(Component.text("The container is empty.", NamedTextColor.YELLOW));
    }

    private static void help(Player player)
    {
        player.sendMessage(Component.text("/itemizer container list|add [amount]|remove <slot>|clear", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Add safely copies the off-hand item; it never consumes inventory contents.", NamedTextColor.GRAY));
    }
}
