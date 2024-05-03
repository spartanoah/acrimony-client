/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class ByteArrayCacheEntrySerializer
implements HttpCacheEntrySerializer<byte[]> {
    public static final ByteArrayCacheEntrySerializer INSTANCE = new ByteArrayCacheEntrySerializer();

    @Override
    public byte[] serialize(HttpCacheStorageEntry cacheEntry) throws ResourceIOException {
        if (cacheEntry == null) {
            return null;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(buf);){
            oos.writeObject(cacheEntry);
        } catch (IOException ex) {
            throw new ResourceIOException(ex.getMessage(), ex);
        }
        return buf.toByteArray();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public HttpCacheStorageEntry deserialize(byte[] serializedObject) throws ResourceIOException {
        if (serializedObject == null) {
            return null;
        }
        try (RestrictedObjectInputStream ois = new RestrictedObjectInputStream(new ByteArrayInputStream(serializedObject));){
            HttpCacheStorageEntry httpCacheStorageEntry = (HttpCacheStorageEntry)ois.readObject();
            return httpCacheStorageEntry;
        } catch (IOException | ClassNotFoundException ex) {
            throw new ResourceIOException(ex.getMessage(), ex);
        }
    }

    static class RestrictedObjectInputStream
    extends ObjectInputStream {
        private static final List<Pattern> ALLOWED_CLASS_PATTERNS = Collections.unmodifiableList(Arrays.asList(Pattern.compile("^(\\[L)?org\\.apache\\.hc\\.(.*)"), Pattern.compile("^(?:\\[+L)?java\\.util\\..*$"), Pattern.compile("^(?:\\[+L)?java\\.lang\\..*$"), Pattern.compile("^\\[+Z$"), Pattern.compile("^\\[+B$"), Pattern.compile("^\\[+C$"), Pattern.compile("^\\[+D$"), Pattern.compile("^\\[+F$"), Pattern.compile("^\\[+I$"), Pattern.compile("^\\[+J$"), Pattern.compile("^\\[+S$")));

        private RestrictedObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
            String className = objectStreamClass.getName();
            if (!RestrictedObjectInputStream.isAllowedClassName(className)) {
                throw new ResourceIOException(String.format("Class %s is not allowed for deserialization", objectStreamClass.getName()));
            }
            return super.resolveClass(objectStreamClass);
        }

        static boolean isAllowedClassName(String className) {
            for (Pattern allowedClassPattern : ALLOWED_CLASS_PATTERNS) {
                if (!allowedClassPattern.matcher(className).matches()) continue;
                return true;
            }
            return false;
        }
    }
}

