package dev.plex.itemizerx;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class ItemizerX extends JavaPlugin
{
    final ItemHistory history = new ItemHistory();
    final AttributeManager attributes = new AttributeManager();
    final EnchantmentManager enchantments = new EnchantmentManager();
    private final List<BlockLogger> blockLoggers = new ArrayList<>();
    PresetManager presets;
    ItemEditor editor;
    ItemDialog dialog;

    @Override
    public void onEnable()
    {
        // Metrics at https://bstats.org/plugin/bukkit/ItemizerX/19104
        new Metrics(this, 19104);
        findBlockLoggers();

        presets = new PresetManager(this);
        editor = new ItemEditor(this);
        dialog = new ItemDialog(this);
        getServer().getPluginManager().registerEvents(editor, this);
        getServer().getPluginManager().registerEvents(dialog, this);

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

    void logSignEdit(Player player, BlockState before, BlockState after)
    {
        blockLoggers.forEach(logger -> logger.signEdited(player, before, after));
    }

    private void findBlockLoggers()
    {
        if (getServer().getPluginManager().isPluginEnabled("Oasis"))
        {
            blockLoggers.add(new OasisLogger(this));
            getLogger().info("Logging sign edits to Oasis.");
        }
        if (getServer().getPluginManager().isPluginEnabled("CoreProtect"))
        {
            BlockLogger coreProtect = CoreProtectLogger.find(this);
            if (coreProtect != null)
            {
                blockLoggers.add(coreProtect);
                getLogger().info("Logging sign edits to CoreProtect.");
            }
        }
    }
}
