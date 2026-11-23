package dev.plex.itemizerx;

import com.oasis.api.Cause;
import com.oasis.api.Oasis;
import com.oasis.api.OasisApi;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Names Oasis types, so ItemizerX loads it only when Oasis is enabled. */
final class OasisLogger implements BlockLogger
{
    private final OasisApi api;

    OasisLogger(Plugin plugin)
    {
        this.api = Oasis.api(plugin);
    }

    @Override
    public void signEdited(Player player, BlockState before, BlockState after)
    {
        api.logger().by(player).block(Cause.SIGN, before, after);
    }
}
