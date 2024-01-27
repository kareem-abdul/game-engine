package org.dtomics.gameengine.specification.window;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Builder
@Data
@Accessors(chain = true)
public class WindowConfig {
    private int width;
    private int height;
    @Builder.Default
    private String title = "";
    @Builder.Default
    private boolean vsync = true;

    @Builder.Default
    private boolean focused = true;
    @Builder.Default
    private boolean decorated = true;
    @Builder.Default
    private boolean maximized = false;
    @Builder.Default
    private boolean resizable = true;
    @Builder.Default
    private boolean fullscreen = false;
}
