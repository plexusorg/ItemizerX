package dev.plex.itemizerx.v1_21_R1;

import dev.plex.itemizerx.IEnchantmentManager;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentManager implements IEnchantmentManager {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void addEnchantment(Player player, ItemStack item, String[] args) {
        if (args.length < 4)
        {
            player.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Enchant Commands<white>]<dark_aqua>==============="));
            player.sendMessage(mm.deserialize("<aqua>/itemizer enchant add <<white>name<aqua>> <<white>level<aqua>> <red>- <gold>Add an enchantment"));
            return;
        }

        EnchantmentMap map = EnchantmentMap.fromName(args[2]);
        if (map == null) {
            player.sendMessage(mm.deserialize("<dark_red>The <white>'" + args[2] + "<white>' <dark_red>enchantment does not exist!"));
            return;
        }

        Enchantment enchantment = map.asEnchantment();
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (meta.getEnchants().containsKey(enchantment))
        {
            player.sendMessage(mm.deserialize("<dark_red>This item already have the <white>'" + enchantment.getKey().getKey() + "' <dark_red>enchantment!"));
            return;
        }

        int level;
        try
        {
            level = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException ex)
        {
            player.sendMessage(mm.deserialize("<white>\"" + args[3] + "<white>\"<dark_red> is not a valid number!"));
            return;
        }

        item.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(mm.deserialize("<dark_green>The enchant <white>'" + enchantment.getKey().getKey() + "' <dark_green>has been added to your item"));
    }

    @Override
    public void removeEnchantment(Player player, ItemStack item, String[] args) {
        if (args.length == 2)
        {
            player.sendMessage(mm.deserialize("<dark_aqua>===============<white>[<light_purple>Enchant Commands<white>]<dark_aqua>==============="));
            player.sendMessage(mm.deserialize("<aqua>/itemizer enchant remove <<white>name<aqua>> <red>- <gold>Remove an enchantment"));
            return;
        }

        EnchantmentMap map = EnchantmentMap.fromName(args[2]);
        if (map == null) {
            player.sendMessage(mm.deserialize("<dark_red>The <white>'" + args[2] + "<white>' <dark_red> enchantment does not exist!"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (Objects.requireNonNull(meta.getEnchants()).isEmpty())
        {
            player.sendMessage(mm.deserialize("<dark_red>This item doesn't hold any enchants"));
            return;
        }

        Enchantment enchantment = map.asEnchantment();
        if (!meta.getEnchants().containsKey(enchantment))
        {
            player.sendMessage(mm.deserialize("<dark_red>This item doesn't have the <white>'" + enchantment.getKey().getKey() + "' <dark_red>enchantment!"));
            return;
        }
        item.removeEnchantment(enchantment);
        player.sendMessage(mm.deserialize("<dark_green>The <white>'" + enchantment.getKey().getKey() + "' <dark_green>enchantment has been removed from your item"));
    }

    private enum EnchantmentMap {
        PROTECTION(Enchantment.PROTECTION),
        FIRE_PROTECTION(Enchantment.FIRE_PROTECTION),
        FEATHER_FALLING(Enchantment.FEATHER_FALLING),
        BLAST_PROTECTION(Enchantment.BLAST_PROTECTION),
        PROJECTILE_PROTECTION(Enchantment.PROJECTILE_PROTECTION),
        RESPIRATION(Enchantment.RESPIRATION),
        AQUA_AFFINITY(Enchantment.AQUA_AFFINITY),
        THORNS(Enchantment.THORNS),
        DEPTH_STRIDER(Enchantment.DEPTH_STRIDER),
        FROST_WALKER(Enchantment.FROST_WALKER),
        BINDING_CURSE(Enchantment.BINDING_CURSE),
        SHARPNESS(Enchantment.SHARPNESS),
        SMITE(Enchantment.SMITE),
        BANE_OF_ARTHROPODS(Enchantment.BANE_OF_ARTHROPODS),
        KNOCKBACK(Enchantment.KNOCKBACK),
        FIRE_ASPECT(Enchantment.FIRE_ASPECT),
        LOOTING(Enchantment.LOOTING),
        SWEEPING_EDGE(Enchantment.SWEEPING_EDGE),
        EFFICIENCY(Enchantment.EFFICIENCY),
        SILK_TOUCH(Enchantment.SILK_TOUCH),
        UNBREAKING(Enchantment.UNBREAKING),
        FORTUNE(Enchantment.FORTUNE),
        POWER(Enchantment.POWER),
        PUNCH(Enchantment.PUNCH),
        FLAME(Enchantment.FLAME),
        INFINITY(Enchantment.INFINITY),
        LUCK_OF_THE_SEA(Enchantment.LUCK_OF_THE_SEA),
        LURE(Enchantment.LURE),
        LOYALTY(Enchantment.LOYALTY),
        IMPALING(Enchantment.IMPALING),
        RIPTIDE(Enchantment.RIPTIDE),
        CHANNELING(Enchantment.CHANNELING),
        MULTISHOT(Enchantment.MULTISHOT),
        QUICK_CHARGE(Enchantment.QUICK_CHARGE),
        PIERCING(Enchantment.PIERCING),
        DENSITY(Enchantment.DENSITY),
        BREACH(Enchantment.BREACH),
        WIND_BURST(Enchantment.WIND_BURST),
        MENDING(Enchantment.MENDING),
        VANISHING_CURSE(Enchantment.VANISHING_CURSE),
        SOUL_SPEED(Enchantment.SOUL_SPEED),
        SWIFT_SNEAK(Enchantment.SWIFT_SNEAK);

        private final Enchantment enchantment;

        EnchantmentMap(Enchantment enchantment) {
            this.enchantment = enchantment;
        }

        public static EnchantmentMap fromName(String name) {
            for (EnchantmentMap map : values()) {
                if (map.name().equalsIgnoreCase(name)) {
                    return map;
                }
            }
            return null;
        }

        public Enchantment asEnchantment() {
            return enchantment;
        }
    }
}
