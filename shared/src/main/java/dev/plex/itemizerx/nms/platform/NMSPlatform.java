package dev.plex.itemizerx.nms.platform;

import dev.plex.itemizerx.nms.attribute.NMSAttributeManager;
import dev.plex.itemizerx.nms.player.NMSPlayer;
import dev.plex.itemizerx.nms.factory.Factory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface NMSPlatform {
    Material getLingeringPotionMaterial();
    Material getSplashPotionMaterial();
    // In-case md5 decides to change the position of air in the Material enum
    default Material getAirMaterial() {
        return Material.AIR;
    }
    void register();
    Factory<NMSPlayer, Player> getPlayerFactory();
    NMSAttributeManager getAttributeManager();
}
