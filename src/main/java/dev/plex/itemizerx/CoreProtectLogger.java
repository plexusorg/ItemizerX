package dev.plex.itemizerx;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Names CoreProtect types, so ItemizerX loads it only when CoreProtect is enabled. */
final class CoreProtectLogger implements BlockLogger
{
    private final CoreProtectAPI api;

    private CoreProtectLogger(CoreProtectAPI api)
    {
        this.api = api;
    }

    static CoreProtectLogger find(Plugin plugin)
    {
        if (!(plugin.getServer().getPluginManager().getPlugin("CoreProtect") instanceof CoreProtect coreProtect))
        {
            return null;
        }
        CoreProtectAPI api = coreProtect.getAPI();
        return api.isEnabled() ? new CoreProtectLogger(api) : null;
    }

    @Override
    public void signEdited(Player player, BlockState before, BlockState after)
    {
        // The BlockState forms carry the sign's text, so a rollback restores the old lines.
        api.logRemoval(player.getName(), before);
        api.logPlacement(player.getName(), after);
    }
}
