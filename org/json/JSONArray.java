/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParserConfiguration;
import org.json.JSONPointer;
import org.json.JSONPointerException;
import org.json.JSONString;
import org.json.JSONTokener;
import org.json.NumberConversionUtil;

public class JSONArray
implements Iterable<Object> {
    private final ArrayList<Object> myArrayList;

    public JSONArray() {
        this.myArrayList = new ArrayList();
    }

    public JSONArray(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        char nextChar = x.nextClean();
        if (nextChar == '\u0000') {
            throw x.syntaxError("Expected a ',' or ']'");
        }
        if (nextChar != ']') {
            x.back();
            block5: while (true) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(JSONObject.NULL);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                switch (x.nextClean()) {
                    case '\u0000': {
                        throw x.syntaxError("Expected a ',' or ']'");
                    }
                    case ',': {
                        nextChar = x.nextClean();
                        if (nextChar == '\u0000') {
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            return;
                        }
                        x.back();
                        continue block5;
                    }
                    case ']': {
                        return;
                    }
                }
                break;
            }
            throw x.syntaxError("Expected a ',' or ']'");
        }
    }

    public JSONArray(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public JSONArray(Collection<?> collection) {
        this(collection, 0, new JSONParserConfiguration());
    }

    public JSONArray(Collection<?> collection, JSONParserConfiguration jsonParserConfiguration) {
        this(collection, 0, jsonParserConfiguration);
    }

    JSONArray(Collection<?> collection, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        if (recursionDepth > jsonParserConfiguration.getMaxNestingDepth()) {
            throw new JSONException("JSONArray has reached recursion depth limit of " + jsonParserConfiguration.getMaxNestingDepth());
        }
        if (collection == null) {
            this.myArrayList = new ArrayList();
        } else {
            this.myArrayList = new ArrayList(collection.size());
            this.addAll(collection, true, recursionDepth, jsonParserConfiguration);
        }
    }

    public JSONArray(Iterable<?> iter) {
        this();
        if (iter == null) {
            return;
        }
        this.addAll(iter, true);
    }

    public JSONArray(JSONArray array) {
        this.myArrayList = array == null ? new ArrayList() : new ArrayList<Object>(array.myArrayList);
    }

    public JSONArray(Object array) throws JSONException {
        this();
        if (!array.getClass().isArray()) {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
        this.addAll(array, true, 0);
    }

    public JSONArray(int initialCapacity) throws JSONException {
        if (initialCapacity < 0) {
            throw new JSONException("JSONArray initial capacity cannot be negative.");
        }
        this.myArrayList = new ArrayList(initialCapacity);
    }

    @Override
    public Iterator<Object> iterator() {
        return this.myArrayList.iterator();
    }

    public Object get(int index) throws JSONException {
        Object object = this.opt(index);
        if (object == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return object;
    }

    public boolean getBoolean(int index) throws JSONException {
        Object object = this.get(index);
        if (object.equals(Boolean.FALSE) || object instanceof String && ((String)object).equalsIgnoreCase("false")) {
            return false;
        }
        if (object.equals(Boolean.TRUE) || object instanceof String && ((String)object).equalsIgnoreCase("true")) {
            return true;
        }
        throw JSONArray.wrongValueFormatException(index, "boolean", object, null);
    }

    public double getDouble(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number)object).doubleValue();
        }
        try {
            return Double.parseDouble(object.toString());
        } catch (Exception e) {
            throw JSONArray.wrongValueFormatException(index, "double", object, e);
        }
    }

    public float getFloat(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number)object).floatValue();
        }
        try {
            return Float.parseFloat(object.toString());
        } catch (Exception e) {
            throw JSONArray.wrongValueFormatException(index, "float", object, e);
        }
    }

    public Number getNumber(int index) throws JSONException {
        Object object = this.get(index);
        try {
            if (object instanceof Number) {
                return (Number)object;
            }
            return NumberConversionUtil.stringToNumber(object.toString());
        } catch (Exception e) {
            throw JSONArray.wrongValueFormatException(index, "number", object, e);
        }
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException {
        E val2 = this.optEnum(clazz, index);
        if (val2 == null) {
            throw JSONArray.wrongValueFormatException(index, "enum of type " + JSONObject.quote(clazz.getSimpleName()), this.opt(index), null);
        }
        return val2;
    }

    public BigDecimal getBigDecimal(int index) throws JSONException {
        Object object = this.get(index);
        BigDecimal val2 = JSONObject.objectToBigDecimal(object, null);
        if (val2 == null) {
            throw JSONArray.wrongValueFormatException(index, "BigDecimal", object, null);
        }
        return val2;
    }

    public BigInteger getBigInteger(int index) throws JSONException {
        Object object = this.get(index);
        BigInteger val2 = JSONObject.objectToBigInteger(object, null);
        if (val2 == null) {
            throw JSONArray.wrongValueFormatException(index, "BigInteger", object, null);
        }
        return val2;
    }

    public int getInt(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number)object).intValue();
        }
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            throw JSONArray.wrongValueFormatException(index, "int", object, e);
        }
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof JSONArray) {
            return (JSONArray)object;
        }
        throw JSONArray.wrongValueFormatException(index, "JSONArray", object, null);
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof JSONObject) {
            return (JSONObject)object;
        }
        throw JSONArray.wrongValueFormatException(index, "JSONObject", object, null);
    }

    public long getLong(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number)object).longValue();
        }
        try {
            return Long.parseLong(object.toString());
        } catch (Exception e) {
            throw JSONArray.wrongValueFormatException(index, "long", object, e);
        }
    }

    public String getString(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof String) {
            return (String)object;
        }
        throw JSONArray.wrongValueFormatException(index, "String", object, null);
    }

    public boolean isNull(int index) {
        return JSONObject.NULL.equals(this.opt(index));
    }

    public String join(String separator) throws JSONException {
        int len = this.length();
        if (len == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(JSONObject.valueToString(this.myArrayList.get(0)));
        for (int i = 1; i < len; ++i) {
            sb.append(separator).append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        return sb.toString();
    }

    public int length() {
        return this.myArrayList.size();
    }

    public void clear() {
        this.myArrayList.clear();
    }

    public Object opt(int index) {
        return index < 0 || index >= this.length() ? null : this.myArrayList.get(index);
    }

    public boolean optBoolean(int index) {
        return this.optBoolean(index, false);
    }

    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            return this.getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Boolean optBooleanObject(int index) {
        return this.optBooleanObject(index, false);
    }

    public Boolean optBooleanObject(int index, Boolean defaultValue) {
        try {
            return this.getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public double optDouble(int index) {
        return this.optDouble(index, Double.NaN);
    }

    public double optDouble(int index, double defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        double doubleValue = val2.doubleValue();
        return doubleValue;
    }

    public Double optDoubleObject(int index) {
        return this.optDoubleObject(index, Double.NaN);
    }

    public Double optDoubleObject(int index, Double defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        Double doubleValue = val2.doubleValue();
        return doubleValue;
    }

    public float optFloat(int index) {
        return this.optFloat(index, Float.NaN);
    }

    public float optFloat(int index, float defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        float floatValue = val2.floatValue();
        return floatValue;
    }

    public Float optFloatObject(int index) {
        return this.optFloatObject(index, Float.valueOf(Float.NaN));
    }

    public Float optFloatObject(int index, Float defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        Float floatValue = Float.valueOf(val2.floatValue());
        return floatValue;
    }

    public int optInt(int index) {
        return this.optInt(index, 0);
    }

    public int optInt(int index, int defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        return val2.intValue();
    }

    public Integer optIntegerObject(int index) {
        return this.optIntegerObject(index, 0);
    }

    public Integer optIntegerObject(int index, Integer defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        return val2.intValue();
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
        return this.optEnum(clazz, index, null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
        try {
            Object val2 = this.opt(index);
            if (JSONObject.NULL.equals(val2)) {
                return defaultValue;
            }
            if (clazz.isAssignableFrom(val2.getClass())) {
                Enum myE = (Enum)val2;
                return (E)myE;
            }
            return Enum.valueOf(clazz, val2.toString());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    public BigInteger optBigInteger(int index, BigInteger defaultValue) {
        Object val2 = this.opt(index);
        return JSONObject.objectToBigInteger(val2, defaultValue);
    }

    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
        Object val2 = this.opt(index);
        return JSONObject.objectToBigDecimal(val2, defaultValue);
    }

    public JSONArray optJSONArray(int index) {
        return this.optJSONArray(index, null);
    }

    public JSONArray optJSONArray(int index, JSONArray defaultValue) {
        Object object = this.opt(index);
        return object instanceof JSONArray ? (JSONArray)object : defaultValue;
    }

    public JSONObject optJSONObject(int index) {
        return this.optJSONObject(index, null);
    }

    public JSONObject optJSONObject(int index, JSONObject defaultValue) {
        Object object = this.opt(index);
        return object instanceof JSONObject ? (JSONObject)object : defaultValue;
    }

    public long optLong(int index) {
        return this.optLong(index, 0L);
    }

    public long optLong(int index, long defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        return val2.longValue();
    }

    public Long optLongObject(int index) {
        return this.optLongObject(index, 0L);
    }

    public Long optLongObject(int index, Long defaultValue) {
        Number val2 = this.optNumber(index, null);
        if (val2 == null) {
            return defaultValue;
        }
        return val2.longValue();
    }

    public Number optNumber(int index) {
        return this.optNumber(index, null);
    }

    public Number optNumber(int index, Number defaultValue) {
        Object val2 = this.opt(index);
        if (JSONObject.NULL.equals(val2)) {
            return defaultValue;
        }
        if (val2 instanceof Number) {
            return (Number)val2;
        }
        if (val2 instanceof String) {
            try {
                return NumberConversionUtil.stringToNumber((String)val2);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(int index) {
        return this.optString(index, "");
    }

    public String optString(int index, String defaultValue) {
        Object object = this.opt(index);
        return JSONObject.NULL.equals(object) ? defaultValue : object.toString();
    }

    public JSONArray put(boolean value) {
        return this.put(value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(Collection<?> value) {
        return this.put(new JSONArray(value));
    }

    public JSONArray put(double value) throws JSONException {
        return this.put((Object)value);
    }

    public JSONArray put(float value) throws JSONException {
        return this.put(Float.valueOf(value));
    }

    public JSONArray put(int value) {
        return this.put((Object)value);
    }

    public JSONArray put(long value) {
        return this.put((Object)value);
    }

    public JSONArray put(Map<?, ?> value) {
        return this.put(new JSONObject(value));
    }

    public JSONArray put(Object value) {
        JSONObject.testValidity(value);
        this.myArrayList.add(value);
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        return this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(int index, Collection<?> value) throws JSONException {
        return this.put(index, new JSONArray(value));
    }

    public JSONArray put(int index, double value) throws JSONException {
        return this.put(index, (Object)value);
    }

    public JSONArray put(int index, float value) throws JSONException {
        return this.put(index, Float.valueOf(value));
    }

    public JSONArray put(int index, int value) throws JSONException {
        return this.put(index, (Object)value);
    }

    public JSONArray put(int index, long value) throws JSONException {
        return this.put(index, (Object)value);
    }

    public JSONArray put(int index, Map<?, ?> value) throws JSONException {
        this.put(index, new JSONObject(value, new JSONParserConfiguration()));
        return this;
    }

    public JSONArray put(int index, Map<?, ?> value, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        this.put(index, new JSONObject(value, jsonParserConfiguration));
        return this;
    }

    public JSONArray put(int index, Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < this.length()) {
            JSONObject.testValidity(value);
            this.myArrayList.set(index, value);
            return this;
        }
        if (index == this.length()) {
            return this.put(value);
        }
        this.myArrayList.ensureCapacity(index + 1);
        while (index != this.length()) {
            this.myArrayList.add(JSONObject.NULL);
        }
        return this.put(value);
    }

    public JSONArray putAll(Collection<?> collection) {
        this.addAll(collection, false);
        return this;
    }

    public JSONArray putAll(Iterable<?> iter) {
        this.addAll(iter, false);
        return this;
    }

    public JSONArray putAll(JSONArray array) {
        this.myArrayList.addAll(array.myArrayList);
        return this;
    }

    public JSONArray putAll(Object array) throws JSONException {
        this.addAll(array, false);
        return this;
    }

    public Object query(String jsonPointer) {
        return this.query(new JSONPointer(jsonPointer));
    }

    public Object query(JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }

    public Object optQuery(String jsonPointer) {
        return this.optQuery(new JSONPointer(jsonPointer));
    }

    public Object optQuery(JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        } catch (JSONPointerException e) {
            return null;
        }
    }

    public Object remove(int index) {
        return index >= 0 && index < this.length() ? this.myArrayList.remove(index) : null;
    }

    public boolean similar(Object other) {
        if (!(other instanceof JSONArray)) {
            return false;
        }
        int len = this.length();
        if (len != ((JSONArray)other).length()) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            Object valueOther;
            Object valueThis = this.myArrayList.get(i);
            if (valueThis == (valueOther = ((JSONArray)other).myArrayList.get(i))) continue;
            if (valueThis == null) {
                return false;
            }
            if (!(valueThis instanceof JSONObject ? !((JSONObject)valueThis).similar(valueOther) : (valueThis instanceof JSONArray ? !((JSONArray)valueThis).similar(valueOther) : (valueThis instanceof Number && valueOther instanceof Number ? !JSONObject.isNumberSimilar((Number)valueThis, (Number)valueOther) : (valueThis instanceof JSONString && valueOther instanceof JSONString ? !((JSONString)valueThis).toJSONString().equals(((JSONString)valueOther).toJSONString()) : !valueThis.equals(valueOther)))))) continue;
            return false;
        }
        return true;
    }

    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        if (names == null || names.isEmpty() || this.isEmpty()) {
            return null;
        }
        JSONObject jo = new JSONObject(names.length());
        for (int i = 0; i < names.length(); ++i) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }

    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        StringWriter sw = new StringWriter();
        return this.write(sw, indentFactor, 0).toString();
    }

    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            boolean needsComma = false;
            int length = this.length();
            writer.write(91);
            if (length == 1) {
                try {
                    JSONObject.writeValue(writer, this.myArrayList.get(0), indentFactor, indent);
                } catch (Exception e) {
                    throw new JSONException("Unable to write JSONArray value at index: 0", e);
                }
            }
            if (length != 0) {
                int newIndent = indent + indentFactor;
                for (int i = 0; i < length; ++i) {
                    if (needsComma) {
                        writer.write(44);
                    }
                    if (indentFactor > 0) {
                        writer.write(10);
                    }
                    JSONObject.indent(writer, newIndent);
                    try {
                        JSONObject.writeValue(writer, this.myArrayList.get(i), indentFactor, newIndent);
                    } catch (Exception e) {
                        throw new JSONException("Unable to write JSONArray value at index: " + i, e);
                    }
                    needsComma = true;
                }
                if (indentFactor > 0) {
                    writer.write(10);
                }
                JSONObject.indent(writer, indent);
            }
            writer.write(93);
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    public List<Object> toList() {
        ArrayList<Object> results = new ArrayList<Object>(this.myArrayList.size());
        for (Object element : this.myArrayList) {
            if (element == null || JSONObject.NULL.equals(element)) {
                results.add(null);
                continue;
            }
            if (element instanceof JSONArray) {
                results.add(((JSONArray)element).toList());
                continue;
            }
            if (element instanceof JSONObject) {
                results.add(((JSONObject)element).toMap());
                continue;
            }
            results.add(element);
        }
        return results;
    }

    public boolean isEmpty() {
        return this.myArrayList.isEmpty();
    }

    private void addAll(Collection<?> collection, boolean wrap, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        this.myArrayList.ensureCapacity(this.myArrayList.size() + collection.size());
        if (wrap) {
            for (Object o : collection) {
                this.put(JSONObject.wrap(o, recursionDepth + 1, jsonParserConfiguration));
            }
        } else {
            for (Object o : collection) {
                this.put(o);
            }
        }
    }

    private void addAll(Iterable<?> iter, boolean wrap) {
        if (wrap) {
            for (Object o : iter) {
                this.put(JSONObject.wrap(o));
            }
        } else {
            for (Object o : iter) {
                this.put(o);
            }
        }
    }

    private void addAll(Object array, boolean wrap) throws JSONException {
        this.addAll(array, wrap, 0);
    }

    private void addAll(Object array, boolean wrap, int recursionDepth) {
        this.addAll(array, wrap, recursionDepth, new JSONParserConfiguration());
    }

    private void addAll(Object array, boolean wrap, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            this.myArrayList.ensureCapacity(this.myArrayList.size() + length);
            if (wrap) {
                for (int i = 0; i < length; ++i) {
                    this.put(JSONObject.wrap(Array.get(array, i), recursionDepth + 1, jsonParserConfiguration));
                }
            } else {
                for (int i = 0; i < length; ++i) {
                    this.put(Array.get(array, i));
                }
            }
        } else if (array instanceof JSONArray) {
            this.myArrayList.addAll(((JSONArray)array).myArrayList);
        } else if (array instanceof Collection) {
            this.addAll((Collection)array, wrap, recursionDepth);
        } else if (array instanceof Iterable) {
            this.addAll((Iterable)array, wrap);
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    private static JSONException wrongValueFormatException(int idx, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (null).", cause);
        }
        if (value instanceof Map || value instanceof Iterable || value instanceof JSONObject) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
        return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause);
    }
}

