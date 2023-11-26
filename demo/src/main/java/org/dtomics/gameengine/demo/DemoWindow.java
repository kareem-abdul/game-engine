package org.dtomics.gameengine.demo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.system.CallbackI;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

@Getter
@Setter
@EqualsAndHashCode
public class DemoWindow {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final long pointer;
    private int width;
    private int height;
    private String title;
    private boolean resizable;
    private boolean fullscreen;
    private boolean vsync;
    @Setter(AccessLevel.NONE)
    private double time;
    @Setter(AccessLevel.NONE)
    private int fps;


    @Builder
    private DemoWindow(int width, int height, String title, boolean resizable, boolean fullscreen, boolean vsync) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.fullscreen = fullscreen;
        this.resizable = resizable;
        this.vsync = vsync;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("failed to initialize glfw");
        }
        defaults();
        this.pointer = glfwCreateWindow(width, height, this.title, this.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        if (this.pointer == NULL) {
            throw new IllegalStateException("failed to create glfw window");
        }

        glfwSetKeyCallback(this.pointer, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(this.pointer, true);
            }
        });

        glfwSetFramebufferSizeCallback(this.pointer, (window, width1, height1) -> {
            glViewport(0, 0, width1, height1);
            this.width = width;
            this.height = height;
        });

        var vidMod = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMod != null) {
            glfwSetWindowPos(
                    this.pointer,
                    (vidMod.width() - this.width) / 2,
                    (vidMod.height() - this.height) / 2
            );
        }

        glfwMakeContextCurrent(this.pointer);
        if (vsync) {
            glfwSwapInterval(1);
        }
        glfwShowWindow(this.pointer);
    }

    public void run(Runnable callback) {
        double currentTime = glfwGetTime();
        double lastTime = glfwGetTime();
        double timeElapsed = 0;
        int frameCount = 0;
        while (!glfwWindowShouldClose(this.pointer)) {
            callback.run();
            glfwPollEvents();
            glfwSwapBuffers(this.pointer);
            frameCount++;
            currentTime = glfwGetTime();
            this.time = currentTime - lastTime;
            lastTime = currentTime;

            timeElapsed += this.time;
            if (timeElapsed >= 1.0) {
                this.fps = frameCount;
                frameCount = 0;
                timeElapsed = 0;
            }
        }
        cleanUp();
    }

    public <T extends CallbackI> void on(Class<T> callbackIClass, T callback) {
        switch (callback) {
            case GLFWKeyCallbackI callbackI -> glfwSetKeyCallback(this.pointer, callbackI);
            case GLFWFramebufferSizeCallbackI callbackI -> glfwSetFramebufferSizeCallback(this.pointer, callbackI);
            case GLFWCursorPosCallbackI callbackI -> glfwSetCursorPosCallback(this.pointer, callbackI);
            case GLFWMouseButtonCallbackI callbackI -> glfwSetMouseButtonCallback(this.pointer, callbackI);
            case GLFWScrollCallbackI callbackI -> glfwSetScrollCallback(this.pointer, callbackI);
            default -> throw new IllegalArgumentException("not supported callback");
        }
    }

    public void closeWindow() {
        glfwSetWindowShouldClose(this.pointer, true);
    }

    private void cleanUp() {
        glfwFreeCallbacks(this.pointer);

        glfwDestroyWindow(this.pointer);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void defaults() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, this.resizable ? GLFW_TRUE : GLFW_FALSE);
    }
}
