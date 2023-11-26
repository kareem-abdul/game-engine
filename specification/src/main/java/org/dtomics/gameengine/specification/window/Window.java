package org.dtomics.gameengine.specification.window;

import org.dtomics.gameengine.specification.events.Listener;

public interface Window extends Listener {

    void create();

    void close();

    void cleanUp();

    boolean exitRequested();

    void useCurrent();

    void updateRenderBuffer();

    void pollEvents();

    Configuration config();

    void config(Configuration configuration);
}
