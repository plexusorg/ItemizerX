package dev.plex.itemizerx.nms.factory;

public interface Factory<T, U> {
    T get(final U from);
}
