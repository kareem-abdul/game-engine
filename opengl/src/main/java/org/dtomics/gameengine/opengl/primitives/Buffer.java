package org.dtomics.gameengine.opengl.primitives;

import static org.lwjgl.opengl.GL15C.*;

public class Buffer implements Primitive {

    private int pointer;
    private int type;
    private Object data;

    Buffer(int type) {
        this.type = type;
        this.pointer = glGenBuffers();
    }


    @Override
    public void bind() {

    }

    @Override
    public void unbind() {

    }
}
