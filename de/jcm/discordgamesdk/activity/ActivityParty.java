/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

import de.jcm.discordgamesdk.activity.ActivityPartySize;

public class ActivityParty {
    private final long pointer;
    private final ActivityPartySize size;

    ActivityParty(long pointer) {
        this.pointer = pointer;
        this.size = new ActivityPartySize(this.getSize(pointer));
    }

    public void setID(String id) {
        if (id.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setID(this.pointer, id);
    }

    public String getID() {
        return this.getID(this.pointer);
    }

    public ActivityPartySize size() {
        return this.size;
    }

    private native void setID(long var1, String var3);

    private native String getID(long var1);

    private native long getSize(long var1);
}

