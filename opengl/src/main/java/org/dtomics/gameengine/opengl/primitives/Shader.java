package org.dtomics.gameengine.opengl.primitives;

import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20C.*;

public class Shader implements Primitive {

    private final int program;

    private List<Integer> shaders;
    private boolean linked;

    public Shader() {
        this.program = glCreateProgram();
    }

    public int attachShader(int type, String path) {
        if (linked) {
            throw new IllegalStateException("shader already linked");
        }
        if (shaders == null) {
            shaders = new ArrayList<>();
        }
        int shader = glCreateShader(type);
        glShaderSource(shader, path);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException("failed to compile shader " + type
                    , new RuntimeException(glGetShaderInfoLog(shader)));
        }
        glAttachShader(this.program, shader);
        shaders.add(shader);
        return shader;
    }

    public void link() {
        if (linked) {
            return;
        }
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("failed to link shader program", new RuntimeException(glGetProgramInfoLog(program)));
        }
        shaders.forEach(GL20C::glDeleteShader);
        shaders.clear();
        shaders = null;
        linked = true;
    }

    @Override
    public void bind() {
        glUseProgram(program);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void cleanUp() {
        glDeleteProgram(program);
    }
}
