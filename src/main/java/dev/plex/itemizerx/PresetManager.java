package dev.plex.itemizerx;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

final class PresetManager implements Listener
{
    private static final Key IMPORT = Key.key("itemizerx:preset/import");
    private static final Pattern NAME = Pattern.compile("[a-z0-9_-]{1,32}");
    private static final int MAX_PRESETS_PER_PLAYER = 32;
    private static final int MAX_TOTAL_PRESETS = 2048;
    private static final int MAX_ITEM_LENGTH = 32 * 1024;
    private static final int MAX_EXPORT_ITEM_LENGTH = 12 * 1024;
    private static final int MAX_IMPORT_TOKEN_LENGTH = 20 * 1024;
    private static final long IMPORT_COOLDOWN_MILLIS = 2000;

    private final ItemizerX plugin;
    private final File file;
    private final Map<UUID, Map<String, Preset>> presets = new LinkedHashMap<>();
    private final Map<UUID, Long> lastImport = new LinkedHashMap<>();
    private final Map<UUID, String> pendingImports = new LinkedHashMap<>();
    private boolean saveScheduled;

    PresetManager(ItemizerX plugin)
    {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "presets.yml");
        load();
    }

    synchronized boolean execute(Player player, String[] args)
    {
        if (!player.hasPermission("itemizer.preset"))
        {
            Messages.error(player, "You don't have permission to use item presets.");
            return true;
        }
        if (args.length < 2)
        {
            help(player);
            return true;
        }

        try
        {
            switch (args[1].toLowerCase(Locale.ROOT))
            {
                case "list" -> list(player);
                case "save" ->
                {
                    requireArgs(args, 3, "/itemizer preset save <name>");
                    save(player, name(args[2]), player.getInventory().getItemInMainHand());
                }
                case "load" ->
                {
                    requireArgs(args, 3, "/itemizer preset load <name>");
                    Preset preset = get(player, name(args[2]));
                    player.getInventory().setItemInMainHand(preset.create());
                    Messages.success(player, "Preset '" + args[2] + "' loaded.");
                }
                case "delete", "remove" ->
                {
                    requireArgs(args, 3, "/itemizer preset delete <name>");
                    String name = name(args[2]);
                    Map<String, Preset> own = presets.get(player.getUniqueId());
                    if (own == null || own.remove(name) == null) throw new IllegalArgumentException("Unknown preset '" + name + "'.");
                    scheduleSave();
                    Messages.success(player, "Preset '" + name + "' deleted.");
                }
                case "export" ->
                {
                    requireArgs(args, 3, "/itemizer preset export <name>");
                    String name = name(args[2]);
                    Preset preset = get(player, name);
                    String token = preset.export();
                    player.sendMessage(Component.text("Click to copy preset '" + name + "'.", NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.copyToClipboard(token)));
                }
                case "import" ->
                {
                    requireArgs(args, 3, "/itemizer preset import <name> [token]");
                    String name = name(args[2]);
                    if (args.length == 3)
                    {
                        // The chat box takes 256 characters, so longer tokens go in a dialog.
                        openImportDialog(player, name);
                        return true;
                    }
                    importToken(player, name, StringUtils.join(args, "", 3, args.length));
                }
                default -> help(player);
            }
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid preset." : ex.getMessage());
        }
        return true;
    }

    synchronized java.util.Set<String> names(UUID player)
    {
        Map<String, Preset> own = presets.get(player);
        return own == null ? java.util.Set.of() : java.util.Set.copyOf(own.keySet());
    }

    @SuppressWarnings("UnstableApiUsage")
    private void openImportDialog(Player player, String name)
    {
        pendingImports.put(player.getUniqueId(), name);
        player.showDialog(Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Import preset '" + name + "'", NamedTextColor.DARK_AQUA))
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .inputs(List.of(DialogInput.text("token", Component.text("Token from /itemizer preset export"))
                                .maxLength(MAX_IMPORT_TOKEN_LENGTH)
                                .width(300)
                                .multiline(TextDialogInput.MultilineOptions.create(null, 100))
                                .build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Import", NamedTextColor.GREEN))
                                .action(DialogAction.customClick(IMPORT, null)).build(),
                        ActionButton.builder(Component.text("Cancel", NamedTextColor.RED)).build()))));
    }

    @EventHandler
    public void onImportDialog(PlayerCustomClickEvent event)
    {
        if (!event.getIdentifier().equals(IMPORT) || !(event.getCommonConnection() instanceof PlayerGameConnection connection))
        {
            return;
        }
        Player player = connection.getPlayer();
        String name;
        synchronized (this)
        {
            name = pendingImports.remove(player.getUniqueId());
        }
        DialogResponseView response = event.getDialogResponseView();
        String token = response == null ? null : response.getText("token");
        if (name == null || token == null)
        {
            Messages.error(player, "That import dialog expired; run /itemizer preset import <name> again.");
            return;
        }
        try
        {
            importToken(player, name, token.replaceAll("\\s", ""));
        }
        catch (IllegalArgumentException ex)
        {
            Messages.error(player, ex.getMessage() == null ? "Invalid preset." : ex.getMessage());
        }
    }

    private synchronized void importToken(Player player, String name, String token)
    {
        checkImportRate(player);
        if (token.length() > MAX_IMPORT_TOKEN_LENGTH) throw new IllegalArgumentException("Preset token is too large.");
        savePreset(player, name, Preset.importToken(token));
        Messages.success(player, "Preset '" + name + "' imported.");
    }

    synchronized void playerQuit(UUID player)
    {
        lastImport.remove(player);
        pendingImports.remove(player);
    }

    synchronized void flush()
    {
        saveScheduled = false;
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Preset>> player : presets.entrySet())
        {
            for (Map.Entry<String, Preset> entry : player.getValue().entrySet())
            {
                String path = "players." + player.getKey() + "." + entry.getKey();
                config.set(path + ".item", entry.getValue().item);
                config.set(path + ".amount", entry.getValue().amount);
            }
        }
        try
        {
            plugin.getDataFolder().mkdirs();
            config.save(file);
        }
        catch (IOException ex)
        {
            plugin.getLogger().severe("Could not save presets.yml: " + ex.getMessage());
        }
    }

    private void load()
    {
        if (!file.isFile()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null) return;
        int total = 0;
        for (String uuidValue : players.getKeys(false))
        {
            if (total >= MAX_TOTAL_PRESETS) break;
            try
            {
                UUID uuid = UUID.fromString(uuidValue);
                ConfigurationSection section = players.getConfigurationSection(uuidValue);
                if (section == null) continue;
                Map<String, Preset> own = new LinkedHashMap<>();
                for (String presetName : section.getKeys(false))
                {
                    if (own.size() >= MAX_PRESETS_PER_PLAYER || total >= MAX_TOTAL_PRESETS || !NAME.matcher(presetName).matches()) break;
                    String item = section.getString(presetName + ".item", "");
                    int amount = section.getInt(presetName + ".amount", 1);
                    Preset preset = new Preset(item, amount);
                    preset.validate();
                    own.put(presetName, preset);
                    total++;
                }
                if (!own.isEmpty()) presets.put(uuid, own);
            }
            catch (IllegalArgumentException ignored)
            {
                plugin.getLogger().warning("Skipped an invalid preset entry for " + uuidValue + ".");
            }
        }
    }

    private void save(Player player, String name, ItemStack stack)
    {
        if (stack.getType() == Material.AIR) throw new IllegalArgumentException("Hold an item to save.");
        ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : null;
        String item = stack.getType().getKey() + (meta == null ? "" : meta.getAsComponentString());
        Preset preset = new Preset(item, stack.getAmount());
        preset.validate();
        savePreset(player, name, preset);
        Messages.success(player, "Preset '" + name + "' saved.");
    }

    private void savePreset(Player player, String name, Preset preset)
    {
        Map<String, Preset> own = presets.computeIfAbsent(player.getUniqueId(), ignored -> new LinkedHashMap<>());
        boolean replacing = own.containsKey(name);
        if (!replacing && own.size() >= MAX_PRESETS_PER_PLAYER) throw new IllegalArgumentException("You may save at most " + MAX_PRESETS_PER_PLAYER + " presets.");
        if (!replacing && totalPresets() >= MAX_TOTAL_PRESETS) throw new IllegalArgumentException("The server preset limit has been reached.");
        own.put(name, preset);
        scheduleSave();
    }

    private void list(Player player)
    {
        java.util.Set<String> names = names(player.getUniqueId());
        player.sendMessage(Component.text(names.isEmpty() ? "You have no saved presets." : "Presets: " + String.join(", ", names), NamedTextColor.YELLOW));
    }

    private Preset get(Player player, String name)
    {
        Map<String, Preset> own = presets.get(player.getUniqueId());
        Preset preset = own == null ? null : own.get(name);
        if (preset == null) throw new IllegalArgumentException("Unknown preset '" + name + "'.");
        return preset;
    }

    private void checkImportRate(Player player)
    {
        long now = System.currentTimeMillis();
        long previous = lastImport.getOrDefault(player.getUniqueId(), 0L);
        if (now - previous < IMPORT_COOLDOWN_MILLIS) throw new IllegalArgumentException("Please wait before importing another preset.");
        lastImport.put(player.getUniqueId(), now);
    }

    private void scheduleSave()
    {
        if (saveScheduled) return;
        saveScheduled = true;
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, ignored -> flush(), 20L);
    }

    private int totalPresets()
    {
        return presets.values().stream().mapToInt(Map::size).sum();
    }

    private static String name(String value)
    {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (!NAME.matcher(normalized).matches()) throw new IllegalArgumentException("Preset names use 1-32 letters, numbers, underscores, or hyphens.");
        return normalized;
    }

    private static void requireArgs(String[] args, int amount, String usage)
    {
        if (args.length < amount) throw new IllegalArgumentException(usage);
    }

    private static void help(Player player)
    {
        Messages.help(player, "Preset Commands",
                Messages.command("preset list", "List your presets"),
                Messages.command("preset save <name>", "Save the held item"),
                Messages.command("preset load <name>", "Replace the held item with a preset"),
                Messages.command("preset delete <name>", "Delete a preset"),
                Messages.command("preset export <name>", "Get a token to share a preset"),
                Messages.command("preset import <name> [token]", "Save a shared token; leave it out to paste it in a dialog"));
    }

    private record Preset(String item, int amount)
    {
        void validate()
        {
            if (item.isEmpty() || item.length() > MAX_ITEM_LENGTH) throw new IllegalArgumentException("Preset item data is invalid or too large.");
            if (amount < 1 || amount > 99) throw new IllegalArgumentException("Preset amount is invalid.");
            Bukkit.getItemFactory().createItemStack(item);
        }

        ItemStack create()
        {
            ItemStack result = Bukkit.getItemFactory().createItemStack(item);
            result.setAmount(amount);
            return result;
        }

        String export()
        {
            if (item.length() > MAX_EXPORT_ITEM_LENGTH) throw new IllegalArgumentException("This preset is too large for a chat import token.");
            String payload = amount + "\n" + item;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        }

        static Preset importToken(String token)
        {
            try
            {
                String payload = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
                int separator = payload.indexOf('\n');
                if (separator < 1) throw new IllegalArgumentException();
                Preset preset = new Preset(payload.substring(separator + 1), Integer.parseInt(payload.substring(0, separator)));
                preset.validate();
                return preset;
            }
            catch (RuntimeException ex)
            {
                throw new IllegalArgumentException("Invalid preset token.");
            }
        }
    }
}
