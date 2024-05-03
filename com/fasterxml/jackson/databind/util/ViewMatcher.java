/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.io.Serializable;

public class ViewMatcher
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static final ViewMatcher EMPTY = new ViewMatcher();

    public boolean isVisibleForView(Class<?> activeView) {
        return false;
    }

    public static ViewMatcher construct(Class<?>[] views) {
        if (views == null) {
            return EMPTY;
        }
        switch (views.length) {
            case 0: {
                return EMPTY;
            }
            case 1: {
                return new Single(views[0]);
            }
        }
        return new Multi(views);
    }

    private static final class Multi
    extends ViewMatcher
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Class<?>[] _views;

        public Multi(Class<?>[] v) {
            this._views = v;
        }

        @Override
        public boolean isVisibleForView(Class<?> activeView) {
            for (Class<?> view : this._views) {
                if (activeView != view && !view.isAssignableFrom(activeView)) continue;
                return true;
            }
            return false;
        }
    }

    private static final class Single
    extends ViewMatcher {
        private static final long serialVersionUID = 1L;
        private final Class<?> _view;

        public Single(Class<?> v) {
            this._view = v;
        }

        @Override
        public boolean isVisibleForView(Class<?> activeView) {
            return activeView == this._view || this._view.isAssignableFrom(activeView);
        }
    }
}

