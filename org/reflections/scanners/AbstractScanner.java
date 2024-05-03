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
import org.reflections.scanners.Scanner;

@Deprecated
class AbstractScanner
implements Scanner {
    protected final Scanner scanner;

    AbstractScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String index() {
        return this.scanner.index();
    }

    @Override
    public List<Map.Entry<String, String>> scan(ClassFile cls) {
        return this.scanner.scan(cls);
    }
}

