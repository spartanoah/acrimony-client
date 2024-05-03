/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPDouble;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPLong;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class ClassConstantPool {
    protected HashSet entriesContainsSet = new HashSet();
    protected HashSet othersContainsSet = new HashSet();
    private final HashSet mustStartClassPool = new HashSet();
    protected Map indexCache;
    private final List others = new ArrayList(500);
    private final List entries = new ArrayList(500);
    private boolean resolved;

    public ClassFileEntry add(ClassFileEntry entry) {
        if (entry instanceof ByteCode) {
            return null;
        }
        if (entry instanceof ConstantPoolEntry) {
            if (this.entriesContainsSet.add(entry)) {
                this.entries.add(entry);
            }
        } else if (this.othersContainsSet.add(entry)) {
            this.others.add(entry);
        }
        return entry;
    }

    public void addNestedEntries() {
        boolean added = true;
        ArrayList parents = new ArrayList(512);
        ArrayList<ClassFileEntry> children = new ArrayList<ClassFileEntry>(512);
        parents.addAll(this.entries);
        parents.addAll(this.others);
        while (added || parents.size() > 0) {
            children.clear();
            int entriesOriginalSize = this.entries.size();
            int othersOriginalSize = this.others.size();
            for (int indexParents = 0; indexParents < parents.size(); ++indexParents) {
                boolean isAtStart;
                ClassFileEntry entry = (ClassFileEntry)parents.get(indexParents);
                ClassFileEntry[] entryChildren = entry.getNestedClassFileEntries();
                children.addAll(Arrays.asList(entryChildren));
                boolean bl = isAtStart = entry instanceof ByteCode && ((ByteCode)entry).nestedMustStartClassPool();
                if (isAtStart) {
                    this.mustStartClassPool.addAll(Arrays.asList(entryChildren));
                }
                this.add(entry);
            }
            added = this.entries.size() != entriesOriginalSize || this.others.size() != othersOriginalSize;
            parents.clear();
            parents.addAll(children);
        }
    }

    public int indexOf(ClassFileEntry entry) {
        if (!this.resolved) {
            throw new IllegalStateException("Constant pool is not yet resolved; this does not make any sense");
        }
        if (null == this.indexCache) {
            throw new IllegalStateException("Index cache is not initialized!");
        }
        Integer entryIndex = (Integer)this.indexCache.get(entry);
        if (entryIndex != null) {
            return entryIndex + 1;
        }
        return -1;
    }

    public int size() {
        return this.entries.size();
    }

    public ClassFileEntry get(int i) {
        if (!this.resolved) {
            throw new IllegalStateException("Constant pool is not yet resolved; this does not make any sense");
        }
        return (ClassFileEntry)this.entries.get(--i);
    }

    public void resolve(Segment segment) {
        ClassFileEntry entry;
        int it;
        this.initialSort();
        this.sortClassPool();
        this.resolved = true;
        for (it = 0; it < this.entries.size(); ++it) {
            entry = (ClassFileEntry)this.entries.get(it);
            entry.resolve(this);
        }
        for (it = 0; it < this.others.size(); ++it) {
            entry = (ClassFileEntry)this.others.get(it);
            entry.resolve(this);
        }
    }

    private void initialSort() {
        TreeSet<ConstantPoolEntry> inCpAll = new TreeSet<ConstantPoolEntry>((arg0, arg1) -> ((ConstantPoolEntry)arg0).getGlobalIndex() - ((ConstantPoolEntry)arg1).getGlobalIndex());
        TreeSet<ConstantPoolEntry> cpUtf8sNotInCpAll = new TreeSet<ConstantPoolEntry>((arg0, arg1) -> ((CPUTF8)arg0).underlyingString().compareTo(((CPUTF8)arg1).underlyingString()));
        TreeSet<ConstantPoolEntry> cpClassesNotInCpAll = new TreeSet<ConstantPoolEntry>((arg0, arg1) -> ((CPClass)arg0).getName().compareTo(((CPClass)arg1).getName()));
        for (int index = 0; index < this.entries.size(); ++index) {
            ConstantPoolEntry entry = (ConstantPoolEntry)this.entries.get(index);
            if (entry.getGlobalIndex() == -1) {
                if (entry instanceof CPUTF8) {
                    cpUtf8sNotInCpAll.add(entry);
                    continue;
                }
                if (entry instanceof CPClass) {
                    cpClassesNotInCpAll.add(entry);
                    continue;
                }
                throw new Error("error");
            }
            inCpAll.add(entry);
        }
        this.entries.clear();
        this.entries.addAll(inCpAll);
        this.entries.addAll(cpUtf8sNotInCpAll);
        this.entries.addAll(cpClassesNotInCpAll);
    }

    public List entries() {
        return Collections.unmodifiableList(this.entries);
    }

    protected void sortClassPool() {
        ClassFileEntry entry;
        ArrayList<ClassFileEntry> startOfPool = new ArrayList<ClassFileEntry>(this.entries.size());
        ArrayList<ClassFileEntry> finalSort = new ArrayList<ClassFileEntry>(this.entries.size());
        for (int i = 0; i < this.entries.size(); ++i) {
            ClassFileEntry nextEntry = (ClassFileEntry)this.entries.get(i);
            if (this.mustStartClassPool.contains(nextEntry)) {
                startOfPool.add(nextEntry);
                continue;
            }
            finalSort.add(nextEntry);
        }
        this.indexCache = new HashMap(this.entries.size());
        int index = 0;
        this.entries.clear();
        for (int itIndex = 0; itIndex < startOfPool.size(); ++itIndex) {
            entry = (ClassFileEntry)startOfPool.get(itIndex);
            this.indexCache.put(entry, index);
            if (entry instanceof CPLong || entry instanceof CPDouble) {
                this.entries.add(entry);
                this.entries.add(entry);
                index += 2;
                continue;
            }
            this.entries.add(entry);
            ++index;
        }
        for (int itFinal = 0; itFinal < finalSort.size(); ++itFinal) {
            entry = (ClassFileEntry)finalSort.get(itFinal);
            this.indexCache.put(entry, index);
            if (entry instanceof CPLong || entry instanceof CPDouble) {
                this.entries.add(entry);
                this.entries.add(entry);
                index += 2;
                continue;
            }
            this.entries.add(entry);
            ++index;
        }
    }

    public ClassFileEntry addWithNestedEntries(ClassFileEntry entry) {
        this.add(entry);
        ClassFileEntry[] nestedEntries = entry.getNestedClassFileEntries();
        for (int i = 0; i < nestedEntries.length; ++i) {
            this.addWithNestedEntries(nestedEntries[i]);
        }
        return entry;
    }
}

