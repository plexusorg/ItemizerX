package dev.plex.itemizerx.nms.player.impl;

import dev.plex.itemizerx.nms.player.AbstractNMSPlayer;
import org.bukkit.entity.Player;

public class v1_17_1_NMSPlayer extends AbstractNMSPlayer {
    public v1_17_1_NMSPlayer(final Player player) {
        super(player);
    }

    @Override
    public void sendMessage(final String message) {
        this.player.sendMessage(message);
    }
}
