package dev.plex.itemizerx;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemizerX extends JavaPlugin
{
    public static ItemizerX plugin;
    CoreProtectBridge cpb = new CoreProtectBridge();
    IAttributeManager attr;
    IEnchantmentManager ench;

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
            case "1.21.8" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_21_R1.AttributeManager();
                ench = new dev.plex.itemizerx.v1_21_R1.EnchantmentManager();
            }
            default ->
            {
                getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
                getLogger().severe("ItemizerX only supports version 1.21.8, disabling plugin.");
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
