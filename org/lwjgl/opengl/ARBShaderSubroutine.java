/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL40;

public final class ARBShaderSubroutine {
    public static final int GL_ACTIVE_SUBROUTINES = 36325;
    public static final int GL_ACTIVE_SUBROUTINE_UNIFORMS = 36326;
    public static final int GL_ACTIVE_SUBROUTINE_UNIFORM_LOCATIONS = 36423;
    public static final int GL_ACTIVE_SUBROUTINE_MAX_LENGTH = 36424;
    public static final int GL_ACTIVE_SUBROUTINE_UNIFORM_MAX_LENGTH = 36425;
    public static final int GL_MAX_SUBROUTINES = 36327;
    public static final int GL_MAX_SUBROUTINE_UNIFORM_LOCATIONS = 36328;
    public static final int GL_NUM_COMPATIBLE_SUBROUTINES = 36426;
    public static final int GL_COMPATIBLE_SUBROUTINES = 36427;

    private ARBShaderSubroutine() {
    }

    public static int glGetSubroutineUniformLocation(int program, int shadertype, ByteBuffer name) {
        return GL40.glGetSubroutineUniformLocation(program, shadertype, name);
    }

    public static int glGetSubroutineUniformLocation(int program, int shadertype, CharSequence name) {
        return GL40.glGetSubroutineUniformLocation(program, shadertype, name);
    }

    public static int glGetSubroutineIndex(int program, int shadertype, ByteBuffer name) {
        return GL40.glGetSubroutineIndex(program, shadertype, name);
    }

    public static int glGetSubroutineIndex(int program, int shadertype, CharSequence name) {
        return GL40.glGetSubroutineIndex(program, shadertype, name);
    }

    public static void glGetActiveSubroutineUniform(int program, int shadertype, int index, int pname, IntBuffer values) {
        GL40.glGetActiveSubroutineUniform(program, shadertype, index, pname, values);
    }

    public static int glGetActiveSubroutineUniformi(int program, int shadertype, int index, int pname) {
        return GL40.glGetActiveSubroutineUniformi(program, shadertype, index, pname);
    }

    public static void glGetActiveSubroutineUniformName(int program, int shadertype, int index, IntBuffer length, ByteBuffer name) {
        GL40.glGetActiveSubroutineUniformName(program, shadertype, index, length, name);
    }

    public static String glGetActiveSubroutineUniformName(int program, int shadertype, int index, int bufsize) {
        return GL40.glGetActiveSubroutineUniformName(program, shadertype, index, bufsize);
    }

    public static void glGetActiveSubroutineName(int program, int shadertype, int index, IntBuffer length, ByteBuffer name) {
        GL40.glGetActiveSubroutineName(program, shadertype, index, length, name);
    }

    public static String glGetActiveSubroutineName(int program, int shadertype, int index, int bufsize) {
        return GL40.glGetActiveSubroutineName(program, shadertype, index, bufsize);
    }

    public static void glUniformSubroutinesu(int shadertype, IntBuffer indices) {
        GL40.glUniformSubroutinesu(shadertype, indices);
    }

    public static void glGetUniformSubroutineu(int shadertype, int location, IntBuffer params) {
        GL40.glGetUniformSubroutineu(shadertype, location, params);
    }

    public static int glGetUniformSubroutineui(int shadertype, int location) {
        return GL40.glGetUniformSubroutineui(shadertype, location);
    }

    public static void glGetProgramStage(int program, int shadertype, int pname, IntBuffer values) {
        GL40.glGetProgramStage(program, shadertype, pname, values);
    }

    public static int glGetProgramStagei(int program, int shadertype, int pname) {
        return GL40.glGetProgramStagei(program, shadertype, pname);
    }
}

