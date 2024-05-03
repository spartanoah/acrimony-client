/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class ReflectiveTypeAdapterFactory
implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final Excluder excluder;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
    }

    public boolean excludeField(Field f, boolean serialize) {
        return !this.excluder.excludeClass(f.getType(), serialize) && !this.excluder.excludeField(f, serialize);
    }

    private String getFieldName(Field f) {
        SerializedName serializedName = f.getAnnotation(SerializedName.class);
        return serializedName == null ? this.fieldNamingPolicy.translateName(f) : serializedName.value();
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> raw = type.getRawType();
        if (!Object.class.isAssignableFrom(raw)) {
            return null;
        }
        ObjectConstructor<T> constructor = this.constructorConstructor.get(type);
        return new Adapter(constructor, this.getBoundFields(gson, type, raw));
    }

    private BoundField createBoundField(final Gson context, final Field field, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        return new BoundField(name, serialize, deserialize){
            final TypeAdapter<?> typeAdapter;
            {
                super(x0, x1, x2);
                this.typeAdapter = context.getAdapter(fieldType);
            }

            void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                TypeAdapterRuntimeTypeWrapper t = new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
                ((TypeAdapter)t).write(writer, fieldValue);
            }

            void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = this.typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }
        };
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
        LinkedHashMap<String, BoundField> result = new LinkedHashMap<String, BoundField>();
        if (raw.isInterface()) {
            return result;
        }
        Type declaredType = type.getType();
        while (raw != Object.class) {
            Field[] fields;
            for (Field field : fields = raw.getDeclaredFields()) {
                boolean serialize = this.excludeField(field, true);
                boolean deserialize = this.excludeField(field, false);
                if (!serialize && !deserialize) continue;
                field.setAccessible(true);
                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
                BoundField boundField = this.createBoundField(context, field, this.getFieldName(field), TypeToken.get(fieldType), serialize, deserialize);
                BoundField previous = result.put(boundField.name, boundField);
                if (previous == null) continue;
                throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static final class Adapter<T>
    extends TypeAdapter<T> {
        private final ObjectConstructor<T> constructor;
        private final Map<String, BoundField> boundFields;

        private Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            T instance = this.constructor.construct();
            try {
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = this.boundFields.get(name);
                    if (field == null || !field.deserialized) {
                        in.skipValue();
                        continue;
                    }
                    field.read(in, instance);
                }
            } catch (IllegalStateException e) {
                throw new JsonSyntaxException(e);
            } catch (IllegalAccessException e) {
                throw new AssertionError((Object)e);
            }
            in.endObject();
            return instance;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            try {
                for (BoundField boundField : this.boundFields.values()) {
                    if (!boundField.serialized) continue;
                    out.name(boundField.name);
                    boundField.write(out, value);
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
            out.endObject();
        }
    }

    static abstract class BoundField {
        final String name;
        final boolean serialized;
        final boolean deserialized;

        protected BoundField(String name, boolean serialized, boolean deserialized) {
            this.name = name;
            this.serialized = serialized;
            this.deserialized = deserialized;
        }

        abstract void write(JsonWriter var1, Object var2) throws IOException, IllegalAccessException;

        abstract void read(JsonReader var1, Object var2) throws IOException, IllegalAccessException;
    }
}

