package org.dtomics.gameengine.opengl.window;

import lombok.NonNull;
import org.dtomics.gameengine.specification.window.Configuration;
import org.dtomics.gameengine.specification.window.Window;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlfwWindow implements Window {

    private long pointer;
    private long monitor;
    private boolean created;

    private Configuration configuration;

    @Override
    public  <T,C> void on(@NonNull Class<T> event, @NonNull C callback) {
        switch (callback) {
            case GLFWKeyCallbackI callbackI -> glfwSetKeyCallback(this.pointer, callbackI);
            case GLFWFramebufferSizeCallbackI callbackI -> glfwSetFramebufferSizeCallback(this.pointer, callbackI);
            case GLFWCursorPosCallbackI callbackI -> glfwSetCursorPosCallback(this.pointer, callbackI);
            case GLFWMouseButtonCallbackI callbackI -> glfwSetMouseButtonCallback(this.pointer, callbackI);
            case GLFWScrollCallbackI callbackI -> glfwSetScrollCallback(this.pointer, callbackI);
            case GLFWWindowCloseCallbackI callbackI -> glfwSetWindowCloseCallback(this.pointer, callbackI);
            default -> throw new IllegalStateException("event %s not supported".formatted(event.getCanonicalName()));
        }
    }

    @Override
    public void create() {
        if(this.created) {
            return;
        }
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("failed to initialize glfw");
        }
        defaultGlfwHints();
        this.monitor = glfwGetPrimaryMonitor();
        this.pointer = glfwCreateWindow(
                config().getWidth(),
                config().getHeight(),
                config().getTitle(),
                config().isFullscreen() ? this.monitor : MemoryUtil.NULL,
                MemoryUtil.NULL
        );
        if (this.pointer == NULL) {
            throw new IllegalStateException("failed to create glfw window");
        }

        if (config().isResizable()) {
            glfwSetFramebufferSizeCallback(
                    this.pointer,
                    (window, width, height) -> {
                        config()
                                .setWidth(width)
                                .setHeight(height);
                    }
            );
        }
        this.keepToCenter();
        if (config().isVsync()) {

            glfwSwapInterval(1);
        }
        glfwShowWindow(this.pointer);
        this.created = true;
    }

    @Override
    public void close() {
        if (!created) {
            return;
        }
        glfwSetWindowShouldClose(this.pointer, true);
    }

    @Override
    public void cleanUp() {
        if (!this.created) {
            return;
        }
        glfwFreeCallbacks(this.pointer);
        glfwDestroyWindow(this.pointer);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    @Override
    public boolean exitRequested() {
        return this.created && glfwWindowShouldClose(this.pointer);
    }

    @Override
    public void useCurrent() {
        if (!created) {
            return;
        }
        glfwMakeContextCurrent(this.pointer);
    }

    @Override
    public void updateRenderBuffer() {
        if (!created) {
            return;
        }
        glfwSwapBuffers(this.pointer);
    }

    @Override
    public void pollEvents() {
        if (!created) {
            return;
        }
        glfwPollEvents();
    }

    @Override
    public Configuration config() {
        return getConfigOrDefaultConfig();
    }

    @Override
    public void config(Configuration configuration) {
        if (this.created && this.configuration != null) {
            glfwSetWindowSize(
                    this.pointer,
                    config().getWidth(),
                    config().getHeight()
            );
//            glfwSetWindowAttrib(this.pointer, GLFW_FOCUSED, getGlfwBoolean(config().isFocused()));
            glfwSetWindowAttrib(this.pointer, GLFW_DECORATED, getGlfwBoolean(config().isDecorated()));
//            glfwSetWindowAttrib(this.pointer, GLFW_MAXIMIZED, getGlfwBoolean(config().isMaximized()));
            glfwSetWindowAttrib(this.pointer, GLFW_RESIZABLE, getGlfwBoolean(config().isResizable()));
            setFullScreen(config().isFullscreen());
            glfwSwapInterval(config().isVsync() ? 1 : 0);
        }

        this.configuration = configuration;
    }

    private Configuration getConfigOrDefaultConfig() {
        if (this.configuration != null) {
            return this.configuration;
        }
        return this.configuration = Configuration.builder()
                .vsync(true)
                .fullscreen(false)
                .resizable(true)
                .width(640)
                .height(480)
                .build();
    }

    private void setFullScreen(boolean fullScreen) {
        if (fullScreen == this.isFullScreen()) {
            return;
        }
        final var vidMode = glfwGetVideoMode(this.pointer);
        if (vidMode == null) {
            return;
        }
        config()
                .setWidth(vidMode.width())
                .setHeight(vidMode.height());
        if (fullScreen) {
            glfwSetWindowMonitor(
                    this.pointer,
                    this.monitor,
                    0, 0,
                    vidMode.width(), vidMode.height(),
                    vidMode.refreshRate()
            );
            return;
        }
        glfwSetWindowMonitor(
                this.pointer,
                NULL,
                0, 0,
                vidMode.width(), vidMode.height(),
                vidMode.refreshRate()
        );
    }

    private boolean isFullScreen() {
        return glfwGetWindowMonitor(this.pointer) != NULL;
    }

    private void defaultGlfwHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, getGlfwBoolean(config().isResizable()));
        glfwWindowHint(GLFW_DECORATED, getGlfwBoolean(config().isDecorated()));
        glfwWindowHint(GLFW_MAXIMIZED, getGlfwBoolean(config().isMaximized()));
    }

    private void keepToCenter() {
        var vidMode = glfwGetVideoMode(this.monitor);
        if (vidMode != null) {
            glfwSetWindowPos(
                    this.pointer,
                    (vidMode.width() - config().getWidth()) / 2,
                    (vidMode.height() - config().getHeight()) / 2
            );
        }
    }

    private int getGlfwBoolean(boolean value) {
        return value ? GLFW_TRUE : GLFW_FALSE;
    }
}
