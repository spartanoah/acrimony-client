/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import net.java.games.input.Component;
import net.java.games.input.Usage;

final class KeyboardUsage
implements Usage {
    private static final KeyboardUsage[] map = new KeyboardUsage[255];
    public static final KeyboardUsage ERRORROLLOVER = new KeyboardUsage(1);
    public static final KeyboardUsage POSTFAIL = new KeyboardUsage(2);
    public static final KeyboardUsage ERRORUNDEFINED = new KeyboardUsage(3);
    public static final KeyboardUsage A = new KeyboardUsage(Component.Identifier.Key.A, 4);
    public static final KeyboardUsage B = new KeyboardUsage(Component.Identifier.Key.B, 5);
    public static final KeyboardUsage C = new KeyboardUsage(Component.Identifier.Key.C, 6);
    public static final KeyboardUsage D = new KeyboardUsage(Component.Identifier.Key.D, 7);
    public static final KeyboardUsage E = new KeyboardUsage(Component.Identifier.Key.E, 8);
    public static final KeyboardUsage F = new KeyboardUsage(Component.Identifier.Key.F, 9);
    public static final KeyboardUsage G = new KeyboardUsage(Component.Identifier.Key.G, 10);
    public static final KeyboardUsage H = new KeyboardUsage(Component.Identifier.Key.H, 11);
    public static final KeyboardUsage I = new KeyboardUsage(Component.Identifier.Key.I, 12);
    public static final KeyboardUsage J = new KeyboardUsage(Component.Identifier.Key.J, 13);
    public static final KeyboardUsage K = new KeyboardUsage(Component.Identifier.Key.K, 14);
    public static final KeyboardUsage L = new KeyboardUsage(Component.Identifier.Key.L, 15);
    public static final KeyboardUsage M = new KeyboardUsage(Component.Identifier.Key.M, 16);
    public static final KeyboardUsage N = new KeyboardUsage(Component.Identifier.Key.N, 17);
    public static final KeyboardUsage O = new KeyboardUsage(Component.Identifier.Key.O, 18);
    public static final KeyboardUsage P = new KeyboardUsage(Component.Identifier.Key.P, 19);
    public static final KeyboardUsage Q = new KeyboardUsage(Component.Identifier.Key.Q, 20);
    public static final KeyboardUsage R = new KeyboardUsage(Component.Identifier.Key.R, 21);
    public static final KeyboardUsage S = new KeyboardUsage(Component.Identifier.Key.S, 22);
    public static final KeyboardUsage T = new KeyboardUsage(Component.Identifier.Key.T, 23);
    public static final KeyboardUsage U = new KeyboardUsage(Component.Identifier.Key.U, 24);
    public static final KeyboardUsage V = new KeyboardUsage(Component.Identifier.Key.V, 25);
    public static final KeyboardUsage W = new KeyboardUsage(Component.Identifier.Key.W, 26);
    public static final KeyboardUsage X = new KeyboardUsage(Component.Identifier.Key.X, 27);
    public static final KeyboardUsage Y = new KeyboardUsage(Component.Identifier.Key.Y, 28);
    public static final KeyboardUsage Z = new KeyboardUsage(Component.Identifier.Key.Z, 29);
    public static final KeyboardUsage _1 = new KeyboardUsage(Component.Identifier.Key._1, 30);
    public static final KeyboardUsage _2 = new KeyboardUsage(Component.Identifier.Key._2, 31);
    public static final KeyboardUsage _3 = new KeyboardUsage(Component.Identifier.Key._3, 32);
    public static final KeyboardUsage _4 = new KeyboardUsage(Component.Identifier.Key._4, 33);
    public static final KeyboardUsage _5 = new KeyboardUsage(Component.Identifier.Key._5, 34);
    public static final KeyboardUsage _6 = new KeyboardUsage(Component.Identifier.Key._6, 35);
    public static final KeyboardUsage _7 = new KeyboardUsage(Component.Identifier.Key._7, 36);
    public static final KeyboardUsage _8 = new KeyboardUsage(Component.Identifier.Key._8, 37);
    public static final KeyboardUsage _9 = new KeyboardUsage(Component.Identifier.Key._9, 38);
    public static final KeyboardUsage _0 = new KeyboardUsage(Component.Identifier.Key._0, 39);
    public static final KeyboardUsage ENTER = new KeyboardUsage(Component.Identifier.Key.RETURN, 40);
    public static final KeyboardUsage ESCAPE = new KeyboardUsage(Component.Identifier.Key.ESCAPE, 41);
    public static final KeyboardUsage BACKSPACE = new KeyboardUsage(Component.Identifier.Key.BACK, 42);
    public static final KeyboardUsage TAB = new KeyboardUsage(Component.Identifier.Key.TAB, 43);
    public static final KeyboardUsage SPACEBAR = new KeyboardUsage(Component.Identifier.Key.SPACE, 44);
    public static final KeyboardUsage HYPHEN = new KeyboardUsage(Component.Identifier.Key.MINUS, 45);
    public static final KeyboardUsage EQUALSIGN = new KeyboardUsage(Component.Identifier.Key.EQUALS, 46);
    public static final KeyboardUsage OPENBRACKET = new KeyboardUsage(Component.Identifier.Key.LBRACKET, 47);
    public static final KeyboardUsage CLOSEBRACKET = new KeyboardUsage(Component.Identifier.Key.RBRACKET, 48);
    public static final KeyboardUsage BACKSLASH = new KeyboardUsage(Component.Identifier.Key.BACKSLASH, 49);
    public static final KeyboardUsage NONUSPOUNT = new KeyboardUsage(Component.Identifier.Key.PERIOD, 50);
    public static final KeyboardUsage SEMICOLON = new KeyboardUsage(Component.Identifier.Key.SEMICOLON, 51);
    public static final KeyboardUsage QUOTE = new KeyboardUsage(Component.Identifier.Key.APOSTROPHE, 52);
    public static final KeyboardUsage TILDE = new KeyboardUsage(Component.Identifier.Key.GRAVE, 53);
    public static final KeyboardUsage COMMA = new KeyboardUsage(Component.Identifier.Key.COMMA, 54);
    public static final KeyboardUsage PERIOD = new KeyboardUsage(Component.Identifier.Key.PERIOD, 55);
    public static final KeyboardUsage SLASH = new KeyboardUsage(Component.Identifier.Key.SLASH, 56);
    public static final KeyboardUsage CAPSLOCK = new KeyboardUsage(Component.Identifier.Key.CAPITAL, 57);
    public static final KeyboardUsage F1 = new KeyboardUsage(Component.Identifier.Key.F1, 58);
    public static final KeyboardUsage F2 = new KeyboardUsage(Component.Identifier.Key.F2, 59);
    public static final KeyboardUsage F3 = new KeyboardUsage(Component.Identifier.Key.F3, 60);
    public static final KeyboardUsage F4 = new KeyboardUsage(Component.Identifier.Key.F4, 61);
    public static final KeyboardUsage F5 = new KeyboardUsage(Component.Identifier.Key.F5, 62);
    public static final KeyboardUsage F6 = new KeyboardUsage(Component.Identifier.Key.F6, 63);
    public static final KeyboardUsage F7 = new KeyboardUsage(Component.Identifier.Key.F7, 64);
    public static final KeyboardUsage F8 = new KeyboardUsage(Component.Identifier.Key.F8, 65);
    public static final KeyboardUsage F9 = new KeyboardUsage(Component.Identifier.Key.F9, 66);
    public static final KeyboardUsage F10 = new KeyboardUsage(Component.Identifier.Key.F10, 67);
    public static final KeyboardUsage F11 = new KeyboardUsage(Component.Identifier.Key.F11, 68);
    public static final KeyboardUsage F12 = new KeyboardUsage(Component.Identifier.Key.F12, 69);
    public static final KeyboardUsage PRINTSCREEN = new KeyboardUsage(Component.Identifier.Key.SYSRQ, 70);
    public static final KeyboardUsage SCROLLLOCK = new KeyboardUsage(Component.Identifier.Key.SCROLL, 71);
    public static final KeyboardUsage PAUSE = new KeyboardUsage(Component.Identifier.Key.PAUSE, 72);
    public static final KeyboardUsage INSERT = new KeyboardUsage(Component.Identifier.Key.INSERT, 73);
    public static final KeyboardUsage HOME = new KeyboardUsage(Component.Identifier.Key.HOME, 74);
    public static final KeyboardUsage PAGEUP = new KeyboardUsage(Component.Identifier.Key.PAGEUP, 75);
    public static final KeyboardUsage DELETE = new KeyboardUsage(Component.Identifier.Key.DELETE, 76);
    public static final KeyboardUsage END = new KeyboardUsage(Component.Identifier.Key.END, 77);
    public static final KeyboardUsage PAGEDOWN = new KeyboardUsage(Component.Identifier.Key.PAGEDOWN, 78);
    public static final KeyboardUsage RIGHTARROW = new KeyboardUsage(Component.Identifier.Key.RIGHT, 79);
    public static final KeyboardUsage LEFTARROW = new KeyboardUsage(Component.Identifier.Key.LEFT, 80);
    public static final KeyboardUsage DOWNARROW = new KeyboardUsage(Component.Identifier.Key.DOWN, 81);
    public static final KeyboardUsage UPARROW = new KeyboardUsage(Component.Identifier.Key.UP, 82);
    public static final KeyboardUsage KEYPAD_NUMLOCK = new KeyboardUsage(Component.Identifier.Key.NUMLOCK, 83);
    public static final KeyboardUsage KEYPAD_SLASH = new KeyboardUsage(Component.Identifier.Key.DIVIDE, 84);
    public static final KeyboardUsage KEYPAD_ASTERICK = new KeyboardUsage(85);
    public static final KeyboardUsage KEYPAD_HYPHEN = new KeyboardUsage(Component.Identifier.Key.SUBTRACT, 86);
    public static final KeyboardUsage KEYPAD_PLUS = new KeyboardUsage(Component.Identifier.Key.ADD, 87);
    public static final KeyboardUsage KEYPAD_ENTER = new KeyboardUsage(Component.Identifier.Key.NUMPADENTER, 88);
    public static final KeyboardUsage KEYPAD_1 = new KeyboardUsage(Component.Identifier.Key.NUMPAD1, 89);
    public static final KeyboardUsage KEYPAD_2 = new KeyboardUsage(Component.Identifier.Key.NUMPAD2, 90);
    public static final KeyboardUsage KEYPAD_3 = new KeyboardUsage(Component.Identifier.Key.NUMPAD3, 91);
    public static final KeyboardUsage KEYPAD_4 = new KeyboardUsage(Component.Identifier.Key.NUMPAD4, 92);
    public static final KeyboardUsage KEYPAD_5 = new KeyboardUsage(Component.Identifier.Key.NUMPAD5, 93);
    public static final KeyboardUsage KEYPAD_6 = new KeyboardUsage(Component.Identifier.Key.NUMPAD6, 94);
    public static final KeyboardUsage KEYPAD_7 = new KeyboardUsage(Component.Identifier.Key.NUMPAD7, 95);
    public static final KeyboardUsage KEYPAD_8 = new KeyboardUsage(Component.Identifier.Key.NUMPAD8, 96);
    public static final KeyboardUsage KEYPAD_9 = new KeyboardUsage(Component.Identifier.Key.NUMPAD9, 97);
    public static final KeyboardUsage KEYPAD_0 = new KeyboardUsage(Component.Identifier.Key.NUMPAD0, 98);
    public static final KeyboardUsage KEYPAD_PERIOD = new KeyboardUsage(Component.Identifier.Key.DECIMAL, 99);
    public static final KeyboardUsage NONUSBACKSLASH = new KeyboardUsage(Component.Identifier.Key.BACKSLASH, 100);
    public static final KeyboardUsage APPLICATION = new KeyboardUsage(Component.Identifier.Key.APPS, 101);
    public static final KeyboardUsage POWER = new KeyboardUsage(Component.Identifier.Key.POWER, 102);
    public static final KeyboardUsage KEYPAD_EQUALSIGN = new KeyboardUsage(Component.Identifier.Key.NUMPADEQUAL, 103);
    public static final KeyboardUsage F13 = new KeyboardUsage(Component.Identifier.Key.F13, 104);
    public static final KeyboardUsage F14 = new KeyboardUsage(Component.Identifier.Key.F14, 105);
    public static final KeyboardUsage F15 = new KeyboardUsage(Component.Identifier.Key.F15, 106);
    public static final KeyboardUsage F16 = new KeyboardUsage(107);
    public static final KeyboardUsage F17 = new KeyboardUsage(108);
    public static final KeyboardUsage F18 = new KeyboardUsage(109);
    public static final KeyboardUsage F19 = new KeyboardUsage(110);
    public static final KeyboardUsage F20 = new KeyboardUsage(111);
    public static final KeyboardUsage F21 = new KeyboardUsage(112);
    public static final KeyboardUsage F22 = new KeyboardUsage(113);
    public static final KeyboardUsage F23 = new KeyboardUsage(114);
    public static final KeyboardUsage F24 = new KeyboardUsage(115);
    public static final KeyboardUsage EXECUTE = new KeyboardUsage(116);
    public static final KeyboardUsage HELP = new KeyboardUsage(117);
    public static final KeyboardUsage MENU = new KeyboardUsage(118);
    public static final KeyboardUsage SELECT = new KeyboardUsage(119);
    public static final KeyboardUsage STOP = new KeyboardUsage(Component.Identifier.Key.STOP, 120);
    public static final KeyboardUsage AGAIN = new KeyboardUsage(121);
    public static final KeyboardUsage UNDO = new KeyboardUsage(122);
    public static final KeyboardUsage CUT = new KeyboardUsage(123);
    public static final KeyboardUsage COPY = new KeyboardUsage(124);
    public static final KeyboardUsage PASTE = new KeyboardUsage(125);
    public static final KeyboardUsage FIND = new KeyboardUsage(126);
    public static final KeyboardUsage MUTE = new KeyboardUsage(127);
    public static final KeyboardUsage VOLUMEUP = new KeyboardUsage(128);
    public static final KeyboardUsage VOLUMEDOWN = new KeyboardUsage(129);
    public static final KeyboardUsage LOCKINGCAPSLOCK = new KeyboardUsage(Component.Identifier.Key.CAPITAL, 130);
    public static final KeyboardUsage LOCKINGNUMLOCK = new KeyboardUsage(Component.Identifier.Key.NUMLOCK, 131);
    public static final KeyboardUsage LOCKINGSCROLLLOCK = new KeyboardUsage(Component.Identifier.Key.SCROLL, 132);
    public static final KeyboardUsage KEYPAD_COMMA = new KeyboardUsage(Component.Identifier.Key.COMMA, 133);
    public static final KeyboardUsage KEYPAD_EQUALSSIGNAS400 = new KeyboardUsage(134);
    public static final KeyboardUsage INTERNATIONAL1 = new KeyboardUsage(135);
    public static final KeyboardUsage INTERNATIONAL2 = new KeyboardUsage(136);
    public static final KeyboardUsage INTERNATIONAL3 = new KeyboardUsage(137);
    public static final KeyboardUsage INTERNATIONAL4 = new KeyboardUsage(138);
    public static final KeyboardUsage INTERNATIONAL5 = new KeyboardUsage(139);
    public static final KeyboardUsage INTERNATIONAL6 = new KeyboardUsage(140);
    public static final KeyboardUsage INTERNATIONAL7 = new KeyboardUsage(141);
    public static final KeyboardUsage INTERNATIONAL8 = new KeyboardUsage(142);
    public static final KeyboardUsage INTERNATIONAL9 = new KeyboardUsage(143);
    public static final KeyboardUsage LANG1 = new KeyboardUsage(144);
    public static final KeyboardUsage LANG2 = new KeyboardUsage(145);
    public static final KeyboardUsage LANG3 = new KeyboardUsage(146);
    public static final KeyboardUsage LANG4 = new KeyboardUsage(147);
    public static final KeyboardUsage LANG5 = new KeyboardUsage(148);
    public static final KeyboardUsage LANG6 = new KeyboardUsage(149);
    public static final KeyboardUsage LANG7 = new KeyboardUsage(150);
    public static final KeyboardUsage LANG8 = new KeyboardUsage(151);
    public static final KeyboardUsage LANG9 = new KeyboardUsage(152);
    public static final KeyboardUsage ALTERNATEERASE = new KeyboardUsage(153);
    public static final KeyboardUsage SYSREQORATTENTION = new KeyboardUsage(Component.Identifier.Key.SYSRQ, 154);
    public static final KeyboardUsage CANCEL = new KeyboardUsage(155);
    public static final KeyboardUsage CLEAR = new KeyboardUsage(156);
    public static final KeyboardUsage PRIOR = new KeyboardUsage(Component.Identifier.Key.PAGEUP, 157);
    public static final KeyboardUsage RETURN = new KeyboardUsage(Component.Identifier.Key.RETURN, 158);
    public static final KeyboardUsage SEPARATOR = new KeyboardUsage(159);
    public static final KeyboardUsage OUT = new KeyboardUsage(160);
    public static final KeyboardUsage OPER = new KeyboardUsage(161);
    public static final KeyboardUsage CLEARORAGAIN = new KeyboardUsage(162);
    public static final KeyboardUsage CRSELORPROPS = new KeyboardUsage(163);
    public static final KeyboardUsage EXSEL = new KeyboardUsage(164);
    public static final KeyboardUsage LEFTCONTROL = new KeyboardUsage(Component.Identifier.Key.LCONTROL, 224);
    public static final KeyboardUsage LEFTSHIFT = new KeyboardUsage(Component.Identifier.Key.LSHIFT, 225);
    public static final KeyboardUsage LEFTALT = new KeyboardUsage(Component.Identifier.Key.LALT, 226);
    public static final KeyboardUsage LEFTGUI = new KeyboardUsage(Component.Identifier.Key.LWIN, 227);
    public static final KeyboardUsage RIGHTCONTROL = new KeyboardUsage(Component.Identifier.Key.RCONTROL, 228);
    public static final KeyboardUsage RIGHTSHIFT = new KeyboardUsage(Component.Identifier.Key.RSHIFT, 229);
    public static final KeyboardUsage RIGHTALT = new KeyboardUsage(Component.Identifier.Key.RALT, 230);
    public static final KeyboardUsage RIGHTGUI = new KeyboardUsage(Component.Identifier.Key.RWIN, 231);
    private final int usage;
    private final Component.Identifier.Key identifier;

    public final Component.Identifier.Key getIdentifier() {
        return this.identifier;
    }

    public static final KeyboardUsage map(int usage) {
        if (usage < 0 || usage >= map.length) {
            return null;
        }
        return map[usage];
    }

    private KeyboardUsage(int usage) {
        this(Component.Identifier.Key.UNKNOWN, usage);
    }

    private KeyboardUsage(Component.Identifier.Key id, int usage) {
        this.identifier = id;
        this.usage = usage;
        KeyboardUsage.map[usage] = this;
    }

    public final String toString() {
        return "KeyboardUsage (0x" + Integer.toHexString(this.usage) + ")";
    }
}

