package dev.plex.itemizerx;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ItemizerX extends JavaPlugin
{
    public static ItemizerX plugin;
    CoreProtectBridge cpb = new CoreProtectBridge();
    IAttributeManager attr;
    IEnchantmentManager ench;
    final ItemHistory history = new ItemHistory();
    PresetManager presets;
    ItemEditor editor;

    @Override
    public void onLoad()
    {
        plugin = this;
    }

    @Override
    public void onEnable()
    {
        plugin = this;
        // Metrics at https://bstats.org/plugin/bukkit/ItemizerX/19104
        new Metrics(this, 19104);
        cpb.getCoreProtect();
        if (!isSupportedVersion(getServerVersion()))
        {
            getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
            getLogger().severe("ItemizerX 3 supports Minecraft 26.1 through 26.2, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        attr = new AttributeManager();
        ench = new EnchantmentManager();
        presets = new PresetManager(this);
        editor = new ItemEditor(this);
        getServer().getPluginManager().registerEvents(editor, this);
        ItemizerXCommand dispatcher = new ItemizerXCommand();
        ItemizerXTab tab = new ItemizerXTab();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        ItemizerXBrigadierCommand.create(dispatcher, tab),
                        "The main command for ItemizerX",
                        List.of("ii", "it")));
        getLogger().info("Using the registry-backed Bukkit item API; no NMS adapter is required for " + getServerVersion() + ".");
    }

    public String getServerVersion()
    {
        return Bukkit.getServer().getMinecraftVersion();
    }

    static boolean isSupportedVersion(String version)
    {
        return version.equals("26.1") || version.startsWith("26.1.") || version.equals("26.2");
    }

    @Override
    public void onDisable()
    {
        if (presets != null)
        {
            presets.flush();
        }
        plugin = null;
    }
}
