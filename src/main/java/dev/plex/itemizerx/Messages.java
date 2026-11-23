package dev.plex.itemizerx;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Messages
{
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern ARGUMENT = Pattern.compile("<([^>]+)>|\\[([^\\]]+)]");

    private Messages()
    {
    }

    static void success(Audience audience, String message)
    {
        audience.sendMessage(Component.text(message, NamedTextColor.DARK_GREEN));
    }

    static void error(Audience audience, String message)
    {
        audience.sendMessage(Component.text(message, NamedTextColor.DARK_RED));
    }

    static void info(Audience audience, String message)
    {
        audience.sendMessage(Component.text(message, NamedTextColor.YELLOW));
    }

    /** A help screen: a title bar, then one MiniMessage line each, such as {@link #command} builds. */
    static void help(Audience audience, String title, String... lines)
    {
        audience.sendMessage(MM.deserialize("<dark_aqua>===============<white>[<light_purple>" + title
                + "<white>]<dark_aqua>==============="));
        for (String line : lines)
        {
            audience.sendMessage(MM.deserialize(line));
        }
    }

    /** One help line for {@code /itemizer <syntax>}, with its {@code <required>} and {@code [optional]} arguments in white. */
    static String command(String syntax, String description)
    {
        Matcher matcher = ARGUMENT.matcher(syntax);
        StringBuilder line = new StringBuilder("<aqua>/itemizer ");
        while (matcher.find())
        {
            String replacement = matcher.group(1) != null
                    ? "<<white>" + matcher.group(1) + "<aqua>>"
                    : "[<white>" + matcher.group(2) + "<aqua>]";
            matcher.appendReplacement(line, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(line);
        return line.append(" <red>- <gold>").append(description).toString();
    }
}
