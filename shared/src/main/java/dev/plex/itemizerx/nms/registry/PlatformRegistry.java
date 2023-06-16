package dev.plex.itemizerx.nms.registry;

import com.google.common.reflect.ClassPath;
import dev.plex.itemizerx.nms.platform.NMSPlatform;
import dev.plex.itemizerx.nms.registry.exception.PlatformLoadException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PlatformRegistry {
    private static final Map<String, NMSPlatform> PLATFORM_MAP = new Object2ObjectArrayMap<>();

    public static void init() throws PlatformLoadException {
        final ClassPath classPath;

        try {
            classPath = ClassPath.from(PlatformRegistry.class.getClassLoader());
        } catch (IOException e) {
            throw new PlatformLoadException("Failed to get class path of platform registry", e);
        }

        final Set<ClassPath.ClassInfo> classInfoSet = classPath.getTopLevelClasses("dev.plex.itemizerx.nms.factory.impl");
        final Class<NMSPlatform> nmsPlatformClass = NMSPlatform.class;

        for (final ClassPath.ClassInfo classInfo : classInfoSet) {
            final Class<?> loadedClass = classInfo.load();

            if (!nmsPlatformClass.isAssignableFrom(loadedClass)) {
                continue;
            }

            final Class<? extends NMSPlatform> platformImplementation = loadedClass.asSubclass(nmsPlatformClass);

            try {
                platformImplementation.getDeclaredConstructor().newInstance().register();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new PlatformLoadException("Failed to construct NMS platform", e);
            }
        }
    }

    public static void registerPlatform(final String minecraftVersion, final NMSPlatform nmsPlatform) {
        PLATFORM_MAP.put(minecraftVersion, nmsPlatform);
    }

    public static Optional<NMSPlatform> getPlatform(final String minecraftVersion) {
        return Optional.ofNullable(PLATFORM_MAP.get(minecraftVersion));
    }
}
