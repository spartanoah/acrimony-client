/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.Pack200Adapter;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingOptions;
import org.apache.commons.compress.java.util.jar.Pack200;

public class Pack200PackerAdapter
extends Pack200Adapter
implements Pack200.Packer {
    private final PackingOptions options = new PackingOptions();

    @Override
    public void pack(JarFile file, OutputStream out) throws IOException {
        if (file == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        this.completed(0.0);
        try {
            new Archive(file, out, this.options).pack();
        } catch (Pack200Exception e) {
            throw new IOException("Failed to pack Jar:" + String.valueOf(e));
        }
        this.completed(1.0);
    }

    @Override
    public void pack(JarInputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        this.completed(0.0);
        PackingOptions options = new PackingOptions();
        try {
            new Archive(in, out, options).pack();
        } catch (Pack200Exception e) {
            throw new IOException("Failed to pack Jar:" + String.valueOf(e));
        }
        this.completed(1.0);
        in.close();
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
        if (newValue != null && !newValue.equals(oldValue)) {
            if (propertyName.startsWith("pack.class.attribute.")) {
                String attributeName = propertyName.substring("pack.class.attribute.".length());
                this.options.addClassAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.startsWith("pack.code.attribute.")) {
                String attributeName = propertyName.substring("pack.code.attribute.".length());
                this.options.addCodeAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals("pack.deflate.hint")) {
                this.options.setDeflateHint((String)newValue);
            } else if (propertyName.equals("pack.effort")) {
                this.options.setEffort(Integer.parseInt((String)newValue));
            } else if (propertyName.startsWith("pack.field.attribute.")) {
                String attributeName = propertyName.substring("pack.field.attribute.".length());
                this.options.addFieldAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals("pack.keep.file.order")) {
                this.options.setKeepFileOrder(Boolean.parseBoolean((String)newValue));
            } else if (propertyName.startsWith("pack.method.attribute.")) {
                String attributeName = propertyName.substring("pack.method.attribute.".length());
                this.options.addMethodAttributeAction(attributeName, (String)newValue);
            } else if (propertyName.equals("pack.modification.time")) {
                this.options.setModificationTime((String)newValue);
            } else if (propertyName.startsWith("pack.pass.file.")) {
                if (oldValue != null && !oldValue.equals("")) {
                    this.options.removePassFile((String)oldValue);
                }
                this.options.addPassFile((String)newValue);
            } else if (propertyName.equals("pack.segment.limit")) {
                this.options.setSegmentLimit(Long.parseLong((String)newValue));
            } else if (propertyName.equals("pack.unknown.attribute")) {
                this.options.setUnknownAttributeAction((String)newValue);
            }
        }
    }
}

