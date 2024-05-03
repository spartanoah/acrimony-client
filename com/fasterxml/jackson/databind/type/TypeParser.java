/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TypeParser
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final TypeFactory _factory;

    public TypeParser(TypeFactory f) {
        this._factory = f;
    }

    public TypeParser withFactory(TypeFactory f) {
        return f == this._factory ? this : new TypeParser(f);
    }

    public JavaType parse(String canonical) throws IllegalArgumentException {
        MyTokenizer tokens = new MyTokenizer(canonical.trim());
        JavaType type = this.parseType(tokens);
        if (tokens.hasMoreTokens()) {
            throw this._problem(tokens, "Unexpected tokens after complete type");
        }
        return type;
    }

    protected JavaType parseType(MyTokenizer tokens) throws IllegalArgumentException {
        if (!tokens.hasMoreTokens()) {
            throw this._problem(tokens, "Unexpected end-of-string");
        }
        Class<?> base = this.findClass(tokens.nextToken(), tokens);
        if (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("<".equals(token)) {
                List<JavaType> parameterTypes = this.parseTypes(tokens);
                TypeBindings b = TypeBindings.create(base, parameterTypes);
                return this._factory._fromClass(null, base, b);
            }
            tokens.pushBack(token);
        }
        return this._factory._fromClass(null, base, TypeBindings.emptyBindings());
    }

    protected List<JavaType> parseTypes(MyTokenizer tokens) throws IllegalArgumentException {
        ArrayList<JavaType> types = new ArrayList<JavaType>();
        while (tokens.hasMoreTokens()) {
            types.add(this.parseType(tokens));
            if (!tokens.hasMoreTokens()) break;
            String token = tokens.nextToken();
            if (">".equals(token)) {
                return types;
            }
            if (",".equals(token)) continue;
            throw this._problem(tokens, "Unexpected token '" + token + "', expected ',' or '>')");
        }
        throw this._problem(tokens, "Unexpected end-of-string");
    }

    protected Class<?> findClass(String className, MyTokenizer tokens) {
        try {
            return this._factory.findClass(className);
        } catch (Exception e) {
            ClassUtil.throwIfRTE(e);
            throw this._problem(tokens, "Cannot locate class '" + className + "', problem: " + e.getMessage());
        }
    }

    protected IllegalArgumentException _problem(MyTokenizer tokens, String msg) {
        return new IllegalArgumentException(String.format("Failed to parse type '%s' (remaining: '%s'): %s", tokens.getAllInput(), tokens.getRemainingInput(), msg));
    }

    static final class MyTokenizer
    extends StringTokenizer {
        protected final String _input;
        protected int _index;
        protected String _pushbackToken;

        public MyTokenizer(String str) {
            super(str, "<,>", true);
            this._input = str;
        }

        @Override
        public boolean hasMoreTokens() {
            return this._pushbackToken != null || super.hasMoreTokens();
        }

        @Override
        public String nextToken() {
            String token;
            if (this._pushbackToken != null) {
                token = this._pushbackToken;
                this._pushbackToken = null;
            } else {
                token = super.nextToken();
                this._index += token.length();
                token = token.trim();
            }
            return token;
        }

        public void pushBack(String token) {
            this._pushbackToken = token;
        }

        public String getAllInput() {
            return this._input;
        }

        public String getRemainingInput() {
            return this._input.substring(this._index);
        }
    }
}

