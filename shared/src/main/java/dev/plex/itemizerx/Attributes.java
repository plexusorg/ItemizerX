package dev.plex.itemizerx;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

import java.util.List;
import java.util.Locale;

/**
 * Registry-backed attribute lookup. This intentionally contains no fixed list: every attribute
 * supplied by the running Minecraft version (including datapack entries and new 26.x attributes)
 * is available automatically.
 */
public final class Attributes
{
    private Attributes()
    {
    }

    public static Attribute get(String input)
    {
        String name = input.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        Attribute attribute = key == null ? null : Registry.ATTRIBUTE.get(key);
        if (attribute != null)
        {
            return attribute;
        }

        // Accept the pre-1.21 names that ItemizerX historically exposed.
        String legacyName = name.replace("minecraft:", "")
                .replaceFirst("^(generic|player|zombie)\\.", "");
        legacyName = switch (legacyName)
        {
            case "damage" -> "attack_damage";
            case "zombie_reinforcements", "spawn_reinforcements" -> "spawn_reinforcements_chance";
            default -> legacyName;
        };
        return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(legacyName));
    }

    public static String getAttributes()
    {
        return String.join(", ", getAttributeList());
    }

    public static List<String> getAttributeList()
    {
        return Registry.ATTRIBUTE.keyStream()
                .map(NamespacedKey::toString)
                .sorted()
                .toList();
    }
}
