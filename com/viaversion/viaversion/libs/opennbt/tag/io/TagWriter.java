/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.io;

import com.viaversion.viaversion.libs.fastutil.io.FastBufferedOutputStream;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.libs.opennbt.tag.io.NBTIO;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.zip.GZIPOutputStream;

public final class TagWriter {
    private boolean named;

    public TagWriter named() {
        this.named = true;
        return this;
    }

    public void write(DataOutput out, Tag tag) throws IOException {
        NBTIO.writeTag(out, tag, this.named);
    }

    public void write(OutputStream out, Tag tag) throws IOException {
        NBTIO.writeTag(new DataOutputStream(out), tag, this.named);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(Path path, Tag tag, boolean compressed) throws IOException {
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.createFile(path, new FileAttribute[0]);
        }
        try (OutputStream out = new FastBufferedOutputStream(Files.newOutputStream(path, new OpenOption[0]));){
            if (compressed) {
                out = new GZIPOutputStream(out);
            }
            this.write(out, tag);
        }
    }
}

