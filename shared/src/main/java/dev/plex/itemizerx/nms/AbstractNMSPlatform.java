package dev.plex.itemizerx.nms;

import dev.plex.itemizerx.nms.attribute.NMSAttributeManager;
import dev.plex.itemizerx.nms.factory.Factory;
import dev.plex.itemizerx.nms.platform.NMSPlatform;
import dev.plex.itemizerx.nms.player.NMSPlayer;
import org.bukkit.entity.Player;

public abstract class AbstractNMSPlatform implements NMSPlatform {
    protected final Factory<NMSPlayer, Player> playerFactory;
    protected final NMSAttributeManager attributeManager;
    protected AbstractNMSPlatform(final Factory<NMSPlayer, Player> playerFactory, final NMSAttributeManager attributeManager) {
        this.playerFactory = playerFactory;
        this.attributeManager = attributeManager;
    }

    @Override
    public Factory<NMSPlayer, Player> getPlayerFactory() {
        return this.playerFactory;
    }

    @Override
    public NMSAttributeManager getAttributeManager() {
        return this.attributeManager;
    }
}
