/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import net.java.games.input.Controller;
import net.java.games.input.LinuxNativeTypesMap;

final class LinuxInputID {
    private final int bustype;
    private final int vendor;
    private final int product;
    private final int version;

    public LinuxInputID(int bustype, int vendor, int product, int version) {
        this.bustype = bustype;
        this.vendor = vendor;
        this.product = product;
        this.version = version;
    }

    public final Controller.PortType getPortType() {
        return LinuxNativeTypesMap.getPortType(this.bustype);
    }

    public final String toString() {
        return "LinuxInputID: bustype = 0x" + Integer.toHexString(this.bustype) + " | vendor = 0x" + Integer.toHexString(this.vendor) + " | product = 0x" + Integer.toHexString(this.product) + " | version = 0x" + Integer.toHexString(this.version);
    }
}

