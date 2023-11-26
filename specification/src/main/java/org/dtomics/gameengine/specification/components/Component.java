package org.dtomics.gameengine.specification.components;

import org.dtomics.gameengine.specification.window.Window;

public interface Component {
    void init();
    void update(Window window);
}
