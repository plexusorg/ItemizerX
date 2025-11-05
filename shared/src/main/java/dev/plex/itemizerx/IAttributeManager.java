package dev.plex.itemizerx;

import org.bukkit.entity.Player;

public interface IAttributeManager
{
    void addAttr(final Player player, final String[] args);

    void removeAttr(final Player player, final String string);

    void listAttr(final Player player);
}
