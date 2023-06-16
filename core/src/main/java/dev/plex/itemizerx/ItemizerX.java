package dev.plex.itemizerx;

import dev.plex.itemizerx.command.ItemizerXCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemizerX extends JavaPlugin {

    public static ItemizerX plugin;
    public final CoreProtectBridge cpb = new CoreProtectBridge();

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        plugin = this;
        cpb.getCoreProtect();
        switch (getNMSVersion()) {
            case "v1_20_R1": {
                getCommand("itemizer").setExecutor(new ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_20_R1.ItemizerXTab());
                return;
            }
            case "v1_19_R3": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_19_R3.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_19_R3.ItemizerXTab());
                return;
            }
            case "v1_19_R2": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_19_R2.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_19_R2.ItemizerXTab());
                return;
            }
            case "v1_19_R1": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_19_R1.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_19_R1.ItemizerXTab());
                return;
            }
            case "v1_18_R2": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_18_R2.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_18_R2.ItemizerXTab());
                return;
            }
            case "v1_18_R1": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_18_R1.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_18_R1.ItemizerXTab());
                return;
            }
            case "v1_17_R1": {
                getCommand("itemizer").setExecutor(new dev.plex.itemizerx.v1_17_R1.ItemizerXCommand());
                getCommand("itemizer").setTabCompleter(new dev.plex.itemizerx.v1_17_R1.ItemizerXTab());
                return;
            }
            default: {
                getLogger().severe("You are trying to run ItemizerX on an incompatible server version.");
                getLogger().severe("ItemizerX only supports versions 1.17 to 1.20, disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable() {
        plugin = null;
    }
}
