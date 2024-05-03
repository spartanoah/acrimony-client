/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.serializers;

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import org.reflections.Reflections;
import org.reflections.serializers.Serializer;

public class JsonSerializer
implements Serializer {
    @Override
    public Reflections read(InputStream inputStream) {
        return new GsonBuilder().setPrettyPrinting().create().fromJson((Reader)new InputStreamReader(inputStream), Reflections.class);
    }

    @Override
    public File save(Reflections reflections, String filename) {
        try {
            File file = Serializer.prepareFile(filename);
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(reflections);
            Files.write(file.toPath(), json.getBytes(Charset.defaultCharset()), new OpenOption[0]);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

