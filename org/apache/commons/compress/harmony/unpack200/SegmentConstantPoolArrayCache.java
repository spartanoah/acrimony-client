/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

public class SegmentConstantPoolArrayCache {
    protected IdentityHashMap knownArrays = new IdentityHashMap(1000);
    protected List lastIndexes;
    protected String[] lastArray;
    protected String lastKey;

    public List indexesForArrayKey(String[] array, String key) {
        if (!this.arrayIsCached(array)) {
            this.cacheArray(array);
        }
        if (this.lastArray == array && this.lastKey == key) {
            return this.lastIndexes;
        }
        this.lastArray = array;
        this.lastKey = key;
        this.lastIndexes = ((CachedArray)this.knownArrays.get(array)).indexesForKey(key);
        return this.lastIndexes;
    }

    protected boolean arrayIsCached(String[] array) {
        if (!this.knownArrays.containsKey(array)) {
            return false;
        }
        CachedArray cachedArray = (CachedArray)this.knownArrays.get(array);
        return cachedArray.lastKnownSize() == array.length;
    }

    protected void cacheArray(String[] array) {
        if (this.arrayIsCached(array)) {
            throw new IllegalArgumentException("Trying to cache an array that already exists");
        }
        this.knownArrays.put(array, new CachedArray(array));
        this.lastArray = null;
    }

    protected class CachedArray {
        String[] primaryArray;
        int lastKnownSize;
        HashMap primaryTable;

        public CachedArray(String[] array) {
            this.primaryArray = array;
            this.lastKnownSize = array.length;
            this.primaryTable = new HashMap(this.lastKnownSize);
            this.cacheIndexes();
        }

        public int lastKnownSize() {
            return this.lastKnownSize;
        }

        public List indexesForKey(String key) {
            if (!this.primaryTable.containsKey(key)) {
                return Collections.EMPTY_LIST;
            }
            return (List)this.primaryTable.get(key);
        }

        protected void cacheIndexes() {
            for (int index = 0; index < this.primaryArray.length; ++index) {
                String key = this.primaryArray[index];
                if (!this.primaryTable.containsKey(key)) {
                    this.primaryTable.put(key, new ArrayList());
                }
                ((ArrayList)this.primaryTable.get(key)).add(index);
            }
        }
    }
}

