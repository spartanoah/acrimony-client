/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public abstract class FilterProvider {
    @Deprecated
    public abstract BeanPropertyFilter findFilter(Object var1);

    public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
        BeanPropertyFilter old = this.findFilter(filterId);
        if (old == null) {
            return null;
        }
        return SimpleBeanPropertyFilter.from(old);
    }
}

