/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import org.apache.commons.compress.harmony.pack200.Pack200Exception;

public class SegmentOptions {
    private static final int DEFLATE_HINT = 32;
    private static final int HAVE_ALL_CODE_FLAGS = 4;
    private static final int HAVE_CLASS_FLAGS_HI = 512;
    private static final int HAVE_CODE_FLAGS_HI = 1024;
    private static final int HAVE_CP_NUMBERS = 2;
    private static final int HAVE_FIELD_FLAGS_HI = 1024;
    private static final int HAVE_FILE_HEADERS = 16;
    private static final int HAVE_FILE_MODTIME = 64;
    private static final int HAVE_FILE_OPTIONS = 128;
    private static final int HAVE_FILE_SIZE_HI = 256;
    private static final int HAVE_METHOD_FLAGS_HI = 2048;
    private static final int HAVE_SPECIAL_FORMATS = 1;
    private static final int UNUSED = -8184;
    private final int options;

    public SegmentOptions(int options) throws Pack200Exception {
        if ((options & 0xFFFFE008) != 0) {
            throw new Pack200Exception("Some unused flags are non-zero");
        }
        this.options = options;
    }

    public boolean hasAllCodeFlags() {
        return (this.options & 4) != 0;
    }

    public boolean hasArchiveFileCounts() {
        return (this.options & 0x10) != 0;
    }

    public boolean hasClassFlagsHi() {
        return (this.options & 0x200) != 0;
    }

    public boolean hasCodeFlagsHi() {
        return (this.options & 0x400) != 0;
    }

    public boolean hasCPNumberCounts() {
        return (this.options & 2) != 0;
    }

    public boolean hasFieldFlagsHi() {
        return (this.options & 0x400) != 0;
    }

    public boolean hasFileModtime() {
        return (this.options & 0x40) != 0;
    }

    public boolean hasFileOptions() {
        return (this.options & 0x80) != 0;
    }

    public boolean hasFileSizeHi() {
        return (this.options & 0x100) != 0;
    }

    public boolean hasMethodFlagsHi() {
        return (this.options & 0x800) != 0;
    }

    public boolean hasSpecialFormats() {
        return (this.options & 1) != 0;
    }

    public boolean shouldDeflate() {
        return (this.options & 0x20) != 0;
    }
}

