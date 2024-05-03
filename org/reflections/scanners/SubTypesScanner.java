/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.bytecode.ClassFile
 */
package org.reflections.scanners;

import java.util.List;
import java.util.Map;
import javassist.bytecode.ClassFile;
import org.reflections.scanners.AbstractScanner;
import org.reflections.scanners.Scanners;

@Deprecated
public class SubTypesScanner
extends AbstractScanner {
    @Deprecated
    public SubTypesScanner() {
        super(Scanners.SubTypes);
    }

    @Deprecated
    public SubTypesScanner(boolean excludeObjectClass) {
        super(excludeObjectClass ? Scanners.SubTypes : Scanners.SubTypes.filterResultsBy(s -> true));
    }

    @Override
    public List<Map.Entry<String, String>> scan(ClassFile cls) {
        return this.scanner.scan(cls);
    }
}

