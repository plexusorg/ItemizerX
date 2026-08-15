package dev.plex.itemizerx;

import org.bukkit.entity.Player;

public interface IAttributeManager
{
    void addAttr(final Player player, final String[] args);

    void editAttr(final Player player, final String[] args);

    void removeAttr(final Player player, final String[] args);

    void listAttr(final Player player);
}
