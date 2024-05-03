/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public interface VisibilityChecker<T extends VisibilityChecker<T>> {
    public T with(JsonAutoDetect var1);

    public T withOverrides(JsonAutoDetect.Value var1);

    public T with(JsonAutoDetect.Visibility var1);

    public T withVisibility(PropertyAccessor var1, JsonAutoDetect.Visibility var2);

    public T withGetterVisibility(JsonAutoDetect.Visibility var1);

    public T withIsGetterVisibility(JsonAutoDetect.Visibility var1);

    public T withSetterVisibility(JsonAutoDetect.Visibility var1);

    public T withCreatorVisibility(JsonAutoDetect.Visibility var1);

    public T withFieldVisibility(JsonAutoDetect.Visibility var1);

    public boolean isGetterVisible(Method var1);

    public boolean isGetterVisible(AnnotatedMethod var1);

    public boolean isIsGetterVisible(Method var1);

    public boolean isIsGetterVisible(AnnotatedMethod var1);

    public boolean isSetterVisible(Method var1);

    public boolean isSetterVisible(AnnotatedMethod var1);

    public boolean isCreatorVisible(Member var1);

    public boolean isCreatorVisible(AnnotatedMember var1);

    public boolean isFieldVisible(Field var1);

    public boolean isFieldVisible(AnnotatedField var1);

    public static class Std
    implements VisibilityChecker<Std>,
    Serializable {
        private static final long serialVersionUID = 1L;
        protected static final Std DEFAULT = new Std(JsonAutoDetect.Visibility.PUBLIC_ONLY, JsonAutoDetect.Visibility.PUBLIC_ONLY, JsonAutoDetect.Visibility.ANY, JsonAutoDetect.Visibility.ANY, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        protected final JsonAutoDetect.Visibility _getterMinLevel;
        protected final JsonAutoDetect.Visibility _isGetterMinLevel;
        protected final JsonAutoDetect.Visibility _setterMinLevel;
        protected final JsonAutoDetect.Visibility _creatorMinLevel;
        protected final JsonAutoDetect.Visibility _fieldMinLevel;

        public static Std defaultInstance() {
            return DEFAULT;
        }

        public Std(JsonAutoDetect ann) {
            this._getterMinLevel = ann.getterVisibility();
            this._isGetterMinLevel = ann.isGetterVisibility();
            this._setterMinLevel = ann.setterVisibility();
            this._creatorMinLevel = ann.creatorVisibility();
            this._fieldMinLevel = ann.fieldVisibility();
        }

        public Std(JsonAutoDetect.Visibility getter, JsonAutoDetect.Visibility isGetter, JsonAutoDetect.Visibility setter, JsonAutoDetect.Visibility creator, JsonAutoDetect.Visibility field) {
            this._getterMinLevel = getter;
            this._isGetterMinLevel = isGetter;
            this._setterMinLevel = setter;
            this._creatorMinLevel = creator;
            this._fieldMinLevel = field;
        }

        public Std(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                this._getterMinLevel = Std.DEFAULT._getterMinLevel;
                this._isGetterMinLevel = Std.DEFAULT._isGetterMinLevel;
                this._setterMinLevel = Std.DEFAULT._setterMinLevel;
                this._creatorMinLevel = Std.DEFAULT._creatorMinLevel;
                this._fieldMinLevel = Std.DEFAULT._fieldMinLevel;
            } else {
                this._getterMinLevel = v;
                this._isGetterMinLevel = v;
                this._setterMinLevel = v;
                this._creatorMinLevel = v;
                this._fieldMinLevel = v;
            }
        }

        public static Std construct(JsonAutoDetect.Value vis) {
            return DEFAULT.withOverrides(vis);
        }

        protected Std _with(JsonAutoDetect.Visibility g, JsonAutoDetect.Visibility isG, JsonAutoDetect.Visibility s, JsonAutoDetect.Visibility cr, JsonAutoDetect.Visibility f) {
            if (g == this._getterMinLevel && isG == this._isGetterMinLevel && s == this._setterMinLevel && cr == this._creatorMinLevel && f == this._fieldMinLevel) {
                return this;
            }
            return new Std(g, isG, s, cr, f);
        }

        @Override
        public Std with(JsonAutoDetect ann) {
            Std curr = this;
            if (ann != null) {
                return this._with(this._defaultOrOverride(this._getterMinLevel, ann.getterVisibility()), this._defaultOrOverride(this._isGetterMinLevel, ann.isGetterVisibility()), this._defaultOrOverride(this._setterMinLevel, ann.setterVisibility()), this._defaultOrOverride(this._creatorMinLevel, ann.creatorVisibility()), this._defaultOrOverride(this._fieldMinLevel, ann.fieldVisibility()));
            }
            return curr;
        }

        @Override
        public Std withOverrides(JsonAutoDetect.Value vis) {
            Std curr = this;
            if (vis != null) {
                return this._with(this._defaultOrOverride(this._getterMinLevel, vis.getGetterVisibility()), this._defaultOrOverride(this._isGetterMinLevel, vis.getIsGetterVisibility()), this._defaultOrOverride(this._setterMinLevel, vis.getSetterVisibility()), this._defaultOrOverride(this._creatorMinLevel, vis.getCreatorVisibility()), this._defaultOrOverride(this._fieldMinLevel, vis.getFieldVisibility()));
            }
            return curr;
        }

        private JsonAutoDetect.Visibility _defaultOrOverride(JsonAutoDetect.Visibility defaults, JsonAutoDetect.Visibility override) {
            if (override == JsonAutoDetect.Visibility.DEFAULT) {
                return defaults;
            }
            return override;
        }

        @Override
        public Std with(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                return DEFAULT;
            }
            return new Std(v);
        }

        @Override
        public Std withVisibility(PropertyAccessor method, JsonAutoDetect.Visibility v) {
            switch (method) {
                case GETTER: {
                    return this.withGetterVisibility(v);
                }
                case SETTER: {
                    return this.withSetterVisibility(v);
                }
                case CREATOR: {
                    return this.withCreatorVisibility(v);
                }
                case FIELD: {
                    return this.withFieldVisibility(v);
                }
                case IS_GETTER: {
                    return this.withIsGetterVisibility(v);
                }
                case ALL: {
                    return this.with(v);
                }
            }
            return this;
        }

        @Override
        public Std withGetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._getterMinLevel;
            }
            if (this._getterMinLevel == v) {
                return this;
            }
            return new Std(v, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
        }

        @Override
        public Std withIsGetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._isGetterMinLevel;
            }
            if (this._isGetterMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, v, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
        }

        @Override
        public Std withSetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._setterMinLevel;
            }
            if (this._setterMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, v, this._creatorMinLevel, this._fieldMinLevel);
        }

        @Override
        public Std withCreatorVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._creatorMinLevel;
            }
            if (this._creatorMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, v, this._fieldMinLevel);
        }

        @Override
        public Std withFieldVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._fieldMinLevel;
            }
            if (this._fieldMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, v);
        }

        @Override
        public boolean isCreatorVisible(Member m) {
            return this._creatorMinLevel.isVisible(m);
        }

        @Override
        public boolean isCreatorVisible(AnnotatedMember m) {
            return this.isCreatorVisible(m.getMember());
        }

        @Override
        public boolean isFieldVisible(Field f) {
            return this._fieldMinLevel.isVisible(f);
        }

        @Override
        public boolean isFieldVisible(AnnotatedField f) {
            return this.isFieldVisible(f.getAnnotated());
        }

        @Override
        public boolean isGetterVisible(Method m) {
            return this._getterMinLevel.isVisible(m);
        }

        @Override
        public boolean isGetterVisible(AnnotatedMethod m) {
            return this.isGetterVisible(m.getAnnotated());
        }

        @Override
        public boolean isIsGetterVisible(Method m) {
            return this._isGetterMinLevel.isVisible(m);
        }

        @Override
        public boolean isIsGetterVisible(AnnotatedMethod m) {
            return this.isIsGetterVisible(m.getAnnotated());
        }

        @Override
        public boolean isSetterVisible(Method m) {
            return this._setterMinLevel.isVisible(m);
        }

        @Override
        public boolean isSetterVisible(AnnotatedMethod m) {
            return this.isSetterVisible(m.getAnnotated());
        }

        public String toString() {
            return String.format("[Visibility: getter=%s,isGetter=%s,setter=%s,creator=%s,field=%s]", new Object[]{this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel});
        }
    }
}

