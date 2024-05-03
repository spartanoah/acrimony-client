/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.input;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.OpenGLPackageAccess;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.InputImplementation;

public class Keyboard {
    public static final int EVENT_SIZE = 18;
    public static final int CHAR_NONE = 0;
    public static final int KEY_NONE = 0;
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_1 = 2;
    public static final int KEY_2 = 3;
    public static final int KEY_3 = 4;
    public static final int KEY_4 = 5;
    public static final int KEY_5 = 6;
    public static final int KEY_6 = 7;
    public static final int KEY_7 = 8;
    public static final int KEY_8 = 9;
    public static final int KEY_9 = 10;
    public static final int KEY_0 = 11;
    public static final int KEY_MINUS = 12;
    public static final int KEY_EQUALS = 13;
    public static final int KEY_BACK = 14;
    public static final int KEY_TAB = 15;
    public static final int KEY_Q = 16;
    public static final int KEY_W = 17;
    public static final int KEY_E = 18;
    public static final int KEY_R = 19;
    public static final int KEY_T = 20;
    public static final int KEY_Y = 21;
    public static final int KEY_U = 22;
    public static final int KEY_I = 23;
    public static final int KEY_O = 24;
    public static final int KEY_P = 25;
    public static final int KEY_LBRACKET = 26;
    public static final int KEY_RBRACKET = 27;
    public static final int KEY_RETURN = 28;
    public static final int KEY_LCONTROL = 29;
    public static final int KEY_A = 30;
    public static final int KEY_S = 31;
    public static final int KEY_D = 32;
    public static final int KEY_F = 33;
    public static final int KEY_G = 34;
    public static final int KEY_H = 35;
    public static final int KEY_J = 36;
    public static final int KEY_K = 37;
    public static final int KEY_L = 38;
    public static final int KEY_SEMICOLON = 39;
    public static final int KEY_APOSTROPHE = 40;
    public static final int KEY_GRAVE = 41;
    public static final int KEY_LSHIFT = 42;
    public static final int KEY_BACKSLASH = 43;
    public static final int KEY_Z = 44;
    public static final int KEY_X = 45;
    public static final int KEY_C = 46;
    public static final int KEY_V = 47;
    public static final int KEY_B = 48;
    public static final int KEY_N = 49;
    public static final int KEY_M = 50;
    public static final int KEY_COMMA = 51;
    public static final int KEY_PERIOD = 52;
    public static final int KEY_SLASH = 53;
    public static final int KEY_RSHIFT = 54;
    public static final int KEY_MULTIPLY = 55;
    public static final int KEY_LMENU = 56;
    public static final int KEY_SPACE = 57;
    public static final int KEY_CAPITAL = 58;
    public static final int KEY_F1 = 59;
    public static final int KEY_F2 = 60;
    public static final int KEY_F3 = 61;
    public static final int KEY_F4 = 62;
    public static final int KEY_F5 = 63;
    public static final int KEY_F6 = 64;
    public static final int KEY_F7 = 65;
    public static final int KEY_F8 = 66;
    public static final int KEY_F9 = 67;
    public static final int KEY_F10 = 68;
    public static final int KEY_NUMLOCK = 69;
    public static final int KEY_SCROLL = 70;
    public static final int KEY_NUMPAD7 = 71;
    public static final int KEY_NUMPAD8 = 72;
    public static final int KEY_NUMPAD9 = 73;
    public static final int KEY_SUBTRACT = 74;
    public static final int KEY_NUMPAD4 = 75;
    public static final int KEY_NUMPAD5 = 76;
    public static final int KEY_NUMPAD6 = 77;
    public static final int KEY_ADD = 78;
    public static final int KEY_NUMPAD1 = 79;
    public static final int KEY_NUMPAD2 = 80;
    public static final int KEY_NUMPAD3 = 81;
    public static final int KEY_NUMPAD0 = 82;
    public static final int KEY_DECIMAL = 83;
    public static final int KEY_F11 = 87;
    public static final int KEY_F12 = 88;
    public static final int KEY_F13 = 100;
    public static final int KEY_F14 = 101;
    public static final int KEY_F15 = 102;
    public static final int KEY_F16 = 103;
    public static final int KEY_F17 = 104;
    public static final int KEY_F18 = 105;
    public static final int KEY_KANA = 112;
    public static final int KEY_F19 = 113;
    public static final int KEY_CONVERT = 121;
    public static final int KEY_NOCONVERT = 123;
    public static final int KEY_YEN = 125;
    public static final int KEY_NUMPADEQUALS = 141;
    public static final int KEY_CIRCUMFLEX = 144;
    public static final int KEY_AT = 145;
    public static final int KEY_COLON = 146;
    public static final int KEY_UNDERLINE = 147;
    public static final int KEY_KANJI = 148;
    public static final int KEY_STOP = 149;
    public static final int KEY_AX = 150;
    public static final int KEY_UNLABELED = 151;
    public static final int KEY_NUMPADENTER = 156;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_SECTION = 167;
    public static final int KEY_NUMPADCOMMA = 179;
    public static final int KEY_DIVIDE = 181;
    public static final int KEY_SYSRQ = 183;
    public static final int KEY_RMENU = 184;
    public static final int KEY_FUNCTION = 196;
    public static final int KEY_PAUSE = 197;
    public static final int KEY_HOME = 199;
    public static final int KEY_UP = 200;
    public static final int KEY_PRIOR = 201;
    public static final int KEY_LEFT = 203;
    public static final int KEY_RIGHT = 205;
    public static final int KEY_END = 207;
    public static final int KEY_DOWN = 208;
    public static final int KEY_NEXT = 209;
    public static final int KEY_INSERT = 210;
    public static final int KEY_DELETE = 211;
    public static final int KEY_CLEAR = 218;
    public static final int KEY_LMETA = 219;
    public static final int KEY_LWIN = 219;
    public static final int KEY_RMETA = 220;
    public static final int KEY_RWIN = 220;
    public static final int KEY_APPS = 221;
    public static final int KEY_POWER = 222;
    public static final int KEY_SLEEP = 223;
    public static final int KEYBOARD_SIZE = 256;
    private static final int BUFFER_SIZE = 50;
    private static final String[] keyName = new String[256];
    private static final Map<String, Integer> keyMap = new HashMap<String, Integer>(253);
    private static int counter;
    private static final int keyCount;
    private static boolean created;
    private static boolean repeat_enabled;
    private static final ByteBuffer keyDownBuffer;
    private static ByteBuffer readBuffer;
    private static KeyEvent current_event;
    private static KeyEvent tmp_event;
    private static boolean initialized;
    private static InputImplementation implementation;

    private Keyboard() {
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        Sys.initialize();
        initialized = true;
    }

    private static void create(InputImplementation impl) throws LWJGLException {
        if (created) {
            return;
        }
        if (!initialized) {
            Keyboard.initialize();
        }
        implementation = impl;
        implementation.createKeyboard();
        created = true;
        readBuffer = ByteBuffer.allocate(900);
        Keyboard.reset();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void create() throws LWJGLException {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            if (!Display.isCreated()) {
                throw new IllegalStateException("Display must be created.");
            }
            Keyboard.create(OpenGLPackageAccess.createImplementation());
        }
    }

    private static void reset() {
        readBuffer.limit(0);
        for (int i = 0; i < keyDownBuffer.remaining(); ++i) {
            keyDownBuffer.put(i, (byte)0);
        }
        Keyboard.current_event.reset();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isCreated() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return created;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void destroy() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            if (!created) {
                return;
            }
            created = false;
            implementation.destroyKeyboard();
            Keyboard.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void poll() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            if (!created) {
                throw new IllegalStateException("Keyboard must be created before you can poll the device");
            }
            implementation.pollKeyboard(keyDownBuffer);
            Keyboard.read();
        }
    }

    private static void read() {
        readBuffer.compact();
        implementation.readKeyboard(readBuffer);
        readBuffer.flip();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isKeyDown(int key) {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            if (!created) {
                throw new IllegalStateException("Keyboard must be created before you can query key state");
            }
            return keyDownBuffer.get(key) != 0;
        }
    }

    public static synchronized String getKeyName(int key) {
        return keyName[key];
    }

    public static synchronized int getKeyIndex(String keyName) {
        Integer ret = keyMap.get(keyName);
        if (ret == null) {
            return 0;
        }
        return ret;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getNumKeyboardEvents() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            if (!created) {
                throw new IllegalStateException("Keyboard must be created before you can read events");
            }
            int old_position = readBuffer.position();
            int num_events = 0;
            while (Keyboard.readNext(tmp_event) && (!tmp_event.repeat || repeat_enabled)) {
                ++num_events;
            }
            readBuffer.position(old_position);
            return num_events;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean next() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            boolean result;
            if (!created) {
                throw new IllegalStateException("Keyboard must be created before you can read events");
            }
            while ((result = Keyboard.readNext(current_event)) && current_event.repeat && !repeat_enabled) {
            }
            return result;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void enableRepeatEvents(boolean enable) {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            repeat_enabled = enable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean areRepeatEventsEnabled() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return repeat_enabled;
        }
    }

    private static boolean readNext(KeyEvent event) {
        if (readBuffer.hasRemaining()) {
            event.key = Keyboard.readBuffer.getInt() & 0xFF;
            event.state = Keyboard.readBuffer.get() != 0;
            event.character = Keyboard.readBuffer.getInt();
            event.nanos = Keyboard.readBuffer.getLong();
            event.repeat = Keyboard.readBuffer.get() == 1;
            return true;
        }
        return false;
    }

    public static int getKeyCount() {
        return keyCount;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static char getEventCharacter() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return (char)current_event.character;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getEventKey() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return current_event.key;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean getEventKeyState() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return current_event.state;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long getEventNanoseconds() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return current_event.nanos;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isRepeatEvent() {
        Object object = OpenGLPackageAccess.global_lock;
        synchronized (object) {
            return current_event.repeat;
        }
    }

    static {
        Field[] fields = Keyboard.class.getFields();
        try {
            for (Field field : fields) {
                String name;
                if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()) || !field.getType().equals(Integer.TYPE) || !field.getName().startsWith("KEY_") || field.getName().endsWith("WIN")) continue;
                int key = field.getInt(null);
                Keyboard.keyName[key] = name = field.getName().substring(4);
                keyMap.put(name, key);
                ++counter;
            }
        } catch (Exception exception) {
            // empty catch block
        }
        keyCount = counter;
        keyDownBuffer = BufferUtils.createByteBuffer(256);
        current_event = new KeyEvent();
        tmp_event = new KeyEvent();
    }

    private static final class KeyEvent {
        private int character;
        private int key;
        private boolean state;
        private long nanos;
        private boolean repeat;

        private KeyEvent() {
        }

        private void reset() {
            this.character = 0;
            this.key = 0;
            this.state = false;
            this.repeat = false;
        }
    }
}

