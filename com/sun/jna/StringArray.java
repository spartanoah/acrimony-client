/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.NativeString;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringArray
extends Memory
implements Function.PostCallRead {
    private boolean wide;
    private List natives = new ArrayList();
    private Object[] original;

    public StringArray(String[] strings) {
        this(strings, false);
    }

    public StringArray(String[] strings, boolean wide) {
        this((Object[])strings, wide);
    }

    public StringArray(WString[] strings) {
        this(strings, true);
    }

    private StringArray(Object[] strings, boolean wide) {
        super((strings.length + 1) * Pointer.SIZE);
        this.original = strings;
        this.wide = wide;
        for (int i = 0; i < strings.length; ++i) {
            Pointer p = null;
            if (strings[i] != null) {
                NativeString ns = new NativeString(strings[i].toString(), wide);
                this.natives.add(ns);
                p = ns.getPointer();
            }
            this.setPointer(Pointer.SIZE * i, p);
        }
        this.setPointer(Pointer.SIZE * strings.length, null);
    }

    public void read() {
        boolean returnWide = this.original instanceof WString[];
        for (int si = 0; si < this.original.length; ++si) {
            Pointer p = this.getPointer(si * Pointer.SIZE);
            CharSequence s = null;
            if (p != null) {
                s = p.getString(0L, this.wide);
                if (returnWide) {
                    s = new WString((String)s);
                }
            }
            this.original[si] = s;
        }
    }

    public String toString() {
        String s = this.wide ? "const wchar_t*[]" : "const char*[]";
        s = s + Arrays.asList(this.original);
        return s;
    }
}

