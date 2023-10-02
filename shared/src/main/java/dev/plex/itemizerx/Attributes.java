package dev.plex.itemizerx;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum Attributes
{
    MAX_HEALTH("generic.max_health", 0),
    FOLLOW_RANGE("generic.follow_range", 1),
    KNOCKBACK_RESISTANCE("generic.knockback_resistance", 1),
    MOVEMENT_SPEED("generic.movement_speed", 1),
    FLYING_SPEED("generic.flying_speed", 1),
    DAMAGE("generic.attack_damage", 0),
    ATTACK_KNOCKBACK("generic.attack_knockback", 0),
    ATTACK_SPEED("generic.attack_speed", 1),
    ARMOR("generic.armor", 0),
    ARMOR_TOUGHNESS("generic.armor_toughness", 0),
    LUCK("generic.luck", 0),
    HORSE_JUMP("horse.jump_strength", 1),
    ZOMBIE_REINFORCEMENTS("zombie.spawn_reinforcements", 1);

    public final String mcName;
    public final int op;

    Attributes(String mcName, int op)
    {
        this.mcName = mcName;
        this.op = op;
    }

    public static Attributes get(String name)
    {
        for (Attributes attr : values())
        {
            if (attr.name().equalsIgnoreCase(name) || attr.mcName.equalsIgnoreCase(name.replace("minecraft:", "")))
            {
                return attr;
            }
        }
        return null;
    }

    public static String getAttributes()
    {
        return StringUtils.join(values(), ", ");
    }

    public static List<String> getAttributeList()
    {
        List<String> attributes = new ArrayList<>();
        for (Attributes attr : values())
        {
            attributes.add(attr.name());
        }
        return attributes;
    }
}