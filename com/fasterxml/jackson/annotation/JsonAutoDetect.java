/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JacksonAnnotationValue;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonAutoDetect {
    public Visibility getterVisibility() default Visibility.DEFAULT;

    public Visibility isGetterVisibility() default Visibility.DEFAULT;

    public Visibility setterVisibility() default Visibility.DEFAULT;

    public Visibility creatorVisibility() default Visibility.DEFAULT;

    public Visibility fieldVisibility() default Visibility.DEFAULT;

    public static class Value
    implements JacksonAnnotationValue<JsonAutoDetect>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private static final Visibility DEFAULT_FIELD_VISIBILITY = Visibility.PUBLIC_ONLY;
        protected static final Value DEFAULT = new Value(DEFAULT_FIELD_VISIBILITY, Visibility.PUBLIC_ONLY, Visibility.PUBLIC_ONLY, Visibility.ANY, Visibility.PUBLIC_ONLY);
        protected static final Value NO_OVERRIDES = new Value(Visibility.DEFAULT, Visibility.DEFAULT, Visibility.DEFAULT, Visibility.DEFAULT, Visibility.DEFAULT);
        protected final Visibility _fieldVisibility;
        protected final Visibility _getterVisibility;
        protected final Visibility _isGetterVisibility;
        protected final Visibility _setterVisibility;
        protected final Visibility _creatorVisibility;

        private Value(Visibility fields, Visibility getters, Visibility isGetters, Visibility setters, Visibility creators) {
            this._fieldVisibility = fields;
            this._getterVisibility = getters;
            this._isGetterVisibility = isGetters;
            this._setterVisibility = setters;
            this._creatorVisibility = creators;
        }

        public static Value defaultVisibility() {
            return DEFAULT;
        }

        public static Value noOverrides() {
            return NO_OVERRIDES;
        }

        public static Value from(JsonAutoDetect src) {
            return Value.construct(src.fieldVisibility(), src.getterVisibility(), src.isGetterVisibility(), src.setterVisibility(), src.creatorVisibility());
        }

        public static Value construct(PropertyAccessor acc, Visibility visibility) {
            Visibility fields = Visibility.DEFAULT;
            Visibility getters = Visibility.DEFAULT;
            Visibility isGetters = Visibility.DEFAULT;
            Visibility setters = Visibility.DEFAULT;
            Visibility creators = Visibility.DEFAULT;
            switch (acc) {
                case CREATOR: {
                    creators = visibility;
                    break;
                }
                case FIELD: {
                    fields = visibility;
                    break;
                }
                case GETTER: {
                    getters = visibility;
                    break;
                }
                case IS_GETTER: {
                    isGetters = visibility;
                    break;
                }
                case NONE: {
                    break;
                }
                case SETTER: {
                    setters = visibility;
                    break;
                }
                case ALL: {
                    setters = creators = visibility;
                    isGetters = creators;
                    getters = creators;
                    fields = creators;
                }
            }
            return Value.construct(fields, getters, isGetters, setters, creators);
        }

        public static Value construct(Visibility fields, Visibility getters, Visibility isGetters, Visibility setters, Visibility creators) {
            Value v = Value._predefined(fields, getters, isGetters, setters, creators);
            if (v == null) {
                v = new Value(fields, getters, isGetters, setters, creators);
            }
            return v;
        }

        public Value withFieldVisibility(Visibility v) {
            return Value.construct(v, this._getterVisibility, this._isGetterVisibility, this._setterVisibility, this._creatorVisibility);
        }

        public Value withGetterVisibility(Visibility v) {
            return Value.construct(this._fieldVisibility, v, this._isGetterVisibility, this._setterVisibility, this._creatorVisibility);
        }

        public Value withIsGetterVisibility(Visibility v) {
            return Value.construct(this._fieldVisibility, this._getterVisibility, v, this._setterVisibility, this._creatorVisibility);
        }

        public Value withSetterVisibility(Visibility v) {
            return Value.construct(this._fieldVisibility, this._getterVisibility, this._isGetterVisibility, v, this._creatorVisibility);
        }

        public Value withCreatorVisibility(Visibility v) {
            return Value.construct(this._fieldVisibility, this._getterVisibility, this._isGetterVisibility, this._setterVisibility, v);
        }

        public static Value merge(Value base, Value overrides) {
            return base == null ? overrides : base.withOverrides(overrides);
        }

        public Value withOverrides(Value overrides) {
            Visibility creators;
            Visibility setters;
            Visibility isGetters;
            Visibility getters;
            if (overrides == null || overrides == NO_OVERRIDES || overrides == this) {
                return this;
            }
            if (Value._equals(this, overrides)) {
                return this;
            }
            Visibility fields = overrides._fieldVisibility;
            if (fields == Visibility.DEFAULT) {
                fields = this._fieldVisibility;
            }
            if ((getters = overrides._getterVisibility) == Visibility.DEFAULT) {
                getters = this._getterVisibility;
            }
            if ((isGetters = overrides._isGetterVisibility) == Visibility.DEFAULT) {
                isGetters = this._isGetterVisibility;
            }
            if ((setters = overrides._setterVisibility) == Visibility.DEFAULT) {
                setters = this._setterVisibility;
            }
            if ((creators = overrides._creatorVisibility) == Visibility.DEFAULT) {
                creators = this._creatorVisibility;
            }
            return Value.construct(fields, getters, isGetters, setters, creators);
        }

        @Override
        public Class<JsonAutoDetect> valueFor() {
            return JsonAutoDetect.class;
        }

        public Visibility getFieldVisibility() {
            return this._fieldVisibility;
        }

        public Visibility getGetterVisibility() {
            return this._getterVisibility;
        }

        public Visibility getIsGetterVisibility() {
            return this._isGetterVisibility;
        }

        public Visibility getSetterVisibility() {
            return this._setterVisibility;
        }

        public Visibility getCreatorVisibility() {
            return this._creatorVisibility;
        }

        protected Object readResolve() {
            Value v = Value._predefined(this._fieldVisibility, this._getterVisibility, this._isGetterVisibility, this._setterVisibility, this._creatorVisibility);
            return v == null ? this : v;
        }

        public String toString() {
            return String.format("JsonAutoDetect.Value(fields=%s,getters=%s,isGetters=%s,setters=%s,creators=%s)", new Object[]{this._fieldVisibility, this._getterVisibility, this._isGetterVisibility, this._setterVisibility, this._creatorVisibility});
        }

        public int hashCode() {
            return 1 + this._fieldVisibility.ordinal() ^ 3 * this._getterVisibility.ordinal() - 7 * this._isGetterVisibility.ordinal() + 11 * this._setterVisibility.ordinal() ^ 13 * this._creatorVisibility.ordinal();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            return o.getClass() == this.getClass() && Value._equals(this, (Value)o);
        }

        private static Value _predefined(Visibility fields, Visibility getters, Visibility isGetters, Visibility setters, Visibility creators) {
            if (fields == DEFAULT_FIELD_VISIBILITY) {
                if (getters == Value.DEFAULT._getterVisibility && isGetters == Value.DEFAULT._isGetterVisibility && setters == Value.DEFAULT._setterVisibility && creators == Value.DEFAULT._creatorVisibility) {
                    return DEFAULT;
                }
            } else if (fields == Visibility.DEFAULT && getters == Visibility.DEFAULT && isGetters == Visibility.DEFAULT && setters == Visibility.DEFAULT && creators == Visibility.DEFAULT) {
                return NO_OVERRIDES;
            }
            return null;
        }

        private static boolean _equals(Value a, Value b) {
            return a._fieldVisibility == b._fieldVisibility && a._getterVisibility == b._getterVisibility && a._isGetterVisibility == b._isGetterVisibility && a._setterVisibility == b._setterVisibility && a._creatorVisibility == b._creatorVisibility;
        }
    }

    public static enum Visibility {
        ANY,
        NON_PRIVATE,
        PROTECTED_AND_PUBLIC,
        PUBLIC_ONLY,
        NONE,
        DEFAULT;


        public boolean isVisible(Member m) {
            switch (this) {
                case ANY: {
                    return true;
                }
                case NONE: {
                    return false;
                }
                case NON_PRIVATE: {
                    return !Modifier.isPrivate(m.getModifiers());
                }
                case PROTECTED_AND_PUBLIC: {
                    if (Modifier.isProtected(m.getModifiers())) {
                        return true;
                    }
                }
                case PUBLIC_ONLY: {
                    return Modifier.isPublic(m.getModifiers());
                }
            }
            return false;
        }
    }
}

