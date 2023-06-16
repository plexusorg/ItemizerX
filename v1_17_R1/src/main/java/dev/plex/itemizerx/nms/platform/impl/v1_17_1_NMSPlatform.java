package dev.plex.itemizerx.nms.platform.impl;

import dev.plex.itemizerx.nms.AbstractNMSPlatform;
import dev.plex.itemizerx.nms.attribute.impl.v1_17_1_NMSAttributeManager;
import dev.plex.itemizerx.nms.factory.impl.v1_17_1_NMSPlayerFactory;
import dev.plex.itemizerx.nms.registry.PlatformRegistry;
import org.bukkit.Material;

public class v1_17_1_NMSPlatform extends AbstractNMSPlatform {
    public v1_17_1_NMSPlatform() {
        super(new v1_17_1_NMSPlayerFactory(), new v1_17_1_NMSAttributeManager());
    }

    @Override
    public Material getLingeringPotionMaterial() {
        return Material.LINGERING_POTION;
    }

    @Override
    public Material getSplashPotionMaterial() {
        return Material.SPLASH_POTION;
    }

    @Override
    public void register() {
        PlatformRegistry.registerPlatform("1.17.1", this);
    }
}
