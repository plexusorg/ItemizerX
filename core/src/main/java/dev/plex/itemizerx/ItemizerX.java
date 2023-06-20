package dev.plex.itemizerx;

import org.bukkit.plugin.java.JavaPlugin;

public class ItemizerX extends JavaPlugin {

    public static ItemizerX plugin;
    CoreProtectBridge cpb = new CoreProtectBridge();
    IAttributeManager attr;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        plugin = this;
        cpb.getCoreProtect();
        getCommand("itemizer").setExecutor(new ItemizerXCommand());
        getCommand("itemizer").setTabCompleter(new ItemizerXTab());
        switch (getNMSVersion()) {
            case "v1_20_R1": {
                attr = new dev.plex.itemizerx.v1_20_R1.AttributeManager();
                return;
            }
            case "v1_19_R3": {
                attr = new dev.plex.itemizerx.v1_19_R3.AttributeManager();
                return;
            }
            case "v1_19_R2": {
                attr = new dev.plex.itemizerx.v1_19_R2.AttributeManager();
                return;
            }
            case "v1_19_R1": {
                attr = new dev.plex.itemizerx.v1_19_R1.AttributeManager();
                return;
            }
            case "v1_18_R2": {
                attr = new dev.plex.itemizerx.v1_18_R2.AttributeManager();
                return;
            }
            case "v1_18_R1": {
                attr = new dev.plex.itemizerx.v1_18_R1.AttributeManager();
                return;
            }
            case "v1_17_R1": {
                attr = new dev.plex.itemizerx.v1_17_R1.AttributeManager();
                return;
            }
            default: {
                getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
                getLogger().severe("ItemizerX only supports versions 1.17 to 1.20.1, disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public String getNMSVersion() {
        String v = getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    @Override
    public void onDisable() {
        plugin = null;
    }
}
