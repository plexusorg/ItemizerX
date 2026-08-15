package dev.plex.itemizerx;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class ItemDialog implements Listener
{
    private static final Key APPLY = Key.key("itemizerx:dialog/apply");
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(5);
    private static final int MAX_SESSIONS = 1024;

    private final ItemizerX plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    ItemDialog(ItemizerX plugin)
    {
        this.plugin = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    void open(Player player)
    {
        if (!player.hasPermission("itemizer.gui"))
        {
            player.sendMessage(Component.text("You don't have permission to use the item dialog.", NamedTextColor.DARK_RED));
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR)
        {
            player.sendMessage(Component.text("Hold an item before opening the editor.", NamedTextColor.DARK_RED));
            return;
        }
        if (!sessions.containsKey(player.getUniqueId()) && sessions.size() >= MAX_SESSIONS)
        {
            player.sendMessage(Component.text("Too many item dialogs are currently open.", NamedTextColor.DARK_RED));
            return;
        }

        sessions.put(player.getUniqueId(), new Session(ItemHistory.fingerprint(held),
                System.currentTimeMillis() + SESSION_LIFETIME.toMillis()));
        ItemMeta meta = held.getItemMeta();
        boolean unbreakable = meta != null && meta.isUnbreakable();
        boolean glint = meta != null && Boolean.TRUE.equals(meta.getEnchantmentGlintOverride());
        boolean hideTooltip = meta != null && meta.isHideTooltip();
        boolean glider = meta != null && meta.isGlider();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("ItemizerX Editor", NamedTextColor.DARK_AQUA))
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(List.of(
                                DialogBody.item(held.clone()).showDecorations(true).showTooltip(true).width(64).height(64).build(),
                                DialogBody.plainMessage(Component.text("Edit the held item and apply the values below.", NamedTextColor.GRAY))))
                        .inputs(List.of(
                                DialogInput.numberRange("amount", Component.text("Stack amount"), 1, 99)
                                        .initial((float) Math.min(99, held.getAmount())).step(1f).width(300).build(),
                                DialogInput.bool("unbreakable", Component.text("Unbreakable")).initial(unbreakable).build(),
                                DialogInput.bool("glint", Component.text("Enchantment glint")).initial(glint).build(),
                                DialogInput.bool("hide_tooltip", Component.text("Hide tooltip")).initial(hideTooltip).build(),
                                DialogInput.bool("glider", Component.text("Glider")).initial(glider).build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Apply", NamedTextColor.GREEN))
                                .tooltip(Component.text("Apply these changes to the held item."))
                                .action(DialogAction.customClick(APPLY, null)).build(),
                        ActionButton.builder(Component.text("Cancel", NamedTextColor.RED))
                                .tooltip(Component.text("Discard these changes.")).build())));
        player.showDialog(dialog);
    }

    @EventHandler
    public void onDialogAction(PlayerCustomClickEvent event)
    {
        if (!event.getIdentifier().equals(APPLY) || !(event.getCommonConnection() instanceof PlayerGameConnection connection))
        {
            return;
        }
        Player player = connection.getPlayer();
        Session session = sessions.remove(player.getUniqueId());
        if (session == null || session.expiresAt < System.currentTimeMillis())
        {
            player.sendMessage(Component.text("That item dialog expired; open it again.", NamedTextColor.DARK_RED));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!MessageDigest.isEqual(session.fingerprint, ItemHistory.fingerprint(item)))
        {
            player.sendMessage(Component.text("The held item changed, so the dialog edit was refused.", NamedTextColor.DARK_RED));
            return;
        }

        DialogResponseView response = event.getDialogResponseView();
        Float amountValue = response.getFloat("amount");
        Boolean unbreakable = response.getBoolean("unbreakable");
        Boolean glint = response.getBoolean("glint");
        Boolean hideTooltip = response.getBoolean("hide_tooltip");
        Boolean glider = response.getBoolean("glider");
        if (amountValue == null || unbreakable == null || glint == null || hideTooltip == null || glider == null)
        {
            player.sendMessage(Component.text("The dialog response was incomplete.", NamedTextColor.DARK_RED));
            return;
        }

        ItemStack before = item.clone();
        int amount = Math.max(1, Math.min(99, Math.round(amountValue)));
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null)
        {
            meta.setUnbreakable(unbreakable);
            meta.setEnchantmentGlintOverride(glint);
            meta.setHideTooltip(hideTooltip);
            meta.setGlider(glider);
            item.setItemMeta(meta);
        }
        plugin.history.record(player, before, item.clone());
        player.sendMessage(Component.text("Dialog changes applied.", NamedTextColor.DARK_GREEN));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    private record Session(byte[] fingerprint, long expiresAt)
    {
    }
}
