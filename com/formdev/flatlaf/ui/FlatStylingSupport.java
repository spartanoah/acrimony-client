/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.beans.PropertyChangeListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class FlatStylingSupport {
    public static Object getStyle(JComponent c) {
        return c.getClientProperty("FlatLaf.style");
    }

    public static Object getStyleClass(JComponent c) {
        return c.getClientProperty("FlatLaf.styleClass");
    }

    static boolean hasStyleProperty(JComponent c) {
        return FlatStylingSupport.getStyle(c) != null || FlatStylingSupport.getStyleClass(c) != null;
    }

    public static Object getResolvedStyle(JComponent c, String type) throws IllegalArgumentException {
        Object style = FlatStylingSupport.getStyle(c);
        Object styleClass = FlatStylingSupport.getStyleClass(c);
        Object styleForClasses = FlatStylingSupport.getStyleForClasses(styleClass, type);
        return FlatStylingSupport.joinStyles(styleForClasses, style);
    }

    public static Object getStyleForClasses(Object styleClass, String type) throws IllegalArgumentException {
        if (styleClass == null) {
            return null;
        }
        if (styleClass instanceof String && ((String)((Object)styleClass)).indexOf(32) >= 0) {
            styleClass = StringUtils.split((String)((Object)styleClass), ' ', true, true);
        }
        if (styleClass instanceof String) {
            return FlatStylingSupport.getStyleForClass(((String)((Object)styleClass)).trim(), type);
        }
        if (styleClass instanceof String[]) {
            Object style = null;
            for (String cls : (String[])styleClass) {
                style = FlatStylingSupport.joinStyles(style, FlatStylingSupport.getStyleForClass(cls, type));
            }
            return style;
        }
        if (styleClass instanceof List) {
            Object style = null;
            for (String cls : styleClass) {
                style = FlatStylingSupport.joinStyles(style, FlatStylingSupport.getStyleForClass(cls, type));
            }
            return style;
        }
        return null;
    }

    private static Object getStyleForClass(String styleClass, String type) throws IllegalArgumentException {
        return FlatStylingSupport.joinStyles(UIManager.get("[style]." + styleClass), UIManager.get("[style]" + type + '.' + styleClass));
    }

    public static Object joinStyles(Object style1, Object style2) throws IllegalArgumentException {
        Map<String, Object> map2;
        Map<String, Object> map1;
        if (style1 == null) {
            return style2;
        }
        if (style2 == null) {
            return style1;
        }
        if (style1 instanceof String && style2 instanceof String) {
            return style1 + "; " + style2;
        }
        Map<String, Object> map = map1 = style1 instanceof String ? FlatStylingSupport.parse((String)style1) : (Map<String, Object>)style1;
        if (map1 == null) {
            return style2;
        }
        Map<String, Object> map3 = map2 = style2 instanceof String ? FlatStylingSupport.parse((String)style2) : (Map<String, Object>)style2;
        if (map2 == null) {
            return style1;
        }
        HashMap<String, Object> map4 = new HashMap<String, Object>(map1);
        map4.putAll(map2);
        return map4;
    }

    public static String concatStyles(String style1, String style2) {
        if (style1 == null) {
            return style2;
        }
        if (style2 == null) {
            return style1;
        }
        return style1 + "; " + style2;
    }

    public static Map<String, Object> parseAndApply(Map<String, Object> oldStyleValues, Object style, BiFunction<String, Object, Object> applyProperty) throws UnknownStyleException, IllegalArgumentException {
        if (oldStyleValues != null) {
            for (Map.Entry<String, Object> e : oldStyleValues.entrySet()) {
                applyProperty.apply(e.getKey(), e.getValue());
            }
        }
        if (style == null) {
            return null;
        }
        if (style instanceof String) {
            String str = (String)style;
            if (StringUtils.isTrimmedEmpty(str)) {
                return null;
            }
            return FlatStylingSupport.applyStyle(FlatStylingSupport.parse(str), applyProperty);
        }
        if (style instanceof Map) {
            Map map = (Map)style;
            return FlatStylingSupport.applyStyle(map, applyProperty);
        }
        return null;
    }

    private static Map<String, Object> applyStyle(Map<String, Object> style, BiFunction<String, Object, Object> applyProperty) {
        if (style.isEmpty()) {
            return null;
        }
        HashMap<String, Object> oldValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> e : style.entrySet()) {
            String key = e.getKey();
            Object newValue = e.getValue();
            if (key.startsWith("[")) {
                if (!(SystemInfo.isWindows && key.startsWith("[win]") || SystemInfo.isMacOS && key.startsWith("[mac]") || SystemInfo.isLinux && key.startsWith("[linux]") || key.startsWith("[light]") && !FlatLaf.isLafDark()) && (!key.startsWith("[dark]") || !FlatLaf.isLafDark())) continue;
                key = key.substring(key.indexOf(93) + 1);
            }
            Object oldValue = applyProperty.apply(key, newValue);
            oldValues.put(key, oldValue);
        }
        return oldValues;
    }

    public static Map<String, Object> parse(String style) throws IllegalArgumentException {
        if (style == null || StringUtils.isTrimmedEmpty(style)) {
            return null;
        }
        LinkedHashMap<String, Object> map = null;
        for (String part : StringUtils.split(style, ';', true, true)) {
            int sepIndex = part.indexOf(58);
            if (sepIndex < 0) {
                throw new IllegalArgumentException("missing colon in '" + part + "'");
            }
            String key = StringUtils.substringTrimmed(part, 0, sepIndex);
            String value = StringUtils.substringTrimmed(part, sepIndex + 1);
            if (key.isEmpty()) {
                throw new IllegalArgumentException("missing key in '" + part + "'");
            }
            if (value.isEmpty()) {
                throw new IllegalArgumentException("missing value in '" + part + "'");
            }
            if (map == null) {
                map = new LinkedHashMap<String, Object>();
            }
            map.put(key, FlatStylingSupport.parseValue(key, value));
        }
        return map;
    }

    private static Object parseValue(String key, String value) throws IllegalArgumentException {
        if (value.startsWith("$")) {
            return UIManager.get(value.substring(1));
        }
        if (key.startsWith("[")) {
            key = key.substring(key.indexOf(93) + 1);
        }
        return FlatLaf.parseDefaultsValue(key, value, null);
    }

    public static Object applyToAnnotatedObject(Object obj, String key, Object value) throws UnknownStyleException, IllegalArgumentException {
        String fieldName = FlatStylingSupport.keyToFieldName(key);
        return FlatStylingSupport.applyToField(obj, fieldName, key, value, field -> {
            Styleable styleable = field.getAnnotation(Styleable.class);
            return styleable != null && styleable.dot() == (fieldName != key);
        });
    }

    private static String keyToFieldName(String key) {
        int dotIndex = key.indexOf(46);
        if (dotIndex < 0) {
            return key;
        }
        return key.substring(0, dotIndex) + Character.toUpperCase(key.charAt(dotIndex + 1)) + key.substring(dotIndex + 2);
    }

    static Object applyToField(Object obj, String fieldName, String key, Object value) throws UnknownStyleException, IllegalArgumentException {
        return FlatStylingSupport.applyToField(obj, fieldName, key, value, null);
    }

    private static Object applyToField(Object obj, String fieldName, String key, Object value, Predicate<Field> predicate) throws UnknownStyleException, IllegalArgumentException {
        String superclassName;
        Class<?> cls = obj.getClass();
        do {
            try {
                StyleableField[] f = cls.getDeclaredField(fieldName);
                if (predicate == null || predicate.test((Field)f)) {
                    return FlatStylingSupport.applyToField((Field)f, obj, value, false);
                }
            } catch (NoSuchFieldException f) {
                // empty catch block
            }
            for (StyleableField styleableField : (StyleableField[])cls.getAnnotationsByType(StyleableField.class)) {
                if (!key.equals(styleableField.key())) continue;
                return FlatStylingSupport.applyToField(FlatStylingSupport.getStyleableField(styleableField), obj, value, true);
            }
            if ((cls = cls.getSuperclass()) != null) continue;
            throw new UnknownStyleException(key);
        } while (predicate == null || !(superclassName = cls.getName()).startsWith("java.") && !superclassName.startsWith("javax."));
        throw new UnknownStyleException(key);
    }

    private static Object applyToField(Field f, Object obj, Object value, boolean useMethodHandles) throws IllegalArgumentException {
        FlatStylingSupport.checkValidField(f);
        if (useMethodHandles && obj instanceof StyleableLookupProvider) {
            try {
                MethodHandles.Lookup lookup = ((StyleableLookupProvider)obj).getLookupForStyling();
                Object oldValue = lookup.unreflectGetter(f).invoke(obj);
                lookup.unreflectSetter(f).invoke(obj, FlatStylingSupport.convertToEnum(value, f.getType()));
                return oldValue;
            } catch (Throwable ex) {
                throw FlatStylingSupport.newFieldAccessFailed(f, ex);
            }
        }
        try {
            f.setAccessible(true);
            Object oldValue = f.get(obj);
            f.set(obj, FlatStylingSupport.convertToEnum(value, f.getType()));
            return oldValue;
        } catch (IllegalAccessException ex) {
            throw FlatStylingSupport.newFieldAccessFailed(f, ex);
        }
    }

    private static Object getFieldValue(Field f, Object obj, boolean useMethodHandles) throws IllegalArgumentException {
        FlatStylingSupport.checkValidField(f);
        if (useMethodHandles && obj instanceof StyleableLookupProvider) {
            try {
                MethodHandles.Lookup lookup = ((StyleableLookupProvider)obj).getLookupForStyling();
                return lookup.unreflectGetter(f).invoke(obj);
            } catch (Throwable ex) {
                throw FlatStylingSupport.newFieldAccessFailed(f, ex);
            }
        }
        try {
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException ex) {
            throw FlatStylingSupport.newFieldAccessFailed(f, ex);
        }
    }

    private static IllegalArgumentException newFieldAccessFailed(Field f, Throwable ex) {
        return new IllegalArgumentException("failed to access field '" + f.getDeclaringClass().getName() + "." + f.getName() + "'", ex);
    }

    private static void checkValidField(Field f) throws IllegalArgumentException {
        if (!FlatStylingSupport.isValidField(f)) {
            throw new IllegalArgumentException("field '" + f.getDeclaringClass().getName() + "." + f.getName() + "' is final or static");
        }
    }

    private static boolean isValidField(Field f) {
        int modifiers = f.getModifiers();
        return (modifiers & 0x18) == 0 && !f.isSynthetic();
    }

    private static Field getStyleableField(StyleableField styleableField) throws IllegalArgumentException {
        String fieldName = styleableField.fieldName();
        if (fieldName.isEmpty()) {
            fieldName = styleableField.key();
        }
        try {
            return styleableField.cls().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            throw new IllegalArgumentException("field '" + styleableField.cls().getName() + "." + fieldName + "' not found", ex);
        }
    }

    private static Object applyToProperty(Object obj, String name, Object value) throws UnknownStyleException, IllegalArgumentException {
        Class<?> cls = obj.getClass();
        String getterName = FlatStylingSupport.buildMethodName("get", name);
        String setterName = FlatStylingSupport.buildMethodName("set", name);
        try {
            Method getter;
            try {
                getter = cls.getMethod(getterName, new Class[0]);
            } catch (NoSuchMethodException ex) {
                getter = cls.getMethod(FlatStylingSupport.buildMethodName("is", name), new Class[0]);
            }
            Method setter = cls.getMethod(setterName, getter.getReturnType());
            Object oldValue = getter.invoke(obj, new Object[0]);
            setter.invoke(obj, FlatStylingSupport.convertToEnum(value, getter.getReturnType()));
            return oldValue;
        } catch (NoSuchMethodException ex) {
            throw new UnknownStyleException(name);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to invoke property methods '" + cls.getName() + "." + getterName + "()' or '" + setterName + "(...)'", ex);
        }
    }

    private static String buildMethodName(String prefix, String name) {
        int prefixLength = prefix.length();
        int nameLength = name.length();
        char[] chars = new char[prefixLength + nameLength];
        prefix.getChars(0, prefixLength, chars, 0);
        name.getChars(0, nameLength, chars, prefixLength);
        chars[prefixLength] = Character.toUpperCase(chars[prefixLength]);
        return new String(chars);
    }

    private static Object convertToEnum(Object value, Class<?> type) throws IllegalArgumentException {
        if (Enum.class.isAssignableFrom(type) && value instanceof String) {
            try {
                value = Enum.valueOf(type, (String)value);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("unknown enum value '" + value + "' in enum '" + type.getName() + "'", ex);
            }
        }
        return value;
    }

    public static Object applyToAnnotatedObjectOrComponent(Object obj, Object comp, String key, Object value) throws UnknownStyleException, IllegalArgumentException {
        try {
            return FlatStylingSupport.applyToAnnotatedObject(obj, key, value);
        } catch (UnknownStyleException ex) {
            try {
                if (comp != null) {
                    return FlatStylingSupport.applyToProperty(comp, key, value);
                }
            } catch (UnknownStyleException unknownStyleException) {
                // empty catch block
            }
            throw ex;
        }
    }

    static Object applyToAnnotatedObjectOrBorder(Object obj, String key, Object value, JComponent c, AtomicBoolean borderShared) throws IllegalArgumentException {
        try {
            return FlatStylingSupport.applyToAnnotatedObject(obj, key, value);
        } catch (UnknownStyleException ex) {
            Border border = c.getBorder();
            if (border instanceof StyleableBorder) {
                if (borderShared.get()) {
                    border = FlatStylingSupport.cloneBorder(border);
                    c.setBorder(border);
                    borderShared.set(false);
                }
                try {
                    return ((StyleableBorder)((Object)border)).applyStyleProperty(key, value);
                } catch (UnknownStyleException unknownStyleException) {
                    // empty catch block
                }
            }
            try {
                return FlatStylingSupport.applyToProperty(c, key, value);
            } catch (UnknownStyleException unknownStyleException) {
                throw ex;
            }
        }
    }

    static PropertyChangeListener createPropertyChangeListener(JComponent c, Runnable installStyle, PropertyChangeListener superListener) {
        return e -> {
            if (superListener != null) {
                superListener.propertyChange(e);
            }
            switch (e.getPropertyName()) {
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    installStyle.run();
                    c.revalidate();
                    c.repaint();
                }
            }
        };
    }

    static Border cloneBorder(Border border) throws IllegalArgumentException {
        Class<?> borderClass = border.getClass();
        try {
            return (Border)borderClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to clone border '" + borderClass.getName() + "'", ex);
        }
    }

    static Icon cloneIcon(Icon icon) throws IllegalArgumentException {
        Class<?> iconClass = icon.getClass();
        try {
            return (Icon)iconClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to clone icon '" + iconClass.getName() + "'", ex);
        }
    }

    public static Map<String, Class<?>> getAnnotatedStyleableInfos(Object obj) throws IllegalArgumentException {
        return FlatStylingSupport.getAnnotatedStyleableInfos(obj, null);
    }

    public static Map<String, Class<?>> getAnnotatedStyleableInfos(Object obj, Border border) throws IllegalArgumentException {
        StyleableInfosMap infos = new StyleableInfosMap();
        FlatStylingSupport.collectAnnotatedStyleableInfos(obj, infos);
        FlatStylingSupport.collectStyleableInfos(border, infos);
        return infos;
    }

    public static void collectAnnotatedStyleableInfos(Object obj, Map<String, Class<?>> infos) throws IllegalArgumentException {
        String superclassName;
        HashSet<String> processedFields = new HashSet<String>();
        Class<?> cls = obj.getClass();
        do {
            for (Field f : cls.getDeclaredFields()) {
                Styleable styleable;
                if (!FlatStylingSupport.isValidField(f) || (styleable = f.getAnnotation(Styleable.class)) == null) continue;
                String name = f.getName();
                Class<?> type = f.getType();
                if (processedFields.contains(name)) continue;
                processedFields.add(name);
                if (styleable.dot()) {
                    int len = name.length();
                    for (int i = 0; i < len; ++i) {
                        if (!Character.isUpperCase(name.charAt(i))) continue;
                        name = name.substring(0, i) + '.' + Character.toLowerCase(name.charAt(i)) + name.substring(i + 1);
                        break;
                    }
                }
                if (styleable.type() != Void.class) {
                    type = styleable.type();
                }
                infos.put(name, type);
            }
            for (StyleableField styleableField : (StyleableField[])cls.getAnnotationsByType(StyleableField.class)) {
                String name = styleableField.key();
                if (processedFields.contains(name)) continue;
                processedFields.add(name);
                Field f = FlatStylingSupport.getStyleableField(styleableField);
                infos.put(name, f.getType());
            }
            if ((cls = cls.getSuperclass()) != null) continue;
            return;
        } while (!(superclassName = cls.getName()).startsWith("java.") && !superclassName.startsWith("javax."));
    }

    public static void collectStyleableInfos(Border border, Map<String, Class<?>> infos) {
        if (border instanceof StyleableBorder) {
            infos.putAll(((StyleableBorder)((Object)border)).getStyleableInfos());
        }
    }

    public static void putAllPrefixKey(Map<String, Class<?>> infos, String keyPrefix, Map<String, Class<?>> infos2) {
        for (Map.Entry<String, Class<?>> e : infos2.entrySet()) {
            infos.put(keyPrefix.concat(e.getKey()), e.getValue());
        }
    }

    public static Object getAnnotatedStyleableValue(Object obj, String key) throws IllegalArgumentException {
        String superclassName;
        String fieldName = FlatStylingSupport.keyToFieldName(key);
        Class<?> cls = obj.getClass();
        do {
            try {
                StyleableField[] f = cls.getDeclaredField(fieldName);
                Styleable styleable = f.getAnnotation(Styleable.class);
                if (styleable != null) {
                    if (styleable.dot() != (fieldName != key)) {
                        throw new IllegalArgumentException("'Styleable.dot' on field '" + fieldName + "' does not match key '" + key + "'");
                    }
                    if (styleable.type() != Void.class) {
                        throw new IllegalArgumentException("'Styleable.type' on field '" + fieldName + "' not supported");
                    }
                    return FlatStylingSupport.getFieldValue((Field)f, obj, false);
                }
            } catch (NoSuchFieldException f) {
                // empty catch block
            }
            for (StyleableField styleableField : (StyleableField[])cls.getAnnotationsByType(StyleableField.class)) {
                if (!key.equals(styleableField.key())) continue;
                return FlatStylingSupport.getFieldValue(FlatStylingSupport.getStyleableField(styleableField), obj, true);
            }
            if ((cls = cls.getSuperclass()) != null) continue;
            return null;
        } while (!(superclassName = cls.getName()).startsWith("java.") && !superclassName.startsWith("javax."));
        return null;
    }

    public static Object getAnnotatedStyleableValue(Object obj, Border border, String key) {
        Object value;
        if (border instanceof StyleableBorder && (value = ((StyleableBorder)((Object)border)).getStyleableValue(key)) != null) {
            return value;
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(obj, key);
    }

    static class StyleableInfosMap<K, V>
    extends LinkedHashMap<K, V> {
        StyleableInfosMap() {
        }

        @Override
        public V put(K key, V value) throws IllegalArgumentException {
            V oldValue = super.put(key, value);
            if (oldValue != null) {
                throw new IllegalArgumentException("duplicate key '" + key + "'");
            }
            return oldValue;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Map.Entry<K, V> e : m.entrySet()) {
                this.put(e.getKey(), e.getValue());
            }
        }
    }

    public static class UnknownStyleException
    extends IllegalArgumentException {
        public UnknownStyleException(String key) {
            super(key);
        }

        @Override
        public String getMessage() {
            return "unknown style '" + super.getMessage() + "'";
        }
    }

    public static interface StyleableLookupProvider {
        public MethodHandles.Lookup getLookupForStyling();
    }

    public static interface StyleableBorder {
        public Object applyStyleProperty(String var1, Object var2);

        public Map<String, Class<?>> getStyleableInfos() throws IllegalArgumentException;

        public Object getStyleableValue(String var1) throws IllegalArgumentException;
    }

    public static interface StyleableUI {
        public Map<String, Class<?>> getStyleableInfos(JComponent var1) throws IllegalArgumentException;

        public Object getStyleableValue(JComponent var1, String var2) throws IllegalArgumentException;
    }

    @Target(value={ElementType.TYPE})
    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface StyleableFields {
        public StyleableField[] value();
    }

    @Target(value={ElementType.TYPE})
    @Retention(value=RetentionPolicy.RUNTIME)
    @Repeatable(value=StyleableFields.class)
    public static @interface StyleableField {
        public Class<?> cls();

        public String key();

        public String fieldName() default "";
    }

    @Target(value={ElementType.FIELD})
    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface Styleable {
        public boolean dot() default false;

        public Class<?> type() default Void.class;
    }
}

