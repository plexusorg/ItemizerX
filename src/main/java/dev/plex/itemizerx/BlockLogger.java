package dev.plex.itemizerx;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/** Logs the block edits ItemizerX makes to a block-logging plugin, so staff can look them up and roll them back. */
interface BlockLogger
{
    /** Call on the main thread with the state taken before the edit and the one taken after it. */
    void signEdited(Player player, BlockState before, BlockState after);
}
