package org.dtomics.gameengine.specification.events;

public interface Listener {
    <T,C> void  on(Class<T> event, C callback);
}

