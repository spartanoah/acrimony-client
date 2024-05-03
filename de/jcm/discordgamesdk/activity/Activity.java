/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

import de.jcm.discordgamesdk.activity.ActivityAssets;
import de.jcm.discordgamesdk.activity.ActivityParty;
import de.jcm.discordgamesdk.activity.ActivitySecrets;
import de.jcm.discordgamesdk.activity.ActivityTimestamps;
import de.jcm.discordgamesdk.activity.ActivityType;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;

public class Activity
implements AutoCloseable {
    private static final ReferenceQueue<Activity> QUEUE = new ReferenceQueue();
    private static final ArrayList<ActivityReference> REFERENCES = new ArrayList();
    private static final Thread QUEUE_THREAD = new Thread(() -> {
        while (true) {
            try {
                while (true) {
                    ActivityReference reference = (ActivityReference)QUEUE.remove();
                    Activity.free(reference.pointer);
                    REFERENCES.remove(reference);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }, "Activity-Cleaner");
    private final long pointer;
    private final ActivityTimestamps timestamps;
    private final ActivityAssets assets;
    private final ActivityParty party;
    private final ActivitySecrets secrets;

    public Activity() {
        this.pointer = this.allocate();
        this.timestamps = new ActivityTimestamps(this.getTimestamps(this.pointer));
        this.assets = new ActivityAssets(this.getAssets(this.pointer));
        this.party = new ActivityParty(this.getParty(this.pointer));
        this.secrets = new ActivitySecrets(this.getSecrets(this.pointer));
    }

    public Activity(long pointer) {
        this.pointer = pointer;
        this.timestamps = new ActivityTimestamps(this.getTimestamps(pointer));
        this.assets = new ActivityAssets(this.getAssets(pointer));
        this.party = new ActivityParty(this.getParty(pointer));
        this.secrets = new ActivitySecrets(this.getSecrets(pointer));
        ActivityReference reference = new ActivityReference(this, QUEUE);
        REFERENCES.add(reference);
    }

    public long getApplicationId() {
        return this.getApplicationId(this.pointer);
    }

    public String getName() {
        return this.getName(this.pointer);
    }

    public void setState(String state) {
        if (state.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setState(this.pointer, state);
    }

    public String getState() {
        return this.getState(this.pointer);
    }

    public void setDetails(String details) {
        if (details.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 127");
        }
        this.setDetails(this.pointer, details);
    }

    public String getDetails() {
        return this.getDetails(this.pointer);
    }

    public void setType(ActivityType type) {
        this.setType(this.pointer, type.ordinal());
    }

    public ActivityType getType() {
        return ActivityType.values()[this.getType(this.pointer)];
    }

    public ActivityTimestamps timestamps() {
        return this.timestamps;
    }

    public ActivityAssets assets() {
        return this.assets;
    }

    public ActivityParty party() {
        return this.party;
    }

    public ActivitySecrets secrets() {
        return this.secrets;
    }

    public void setInstance(boolean instance) {
        this.setInstance(this.pointer, instance);
    }

    public boolean getInstance() {
        return this.getInstance(this.pointer);
    }

    private native long allocate();

    private static native void free(long var0);

    private native long getApplicationId(long var1);

    private native String getName(long var1);

    private native void setState(long var1, String var3);

    private native String getState(long var1);

    private native void setDetails(long var1, String var3);

    private native String getDetails(long var1);

    private native void setType(long var1, int var3);

    private native int getType(long var1);

    private native long getTimestamps(long var1);

    private native long getAssets(long var1);

    private native long getParty(long var1);

    private native long getSecrets(long var1);

    private native void setInstance(long var1, boolean var3);

    private native boolean getInstance(long var1);

    @Override
    public void close() {
        Activity.free(this.pointer);
    }

    public long getPointer() {
        return this.pointer;
    }

    public String toString() {
        return "Activity@" + this.pointer + "{applicationId=" + this.getApplicationId() + ", name = " + this.getName() + ", state = " + this.getState() + ", details = " + this.getDetails() + ", type = " + (Object)((Object)this.getType()) + ", timestamps=" + this.timestamps() + ", assets=" + this.assets() + ", party=" + this.party() + ", secrets=" + this.secrets() + '}';
    }

    static {
        QUEUE_THREAD.start();
    }

    private static class ActivityReference
    extends PhantomReference<Activity> {
        private final long pointer;

        public ActivityReference(Activity referent, ReferenceQueue<? super Activity> q) {
            super(referent, q);
            this.pointer = referent.pointer;
        }

        public long getPointer() {
            return this.pointer;
        }
    }
}

