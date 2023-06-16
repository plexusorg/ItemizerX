package dev.plex.itemizerx.nms.player;

import org.bukkit.entity.Player;

public abstract class AbstractNMSPlayer implements NMSPlayer {
    protected final Player player;

    protected AbstractNMSPlayer(final Player player) {
        this.player = player;
    }
}
