package dev.plex.itemizerx;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

final class Messages
{
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

    static void usage(Audience audience, String message)
    {
        audience.sendMessage(Component.text(message, NamedTextColor.AQUA));
    }
}
