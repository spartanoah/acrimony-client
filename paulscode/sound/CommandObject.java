/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package paulscode.sound;

public class CommandObject {
    public static final int INITIALIZE = 1;
    public static final int LOAD_SOUND = 2;
    public static final int LOAD_DATA = 3;
    public static final int UNLOAD_SOUND = 4;
    public static final int QUEUE_SOUND = 5;
    public static final int DEQUEUE_SOUND = 6;
    public static final int FADE_OUT = 7;
    public static final int FADE_OUT_IN = 8;
    public static final int CHECK_FADE_VOLUMES = 9;
    public static final int NEW_SOURCE = 10;
    public static final int RAW_DATA_STREAM = 11;
    public static final int QUICK_PLAY = 12;
    public static final int SET_POSITION = 13;
    public static final int SET_VOLUME = 14;
    public static final int SET_PITCH = 15;
    public static final int SET_PRIORITY = 16;
    public static final int SET_LOOPING = 17;
    public static final int SET_ATTENUATION = 18;
    public static final int SET_DIST_OR_ROLL = 19;
    public static final int CHANGE_DOPPLER_FACTOR = 20;
    public static final int CHANGE_DOPPLER_VELOCITY = 21;
    public static final int SET_VELOCITY = 22;
    public static final int SET_LISTENER_VELOCITY = 23;
    public static final int PLAY = 24;
    public static final int FEED_RAW_AUDIO_DATA = 25;
    public static final int PAUSE = 26;
    public static final int STOP = 27;
    public static final int REWIND = 28;
    public static final int FLUSH = 29;
    public static final int CULL = 30;
    public static final int ACTIVATE = 31;
    public static final int SET_TEMPORARY = 32;
    public static final int REMOVE_SOURCE = 33;
    public static final int MOVE_LISTENER = 34;
    public static final int SET_LISTENER_POSITION = 35;
    public static final int TURN_LISTENER = 36;
    public static final int SET_LISTENER_ANGLE = 37;
    public static final int SET_LISTENER_ORIENTATION = 38;
    public static final int SET_MASTER_VOLUME = 39;
    public static final int NEW_LIBRARY = 40;
    public byte[] buffer;
    public int[] intArgs;
    public float[] floatArgs;
    public long[] longArgs;
    public boolean[] boolArgs;
    public String[] stringArgs;
    public Class[] classArgs;
    public Object[] objectArgs;
    public int Command;

    public CommandObject(int cmd) {
        this.Command = cmd;
    }

    public CommandObject(int cmd, int i) {
        this.Command = cmd;
        this.intArgs = new int[1];
        this.intArgs[0] = i;
    }

    public CommandObject(int cmd, Class c) {
        this.Command = cmd;
        this.classArgs = new Class[1];
        this.classArgs[0] = c;
    }

    public CommandObject(int cmd, float f) {
        this.Command = cmd;
        this.floatArgs = new float[1];
        this.floatArgs[0] = f;
    }

    public CommandObject(int cmd, String s) {
        this.Command = cmd;
        this.stringArgs = new String[1];
        this.stringArgs[0] = s;
    }

    public CommandObject(int cmd, Object o) {
        this.Command = cmd;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = o;
    }

    public CommandObject(int cmd, String s, Object o) {
        this.Command = cmd;
        this.stringArgs = new String[1];
        this.stringArgs[0] = s;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = o;
    }

    public CommandObject(int cmd, String s, byte[] buff) {
        this.Command = cmd;
        this.stringArgs = new String[1];
        this.stringArgs[0] = s;
        this.buffer = buff;
    }

    public CommandObject(int cmd, String s, Object o, long l) {
        this.Command = cmd;
        this.stringArgs = new String[1];
        this.stringArgs[0] = s;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = o;
        this.longArgs = new long[1];
        this.longArgs[0] = l;
    }

    public CommandObject(int cmd, String s, Object o, long l1, long l2) {
        this.Command = cmd;
        this.stringArgs = new String[1];
        this.stringArgs[0] = s;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = o;
        this.longArgs = new long[2];
        this.longArgs[0] = l1;
        this.longArgs[1] = l2;
    }

    public CommandObject(int cmd, String s1, String s2) {
        this.Command = cmd;
        this.stringArgs = new String[2];
        this.stringArgs[0] = s1;
        this.stringArgs[1] = s2;
    }

    public CommandObject(int cmd, String s, int i) {
        this.Command = cmd;
        this.intArgs = new int[1];
        this.stringArgs = new String[1];
        this.intArgs[0] = i;
        this.stringArgs[0] = s;
    }

    public CommandObject(int cmd, String s, float f) {
        this.Command = cmd;
        this.floatArgs = new float[1];
        this.stringArgs = new String[1];
        this.floatArgs[0] = f;
        this.stringArgs[0] = s;
    }

    public CommandObject(int cmd, String s, boolean b) {
        this.Command = cmd;
        this.boolArgs = new boolean[1];
        this.stringArgs = new String[1];
        this.boolArgs[0] = b;
        this.stringArgs[0] = s;
    }

    public CommandObject(int cmd, float f1, float f2, float f3) {
        this.Command = cmd;
        this.floatArgs = new float[3];
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
    }

    public CommandObject(int cmd, String s, float f1, float f2, float f3) {
        this.Command = cmd;
        this.floatArgs = new float[3];
        this.stringArgs = new String[1];
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
        this.stringArgs[0] = s;
    }

    public CommandObject(int cmd, float f1, float f2, float f3, float f4, float f5, float f6) {
        this.Command = cmd;
        this.floatArgs = new float[6];
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
        this.floatArgs[3] = f4;
        this.floatArgs[4] = f5;
        this.floatArgs[5] = f6;
    }

    public CommandObject(int cmd, boolean b1, boolean b2, boolean b3, String s, Object o, float f1, float f2, float f3, int i, float f4) {
        this.Command = cmd;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[3];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = i;
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
        this.floatArgs[3] = f4;
        this.boolArgs[0] = b1;
        this.boolArgs[1] = b2;
        this.boolArgs[2] = b3;
        this.stringArgs[0] = s;
        this.objectArgs[0] = o;
    }

    public CommandObject(int cmd, boolean b1, boolean b2, boolean b3, String s, Object o, float f1, float f2, float f3, int i, float f4, boolean b4) {
        this.Command = cmd;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[4];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = i;
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
        this.floatArgs[3] = f4;
        this.boolArgs[0] = b1;
        this.boolArgs[1] = b2;
        this.boolArgs[2] = b3;
        this.boolArgs[3] = b4;
        this.stringArgs[0] = s;
        this.objectArgs[0] = o;
    }

    public CommandObject(int cmd, Object o, boolean b, String s, float f1, float f2, float f3, int i, float f4) {
        this.Command = cmd;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[1];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = i;
        this.floatArgs[0] = f1;
        this.floatArgs[1] = f2;
        this.floatArgs[2] = f3;
        this.floatArgs[3] = f4;
        this.boolArgs[0] = b;
        this.stringArgs[0] = s;
        this.objectArgs[0] = o;
    }
}

