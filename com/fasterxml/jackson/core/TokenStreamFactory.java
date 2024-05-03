/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.io.DataOutputAsStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;

public abstract class TokenStreamFactory
implements Versioned,
Serializable {
    private static final long serialVersionUID = 2L;

    public abstract boolean requiresPropertyOrdering();

    public abstract boolean canHandleBinaryNatively();

    public abstract boolean canParseAsync();

    public abstract Class<? extends FormatFeature> getFormatReadFeatureType();

    public abstract Class<? extends FormatFeature> getFormatWriteFeatureType();

    public abstract boolean canUseSchema(FormatSchema var1);

    public abstract String getFormatName();

    public abstract boolean isEnabled(JsonParser.Feature var1);

    public abstract boolean isEnabled(JsonGenerator.Feature var1);

    public abstract int getParserFeatures();

    public abstract int getGeneratorFeatures();

    public abstract int getFormatParserFeatures();

    public abstract int getFormatGeneratorFeatures();

    public abstract JsonParser createParser(byte[] var1) throws IOException;

    public abstract JsonParser createParser(byte[] var1, int var2, int var3) throws IOException;

    public abstract JsonParser createParser(char[] var1) throws IOException;

    public abstract JsonParser createParser(char[] var1, int var2, int var3) throws IOException;

    public abstract JsonParser createParser(DataInput var1) throws IOException;

    public abstract JsonParser createParser(File var1) throws IOException;

    public abstract JsonParser createParser(InputStream var1) throws IOException;

    public abstract JsonParser createParser(Reader var1) throws IOException;

    public abstract JsonParser createParser(String var1) throws IOException;

    public abstract JsonParser createParser(URL var1) throws IOException;

    public abstract JsonParser createNonBlockingByteArrayParser() throws IOException;

    public abstract JsonGenerator createGenerator(DataOutput var1, JsonEncoding var2) throws IOException;

    public abstract JsonGenerator createGenerator(DataOutput var1) throws IOException;

    public abstract JsonGenerator createGenerator(File var1, JsonEncoding var2) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream var1) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream var1, JsonEncoding var2) throws IOException;

    public abstract JsonGenerator createGenerator(Writer var1) throws IOException;

    protected OutputStream _createDataOutputWrapper(DataOutput out) {
        return new DataOutputAsStream(out);
    }

    protected InputStream _optimizedStreamFromURL(URL url) throws IOException {
        String path;
        String host;
        if ("file".equals(url.getProtocol()) && ((host = url.getHost()) == null || host.length() == 0) && (path = url.getPath()).indexOf(37) < 0) {
            return new FileInputStream(url.getPath());
        }
        return url.openStream();
    }
}

