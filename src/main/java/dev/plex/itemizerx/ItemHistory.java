package dev.plex.itemizerx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bounded in-memory undo history. Entries contain reversible string deltas for the canonical item
 * component patch, not complete ItemStack snapshots. Hashes prevent applying a delta to a different
 * item and corrupting it.
 */
final class ItemHistory
{
    private static final int MAX_PLAYERS = 1024;
    private static final int MAX_ENTRIES = 50;
    private static final int MAX_BYTES_PER_PLAYER = 256 * 1024;
    private static final int MAX_DELTA_BYTES = 32 * 1024;
    private static final int MAX_COMPONENT_BYTES = 64 * 1024;

    private final Map<UUID, PlayerHistory> histories = new LinkedHashMap<>(16, 0.75f, true);

    static byte[] fingerprint(ItemStack item)
    {
        return ItemState.capture(item).hash();
    }

    /** The size of the item's component patch, in UTF-8 bytes. */
    static int componentBytes(ItemStack item)
    {
        if (item == null || item.getType().isAir() || !item.hasItemMeta())
        {
            return 0;
        }
        return item.getItemMeta().getAsComponentString().getBytes(StandardCharsets.UTF_8).length;
    }

    synchronized boolean record(Player player, ItemStack before, ItemStack after)
    {
        ItemDelta delta;
        try
        {
            delta = ItemDelta.between(before, after);
        }
        catch (IllegalArgumentException ex)
        {
            player.sendMessage(Component.text("This edit was too large to add to undo history.", NamedTextColor.YELLOW));
            return false;
        }
        if (delta == null)
        {
            return false;
        }

        PlayerHistory history = history(player.getUniqueId());
        history.redo.clear();
        history.undo.addLast(delta);
        history.bytes += delta.size();
        while (history.undo.size() > MAX_ENTRIES || history.bytes > MAX_BYTES_PER_PLAYER)
        {
            history.bytes -= history.undo.removeFirst().size();
        }
        return true;
    }

    synchronized void undo(Player player)
    {
        PlayerHistory history = histories.get(player.getUniqueId());
        if (history == null || history.undo.isEmpty())
        {
            message(player, "There is nothing to undo.", NamedTextColor.YELLOW);
            return;
        }

        ItemDelta delta = history.undo.peekLast();
        try
        {
            ItemStack restored = delta.apply(player.getInventory().getItemInMainHand(), false);
            player.getInventory().setItemInMainHand(restored);
            history.undo.removeLast();
            history.bytes -= delta.size();
            history.redo.addLast(delta);
            message(player, "Item edit undone.", NamedTextColor.DARK_GREEN);
        }
        catch (IllegalStateException ex)
        {
            message(player, "Undo refused: the held item no longer matches the edited item.", NamedTextColor.DARK_RED);
        }
    }

    synchronized void redo(Player player)
    {
        PlayerHistory history = histories.get(player.getUniqueId());
        if (history == null || history.redo.isEmpty())
        {
            message(player, "There is nothing to redo.", NamedTextColor.YELLOW);
            return;
        }

        ItemDelta delta = history.redo.peekLast();
        try
        {
            ItemStack restored = delta.apply(player.getInventory().getItemInMainHand(), true);
            player.getInventory().setItemInMainHand(restored);
            history.redo.removeLast();
            history.undo.addLast(delta);
            history.bytes += delta.size();
            message(player, "Item edit redone.", NamedTextColor.DARK_GREEN);
        }
        catch (IllegalStateException ex)
        {
            message(player, "Redo refused: the held item no longer matches the expected item.", NamedTextColor.DARK_RED);
        }
    }

    synchronized void clear(UUID player)
    {
        histories.remove(player);
    }

    private PlayerHistory history(UUID player)
    {
        PlayerHistory existing = histories.get(player);
        if (existing != null)
        {
            return existing;
        }
        if (histories.size() >= MAX_PLAYERS)
        {
            UUID oldest = histories.keySet().iterator().next();
            histories.remove(oldest);
        }
        PlayerHistory created = new PlayerHistory();
        histories.put(player, created);
        return created;
    }

    private static void message(Player player, String text, NamedTextColor color)
    {
        player.sendMessage(Component.text(text, color));
    }

    private static final class PlayerHistory
    {
        private final Deque<ItemDelta> undo = new ArrayDeque<>();
        private final Deque<ItemDelta> redo = new ArrayDeque<>();
        private int bytes;
    }

    private record ItemDelta(String oldType, String newType, int oldAmount, int newAmount,
                             StringPatch components, byte[] oldHash, byte[] newHash, int size)
    {
        static ItemDelta between(ItemStack before, ItemStack after)
        {
            ItemState oldState = ItemState.capture(before);
            ItemState newState = ItemState.capture(after);
            if (oldState.equals(newState))
            {
                return null;
            }
            StringPatch patch = StringPatch.between(oldState.components, newState.components);
            int bytes = patch.size()
                    + oldState.type.getBytes(StandardCharsets.UTF_8).length
                    + newState.type.getBytes(StandardCharsets.UTF_8).length + 80;
            if (bytes > MAX_DELTA_BYTES)
            {
                throw new IllegalArgumentException("delta too large");
            }
            return new ItemDelta(oldState.type, newState.type, oldState.amount, newState.amount,
                    patch, oldState.hash(), newState.hash(), bytes);
        }

        ItemStack apply(ItemStack currentItem, boolean forward)
        {
            ItemState current = ItemState.capture(currentItem);
            byte[] expectedHash = forward ? oldHash : newHash;
            if (!MessageDigest.isEqual(current.hash(), expectedHash))
            {
                throw new IllegalStateException("item changed");
            }

            String targetType = forward ? newType : oldType;
            int targetAmount = forward ? newAmount : oldAmount;
            String targetComponents = components.apply(current.components, forward);
            try
            {
                ItemStack result = Bukkit.getItemFactory().createItemStack(targetType + targetComponents);
                result.setAmount(targetAmount);
                return result;
            }
            catch (IllegalArgumentException ex)
            {
                throw new IllegalStateException("delta produced invalid item", ex);
            }
        }
    }

    private record ItemState(String type, int amount, String components)
    {
        static ItemState capture(ItemStack item)
        {
            if (item == null || item.getType() == Material.AIR)
            {
                return new ItemState("minecraft:air", 0, "");
            }
            ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
            String components = meta == null ? "" : meta.getAsComponentString();
            if (components.getBytes(StandardCharsets.UTF_8).length > MAX_COMPONENT_BYTES)
            {
                throw new IllegalArgumentException("component patch too large");
            }
            return new ItemState(item.getType().getKey().toString(), item.getAmount(), components);
        }

        byte[] hash()
        {
            try
            {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(type.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(Integer.toString(amount).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(components.getBytes(StandardCharsets.UTF_8));
                return digest.digest();
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new IllegalStateException("SHA-256 unavailable", ex);
            }
        }
    }

    private record StringPatch(int prefixLength, int suffixLength, String removed, String inserted)
    {
        static StringPatch between(String before, String after)
        {
            int maximumPrefix = Math.min(before.length(), after.length());
            int prefix = 0;
            while (prefix < maximumPrefix && before.charAt(prefix) == after.charAt(prefix))
            {
                prefix++;
            }

            int maximumSuffix = Math.min(before.length() - prefix, after.length() - prefix);
            int suffix = 0;
            while (suffix < maximumSuffix
                    && before.charAt(before.length() - suffix - 1) == after.charAt(after.length() - suffix - 1))
            {
                suffix++;
            }
            return new StringPatch(prefix, suffix,
                    before.substring(prefix, before.length() - suffix),
                    after.substring(prefix, after.length() - suffix));
        }

        String apply(String current, boolean forward)
        {
            String expected = forward ? removed : inserted;
            String replacement = forward ? inserted : removed;
            int end = current.length() - suffixLength;
            if (prefixLength > end || !current.substring(prefixLength, end).equals(expected))
            {
                throw new IllegalStateException("component patch does not match");
            }
            return current.substring(0, prefixLength) + replacement + current.substring(end);
        }

        int size()
        {
            return removed.getBytes(StandardCharsets.UTF_8).length
                    + inserted.getBytes(StandardCharsets.UTF_8).length + 16;
        }
    }
}
