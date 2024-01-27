package org.dtomics.gameengine.specification.window;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * WindowFactory
 */
public abstract class WindowFactory {

    protected abstract <T extends Window> T create(WindowConfig config);

    private static final Map<Class<? extends Window>, WindowFactory> windowFactoryCache = new HashMap<>();

    protected WindowFactory(Class<? extends Window> clz) {
        windowFactoryCache.put(clz, this);
    }

    public static <T extends Window> Optional<T> create(final Class<T> clz, final WindowConfig config) {
        return Optional.ofNullable(windowFactoryCache.get(clz))
            .map(factory -> factory.create(config));
    }

}
