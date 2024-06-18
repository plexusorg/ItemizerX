package dev.plex.itemizerx.v1_20_R4;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.StringUtils;

public enum Attributes_v1_20_R4
{
    MAX_HEALTH("generic.max_health", 0, Attributes.MAX_HEALTH),
    FOLLOW_RANGE("generic.follow_range", 1, Attributes.FOLLOW_RANGE),
    KNOCKBACK_RESISTANCE("generic.knockback_resistance", 1, Attributes.KNOCKBACK_RESISTANCE),
    MOVEMENT_SPEED("generic.movement_speed", 1, Attributes.MOVEMENT_SPEED),
    FLYING_SPEED("generic.flying_speed", 1, Attributes.FLYING_SPEED),
    DAMAGE("generic.attack_damage", 0, Attributes.ATTACK_DAMAGE),
    ATTACK_KNOCKBACK("generic.attack_knockback", 0, Attributes.ATTACK_KNOCKBACK),
    ATTACK_SPEED("generic.attack_speed", 1, Attributes.ATTACK_SPEED),
    ARMOR("generic.armor", 0, Attributes.ARMOR),
    ARMOR_TOUGHNESS("generic.armor_toughness", 0, Attributes.ARMOR_TOUGHNESS),
    LUCK("generic.luck", 0, Attributes.LUCK),
    JUMP_STRENGTH("generic.jump_strength", 1, Attributes.JUMP_STRENGTH),
    ZOMBIE_REINFORCEMENTS("zombie.spawn_reinforcements", 1, Attributes.SPAWN_REINFORCEMENTS_CHANCE);

    public final String mcName;
    public final int op;
    public final Holder<Attribute> attributeHolder;

    Attributes_v1_20_R4(String mcName, int op, Holder<Attribute> attributeHolder)
    {
        this.mcName = mcName;
        this.op = op;
        this.attributeHolder = attributeHolder;
    }

    public static Attributes_v1_20_R4 get(String name)
    {
        for (Attributes_v1_20_R4 attr : values())
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
        for (Attributes_v1_20_R4 attr : values())
        {
            attributes.add(attr.name());
        }
        return attributes;
    }
}
