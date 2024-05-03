/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.StrBuilder;

public class ObjectUtils {
    public static final Null NULL = new Null();

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    public static <T> T firstNonNull(T ... values) {
        if (values != null) {
            for (T val2 : values) {
                if (val2 == null) continue;
                return val2;
            }
        }
        return null;
    }

    @Deprecated
    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        return object1.equals(object2);
    }

    public static boolean notEqual(Object object1, Object object2) {
        return !ObjectUtils.equals(object1, object2);
    }

    @Deprecated
    public static int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    @Deprecated
    public static int hashCodeMulti(Object ... objects) {
        int hash = 1;
        if (objects != null) {
            for (Object object : objects) {
                int tmpHash = ObjectUtils.hashCode(object);
                hash = hash * 31 + tmpHash;
            }
        }
        return hash;
    }

    public static String identityToString(Object object) {
        if (object == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        ObjectUtils.identityToString(builder, object);
        return builder.toString();
    }

    public static void identityToString(Appendable appendable, Object object) throws IOException {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        appendable.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StrBuilder builder, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        builder.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StringBuffer buffer, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StringBuilder builder, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        builder.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    @Deprecated
    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    @Deprecated
    public static String toString(Object obj, String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    public static <T extends Comparable<? super T>> T min(T ... values) {
        T result = null;
        if (values != null) {
            for (T value : values) {
                if (ObjectUtils.compare(value, result, true) >= 0) continue;
                result = value;
            }
        }
        return result;
    }

    public static <T extends Comparable<? super T>> T max(T ... values) {
        T result = null;
        if (values != null) {
            for (T value : values) {
                if (ObjectUtils.compare(value, result, false) <= 0) continue;
                result = value;
            }
        }
        return result;
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return ObjectUtils.compare(c1, c2, false);
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        }
        if (c1 == null) {
            return nullGreater ? 1 : -1;
        }
        if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    public static <T extends Comparable<? super T>> T median(T ... items) {
        Validate.notEmpty(items);
        Validate.noNullElements(items);
        TreeSet sort = new TreeSet();
        Collections.addAll(sort, items);
        Comparable result = (Comparable)sort.toArray()[(sort.size() - 1) / 2];
        return (T)result;
    }

    public static <T> T median(Comparator<T> comparator, T ... items) {
        Validate.notEmpty(items, "null/empty items", new Object[0]);
        Validate.noNullElements(items);
        Validate.notNull(comparator, "null comparator", new Object[0]);
        TreeSet<T> sort = new TreeSet<T>(comparator);
        Collections.addAll(sort, items);
        Object result = sort.toArray()[(sort.size() - 1) / 2];
        return (T)result;
    }

    public static <T> T mode(T ... items) {
        if (ArrayUtils.isNotEmpty(items)) {
            HashMap<T, MutableInt> occurrences = new HashMap<T, MutableInt>(items.length);
            for (T t : items) {
                MutableInt count = (MutableInt)occurrences.get(t);
                if (count == null) {
                    occurrences.put(t, new MutableInt(1));
                    continue;
                }
                count.increment();
            }
            Object result = null;
            int max = 0;
            for (Map.Entry e : occurrences.entrySet()) {
                int cmp = ((MutableInt)e.getValue()).intValue();
                if (cmp == max) {
                    result = null;
                    continue;
                }
                if (cmp <= max) continue;
                max = cmp;
                result = e.getKey();
            }
            return result;
        }
        return null;
    }

    public static <T> T clone(T obj) {
        if (obj instanceof Cloneable) {
            Object result;
            if (obj.getClass().isArray()) {
                Class<?> componentType = obj.getClass().getComponentType();
                if (!componentType.isPrimitive()) {
                    result = ((Object[])obj).clone();
                } else {
                    int length = Array.getLength(obj);
                    result = Array.newInstance(componentType, length);
                    while (length-- > 0) {
                        Array.set(result, length, Array.get(obj, length));
                    }
                }
            } else {
                try {
                    Method clone = obj.getClass().getMethod("clone", new Class[0]);
                    result = clone.invoke(obj, new Object[0]);
                } catch (NoSuchMethodException e) {
                    throw new CloneFailedException("Cloneable type " + obj.getClass().getName() + " has no clone method", e);
                } catch (IllegalAccessException e) {
                    throw new CloneFailedException("Cannot clone Cloneable type " + obj.getClass().getName(), e);
                } catch (InvocationTargetException e) {
                    throw new CloneFailedException("Exception cloning Cloneable type " + obj.getClass().getName(), e.getCause());
                }
            }
            Object checked = result;
            return (T)checked;
        }
        return null;
    }

    public static <T> T cloneIfPossible(T obj) {
        T clone = ObjectUtils.clone(obj);
        return clone == null ? obj : clone;
    }

    public static boolean CONST(boolean v) {
        return v;
    }

    public static byte CONST(byte v) {
        return v;
    }

    public static byte CONST_BYTE(int v) throws IllegalArgumentException {
        if (v < -128 || v > 127) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -128 and 127: [" + v + "]");
        }
        return (byte)v;
    }

    public static char CONST(char v) {
        return v;
    }

    public static short CONST(short v) {
        return v;
    }

    public static short CONST_SHORT(int v) throws IllegalArgumentException {
        if (v < Short.MIN_VALUE || v > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -32768 and 32767: [" + v + "]");
        }
        return (short)v;
    }

    public static int CONST(int v) {
        return v;
    }

    public static long CONST(long v) {
        return v;
    }

    public static float CONST(float v) {
        return v;
    }

    public static double CONST(double v) {
        return v;
    }

    public static <T> T CONST(T v) {
        return v;
    }

    public static class Null
    implements Serializable {
        private static final long serialVersionUID = 7092611880189329093L;

        Null() {
        }

        private Object readResolve() {
            return NULL;
        }
    }
}

