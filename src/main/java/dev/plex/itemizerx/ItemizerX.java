package dev.plex.itemizerx;

import org.bstats.bukkit.Metrics;
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
        switch (getNMSVersion())
        {
            case "v1_20_R1" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_20_R1.AttributeManager();
            }
            case "v1_19_R3" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_19_R3.AttributeManager();
            }
            case "v1_19_R2" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_19_R2.AttributeManager();
            }
            case "v1_19_R1" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_19_R1.AttributeManager();
            }
            case "v1_18_R2" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                attr = new dev.plex.itemizerx.v1_18_R2.AttributeManager();
            }
            case "v1_18_R1" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCompatCommand());
                attr = new dev.plex.itemizerx.v1_18_R1.AttributeManager();

            }
            case "v1_17_R1" ->
            {
                getCommand("itemizer").setExecutor(new ItemizerXCompatCommand());
                attr = new dev.plex.itemizerx.v1_17_R1.AttributeManager();
            }
            default ->
            {
                getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
                getLogger().severe("ItemizerX only supports versions 1.17.1 to 1.20.1, disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public String getNMSVersion()
    {
        String v = getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    @Override
    public void onDisable()
    {
        plugin = null;
    }
}
