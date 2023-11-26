package org.dtomics.gameengine.demo.utils;

import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glBufferSubData;
import static org.lwjgl.opengl.GL15C.glGenBuffers;

public class BufferUtils {

    public static int intBuffer(int type, int[] data, int usage) {
        int buffer = createBuffer(type, data.length * Integer.BYTES, usage);
        glBufferSubData(type, 0, data);
        glBindBuffer(type, 0);
        return buffer;
    }

    public static int floatBuffer(int type, float[] data, int usage) {
        int buffer = createBuffer(type, data.length * Float.BYTES, usage);
        glBufferSubData(type, 0, data);
        glBindBuffer(type, 0);
        return buffer;
    }


    private static int createBuffer(int type, int size, int usage) {
        int buffer = glGenBuffers();
        glBindBuffer(type, buffer);
        glBufferData(type, size, usage);
        return buffer;
    }

}
