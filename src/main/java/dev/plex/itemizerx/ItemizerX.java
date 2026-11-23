package dev.plex.itemizerx;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ItemizerX extends JavaPlugin
{
    final ItemHistory history = new ItemHistory();
    final AttributeManager attributes = new AttributeManager();
    final EnchantmentManager enchantments = new EnchantmentManager();
    PresetManager presets;
    ItemEditor editor;
    ItemDialog dialog;

    @Override
    public void onEnable()
    {
        // Metrics at https://bstats.org/plugin/bukkit/ItemizerX/19104
        new Metrics(this, 19104);

        presets = new PresetManager(this);
        editor = new ItemEditor(this);
        dialog = new ItemDialog(this);
        getServer().getPluginManager().registerEvents(editor, this);
        getServer().getPluginManager().registerEvents(dialog, this);
        getServer().getPluginManager().registerEvents(presets, this);

        ItemizerXCommand command = new ItemizerXCommand(this);
        ItemizerXTab tab = new ItemizerXTab(this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        ItemizerXBrigadierCommand.create(this, command, tab),
                        "The main command for ItemizerX",
                        List.of("ii", "it")));
    }

    @Override
    public void onDisable()
    {
        if (presets != null)
        {
            presets.flush();
        }
    }

}
