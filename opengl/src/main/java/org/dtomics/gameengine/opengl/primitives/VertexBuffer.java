package org.dtomics.gameengine.opengl.primitives;

import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glGenBuffers;

public class VertexBuffer implements Primitive {

    private int pointer;
    private int type;

    VertexBuffer(int type) {
        this.type = type;
        this.pointer = glGenBuffers();
    }


    @Override
    public void bind() {
        glBindBuffer(type, pointer);
    }

    @Override
    public void unbind() {
        glBindBuffer(type, 0);
    }
}
