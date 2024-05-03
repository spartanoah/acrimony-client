/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.bytecode.ClassFile
 */
package org.reflections.scanners;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javassist.bytecode.ClassFile;
import javax.annotation.Nullable;
import org.reflections.vfs.Vfs;

public interface Scanner {
    public List<Map.Entry<String, String>> scan(ClassFile var1);

    @Nullable
    default public List<Map.Entry<String, String>> scan(Vfs.File file) {
        return null;
    }

    default public String index() {
        return this.getClass().getSimpleName();
    }

    default public boolean acceptsInput(String file) {
        return file.endsWith(".class");
    }

    default public Map.Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleEntry<String, String>(key, value);
    }

    default public List<Map.Entry<String, String>> entries(Collection<String> keys, String value) {
        return keys.stream().map(key -> this.entry((String)key, value)).collect(Collectors.toList());
    }

    default public List<Map.Entry<String, String>> entries(String key, String value) {
        return Collections.singletonList(this.entry(key, value));
    }

    default public List<Map.Entry<String, String>> entries(String key, Collection<String> values) {
        return values.stream().map(value -> this.entry(key, (String)value)).collect(Collectors.toList());
    }
}

