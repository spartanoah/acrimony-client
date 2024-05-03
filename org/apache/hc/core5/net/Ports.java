/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.net;

import org.apache.hc.core5.util.Args;

public class Ports {
    public static final int SCHEME_DEFAULT = -1;
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 65535;

    public static int checkWithDefault(int port) {
        return Args.checkRange(port, -1, 65535, "Port number(Use -1 to specify the scheme default port)");
    }

    public static int check(int port) {
        return Args.checkRange(port, 0, 65535, "Port number");
    }
}

