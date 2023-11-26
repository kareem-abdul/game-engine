package org.dtomics.gameengine.demo;

import org.dtomics.gameengine.demo.utils.BufferUtils;
import org.dtomics.gameengine.opengl.render.OpenglRenderThread;
import org.dtomics.gameengine.opengl.window.GlfwWindow;
import org.dtomics.gameengine.specification.window.Configuration;
import org.dtomics.gameengine.specification.window.Window;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL41C.glGetProgramBinary;
import static org.lwjgl.opengl.GL41C.glProgramBinary;

public class Application implements Runnable {
    private final Window window;

    private final ExecutorService loggingExecutor;

    private void log(String msg, Object... args) {
        final var thread = Thread.currentThread().getName();
        loggingExecutor.execute(() -> System.out.printf("[%s] - %s%n", thread, msg.formatted(args)));
    }

    public Application() {
        this.loggingExecutor = Executors.newSingleThreadExecutor();
        this.window = new GlfwWindow();


        log("initializing application window");
        this.window.config(
                Configuration.builder()
                        .width(1280)
                        .height(720)
                        .fullscreen(false)
                        .build()
        );
        this.window.create();
        log("window created");


        this.window.on(GLFWKeyCallbackI.class, (GLFWKeyCallbackI) (w, key, scancode, action, mods) -> {
            log("window key %s %s",
                    action == GLFW_PRESS
                            ? "press"
                            : action == GLFW_RELEASE
                            ? "release"
                            : "repeat",
                    glfwGetKeyName(key, scancode)
            );
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                this.window.close();
            }
        });

        this.window.on(GLFWFramebufferSizeCallbackI.class, (GLFWFramebufferSizeCallbackI) (pointer, width, height) -> {
            log("window resize %dx%d", width, height);
            glViewport(0, 0, width, height);
            this.window.config(
                    this.window.config()
                            .setWidth(width)
                            .setHeight(height)
            );
        });
    }

    private int buffer;
    private int vao;
    private int program;
    private void setUp(Window window) {

        log("starting render thread");
        log("using window in current thread");
        window.useCurrent();
        log("setting up opengl settings");


        log("setting up buffers");
        this.buffer = BufferUtils.floatBuffer(GL_ARRAY_BUFFER, new float[]{
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        }, GL_STATIC_DRAW);

        this.vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

//        glProgramBinary();
//        glGetProgramBinary(program,)

        log("loading shaders");
        this.program = createShaderProgram(
                createShader(GL_VERTEX_SHADER, """
                         #version 330 core
                         layout (location = 0) in vec3 aPos;

                         void main()
                         {
                             gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                         }
                        """),
                createShader(GL_FRAGMENT_SHADER, """
                        #version 330 core

                        out vec4 fragColor;

                        void main() {
                            fragColor=vec4(1.0f, 0.5f, 0.2f, 1.0f);
                        }
                        """)
        );
    }

    @Override
    public void run() {
        glUseProgram(program);
        glClear(GL_COLOR_BUFFER_BIT);
        glBindVertexArray(vao);
        glUseProgram(program);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glUseProgram(0);
        glBindVertexArray(0);
    }

    public void start() {
        final var renderThread = new OpenglRenderThread(window);
        renderThread.setFrameCap(60);
        renderThread.start();

        log("starting main loop");
        while (!this.window.exitRequested()) {
            this.window.pollEvents();
        }
        log("exiting main loop");
        log("cleaning up threads");

        renderThread.close();
        renderThread.cleanUp();

        log("cleaning up window");
        this.window.cleanUp();
        log("exiting application");
        log("stopping logger thread");
        try {
            final var gracefulExit = loggingExecutor.awaitTermination(1, TimeUnit.SECONDS);
            if (!gracefulExit) {
                loggingExecutor.shutdown();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static int createShaderProgram(int... shaders) {
        var program = glCreateProgram();
        for (int i = shaders.length - 1; i >= 0; i--) {
            glAttachShader(program, shaders[i]);
        }
        glLinkProgram(program);
        for (int shader : shaders) {
            glDeleteShader(shader);
        }
        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("failed to link shader program", new RuntimeException(glGetProgramInfoLog(program)));
        }
        return program;
    }

    private static int createShader(int type, String source) {
        var shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException("failed to compile shader " + type
                    , new RuntimeException(glGetShaderInfoLog(shader)));
        }
        return shader;
    }

}
