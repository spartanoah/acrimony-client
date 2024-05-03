/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class BasicPolymorphicTypeValidator
extends PolymorphicTypeValidator.Base
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final Set<Class<?>> _invalidBaseTypes;
    protected final TypeMatcher[] _baseTypeMatchers;
    protected final NameMatcher[] _subTypeNameMatchers;
    protected final TypeMatcher[] _subClassMatchers;

    protected BasicPolymorphicTypeValidator(Set<Class<?>> invalidBaseTypes, TypeMatcher[] baseTypeMatchers, NameMatcher[] subTypeNameMatchers, TypeMatcher[] subClassMatchers) {
        this._invalidBaseTypes = invalidBaseTypes;
        this._baseTypeMatchers = baseTypeMatchers;
        this._subTypeNameMatchers = subTypeNameMatchers;
        this._subClassMatchers = subClassMatchers;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public PolymorphicTypeValidator.Validity validateBaseType(MapperConfig<?> ctxt, JavaType baseType) {
        Class<?> rawBase = baseType.getRawClass();
        if (this._invalidBaseTypes != null && this._invalidBaseTypes.contains(rawBase)) {
            return PolymorphicTypeValidator.Validity.DENIED;
        }
        if (this._baseTypeMatchers != null) {
            for (TypeMatcher m : this._baseTypeMatchers) {
                if (!m.match(ctxt, rawBase)) continue;
                return PolymorphicTypeValidator.Validity.ALLOWED;
            }
        }
        return PolymorphicTypeValidator.Validity.INDETERMINATE;
    }

    @Override
    public PolymorphicTypeValidator.Validity validateSubClassName(MapperConfig<?> ctxt, JavaType baseType, String subClassName) throws JsonMappingException {
        if (this._subTypeNameMatchers != null) {
            for (NameMatcher m : this._subTypeNameMatchers) {
                if (!m.match(ctxt, subClassName)) continue;
                return PolymorphicTypeValidator.Validity.ALLOWED;
            }
        }
        return PolymorphicTypeValidator.Validity.INDETERMINATE;
    }

    @Override
    public PolymorphicTypeValidator.Validity validateSubType(MapperConfig<?> ctxt, JavaType baseType, JavaType subType) throws JsonMappingException {
        if (this._subClassMatchers != null) {
            Class<?> subClass = subType.getRawClass();
            for (TypeMatcher m : this._subClassMatchers) {
                if (!m.match(ctxt, subClass)) continue;
                return PolymorphicTypeValidator.Validity.ALLOWED;
            }
        }
        return PolymorphicTypeValidator.Validity.INDETERMINATE;
    }

    public static class Builder {
        protected Set<Class<?>> _invalidBaseTypes;
        protected List<TypeMatcher> _baseTypeMatchers;
        protected List<NameMatcher> _subTypeNameMatchers;
        protected List<TypeMatcher> _subTypeClassMatchers;

        protected Builder() {
        }

        public Builder allowIfBaseType(final Class<?> baseOfBase) {
            return this._appendBaseMatcher(new TypeMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, Class<?> clazz) {
                    return baseOfBase.isAssignableFrom(clazz);
                }
            });
        }

        public Builder allowIfBaseType(final Pattern patternForBase) {
            return this._appendBaseMatcher(new TypeMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, Class<?> clazz) {
                    return patternForBase.matcher(clazz.getName()).matches();
                }
            });
        }

        public Builder allowIfBaseType(final String prefixForBase) {
            return this._appendBaseMatcher(new TypeMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, Class<?> clazz) {
                    return clazz.getName().startsWith(prefixForBase);
                }
            });
        }

        public Builder allowIfBaseType(TypeMatcher matcher) {
            return this._appendBaseMatcher(matcher);
        }

        public Builder denyForExactBaseType(Class<?> baseTypeToDeny) {
            if (this._invalidBaseTypes == null) {
                this._invalidBaseTypes = new HashSet();
            }
            this._invalidBaseTypes.add(baseTypeToDeny);
            return this;
        }

        public Builder allowIfSubType(final Class<?> subTypeBase) {
            return this._appendSubClassMatcher(new TypeMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, Class<?> clazz) {
                    return subTypeBase.isAssignableFrom(clazz);
                }
            });
        }

        public Builder allowIfSubType(final Pattern patternForSubType) {
            return this._appendSubNameMatcher(new NameMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, String clazzName) {
                    return patternForSubType.matcher(clazzName).matches();
                }
            });
        }

        public Builder allowIfSubType(final String prefixForSubType) {
            return this._appendSubNameMatcher(new NameMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, String clazzName) {
                    return clazzName.startsWith(prefixForSubType);
                }
            });
        }

        public Builder allowIfSubType(TypeMatcher matcher) {
            return this._appendSubClassMatcher(matcher);
        }

        public Builder allowIfSubTypeIsArray() {
            return this._appendSubClassMatcher(new TypeMatcher(){

                @Override
                public boolean match(MapperConfig<?> config, Class<?> clazz) {
                    return clazz.isArray();
                }
            });
        }

        public BasicPolymorphicTypeValidator build() {
            return new BasicPolymorphicTypeValidator(this._invalidBaseTypes, this._baseTypeMatchers == null ? null : this._baseTypeMatchers.toArray(new TypeMatcher[0]), this._subTypeNameMatchers == null ? null : this._subTypeNameMatchers.toArray(new NameMatcher[0]), this._subTypeClassMatchers == null ? null : this._subTypeClassMatchers.toArray(new TypeMatcher[0]));
        }

        protected Builder _appendBaseMatcher(TypeMatcher matcher) {
            if (this._baseTypeMatchers == null) {
                this._baseTypeMatchers = new ArrayList<TypeMatcher>();
            }
            this._baseTypeMatchers.add(matcher);
            return this;
        }

        protected Builder _appendSubNameMatcher(NameMatcher matcher) {
            if (this._subTypeNameMatchers == null) {
                this._subTypeNameMatchers = new ArrayList<NameMatcher>();
            }
            this._subTypeNameMatchers.add(matcher);
            return this;
        }

        protected Builder _appendSubClassMatcher(TypeMatcher matcher) {
            if (this._subTypeClassMatchers == null) {
                this._subTypeClassMatchers = new ArrayList<TypeMatcher>();
            }
            this._subTypeClassMatchers.add(matcher);
            return this;
        }
    }

    public static abstract class NameMatcher {
        public abstract boolean match(MapperConfig<?> var1, String var2);
    }

    public static abstract class TypeMatcher {
        public abstract boolean match(MapperConfig<?> var1, Class<?> var2);
    }
}

