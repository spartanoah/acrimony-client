/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.filter;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.Filterable;

public abstract class AbstractFilterable
extends AbstractLifeCycle
implements Filterable {
    private volatile Filter filter;
    @PluginElement(value="Properties")
    private final Property[] propertyArray;

    protected AbstractFilterable() {
        this(null, Property.EMPTY_ARRAY);
    }

    protected AbstractFilterable(Filter filter) {
        this(filter, Property.EMPTY_ARRAY);
    }

    protected AbstractFilterable(Filter filter, Property[] propertyArray) {
        this.filter = filter;
        this.propertyArray = propertyArray == null ? Property.EMPTY_ARRAY : propertyArray;
    }

    @Override
    public synchronized void addFilter(Filter filter) {
        if (filter == null) {
            return;
        }
        if (this.filter == null) {
            this.filter = filter;
        } else if (this.filter instanceof CompositeFilter) {
            this.filter = ((CompositeFilter)this.filter).addFilter(filter);
        } else {
            Filter[] filters = new Filter[]{this.filter, filter};
            this.filter = CompositeFilter.createFilters(filters);
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public boolean hasFilter() {
        return this.filter != null;
    }

    @Override
    public boolean isFiltered(LogEvent event) {
        return this.filter != null && this.filter.filter(event) == Filter.Result.DENY;
    }

    @Override
    public synchronized void removeFilter(Filter filter) {
        if (this.filter == null || filter == null) {
            return;
        }
        if (this.filter == filter || this.filter.equals(filter)) {
            this.filter = null;
        } else if (this.filter instanceof CompositeFilter) {
            CompositeFilter composite = (CompositeFilter)this.filter;
            if ((composite = composite.removeFilter(filter)).size() > 1) {
                this.filter = composite;
            } else if (composite.size() == 1) {
                Iterator<Filter> iter = composite.iterator();
                this.filter = iter.next();
            } else {
                this.filter = null;
            }
        }
    }

    @Override
    public void start() {
        this.setStarting();
        if (this.filter != null) {
            this.filter.start();
        }
        this.setStarted();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        return this.stop(timeout, timeUnit, true);
    }

    protected boolean stop(long timeout, TimeUnit timeUnit, boolean changeLifeCycleState) {
        if (changeLifeCycleState) {
            this.setStopping();
        }
        boolean stopped = true;
        if (this.filter != null) {
            if (this.filter instanceof LifeCycle2) {
                stopped = ((LifeCycle2)((Object)this.filter)).stop(timeout, timeUnit);
            } else {
                this.filter.stop();
                stopped = true;
            }
        }
        if (changeLifeCycleState) {
            this.setStopped();
        }
        return stopped;
    }

    public Property[] getPropertyArray() {
        return this.propertyArray;
    }

    public static abstract class Builder<B extends Builder<B>> {
        @PluginElement(value="Filter")
        private Filter filter;
        @PluginElement(value="Properties")
        private Property[] propertyArray;

        public B asBuilder() {
            return (B)this;
        }

        public Filter getFilter() {
            return this.filter;
        }

        public Property[] getPropertyArray() {
            return this.propertyArray;
        }

        public B setFilter(Filter filter) {
            this.filter = filter;
            return this.asBuilder();
        }

        public B setPropertyArray(Property[] properties) {
            this.propertyArray = properties;
            return this.asBuilder();
        }

        @Deprecated
        public B withFilter(Filter filter) {
            return this.setFilter(filter);
        }
    }
}

