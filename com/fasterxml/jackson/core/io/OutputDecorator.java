/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.io.IOContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;

public abstract class OutputDecorator
implements Serializable {
    public abstract OutputStream decorate(IOContext var1, OutputStream var2) throws IOException;

    public abstract Writer decorate(IOContext var1, Writer var2) throws IOException;
}

