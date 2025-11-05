package dev.plex.itemizerx;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum Attributes
{
    MAX_HEALTH("generic.max_health", 0, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH),
    FOLLOW_RANGE("generic.follow_range", 1, net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE),
    KNOCKBACK_RESISTANCE("generic.knockback_resistance", 1, net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE),
    MOVEMENT_SPEED("generic.movement_speed", 1, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED),
    FLYING_SPEED("generic.flying_speed", 1, net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED),
    DAMAGE("generic.attack_damage", 0, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
    ATTACK_KNOCKBACK("generic.attack_knockback", 0, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK),
    ATTACK_SPEED("generic.attack_speed", 1, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED),
    ARMOR("generic.armor", 0, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR),
    ARMOR_TOUGHNESS("generic.armor_toughness", 0, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),
    LUCK("generic.luck", 0, net.minecraft.world.entity.ai.attributes.Attributes.LUCK),
    JUMP_STRENGTH("generic.jump_strength", 1, net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH),
    ZOMBIE_REINFORCEMENTS("zombie.spawn_reinforcements", 1, net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE);

    public final String mcName;
    public final int op;
    public final Holder<Attribute> attributeHolder;

    Attributes(String mcName, int op, Holder<Attribute> attributeHolder)
    {
        this.mcName = mcName;
        this.op = op;
        this.attributeHolder = attributeHolder;
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