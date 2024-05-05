/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.snbt.impl.v1_7;

import com.viaversion.viaversion.libs.mcstructs.snbt.ISNbtDeserializer;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtDeserializeException;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.Stack;

public class SNbtDeserializer_v1_7
implements ISNbtDeserializer<Tag> {
    private static final String ARRAY_PATTERN = "\\[[-\\d|,\\s]+]";
    private static final String BYTE_PATTERN = "[-+]?[0-9]+[b|B]";
    private static final String SHORT_PATTERN = "[-+]?[0-9]+[s|S]";
    private static final String INT_PATTERN = "[-+]?[0-9]+";
    private static final String LONG_PATTERN = "[-+]?[0-9]+[l|L]";
    private static final String FLOAT_PATTERN = "[-+]?[0-9]*\\.?[0-9]+[f|F]";
    private static final String DOUBLE_PATTERN = "[-+]?[0-9]*\\.?[0-9]+[d|D]";
    private static final String SHORT_DOUBLE_PATTERN = "[-+]?[0-9]*\\.?[0-9]+";

    @Override
    public Tag deserialize(String s) throws SNbtDeserializeException {
        int tagCount = this.getTagCount(s = s.trim());
        if (tagCount != 1) {
            throw new SNbtDeserializeException("Encountered multiple top tags, only one expected");
        }
        Tag tag = s.startsWith("{") ? this.parse("tag", s) : this.parse(this.find(s, true, false), this.find(s, false, false));
        return tag;
    }

    @Override
    public Tag deserializeValue(String s) throws SNbtDeserializeException {
        return this.parse("tag", s);
    }

    private Tag parse(String name, String value) throws SNbtDeserializeException {
        value = value.trim();
        this.getTagCount(value);
        if (value.startsWith("{")) {
            if (!value.endsWith("}")) {
                throw new SNbtDeserializeException("Unable to locate ending bracket } for: " + value);
            }
            value = value.substring(1, value.length() - 1);
            CompoundTag compound = new CompoundTag();
            while (!value.isEmpty()) {
                String pair = this.findPair(value, false);
                if (pair.isEmpty()) continue;
                String subName = this.find(pair, true, false);
                String subValue = this.find(pair, false, false);
                compound.put(subName, this.parse(subName, subValue));
                if (value.length() < pair.length() + 1) break;
                char next = value.charAt(pair.length());
                if (next != ',' && next != '{' && next != '}' && next != '[' && next != ']') {
                    throw new SNbtDeserializeException("Unexpected token '" + name + "' at: " + value.substring(pair.length()));
                }
                value = value.substring(pair.length() + 1);
            }
            return compound;
        }
        if (value.startsWith("[") && !value.matches(ARRAY_PATTERN)) {
            if (!value.endsWith("]")) {
                throw new SNbtDeserializeException("Unable to locate ending bracket ] for: " + value);
            }
            value = value.substring(1, value.length() - 1);
            ListTag list = new ListTag();
            while (!value.isEmpty()) {
                String pair = this.findPair(value, true);
                if (pair.isEmpty()) continue;
                String subName = this.find(pair, true, true);
                String subValue = this.find(pair, false, true);
                try {
                    list.add(this.parse(subName, subValue));
                } catch (IllegalArgumentException next) {
                    // empty catch block
                }
                if (value.length() < pair.length() + 1) break;
                char next = value.charAt(pair.length());
                if (next != ',' && next != '{' && next != '}' && next != '[' && next != ']') {
                    throw new SNbtDeserializeException("Unexpected token '" + name + "' at: " + value.substring(pair.length()));
                }
                value = value.substring(pair.length() + 1);
            }
            return list;
        }
        return this.parsePrimitive(value);
    }

    /*
     * Exception decompiling
     */
    private Tag parsePrimitive(String value) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [3[CATCHBLOCK], 0[TRYBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         *     at async.DecompilerRunnable.cfrDecompilation(DecompilerRunnable.java:350)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:311)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:26)
         *     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
         *     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
         *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
         *     at java.lang.Thread.run(Thread.java:750)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private int getTagCount(String s) throws SNbtDeserializeException {
        Stack<Character> brackets = new Stack<Character>();
        boolean quoted = false;
        int count = 0;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == '\"') {
                if (i > 0 && chars[i - 1] == '\\') {
                    if (quoted) continue;
                    throw new SNbtDeserializeException("Illegal use of \\\": " + s);
                }
                quoted = !quoted;
                continue;
            }
            if (quoted) continue;
            if (c == '{' || c == '[') {
                if (brackets.isEmpty()) {
                    ++count;
                }
                brackets.push(Character.valueOf(c));
                continue;
            }
            this.checkBrackets(s, c, brackets);
        }
        if (quoted) {
            throw new SNbtDeserializeException("Unbalanced quotation: " + s);
        }
        if (!brackets.isEmpty()) {
            throw new SNbtDeserializeException("Unbalanced brackets " + this.quotesToString(brackets) + ": " + s);
        }
        if (count == 0 && !s.isEmpty()) {
            return 1;
        }
        return count;
    }

    private String findPair(String s, boolean isArray) throws SNbtDeserializeException {
        int i;
        int separatorIndex = this.getCharIndex(s, ':');
        if (separatorIndex < 0 && !isArray) {
            throw new SNbtDeserializeException("Unable to locate name/value for string: " + s);
        }
        int pairSeparator = this.getCharIndex(s, ',');
        if (pairSeparator >= 0 && pairSeparator < separatorIndex && !isArray) {
            throw new SNbtDeserializeException("Name error at: " + s);
        }
        if (isArray && (separatorIndex < 0 || separatorIndex > pairSeparator)) {
            separatorIndex = -1;
        }
        Stack<Character> brackets = new Stack<Character>();
        int quoteEnd = 0;
        boolean quoted = false;
        boolean hasContent = false;
        boolean isString = false;
        char[] chars = s.toCharArray();
        for (i = separatorIndex + 1; i < chars.length; ++i) {
            char c = chars[i];
            if (c == '\"') {
                if (i > 0 && chars[i - 1] == '\\') {
                    if (!quoted) {
                        throw new SNbtDeserializeException("Illegal use of \\\": " + s);
                    }
                } else {
                    boolean bl = quoted = !quoted;
                    if (quoted && !hasContent) {
                        isString = true;
                    }
                    if (!quoted) {
                        quoteEnd = i;
                    }
                }
            } else if (!quoted) {
                if (c == '{' || c == '[') {
                    brackets.push(Character.valueOf(c));
                } else {
                    this.checkBrackets(s, c, brackets);
                    if (c == ',' && brackets.isEmpty()) {
                        return s.substring(0, i);
                    }
                }
            }
            if (Character.isWhitespace(c)) continue;
            if (!quoted && isString && quoteEnd != i) {
                return s.substring(0, quoteEnd + 1);
            }
            hasContent = true;
        }
        return s.substring(0, i);
    }

    private String find(String s, boolean name, boolean isArray) throws SNbtDeserializeException {
        if (isArray && ((s = s.trim()).startsWith("{") || s.startsWith("["))) {
            if (name) {
                return "";
            }
            return s;
        }
        int separatorIndex = s.indexOf(":");
        if (separatorIndex < 0) {
            if (isArray) {
                if (name) {
                    return "";
                }
                return s;
            }
            throw new SNbtDeserializeException("Unable to locate name/value separator for string: " + s);
        }
        if (name) {
            return s.substring(0, separatorIndex).trim();
        }
        return s.substring(separatorIndex + 1).trim();
    }

    private int getCharIndex(String s, char wanted) {
        boolean quoted = false;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == '\"') {
                if (i > 0 && chars[i - 1] == '\\') continue;
                quoted = !quoted;
                continue;
            }
            if (quoted) continue;
            if (c == wanted) {
                return i;
            }
            if (c != '{' && c != '[') continue;
            return -1;
        }
        return -1;
    }

    private void checkBrackets(String s, char close, Stack<Character> brackets) throws SNbtDeserializeException {
        if (close == '}' && (brackets.isEmpty() || brackets.pop().charValue() != '{')) {
            throw new SNbtDeserializeException("Unbalanced curly brackets {}: " + s);
        }
        if (close == ']' && (brackets.isEmpty() || brackets.pop().charValue() != '[')) {
            throw new SNbtDeserializeException("Unbalanced square brackets []: " + s);
        }
    }

    private String quotesToString(Stack<Character> quotes) {
        StringBuilder s = new StringBuilder();
        for (Character c : quotes) {
            s.append(c);
        }
        return s.toString();
    }
}

