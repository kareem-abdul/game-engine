package org.dtomics.gameengine.opengl.render;

import org.dtomics.gameengine.specification.render.RenderThread;
import org.dtomics.gameengine.specification.window.Window;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.opengl.GL11C.glViewport;

public class OpenglRenderThread extends RenderThread {

    public OpenglRenderThread(Window window) {
        super(window);
    }

    @Override
    protected void init(Window window) {
        GL.createCapabilities();
        GL11C.glClearColor(0f, 0f, 0f, 0.3f);
        glViewport(0, 0, window.config().getWidth(), window.config().getHeight());
    }

    @Override
    protected void render(Window window) {

    }

    public <T> T submitAndGet(Callable<T> callable) {
        try {
            return super.submit(callable).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
