package dev.plex.itemizerx;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

import java.util.Locale;

/** Looks attributes up in the server's registry, so datapack and new vanilla attributes work without a release. */
final class Attributes
{
    private Attributes()
    {
    }

    static Attribute get(String input)
    {
        String name = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        return key == null ? null : Registry.ATTRIBUTE.get(key);
    }

    static String all()
    {
        return String.join(", ", Registry.ATTRIBUTE.keyStream().map(NamespacedKey::toString).sorted().toList());
    }
}
