package org.dtomics.gameengine.opengl.primitives;

import lombok.Setter;
import org.dtomics.gameengine.specification.render.Renderable;
import org.dtomics.gameengine.specification.window.Window;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL45C.glCreateVertexArrays;

public class VAO implements Primitive, Renderable {
    private final int pointer;

    private Buffer vertexBuffer;
    private Buffer indexBuffer;
    private Shader shader;

    protected VAO() {
        this.pointer = glCreateVertexArrays();
    }


    @Override
    public void bind() {
        glBindVertexArray(this.pointer);
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void render(Window window) {
        this.bind();
        if (shader != null) shader.bind();
        if (vertexBuffer != null) vertexBuffer.bind();
        if(indexBuffer != null) {
            indexBuffer.bind();
        }


        if(shader != null) shader.unbind();
        if(vertexBuffer!=null) vertexBuffer.unbind();
        if(indexBuffer != null) indexBuffer.unbind();
        this.unbind();
    }

}
