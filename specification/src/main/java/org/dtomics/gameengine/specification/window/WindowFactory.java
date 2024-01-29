package org.dtomics.gameengine.specification.window;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * WindowFactory
 */
public abstract class WindowFactory<W extends Window> {

    protected abstract W create(WindowConfig config);

    private final Class<W> clz;

    protected WindowFactory(Class<W> clz) {
        this.clz = clz;
        windowFactoryCache.put(clz, this);
    }

    protected boolean supports(final Class<? extends Window> clz) {
        return this.clz.isAssignableFrom(clz);
    }

    private static final Map<Class<? extends Window>, WindowFactory<? extends Window>> windowFactoryCache = new HashMap<>();

    public static <T extends Window> void register(final Class<T> clz, WindowFactory<T> factory) {
        windowFactoryCache.put(clz, factory);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends Window> Optional<T> create(final Class<T> clz, final WindowConfig config) {
        return Optional.ofNullable(windowFactoryCache.get(clz))
                .filter(factory -> factory.supports(clz))
                .map(factory -> (T) factory.create(config));
    }

}
