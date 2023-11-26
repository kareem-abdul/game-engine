package org.dtomics.gameengine.specification.render;

import lombok.Getter;
import org.dtomics.gameengine.specification.window.Window;

import java.util.concurrent.*;

public abstract class RenderThread extends Thread {
    private static final String THREAD_NAME = "render";
    private static final int DEFAULT_FRAME_CAP = 60;
    private static final int ONE_SECOND_IN_MILLI = 1000;
    private static final int ONE_SECOND_IN_NANO = 1000_000_000;


    protected abstract void init(Window window);
    protected abstract void render(Window window);

    private final Window window;
    @Getter private int framesPerSecond = 0;

    private volatile boolean closed;
    private double frameTimeCap = (double) ONE_SECOND_IN_NANO / (double) DEFAULT_FRAME_CAP;

    private final Object renderableLock = new Object();
    private final BlockingQueue<Renderable> renderables;
    private final BlockingQueue<Renderable> renderBuffer;
    private final BlockingQueue<FutureTask<?>> tasks;

    public RenderThread(Window window) {
        super(THREAD_NAME);
        this.window = window;
        this.renderables = new LinkedBlockingDeque<>();
        this.renderBuffer = new LinkedBlockingQueue<>();
        this.tasks = new LinkedBlockingDeque<>();
    }

    @Override
    public void run() {
        window.useCurrent();

        this.init(window);

        int frames = 0;
        double deltaFrame = 0;

        long previousTime = System.nanoTime();
        long lastCheck = System.currentTimeMillis();

        while (!closed) {
            long currentTime = System.nanoTime();
            deltaFrame += (currentTime - previousTime) / frameTimeCap;
            previousTime = currentTime;
            if (deltaFrame >= 1.0) {
                this.flushTasks();
                this.render(window);
                this.flushRenderables();
                window.updateRenderBuffer();
                deltaFrame--;
                frames++;
            }

            if (System.currentTimeMillis() - lastCheck >= ONE_SECOND_IN_MILLI) {
                lastCheck = System.currentTimeMillis();
                System.out.println("frames " + this.framesPerSecond);
                this.framesPerSecond = frames;
                frames = 0;
            }
        }
    }

    public void submit(Renderable renderable) {
        this.renderables.add(renderable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        final var task = new FutureTask<T>(callable);
        try {
            this.tasks.put(task);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return task;
    }


    public synchronized void close() {
        this.closed = true;
    }

    public void cleanUp() {
        try {
            this.renderables.clear();
            this.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFrameCap(int frameCap) {
        if (frameCap <= 0) {
            throw new IllegalArgumentException("invalid frame cap %d. should be greater than zero".formatted(frameCap));
        }
        this.frameTimeCap = (double) ONE_SECOND_IN_NANO / (double) frameCap;
    }

    private void flushTasks() {
        while (!tasks.isEmpty()) {
            try {
                tasks.take().run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void flushRenderables() {
        synchronized (renderableLock) {
            renderables.drainTo(this.renderBuffer);
        }
        while (!this.renderBuffer.isEmpty()) {
            try {
                this.renderBuffer.take().render(window);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
