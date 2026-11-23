package dev.plex.itemizerx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

final class ItemEditor implements Listener
{
    private final ItemizerX plugin;

    ItemEditor(ItemizerX plugin)
    {
        this.plugin = plugin;
    }

    void open(Player player)
    {
        if (!player.hasPermission("itemizer.gui"))
        {
            player.sendMessage(Component.text("You don't have permission to use the item editor.", NamedTextColor.DARK_RED));
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR)
        {
            player.sendMessage(Component.text("Hold an item before opening the editor.", NamedTextColor.DARK_RED));
            return;
        }
        EditorHolder holder = new EditorHolder(player.getUniqueId());
        render(holder.inventory, player);
        player.openInventory(holder.inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof EditorHolder holder))
        {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !holder.owner.equals(player.getUniqueId()))
        {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize())
        {
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR)
        {
            player.closeInventory();
            return;
        }
        ItemStack before = held.clone();
        boolean record = true;
        switch (event.getRawSlot())
        {
            case 10 -> editMeta(held, meta -> meta.setUnbreakable(!meta.isUnbreakable()));
            case 11 -> editMeta(held, meta -> meta.setEnchantmentGlintOverride(!ItemCommands.hasGlint(meta)));
            case 12 -> editMeta(held, meta -> meta.setHideTooltip(!meta.isHideTooltip()));
            case 14 -> editMeta(held, meta -> meta.setGlider(!meta.isGlider()));
            case 15 -> held.setAmount(Math.max(1, held.getAmount() - 1));
            case 16 -> held.setAmount(Math.min(99, held.getAmount() + 1));
            case 18 ->
            {
                record = false;
                plugin.history.undo(player);
            }
            case 20 ->
            {
                record = false;
                ItemCommands.showComponents(player, held);
            }
            case 22 -> held.setItemMeta(null);
            case 24 ->
            {
                record = false;
                player.closeInventory();
            }
            case 26 ->
            {
                record = false;
                plugin.history.redo(player);
            }
            default -> record = false;
        }
        if (record)
        {
            plugin.history.record(player, before, player.getInventory().getItemInMainHand().clone());
        }
        if (event.getRawSlot() != 24)
        {
            render(holder.inventory, player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event)
    {
        if (event.getView().getTopInventory().getHolder(false) instanceof EditorHolder)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        plugin.history.clear(event.getPlayer().getUniqueId());
        plugin.presets.playerQuit(event.getPlayer().getUniqueId());
    }

    private static void render(Inventory inventory, Player player)
    {
        inventory.clear();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) return;
        ItemMeta meta = held.getItemMeta();

        inventory.setItem(10, button(Material.ANVIL, "Unbreakable: " + (meta != null && meta.isUnbreakable())));
        inventory.setItem(11, button(Material.GLOWSTONE_DUST, "Glint: " + ItemCommands.hasGlint(meta)));
        inventory.setItem(12, button(Material.PAPER, "Hide tooltip: " + (meta != null && meta.isHideTooltip())));
        inventory.setItem(13, held.clone());
        inventory.setItem(14, button(Material.ELYTRA, "Glider: " + (meta != null && meta.isGlider())));
        inventory.setItem(15, button(Material.RED_DYE, "Decrease amount"));
        inventory.setItem(16, button(Material.LIME_DYE, "Increase amount"));
        inventory.setItem(18, button(Material.ARROW, "Undo"));
        inventory.setItem(20, button(Material.WRITABLE_BOOK, "Show components"));
        inventory.setItem(22, button(Material.BARRIER, "Clear item metadata"));
        inventory.setItem(24, button(Material.STRUCTURE_VOID, "Close"));
        inventory.setItem(26, button(Material.SPECTRAL_ARROW, "Redo"));
    }

    private static ItemStack button(Material material, String name)
    {
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(Component.text(name, NamedTextColor.AQUA));
            meta.lore(List.of(Component.text("Click to apply", NamedTextColor.GRAY)));
        });
        return item;
    }

    private static void editMeta(ItemStack item, java.util.function.Consumer<ItemMeta> edit)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        edit.accept(meta);
        item.setItemMeta(meta);
    }

    private static final class EditorHolder implements InventoryHolder
    {
        private final UUID owner;
        private final Inventory inventory;

        private EditorHolder(UUID owner)
        {
            this.owner = owner;
            this.inventory = Bukkit.createInventory(this, 27, Component.text("ItemizerX Editor", NamedTextColor.DARK_AQUA));
        }

        @Override
        public @NotNull Inventory getInventory()
        {
            return inventory;
        }
    }
}
