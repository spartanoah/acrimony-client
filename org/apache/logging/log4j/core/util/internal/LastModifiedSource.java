/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util.internal;

import java.io.File;
import java.net.URI;
import org.apache.logging.log4j.core.util.Source;

public class LastModifiedSource
extends Source {
    private volatile long lastModified;

    public LastModifiedSource(File file) {
        super(file);
        this.lastModified = 0L;
    }

    public LastModifiedSource(URI uri) {
        this(uri, 0L);
    }

    public LastModifiedSource(URI uri, long lastModifiedMillis) {
        super(uri);
        this.lastModified = lastModifiedMillis;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}

