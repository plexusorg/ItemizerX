package dev.plex.itemizerx;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemizerX extends JavaPlugin
{
    public static ItemizerX plugin;
    CoreProtectBridge cpb = new CoreProtectBridge();
    IAttributeManager attr;

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
        getCommand("itemizer").setTabCompleter(new ItemizerXTab());
        switch (getServerVersion())
        {
            /*case "1.21" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_21_R1.AttributeManager();
            }
            case "1.20.5", "1.20.6" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_20_R4.AttributeManager();
            }*/
            case "1.20.4" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_20_R3.AttributeManager();
            }
            case "1.20.3", "1.20.2" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_20_R2.AttributeManager();
            }
            default ->
            {
                getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
                getLogger().severe("ItemizerX only supports versions 1.20.2 to 1.21, disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public String getServerVersion()
    {
        return Bukkit.getServer().getMinecraftVersion();
    }

    @Override
    public void onDisable()
    {
        plugin = null;
    }
}
