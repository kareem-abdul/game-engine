package org.dtomics.gameengine.opengl.primitives;

public interface Primitive {
    void bind();
    void unbind();

    default void cleanUp() {
        throw new IllegalStateException("not implemented");
    }
}
