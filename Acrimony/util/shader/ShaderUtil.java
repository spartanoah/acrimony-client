/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.shader;

import Acrimony.util.IMinecraft;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderUtil {
    private int programID;
    private int fragmentShaderID;
    private int vertexShaderID;

    public ShaderUtil(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = GL20.glCreateProgram();
        try {
            this.fragmentShaderID = this.createShader(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(fragmentShaderLoc)).getInputStream(), 35632);
            GL20.glAttachShader(program, this.fragmentShaderID);
            this.vertexShaderID = this.createShader(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(vertexShaderLoc)).getInputStream(), 35633);
            GL20.glAttachShader(program, this.vertexShaderID);
        } catch (IOException var5) {
            var5.printStackTrace();
        }
        GL20.glLinkProgram(program);
        int status = GL20.glGetProgrami(program, 35714);
        if (status != 0) {
            this.programID = program;
        }
    }

    public ShaderUtil(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "acrimony/shader/vertex.vsh");
    }

    public static void drawQuads(ScaledResolution sr) {
        if (!Minecraft.getMinecraft().gameSettings.ofFastRender) {
            float width = (float)sr.getScaledWidth_double();
            float height = (float)sr.getScaledHeight_double();
            GL11.glBegin(7);
            GL11.glTexCoord2f(0.0f, 1.0f);
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glTexCoord2f(0.0f, 0.0f);
            GL11.glVertex2f(0.0f, height);
            GL11.glTexCoord2f(1.0f, 0.0f);
            GL11.glVertex2f(width, height);
            GL11.glTexCoord2f(1.0f, 1.0f);
            GL11.glVertex2f(width, 0.0f);
            GL11.glEnd();
        }
    }

    public static void drawQuads(float x, float y, float width, float height) {
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }

    public static void drawQuads() {
        ScaledResolution sr = new ScaledResolution(IMinecraft.mc);
        float width = (float)sr.getScaledWidth_double();
        float height = (float)sr.getScaledHeight_double();
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(0.0f, height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(width, 0.0f);
        GL11.glEnd();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, this.readShader(inputStream));
        GL20.glCompileShader(shader);
        int state = GL20.glGetShaderi(shader, 35713);
        if (state == 0) {
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }
        return shader;
    }

    public void init() {
        GL20.glUseProgram(this.programID);
    }

    public void unload() {
        GL20.glUseProgram(0);
    }

    public void setUniformf(String name, float ... args) {
        int loc = GL20.glGetUniformLocation(this.programID, name);
        if (args.length > 1) {
            GL20.glUniform2f(loc, args[0], args[1]);
        } else {
            GL20.glUniform1f(loc, args[0]);
        }
    }

    public void setCustomUniformf(String name, float ... args) {
        int loc = GL20.glGetUniformLocation(this.programID, name);
        switch (args.length) {
            case 1: {
                GL20.glUniform1f(loc, args[0]);
                break;
            }
            case 2: {
                GL20.glUniform2f(loc, args[0], args[1]);
                break;
            }
            case 3: {
                GL20.glUniform3f(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
            }
        }
    }

    public int getUniform(String name) {
        return GL20.glGetUniformLocation(this.programID, name);
    }

    public void setUniformi(String name, int ... args) {
        int loc = GL20.glGetUniformLocation(this.programID, name);
        if (args.length > 1) {
            GL20.glUniform2i(loc, args[0], args[1]);
        } else {
            GL20.glUniform1i(loc, args[0]);
        }
    }

    public void deleteShaderProgram() {
        GL20.glDeleteShader(this.vertexShaderID);
        GL20.glDeleteShader(this.fragmentShaderID);
        GL20.glDeleteProgram(this.programID);
    }

    private String readShader(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException var6) {
            var6.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public int getProgramID() {
        return this.programID;
    }
}

