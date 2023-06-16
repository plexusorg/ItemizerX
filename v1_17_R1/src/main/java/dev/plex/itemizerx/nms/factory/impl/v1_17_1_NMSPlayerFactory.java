package dev.plex.itemizerx.nms.factory.impl;

import dev.plex.itemizerx.nms.player.NMSPlayer;
import dev.plex.itemizerx.nms.factory.NMSPlayerFactory;
import dev.plex.itemizerx.nms.player.impl.v1_17_1_NMSPlayer;
import org.bukkit.entity.Player;

public class v1_17_1_NMSPlayerFactory implements NMSPlayerFactory {
    @Override
    public NMSPlayer get(final Player from) {
        return new v1_17_1_NMSPlayer(from);
    }
}
