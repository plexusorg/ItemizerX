package dev.plex.itemizerx.v1_21_R1;

import dev.plex.itemizerx.Attributes;
import dev.plex.itemizerx.IAttributeManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class AttributeManager implements IAttributeManager {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void addAttr(Player player, String[] args) {
        if (args.length < 4)
        {
            player.sendMessage(mm.deserialize("<aqua>/itemizer attr add <<white>name<aqua>> <<white>strength<aqua>>" +
                " [<white>slot<aqua>] <red>- <gold>Add an attribute"));
            return;
        }

        Attributes a = Attributes.get(args[2]);
        if (a == null)
        {
            player.sendMessage(mm.deserialize("<dark_red>\"" + args[2] + "\" is not a valid attribute type."));
            return;
        }

        double amount;
        try
        {
            amount = Double.parseDouble(args[3]);
        }
        catch (NumberFormatException ex)
        {
            player.sendMessage(mm.deserialize("<dark_red>\"" + args[3] + "\" is not a valid number."));
            return;
        }

        if (Double.isNaN(amount))
        {
            player.sendMessage(mm.deserialize("<dark_red>Please do not use <white>'NaN (Not a Number)'"));
            return;
        }

        ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        final List<Entry> entries = nms.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers();
        for (Entry entry : entries)
        {
            if (entry.attribute().getRegisteredName().equalsIgnoreCase(args[2]))
            {
                player.sendMessage(mm.deserialize("<dark_red>An attribute with the name \"<white>" + args[2] + "<dark_red>\" already exists!"));
                return;
            }
        }

        AtomicReference<EquipmentSlotGroup> group = new AtomicReference<>(EquipmentSlotGroup.ANY);
        if (args.length == 5)
        {
            EquipmentSlot slot;
            try
            {
                slot = EquipmentSlot.byName(args[4].toLowerCase());
            }
            catch (IllegalArgumentException ignored)
            {
                player.sendMessage(mm.deserialize("<dark_green>Supported options:"));
                player.sendMessage(mm.deserialize("<yellow>" + StringUtils.join(
                    Arrays.stream(EquipmentSlot.values()).map(s -> s.getName().toLowerCase()).toArray(), ", ")));
                return;
            }

            group.set(EquipmentSlotGroup.bySlot(slot));
        }

        final ResourceLocation locationID = ResourceLocation.fromNamespaceAndPath("itemizerx", String.format("modifier_%s", a.mcName));
        final AttributeModifier modifier = new AttributeModifier(locationID, amount, Operation.BY_ID.apply(a.op));
        nms.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, x ->
            x.withModifierAdded(a.attributeHolder, modifier, group.get()));
        final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
        player.getInventory().setItemInMainHand(is);
        player.sendMessage(mm.deserialize("<dark_aqua>Attribute added!"));
    }

    @Override
    public void removeAttr(Player player, String string) {
        Attributes a = Attributes.get(string);
        if (a == null)
        {
            player.sendMessage(mm.deserialize("<dark_red>\"" + string + "\" is not a valid attribute type."));
            return;
        }

        ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());
        ItemAttributeModifiers attrModifiers = nms.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        final List<Entry> entries = attrModifiers.modifiers();
        Optional<Entry> optionalEntry = entries.stream().filter(e -> e.attribute().equals(a.attributeHolder)).findFirst();

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        optionalEntry.ifPresentOrElse(entry -> {
            entries.forEach(e -> {
                if (e.attribute().equals(a.attributeHolder)) {
                    return;
                }

                builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
            });

            nms.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            final org.bukkit.inventory.ItemStack is = CraftItemStack.asCraftMirror(nms);
            player.getInventory().setItemInMainHand(is);
            player.sendMessage(mm.deserialize("<dark_aqua>Attribute removed!"));
        }, () -> player.sendMessage(mm.deserialize("<dark_red>The attribute \"" + string + "\" doesn't exist!")));


    }

    @Override
    public void listAttr(Player player) {
        final ItemStack nms = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());

        List<Entry> entries = nms.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers();
        if (entries.isEmpty()) {
            player.sendMessage(mm.deserialize("<yellow>This item has no attributes."));
            return;
        }

        player.sendMessage(mm.deserialize("<dark_green>Item attributes: "));
        entries.forEach(entry -> {
            player.sendMessage(mm.deserialize("<yellow>" + entry.attribute().getRegisteredName() + ", " + entry.modifier().amount() + ", " + entry.slot()));
        });
    }
}
