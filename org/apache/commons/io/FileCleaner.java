/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io;

import java.io.File;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;

@Deprecated
public class FileCleaner {
    static final FileCleaningTracker theInstance = new FileCleaningTracker();

    @Deprecated
    public static void track(File file, Object marker) {
        theInstance.track(file, marker);
    }

    @Deprecated
    public static void track(File file, Object marker, FileDeleteStrategy deleteStrategy) {
        theInstance.track(file, marker, deleteStrategy);
    }

    @Deprecated
    public static void track(String path, Object marker) {
        theInstance.track(path, marker);
    }

    @Deprecated
    public static void track(String path, Object marker, FileDeleteStrategy deleteStrategy) {
        theInstance.track(path, marker, deleteStrategy);
    }

    @Deprecated
    public static int getTrackCount() {
        return theInstance.getTrackCount();
    }

    @Deprecated
    public static synchronized void exitWhenFinished() {
        theInstance.exitWhenFinished();
    }

    public static FileCleaningTracker getInstance() {
        return theInstance;
    }
}

