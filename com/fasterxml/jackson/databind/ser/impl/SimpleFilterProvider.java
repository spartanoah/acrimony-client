/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleFilterProvider
extends FilterProvider
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final Map<String, PropertyFilter> _filtersById;
    protected PropertyFilter _defaultFilter;
    protected boolean _cfgFailOnUnknownId = true;

    public SimpleFilterProvider() {
        this(new HashMap());
    }

    public SimpleFilterProvider(Map<String, ?> mapping) {
        for (Object ob : mapping.values()) {
            if (ob instanceof PropertyFilter) continue;
            this._filtersById = SimpleFilterProvider._convert(mapping);
            return;
        }
        this._filtersById = mapping;
    }

    private static final Map<String, PropertyFilter> _convert(Map<String, ?> filters) {
        HashMap<String, PropertyFilter> result = new HashMap<String, PropertyFilter>();
        for (Map.Entry<String, ?> entry : filters.entrySet()) {
            Object f = entry.getValue();
            if (f instanceof PropertyFilter) {
                result.put(entry.getKey(), (PropertyFilter)f);
                continue;
            }
            if (f instanceof BeanPropertyFilter) {
                result.put(entry.getKey(), SimpleFilterProvider._convert((BeanPropertyFilter)f));
                continue;
            }
            throw new IllegalArgumentException("Unrecognized filter type (" + f.getClass().getName() + ")");
        }
        return result;
    }

    private static final PropertyFilter _convert(BeanPropertyFilter f) {
        return SimpleBeanPropertyFilter.from(f);
    }

    @Deprecated
    public SimpleFilterProvider setDefaultFilter(BeanPropertyFilter f) {
        this._defaultFilter = SimpleBeanPropertyFilter.from(f);
        return this;
    }

    public SimpleFilterProvider setDefaultFilter(PropertyFilter f) {
        this._defaultFilter = f;
        return this;
    }

    public SimpleFilterProvider setDefaultFilter(SimpleBeanPropertyFilter f) {
        this._defaultFilter = f;
        return this;
    }

    public PropertyFilter getDefaultFilter() {
        return this._defaultFilter;
    }

    public SimpleFilterProvider setFailOnUnknownId(boolean state) {
        this._cfgFailOnUnknownId = state;
        return this;
    }

    public boolean willFailOnUnknownId() {
        return this._cfgFailOnUnknownId;
    }

    @Deprecated
    public SimpleFilterProvider addFilter(String id, BeanPropertyFilter filter) {
        this._filtersById.put(id, SimpleFilterProvider._convert(filter));
        return this;
    }

    public SimpleFilterProvider addFilter(String id, PropertyFilter filter) {
        this._filtersById.put(id, filter);
        return this;
    }

    public SimpleFilterProvider addFilter(String id, SimpleBeanPropertyFilter filter) {
        this._filtersById.put(id, filter);
        return this;
    }

    public PropertyFilter removeFilter(String id) {
        return this._filtersById.remove(id);
    }

    @Override
    @Deprecated
    public BeanPropertyFilter findFilter(Object filterId) {
        throw new UnsupportedOperationException("Access to deprecated filters not supported");
    }

    @Override
    public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
        PropertyFilter f = this._filtersById.get(filterId);
        if (f == null && (f = this._defaultFilter) == null && this._cfgFailOnUnknownId) {
            throw new IllegalArgumentException("No filter configured with id '" + filterId + "' (type " + filterId.getClass().getName() + ")");
        }
        return f;
    }
}

