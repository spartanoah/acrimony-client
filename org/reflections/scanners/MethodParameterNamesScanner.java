/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.bytecode.ClassFile
 *  javassist.bytecode.CodeAttribute
 *  javassist.bytecode.LocalVariableAttribute
 *  javassist.bytecode.MethodInfo
 */
package org.reflections.scanners;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.reflections.scanners.Scanner;
import org.reflections.util.JavassistHelper;

public class MethodParameterNamesScanner
implements Scanner {
    @Override
    public List<Map.Entry<String, String>> scan(ClassFile classFile) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
        for (MethodInfo method : classFile.getMethods()) {
            String key = JavassistHelper.methodName(classFile, method);
            String value = this.getString(method);
            if (value.isEmpty()) continue;
            entries.add(this.entry(key, value));
        }
        return entries;
    }

    private String getString(MethodInfo method) {
        CodeAttribute codeAttribute = method.getCodeAttribute();
        LocalVariableAttribute table = codeAttribute != null ? (LocalVariableAttribute)codeAttribute.getAttribute("LocalVariableTable") : null;
        int length = JavassistHelper.getParameters(method).size();
        if (length > 0) {
            int shift = Modifier.isStatic(method.getAccessFlags()) ? 0 : 1;
            return IntStream.range(shift, length + shift).mapToObj(i -> method.getConstPool().getUtf8Info(table.nameIndex(i))).filter(name -> !name.startsWith("this$")).collect(Collectors.joining(", "));
        }
        return "";
    }
}

