package dev.plex.itemizerx;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Registers each subcommand as a Brigadier literal with one greedy argument, then hands the words
 * to {@link ItemizerXCommand}. Every edit of the held item made through a command goes into undo
 * history.
 */
final class ItemizerXBrigadierCommand
{
    private static final int MAX_ITEM_BYTES = 32 * 1024;
    private static final Set<String> SUBCOMMANDS = new LinkedHashSet<>(ItemizerXTab.ROOT);

    static
    {
        SUBCOMMANDS.addAll(List.of("components", "properties", "nbt", "pdc", "contents", "presets", "editor"));
    }

    private ItemizerXBrigadierCommand()
    {
    }

    static LiteralCommandNode<CommandSourceStack> create(ItemizerX plugin, ItemizerXCommand command, ItemizerXTab tab)
    {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("itemizer")
                .requires(source -> source.getSender().hasPermission("itemizer.use"))
                .executes(context -> dispatch(context, plugin, command));

        for (String subcommand : SUBCOMMANDS)
        {
            root.then(Commands.literal(subcommand)
                    .executes(context -> dispatch(context, plugin, command))
                    .then(Commands.argument("arguments", StringArgumentType.greedyString())
                            .suggests((context, builder) -> suggest(context, builder, tab))
                            .executes(context -> dispatch(context, plugin, command))));
        }
        return root.build();
    }

    private static int dispatch(CommandContext<CommandSourceStack> context, ItemizerX plugin, ItemizerXCommand command)
    {
        String[] arguments = arguments(context.getInput(), false);
        Player player = context.getSource().getSender() instanceof Player value ? value : null;
        ItemStack before = player == null ? null : player.getInventory().getItemInMainHand().clone();
        command.execute(context.getSource().getSender(), arguments);
        if (player == null)
        {
            return Command.SINGLE_SUCCESS;
        }

        ItemStack after = player.getInventory().getItemInMainHand();
        int size = ItemHistory.componentBytes(after);
        if (size > MAX_ITEM_BYTES && size > ItemHistory.componentBytes(before))
        {
            // Items this large can kick the players who load them, for example from a chest or an item frame.
            player.getInventory().setItemInMainHand(before);
            Messages.error(player, "That edit would make the item's data larger than " + MAX_ITEM_BYTES / 1024
                    + " KiB, so it was undone.");
            return Command.SINGLE_SUCCESS;
        }
        if (shouldRecord(arguments))
        {
            plugin.history.record(player, before, after.clone());
        }
        return Command.SINGLE_SUCCESS;
    }

    private static boolean shouldRecord(String[] arguments)
    {
        return arguments.length > 0
                && !arguments[0].equalsIgnoreCase("undo")
                && !arguments[0].equalsIgnoreCase("redo");
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context,
                                                           SuggestionsBuilder builder, ItemizerXTab tab)
    {
        String input = context.getInput();
        SuggestionsBuilder currentWord = builder.createOffset(input.lastIndexOf(' ') + 1);
        for (String suggestion : tab.complete(context.getSource().getSender(), arguments(input, true)))
        {
            currentWord.suggest(suggestion);
        }
        return currentWord.buildFuture();
    }

    private static String[] arguments(String input, boolean preserveTrailingArgument)
    {
        int firstSpace = input.indexOf(' ');
        if (firstSpace < 0 || firstSpace == input.length() - 1 && !preserveTrailingArgument)
        {
            return new String[0];
        }

        String remainder = input.substring(firstSpace + 1);
        if (remainder.isEmpty())
        {
            return preserveTrailingArgument ? new String[]{""} : new String[0];
        }
        return remainder.split("\\s+", preserveTrailingArgument ? -1 : 0);
    }
}
