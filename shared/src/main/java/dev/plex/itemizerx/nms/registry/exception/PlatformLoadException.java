package dev.plex.itemizerx.nms.registry.exception;

public class PlatformLoadException extends Exception {
    public PlatformLoadException(final String message) {
        super(message);
    }

    public PlatformLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PlatformLoadException(final Throwable cause) {
        super(cause);
    }
}
