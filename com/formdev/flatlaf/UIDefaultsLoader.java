/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf;

import com.formdev.flatlaf.FlatDefaultsAddon;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.DerivedColor;
import com.formdev.flatlaf.util.GrayFilter;
import com.formdev.flatlaf.util.HSLColor;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SoftCache;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;

class UIDefaultsLoader {
    private static final String TYPE_PREFIX = "{";
    private static final String TYPE_PREFIX_END = "}";
    private static final String VARIABLE_PREFIX = "@";
    private static final String PROPERTY_PREFIX = "$";
    private static final String OPTIONAL_PREFIX = "?";
    private static final String WILDCARD_PREFIX = "*.";
    static final String KEY_VARIABLES = "FlatLaf.internal.variables";
    private static int parseColorDepth;
    private static Map<String, ColorUIResource> systemColorCache;
    private static final SoftCache<String, Object> fontCache;
    private static final ValueType[] tempResultValueType;
    private static Map<Class<?>, ValueType> javaValueTypes;
    private static Map<String, ValueType> knownValueTypes;

    UIDefaultsLoader() {
    }

    static void loadDefaultsFromProperties(Class<?> lookAndFeelClass, List<FlatDefaultsAddon> addons, Properties additionalDefaults, boolean dark, UIDefaults defaults) {
        ArrayList lafClasses = new ArrayList();
        Class<?> lafClass = lookAndFeelClass;
        while (FlatLaf.class.isAssignableFrom(lafClass)) {
            lafClasses.add(0, lafClass);
            lafClass = lafClass.getSuperclass();
        }
        UIDefaultsLoader.loadDefaultsFromProperties(lafClasses, addons, additionalDefaults, dark, defaults);
    }

    /*
     * WARNING - void declaration
     */
    static void loadDefaultsFromProperties(List<Class<?>> lafClasses, List<FlatDefaultsAddon> addons, Properties additionalDefaults, boolean dark, UIDefaults defaults) {
        try {
            systemColorCache = FlatLaf.getSystemColorGetter() != null ? new HashMap() : null;
            Properties properties = new Properties();
            for (Class<?> clazz : lafClasses) {
                String propertiesName = '/' + clazz.getName().replace('.', '/') + ".properties";
                InputStream in = clazz.getResourceAsStream(propertiesName);
                try {
                    if (in == null) continue;
                    properties.load(in);
                } finally {
                    if (in == null) continue;
                    in.close();
                }
            }
            for (FlatDefaultsAddon flatDefaultsAddon : addons) {
                for (Class<?> lafClass : lafClasses) {
                    InputStream in = flatDefaultsAddon.getDefaults(lafClass);
                    try {
                        if (in == null) continue;
                        properties.load(in);
                    } finally {
                        if (in == null) continue;
                        in.close();
                    }
                }
            }
            ArrayList<ClassLoader> addonClassLoaders = new ArrayList<ClassLoader>();
            for (FlatDefaultsAddon addon : addons) {
                ClassLoader addonClassLoader = addon.getClass().getClassLoader();
                if (addonClassLoaders.contains(addonClassLoader)) continue;
                addonClassLoaders.add(addonClassLoader);
            }
            List<Object> list = FlatLaf.getCustomDefaultsSources();
            int size = list != null ? list.size() : 0;
            for (int i = 0; i < size; ++i) {
                Iterator source = list.get(i);
                if (source instanceof String && i + 1 < size) {
                    ClassLoader classLoader;
                    String packageName = (String)((Object)source);
                    if ((classLoader = (ClassLoader)list.get(++i)) != null && !addonClassLoaders.contains(classLoader)) {
                        addonClassLoaders.add(classLoader);
                    }
                    packageName = packageName.replace('.', '/');
                    if (classLoader == null) {
                        ClassLoader classLoader2 = FlatLaf.class.getClassLoader();
                    }
                    for (Class<?> lafClass2 : lafClasses) {
                        void var12_27;
                        String propertiesName = packageName + '/' + lafClass2.getSimpleName() + ".properties";
                        InputStream in2 = var12_27.getResourceAsStream(propertiesName);
                        try {
                            if (in2 == null) continue;
                            properties.load(in2);
                        } finally {
                            if (in2 == null) continue;
                            in2.close();
                        }
                    }
                    continue;
                }
                if (source instanceof URL) {
                    URL packageUrl = (URL)((Object)source);
                    for (Class<?> lafClass : lafClasses) {
                        URL propertiesUrl = new URL(packageUrl + lafClass.getSimpleName() + ".properties");
                        try {
                            InputStream in3 = propertiesUrl.openStream();
                            try {
                                properties.load(in3);
                            } finally {
                                if (in3 == null) continue;
                                in3.close();
                            }
                        } catch (FileNotFoundException in3) {}
                    }
                    continue;
                }
                if (!(source instanceof File)) continue;
                File folder = (File)((Object)source);
                for (Class<?> lafClass : lafClasses) {
                    File propertiesFile = new File(folder, lafClass.getSimpleName() + ".properties");
                    if (!propertiesFile.isFile()) continue;
                    try (FileInputStream in = new FileInputStream(propertiesFile);){
                        properties.load(in);
                    }
                }
            }
            if (additionalDefaults != null) {
                properties.putAll(additionalDefaults);
            }
            ArrayList<String> platformSpecificKeys = new ArrayList<String>();
            for (Object okey : properties.keySet()) {
                String string = (String)okey;
                if (!string.startsWith("[") || !string.startsWith("[win]") && !string.startsWith("[mac]") && !string.startsWith("[linux]") && !string.startsWith("[light]") && !string.startsWith("[dark]")) continue;
                platformSpecificKeys.add(string);
            }
            if (!platformSpecificKeys.isEmpty()) {
                String lightOrDarkPrefix = dark ? "[dark]" : "[light]";
                for (String string : platformSpecificKeys) {
                    if (!string.startsWith(lightOrDarkPrefix)) continue;
                    properties.put(string.substring(lightOrDarkPrefix.length()), properties.remove(string));
                }
                String platformPrefix = SystemInfo.isWindows ? "[win]" : (SystemInfo.isMacOS ? "[mac]" : (SystemInfo.isLinux ? "[linux]" : "[unknown]"));
                for (Object key2 : platformSpecificKeys) {
                    Object value2 = properties.remove(key2);
                    if (!((String)key2).startsWith(platformPrefix)) continue;
                    properties.put(((String)key2).substring(platformPrefix.length()), value2);
                }
            }
            HashMap<String, String> wildcards = new HashMap<String, String>();
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                String key3 = (String)entry.getKey();
                if (!key3.startsWith(WILDCARD_PREFIX)) continue;
                wildcards.put(key3.substring(WILDCARD_PREFIX.length()), (String)entry.getValue());
                it.remove();
            }
            for (Object key2 : defaults.keySet()) {
                String wildcardKey;
                String wildcardValue;
                int dot;
                if (!(key2 instanceof String) || properties.containsKey(key2) || (dot = ((String)key2).lastIndexOf(46)) < 0 || (wildcardValue = (String)wildcards.get(wildcardKey = ((String)key2).substring(dot + 1))) == null) continue;
                properties.put(key2, wildcardValue);
            }
            Function<String, String> function = key -> properties.getProperty((String)key);
            Function<String, String> resolver = value -> UIDefaultsLoader.resolveValue(value, propertiesGetter);
            HashMap<String, String> variables = new HashMap<String, String>(50);
            for (Map.Entry e : properties.entrySet()) {
                String key4 = (String)e.getKey();
                if (key4.startsWith(VARIABLE_PREFIX)) {
                    variables.put(key4, (String)e.getValue());
                    continue;
                }
                String value3 = (String)e.getValue();
                try {
                    value3 = UIDefaultsLoader.resolveValue(value3, function);
                    defaults.put(key4, UIDefaultsLoader.parseValue(key4, value3, null, null, resolver, addonClassLoaders));
                } catch (RuntimeException ex) {
                    UIDefaultsLoader.logParseError(key4, value3, ex, true);
                }
            }
            defaults.put(KEY_VARIABLES, variables);
            systemColorCache = null;
        } catch (IOException ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to load properties files.", ex);
        }
    }

    static void logParseError(String key, String value, RuntimeException ex, boolean severe) {
        String message = "FlatLaf: Failed to parse: '" + key + '=' + value + '\'';
        if (severe) {
            LoggingFacade.INSTANCE.logSevere(message, ex);
        } else {
            LoggingFacade.INSTANCE.logConfig(message, ex);
        }
    }

    static String resolveValue(String value, Function<String, String> propertiesGetter) throws IllegalArgumentException {
        String newValue;
        String value0 = value = value.trim();
        if (value.startsWith(PROPERTY_PREFIX)) {
            value = value.substring(PROPERTY_PREFIX.length());
        } else if (!value.startsWith(VARIABLE_PREFIX)) {
            return value;
        }
        boolean optional = false;
        if (value.startsWith(OPTIONAL_PREFIX)) {
            value = value.substring(OPTIONAL_PREFIX.length());
            optional = true;
        }
        if ((newValue = propertiesGetter.apply(value)) == null) {
            if (optional) {
                return "null";
            }
            throw new IllegalArgumentException("variable or property '" + value + "' not found");
        }
        if (newValue.equals(value0)) {
            throw new IllegalArgumentException("endless recursion in variable or property '" + value + "'");
        }
        return UIDefaultsLoader.resolveValue(newValue, propertiesGetter);
    }

    static String resolveValueFromUIManager(String value) throws IllegalArgumentException {
        if (value.startsWith(VARIABLE_PREFIX)) {
            String newValue;
            Map variables = (Map)UIManager.get(KEY_VARIABLES);
            String string = newValue = variables != null ? (String)variables.get(value) : null;
            if (newValue == null) {
                throw new IllegalArgumentException("variable '" + value + "' not found");
            }
            return UIDefaultsLoader.resolveValueFromUIManager(newValue);
        }
        if (!value.startsWith(PROPERTY_PREFIX)) {
            return value;
        }
        String key = value.substring(PROPERTY_PREFIX.length());
        Object newValue = UIManager.get(key);
        if (newValue == null) {
            throw new IllegalArgumentException("property '" + key + "' not found");
        }
        if (newValue instanceof Color) {
            Color color = (Color)newValue;
            int rgb = color.getRGB() & 0xFFFFFF;
            int alpha = color.getAlpha();
            return alpha != 255 ? String.format("#%06x%02x", rgb, alpha) : String.format("#%06x", rgb);
        }
        throw new IllegalArgumentException("property value type '" + newValue.getClass().getName() + "' not supported in references");
    }

    static Object parseValue(String key, String value, Class<?> valueType) throws IllegalArgumentException {
        return UIDefaultsLoader.parseValue(key, value, valueType, null, v -> v, Collections.emptyList());
    }

    static Object parseValue(String key, String value, Class<?> javaValueType, ValueType[] resultValueType, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) throws IllegalArgumentException {
        if (resultValueType == null) {
            resultValueType = tempResultValueType;
        }
        if (key.startsWith("[style]")) {
            resultValueType[0] = ValueType.STRING;
            return value;
        }
        if ((value = value.trim()).equals("null") || value.isEmpty()) {
            resultValueType[0] = ValueType.NULL;
            return null;
        }
        if (value.startsWith("if(") && value.endsWith(")")) {
            List<String> params = UIDefaultsLoader.splitFunctionParams(value.substring(3, value.length() - 1), ',');
            if (params.size() != 3) {
                throw UIDefaultsLoader.newMissingParametersException(value);
            }
            boolean ifCondition = UIDefaultsLoader.parseCondition(params.get(0), resolver, addonClassLoaders);
            String ifValue = params.get(ifCondition ? 1 : 2);
            return UIDefaultsLoader.parseValue(key, resolver.apply(ifValue), javaValueType, resultValueType, resolver, addonClassLoaders);
        }
        ValueType valueType = ValueType.UNKNOWN;
        if (javaValueType != null) {
            if (javaValueTypes == null) {
                javaValueTypes = new HashMap();
                javaValueTypes.put(String.class, ValueType.STRING);
                javaValueTypes.put(Boolean.TYPE, ValueType.BOOLEAN);
                javaValueTypes.put(Boolean.class, ValueType.BOOLEAN);
                javaValueTypes.put(Character.TYPE, ValueType.CHARACTER);
                javaValueTypes.put(Character.class, ValueType.CHARACTER);
                javaValueTypes.put(Integer.TYPE, ValueType.INTEGER);
                javaValueTypes.put(Integer.class, ValueType.INTEGER);
                javaValueTypes.put(Float.TYPE, ValueType.FLOAT);
                javaValueTypes.put(Float.class, ValueType.FLOAT);
                javaValueTypes.put(Border.class, ValueType.BORDER);
                javaValueTypes.put(Icon.class, ValueType.ICON);
                javaValueTypes.put(Insets.class, ValueType.INSETS);
                javaValueTypes.put(Dimension.class, ValueType.DIMENSION);
                javaValueTypes.put(Color.class, ValueType.COLOR);
                javaValueTypes.put(Font.class, ValueType.FONT);
            }
            if ((valueType = javaValueTypes.get(javaValueType)) == null) {
                throw new IllegalArgumentException("unsupported value type '" + javaValueType.getName() + "'");
            }
            if (valueType == ValueType.STRING && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
        } else {
            int end;
            switch (value) {
                case "false": {
                    resultValueType[0] = ValueType.BOOLEAN;
                    return false;
                }
                case "true": {
                    resultValueType[0] = ValueType.BOOLEAN;
                    return true;
                }
            }
            if (value.startsWith("lazy(") && value.endsWith(")")) {
                resultValueType[0] = ValueType.LAZY;
                String uiKey = StringUtils.substringTrimmed(value, 5, value.length() - 1);
                return t -> UIDefaultsLoader.lazyUIManagerGet(uiKey);
            }
            if (value.startsWith("#")) {
                valueType = ValueType.COLOR;
            } else if (value.startsWith(TYPE_PREFIX) && (end = value.indexOf(TYPE_PREFIX_END)) != -1) {
                try {
                    String typeStr = value.substring(TYPE_PREFIX.length(), end);
                    valueType = ValueType.valueOf(typeStr.toUpperCase(Locale.ENGLISH));
                    value = value.substring(end + TYPE_PREFIX_END.length());
                } catch (IllegalArgumentException typeStr) {
                    // empty catch block
                }
            }
            if (valueType == ValueType.UNKNOWN) {
                if (knownValueTypes == null) {
                    knownValueTypes = new HashMap<String, ValueType>();
                    knownValueTypes.put("activeCaptionBorder", ValueType.COLOR);
                    knownValueTypes.put("inactiveCaptionBorder", ValueType.COLOR);
                    knownValueTypes.put("windowBorder", ValueType.COLOR);
                    knownValueTypes.put("SplitPane.dividerSize", ValueType.INTEGER);
                    knownValueTypes.put("SplitPaneDivider.gripDotSize", ValueType.INTEGER);
                    knownValueTypes.put("dividerSize", ValueType.INTEGER);
                    knownValueTypes.put("gripDotSize", ValueType.INTEGER);
                    knownValueTypes.put("TabbedPane.closeCrossPlainSize", ValueType.FLOAT);
                    knownValueTypes.put("TabbedPane.closeCrossFilledSize", ValueType.FLOAT);
                    knownValueTypes.put("closeCrossPlainSize", ValueType.FLOAT);
                    knownValueTypes.put("closeCrossFilledSize", ValueType.FLOAT);
                    knownValueTypes.put("Table.intercellSpacing", ValueType.DIMENSION);
                    knownValueTypes.put("intercellSpacing", ValueType.DIMENSION);
                }
                valueType = knownValueTypes.getOrDefault(key, ValueType.UNKNOWN);
            }
            if (valueType == ValueType.UNKNOWN) {
                if (key.endsWith("UI")) {
                    valueType = ValueType.STRING;
                } else if (key.endsWith("Color") || key.endsWith("ground") && (key.endsWith(".background") || key.endsWith("Background") || key.equals("background") || key.endsWith(".foreground") || key.endsWith("Foreground") || key.equals("foreground"))) {
                    valueType = ValueType.COLOR;
                } else if (key.endsWith(".font") || key.endsWith("Font") || key.equals("font")) {
                    valueType = ValueType.FONT;
                } else if (key.endsWith(".border") || key.endsWith("Border") || key.equals("border")) {
                    valueType = ValueType.BORDER;
                } else if (key.endsWith(".icon") || key.endsWith("Icon") || key.equals("icon")) {
                    valueType = ValueType.ICON;
                } else if (key.endsWith(".margin") || key.equals("margin") || key.endsWith(".padding") || key.equals("padding") || key.endsWith("Margins") || key.endsWith("Insets")) {
                    valueType = ValueType.INSETS;
                } else if (key.endsWith("Size")) {
                    valueType = ValueType.DIMENSION;
                } else if (key.endsWith("Width") || key.endsWith("Height")) {
                    valueType = ValueType.INTEGERORFLOAT;
                } else if (key.endsWith("Char")) {
                    valueType = ValueType.CHARACTER;
                } else if (key.endsWith("grayFilter")) {
                    valueType = ValueType.GRAYFILTER;
                }
            }
        }
        resultValueType[0] = valueType;
        switch (valueType) {
            case STRING: {
                return value;
            }
            case BOOLEAN: {
                return UIDefaultsLoader.parseBoolean(value);
            }
            case CHARACTER: {
                return UIDefaultsLoader.parseCharacter(value);
            }
            case INTEGER: {
                return UIDefaultsLoader.parseInteger(value);
            }
            case INTEGERORFLOAT: {
                return UIDefaultsLoader.parseIntegerOrFloat(value);
            }
            case FLOAT: {
                return UIDefaultsLoader.parseFloat(value);
            }
            case BORDER: {
                return UIDefaultsLoader.parseBorder(value, resolver, addonClassLoaders);
            }
            case ICON: {
                return UIDefaultsLoader.parseInstance(value, resolver, addonClassLoaders);
            }
            case INSETS: {
                return UIDefaultsLoader.parseInsets(value);
            }
            case DIMENSION: {
                return UIDefaultsLoader.parseDimension(value);
            }
            case COLOR: {
                return UIDefaultsLoader.parseColorOrFunction(value, resolver);
            }
            case FONT: {
                return UIDefaultsLoader.parseFont(value);
            }
            case SCALEDINTEGER: {
                return UIDefaultsLoader.parseScaledInteger(value);
            }
            case SCALEDFLOAT: {
                return UIDefaultsLoader.parseScaledFloat(value);
            }
            case SCALEDINSETS: {
                return UIDefaultsLoader.parseScaledInsets(value);
            }
            case SCALEDDIMENSION: {
                return UIDefaultsLoader.parseScaledDimension(value);
            }
            case INSTANCE: {
                return UIDefaultsLoader.parseInstance(value, resolver, addonClassLoaders);
            }
            case CLASS: {
                return UIDefaultsLoader.parseClass(value, addonClassLoaders);
            }
            case GRAYFILTER: {
                return UIDefaultsLoader.parseGrayFilter(value);
            }
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            resultValueType[0] = ValueType.STRING;
            return value.substring(1, value.length() - 1);
        }
        if (value.startsWith("#") || value.endsWith(")")) {
            Object color = UIDefaultsLoader.parseColorOrFunction(value, resolver);
            resultValueType[0] = color != null ? ValueType.COLOR : ValueType.NULL;
            return color;
        }
        char firstChar = value.charAt(0);
        if (firstChar >= '0' && firstChar <= '9' || firstChar == '-' || firstChar == '+' || firstChar == '.') {
            try {
                Integer integer = UIDefaultsLoader.parseInteger(value);
                resultValueType[0] = ValueType.INTEGER;
                return integer;
            } catch (NumberFormatException integer) {
                try {
                    Float f = UIDefaultsLoader.parseFloat(value);
                    resultValueType[0] = ValueType.FLOAT;
                    return f;
                } catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        resultValueType[0] = ValueType.STRING;
        return value;
    }

    private static boolean parseCondition(String condition, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) {
        try {
            Object conditionValue = UIDefaultsLoader.parseValue("", resolver.apply(condition), null, null, resolver, addonClassLoaders);
            return conditionValue != null && !conditionValue.equals(false) && !conditionValue.equals(0);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static Object parseBorder(String value, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) throws IllegalArgumentException {
        if (value.indexOf(44) >= 0) {
            List<String> parts = UIDefaultsLoader.splitFunctionParams(value, ',');
            Insets insets = UIDefaultsLoader.parseInsets(value);
            ColorUIResource lineColor = parts.size() >= 5 ? (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(parts.get(4)), resolver) : null;
            float lineThickness = parts.size() >= 6 && !parts.get(5).isEmpty() ? UIDefaultsLoader.parseFloat(parts.get(5)).floatValue() : 1.0f;
            int arc = parts.size() >= 7 ? UIDefaultsLoader.parseInteger(parts.get(6)) : 0;
            return t -> lineColor != null ? new FlatLineBorder(insets, lineColor, lineThickness, arc) : new FlatEmptyBorder(insets);
        }
        return UIDefaultsLoader.parseInstance(value, resolver, addonClassLoaders);
    }

    private static Object parseInstance(String value, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) {
        return t -> {
            try {
                if (value.indexOf(44) >= 0) {
                    List<String> parts = UIDefaultsLoader.splitFunctionParams(value, ',');
                    String className = parts.get(0);
                    Class<?> cls = UIDefaultsLoader.findClass(className, addonClassLoaders);
                    Executable[] constructors = cls.getDeclaredConstructors();
                    Object result = UIDefaultsLoader.invokeConstructorOrStaticMethod(constructors, parts, resolver);
                    if (result != null) {
                        return result;
                    }
                    LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to instantiate '" + className + "': no constructor found for parameters '" + value.substring(value.indexOf(45)) + "'.", null);
                    return null;
                }
                return UIDefaultsLoader.findClass(value, addonClassLoaders).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to instantiate '" + value + "'.", ex);
                return null;
            }
        };
    }

    private static Object parseClass(String value, List<ClassLoader> addonClassLoaders) {
        return t -> {
            try {
                return UIDefaultsLoader.findClass(value, addonClassLoaders);
            } catch (ClassNotFoundException ex) {
                LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to find class '" + value + "'.", ex);
                return null;
            }
        };
    }

    private static Class<?> findClass(String className, List<ClassLoader> addonClassLoaders) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            for (ClassLoader addonClassLoader : addonClassLoaders) {
                try {
                    return addonClassLoader.loadClass(className);
                } catch (ClassNotFoundException classNotFoundException) {
                }
            }
            throw ex;
        }
    }

    private static Insets parseInsets(String value) throws IllegalArgumentException {
        List<String> numbers = StringUtils.split(value, ',', true, false);
        try {
            return new InsetsUIResource(Integer.parseInt(numbers.get(0)), Integer.parseInt(numbers.get(1)), Integer.parseInt(numbers.get(2)), Integer.parseInt(numbers.get(3)));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid insets '" + value + "'");
        }
    }

    private static Dimension parseDimension(String value) throws IllegalArgumentException {
        List<String> numbers = StringUtils.split(value, ',', true, false);
        try {
            return new DimensionUIResource(Integer.parseInt(numbers.get(0)), Integer.parseInt(numbers.get(1)));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid size '" + value + "'");
        }
    }

    private static Object parseColorOrFunction(String value, Function<String, String> resolver) throws IllegalArgumentException {
        if (value.endsWith(")")) {
            return UIDefaultsLoader.parseColorFunctions(value, resolver);
        }
        return UIDefaultsLoader.parseColor(value);
    }

    static ColorUIResource parseColor(String value) throws IllegalArgumentException {
        int rgba = UIDefaultsLoader.parseColorRGBA(value);
        return (rgba & 0xFF000000) == -16777216 ? new ColorUIResource(rgba) : new ColorUIResource(new Color(rgba, true));
    }

    static int parseColorRGBA(String value) throws IllegalArgumentException {
        int len = value.length();
        if (len != 4 && len != 5 && len != 7 && len != 9 || value.charAt(0) != '#') {
            throw UIDefaultsLoader.newInvalidColorException(value);
        }
        int n = 0;
        for (int i = 1; i < len; ++i) {
            int digit;
            char ch = value.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digit = ch - 48;
            } else if (ch >= 'a' && ch <= 'f') {
                digit = ch - 97 + 10;
            } else if (ch >= 'A' && ch <= 'F') {
                digit = ch - 65 + 10;
            } else {
                throw UIDefaultsLoader.newInvalidColorException(value);
            }
            n = n << 4 | digit;
        }
        if (len <= 5) {
            int n1 = n & 0xF000;
            int n2 = n & 0xF00;
            int n3 = n & 0xF0;
            int n4 = n & 0xF;
            n = n1 << 16 | n1 << 12 | n2 << 12 | n2 << 8 | n3 << 8 | n3 << 4 | n4 << 4 | n4;
        }
        return len == 4 || len == 7 ? 0xFF000000 | n : n >> 8 & 0xFFFFFF | (n & 0xFF) << 24;
    }

    private static IllegalArgumentException newInvalidColorException(String value) {
        return new IllegalArgumentException("invalid color '" + value + "'");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Object parseColorFunctions(String value, Function<String, String> resolver) throws IllegalArgumentException {
        int paramsStart = value.indexOf(40);
        if (paramsStart < 0) {
            throw new IllegalArgumentException("missing opening parenthesis in function '" + value + "'");
        }
        String function = StringUtils.substringTrimmed(value, 0, paramsStart);
        List<String> params = UIDefaultsLoader.splitFunctionParams(value.substring(paramsStart + 1, value.length() - 1), ',');
        if (params.isEmpty()) {
            throw UIDefaultsLoader.newMissingParametersException(value);
        }
        if (parseColorDepth > 100) {
            throw new IllegalArgumentException("endless recursion in color function '" + value + "'");
        }
        ++parseColorDepth;
        try {
            switch (function) {
                case "if": {
                    Object object = UIDefaultsLoader.parseColorIf(value, params, resolver);
                    return object;
                }
                case "systemColor": {
                    Object object = UIDefaultsLoader.parseColorSystemColor(value, params, resolver);
                    return object;
                }
                case "rgb": {
                    ColorUIResource colorUIResource = UIDefaultsLoader.parseColorRgbOrRgba(false, params, resolver);
                    return colorUIResource;
                }
                case "rgba": {
                    ColorUIResource colorUIResource = UIDefaultsLoader.parseColorRgbOrRgba(true, params, resolver);
                    return colorUIResource;
                }
                case "hsl": {
                    ColorUIResource colorUIResource = UIDefaultsLoader.parseColorHslOrHsla(false, params);
                    return colorUIResource;
                }
                case "hsla": {
                    ColorUIResource colorUIResource = UIDefaultsLoader.parseColorHslOrHsla(true, params);
                    return colorUIResource;
                }
                case "lighten": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(2, true, params, resolver);
                    return object;
                }
                case "darken": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(2, false, params, resolver);
                    return object;
                }
                case "saturate": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(1, true, params, resolver);
                    return object;
                }
                case "desaturate": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(1, false, params, resolver);
                    return object;
                }
                case "fadein": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(3, true, params, resolver);
                    return object;
                }
                case "fadeout": {
                    Object object = UIDefaultsLoader.parseColorHSLIncreaseDecrease(3, false, params, resolver);
                    return object;
                }
                case "fade": {
                    Object object = UIDefaultsLoader.parseColorFade(params, resolver);
                    return object;
                }
                case "spin": {
                    Object object = UIDefaultsLoader.parseColorSpin(params, resolver);
                    return object;
                }
                case "changeHue": {
                    Object object = UIDefaultsLoader.parseColorChange(0, params, resolver);
                    return object;
                }
                case "changeSaturation": {
                    Object object = UIDefaultsLoader.parseColorChange(1, params, resolver);
                    return object;
                }
                case "changeLightness": {
                    Object object = UIDefaultsLoader.parseColorChange(2, params, resolver);
                    return object;
                }
                case "changeAlpha": {
                    Object object = UIDefaultsLoader.parseColorChange(3, params, resolver);
                    return object;
                }
                case "mix": {
                    Object object = UIDefaultsLoader.parseColorMix(null, params, resolver);
                    return object;
                }
                case "tint": {
                    Object object = UIDefaultsLoader.parseColorMix("#fff", params, resolver);
                    return object;
                }
                case "shade": {
                    Object object = UIDefaultsLoader.parseColorMix("#000", params, resolver);
                    return object;
                }
                case "contrast": {
                    Object object = UIDefaultsLoader.parseColorContrast(params, resolver);
                    return object;
                }
                case "over": {
                    ColorUIResource colorUIResource = UIDefaultsLoader.parseColorOver(params, resolver);
                    return colorUIResource;
                }
            }
            throw new IllegalArgumentException("unknown color function '" + value + "'");
        } finally {
            --parseColorDepth;
        }
    }

    private static Object parseColorIf(String value, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        if (params.size() != 3) {
            throw UIDefaultsLoader.newMissingParametersException(value);
        }
        boolean ifCondition = UIDefaultsLoader.parseCondition(params.get(0), resolver, Collections.emptyList());
        String ifValue = params.get(ifCondition ? 1 : 2);
        return UIDefaultsLoader.parseColorOrFunction(resolver.apply(ifValue), resolver);
    }

    private static Object parseColorSystemColor(String value, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String defaultValue;
        if (params.size() < 1) {
            throw UIDefaultsLoader.newMissingParametersException(value);
        }
        ColorUIResource systemColor = UIDefaultsLoader.getSystemColor(params.get(0));
        if (systemColor != null) {
            return systemColor;
        }
        String string = defaultValue = params.size() > 1 ? params.get(1) : "";
        if (defaultValue.equals("null") || defaultValue.isEmpty()) {
            return null;
        }
        return UIDefaultsLoader.parseColorOrFunction(resolver.apply(defaultValue), resolver);
    }

    private static ColorUIResource getSystemColor(String name) {
        ColorUIResource uiColor;
        Function<String, Color> systemColorGetter = FlatLaf.getSystemColorGetter();
        if (systemColorGetter == null) {
            return null;
        }
        if (systemColorCache != null && systemColorCache.containsKey(name)) {
            return systemColorCache.get(name);
        }
        Color color = systemColorGetter.apply(name);
        ColorUIResource colorUIResource = uiColor = color != null ? new ColorUIResource(color) : null;
        if (systemColorCache != null) {
            systemColorCache.put(name, uiColor);
        }
        return uiColor;
    }

    private static ColorUIResource parseColorRgbOrRgba(boolean hasAlpha, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        if (hasAlpha && params.size() == 2) {
            String colorStr = params.get(0);
            int alpha = UIDefaultsLoader.parseInteger(params.get(1), 0, 255, true);
            ColorUIResource color = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(colorStr), resolver);
            return new ColorUIResource(new Color((alpha & 0xFF) << 24 | color.getRGB() & 0xFFFFFF, true));
        }
        int red = UIDefaultsLoader.parseInteger(params.get(0), 0, 255, true);
        int green = UIDefaultsLoader.parseInteger(params.get(1), 0, 255, true);
        int blue = UIDefaultsLoader.parseInteger(params.get(2), 0, 255, true);
        int alpha = hasAlpha ? UIDefaultsLoader.parseInteger(params.get(3), 0, 255, true) : 255;
        return hasAlpha ? new ColorUIResource(new Color(red, green, blue, alpha)) : new ColorUIResource(red, green, blue);
    }

    private static ColorUIResource parseColorHslOrHsla(boolean hasAlpha, List<String> params) throws IllegalArgumentException {
        int hue = UIDefaultsLoader.parseInteger(params.get(0), 0, 360, false);
        int saturation = UIDefaultsLoader.parsePercentage(params.get(1));
        int lightness = UIDefaultsLoader.parsePercentage(params.get(2));
        int alpha = hasAlpha ? UIDefaultsLoader.parsePercentage(params.get(3)) : 100;
        float[] hsl = new float[]{hue, saturation, lightness};
        return new ColorUIResource(HSLColor.toRGB(hsl, (float)alpha / 100.0f));
    }

    private static Object parseColorHSLIncreaseDecrease(int hslIndex, boolean increase, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String colorStr = params.get(0);
        int amount = UIDefaultsLoader.parsePercentage(params.get(1));
        boolean relative = false;
        boolean autoInverse = false;
        boolean lazy = false;
        boolean derived = false;
        if (params.size() > 2) {
            String options = params.get(2);
            relative = options.contains("relative");
            autoInverse = options.contains("autoInverse");
            lazy = options.contains("lazy");
            derived = options.contains("derived");
            if (derived && !options.contains("noAutoInverse")) {
                autoInverse = true;
            }
        }
        ColorFunctions.HSLIncreaseDecrease function = new ColorFunctions.HSLIncreaseDecrease(hslIndex, increase, amount, relative, autoInverse);
        if (lazy) {
            return t -> {
                Object color = UIDefaultsLoader.lazyUIManagerGet(colorStr);
                return color instanceof Color ? new ColorUIResource(ColorFunctions.applyFunctions((Color)color, function)) : null;
            };
        }
        return UIDefaultsLoader.parseFunctionBaseColor(colorStr, function, derived, resolver);
    }

    private static Object parseColorFade(List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String colorStr = params.get(0);
        int amount = UIDefaultsLoader.parsePercentage(params.get(1));
        boolean derived = false;
        boolean lazy = false;
        if (params.size() > 2) {
            String options = params.get(2);
            derived = options.contains("derived");
            lazy = options.contains("lazy");
        }
        ColorFunctions.Fade function = new ColorFunctions.Fade(amount);
        if (lazy) {
            return t -> {
                Object color = UIDefaultsLoader.lazyUIManagerGet(colorStr);
                return color instanceof Color ? new ColorUIResource(ColorFunctions.applyFunctions((Color)color, function)) : null;
            };
        }
        return UIDefaultsLoader.parseFunctionBaseColor(colorStr, function, derived, resolver);
    }

    private static Object parseColorSpin(List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String colorStr = params.get(0);
        int amount = UIDefaultsLoader.parseInteger(params.get(1));
        boolean derived = false;
        if (params.size() > 2) {
            String options = params.get(2);
            derived = options.contains("derived");
        }
        ColorFunctions.HSLIncreaseDecrease function = new ColorFunctions.HSLIncreaseDecrease(0, true, amount, false, false);
        return UIDefaultsLoader.parseFunctionBaseColor(colorStr, function, derived, resolver);
    }

    private static Object parseColorChange(int hslIndex, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String colorStr = params.get(0);
        int value = hslIndex == 0 ? UIDefaultsLoader.parseInteger(params.get(1)) : UIDefaultsLoader.parsePercentage(params.get(1));
        boolean derived = false;
        if (params.size() > 2) {
            String options = params.get(2);
            derived = options.contains("derived");
        }
        ColorFunctions.HSLChange function = new ColorFunctions.HSLChange(hslIndex, value);
        return UIDefaultsLoader.parseFunctionBaseColor(colorStr, function, derived, resolver);
    }

    private static Object parseColorMix(String color1Str, List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        int i = 0;
        if (color1Str == null) {
            color1Str = params.get(i++);
        }
        String color2Str = params.get(i++);
        int weight = params.size() > i ? UIDefaultsLoader.parsePercentage(params.get(i)) : 50;
        ColorUIResource color2 = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(color2Str), resolver);
        if (color2 == null) {
            return null;
        }
        ColorFunctions.Mix function = new ColorFunctions.Mix(color2, weight);
        return UIDefaultsLoader.parseFunctionBaseColor(color1Str, function, false, resolver);
    }

    private static Object parseColorContrast(List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String colorStr = params.get(0);
        String darkStr = params.get(1);
        String lightStr = params.get(2);
        int threshold = params.size() > 3 ? UIDefaultsLoader.parsePercentage(params.get(3)) : 43;
        ColorUIResource color = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(colorStr), resolver);
        if (color == null) {
            return null;
        }
        String darkOrLightColor = ColorFunctions.luma(color) * 100.0f < (float)threshold ? lightStr : darkStr;
        return UIDefaultsLoader.parseColorOrFunction(resolver.apply(darkOrLightColor), resolver);
    }

    private static ColorUIResource parseColorOver(List<String> params, Function<String, String> resolver) throws IllegalArgumentException {
        String foregroundStr = params.get(0);
        String backgroundStr = params.get(1);
        ColorUIResource foreground = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(foregroundStr), resolver);
        if (foreground == null || foreground.getAlpha() == 255) {
            return foreground;
        }
        ColorUIResource foreground2 = new ColorUIResource(foreground.getRGB());
        ColorUIResource background = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolver.apply(backgroundStr), resolver);
        if (background == null) {
            return foreground2;
        }
        float weight = (float)foreground.getAlpha() / 255.0f;
        return new ColorUIResource(ColorFunctions.mix(foreground2, background, weight));
    }

    private static Object parseFunctionBaseColor(String colorStr, ColorFunctions.ColorFunction function, boolean derived, Function<String, String> resolver) throws IllegalArgumentException {
        String resolvedColorStr = resolver.apply(colorStr);
        ColorUIResource baseColor = (ColorUIResource)UIDefaultsLoader.parseColorOrFunction(resolvedColorStr, resolver);
        if (baseColor == null) {
            return null;
        }
        Color newColor = ColorFunctions.applyFunctions(baseColor, function);
        if (derived) {
            ColorFunctions.ColorFunction[] functions;
            if (baseColor instanceof DerivedColor && resolvedColorStr == colorStr) {
                ColorFunctions.ColorFunction[] baseFunctions = ((DerivedColor)baseColor).getFunctions();
                functions = new ColorFunctions.ColorFunction[baseFunctions.length + 1];
                System.arraycopy(baseFunctions, 0, functions, 0, baseFunctions.length);
                functions[baseFunctions.length] = function;
            } else {
                functions = new ColorFunctions.ColorFunction[]{function};
            }
            return new DerivedColor(newColor, functions);
        }
        return new ColorUIResource(newColor);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Object parseFont(String value) throws IllegalArgumentException {
        Object font = fontCache.get(value);
        if (font != null) {
            return font;
        }
        int style = -1;
        int styleChange = 0;
        int absoluteSize = 0;
        int relativeSize = 0;
        float scaleSize = 0.0f;
        List<String> families = null;
        String baseFontKey = null;
        StreamTokenizer st = new StreamTokenizer(new StringReader(value));
        st.resetSyntax();
        st.wordChars(33, 255);
        st.whitespaceChars(0, 32);
        st.whitespaceChars(44, 44);
        st.quoteChar(34);
        st.quoteChar(39);
        try {
            block20: while (st.nextToken() != -1) {
                String param;
                switch (param = st.sval) {
                    case "normal": {
                        style = 0;
                        continue block20;
                    }
                    case "bold": {
                        if (style == -1) {
                            style = 0;
                        }
                        style |= 1;
                        continue block20;
                    }
                    case "italic": {
                        if (style == -1) {
                            style = 0;
                        }
                        style |= 2;
                        continue block20;
                    }
                    case "+bold": {
                        styleChange |= 1;
                        continue block20;
                    }
                    case "-bold": {
                        styleChange |= 0x10000;
                        continue block20;
                    }
                    case "+italic": {
                        styleChange |= 2;
                        continue block20;
                    }
                    case "-italic": {
                        styleChange |= 0x20000;
                        continue block20;
                    }
                }
                char firstChar = param.charAt(0);
                if (Character.isDigit(firstChar) || firstChar == '+' || firstChar == '-') {
                    if (absoluteSize != 0 || relativeSize != 0 || scaleSize != 0.0f) {
                        throw new IllegalArgumentException("size specified more than once in '" + value + "'");
                    }
                    if (firstChar == '+' || firstChar == '-') {
                        relativeSize = UIDefaultsLoader.parseInteger(param);
                        continue;
                    }
                    if (param.endsWith("%")) {
                        scaleSize = (float)UIDefaultsLoader.parseInteger(param.substring(0, param.length() - 1)).intValue() / 100.0f;
                        continue;
                    }
                    absoluteSize = UIDefaultsLoader.parseInteger(param);
                    continue;
                }
                if (firstChar == '$') {
                    if (baseFontKey != null) {
                        throw new IllegalArgumentException("baseFontKey specified more than once in '" + value + "'");
                    }
                    baseFontKey = param.substring(1);
                    continue;
                }
                if (families == null) {
                    families = Collections.singletonList(param);
                    continue;
                }
                if (families.size() == 1) {
                    families = new ArrayList<String>(families);
                }
                families.add(param);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (style != -1 && styleChange != 0) {
            throw new IllegalArgumentException("can not mix absolute style (e.g. 'bold') with derived style (e.g. '+italic') in '" + value + "'");
        }
        if (styleChange != 0) {
            if (styleChange & true && (styleChange & 0x10000) != 0) {
                throw new IllegalArgumentException("can not use '+bold' and '-bold' in '" + value + "'");
            }
            if ((styleChange & 2) != 0 && (styleChange & 0x20000) != 0) {
                throw new IllegalArgumentException("can not use '+italic' and '-italic' in '" + value + "'");
            }
        }
        font = new FlatLaf.ActiveFont(baseFontKey, families, style, styleChange, absoluteSize, relativeSize, scaleSize);
        fontCache.put(value, font);
        return font;
    }

    private static int parsePercentage(String value) throws IllegalArgumentException, NumberFormatException {
        int val2;
        if (!value.endsWith("%")) {
            throw new NumberFormatException("invalid percentage '" + value + "'");
        }
        try {
            val2 = Integer.parseInt(value.substring(0, value.length() - 1));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("invalid percentage '" + value + "'");
        }
        if (val2 < 0 || val2 > 100) {
            throw new IllegalArgumentException("percentage out of range (0-100%) '" + value + "'");
        }
        return val2;
    }

    private static Boolean parseBoolean(String value) throws IllegalArgumentException {
        switch (value) {
            case "false": {
                return false;
            }
            case "true": {
                return true;
            }
        }
        throw new IllegalArgumentException("invalid boolean '" + value + "'");
    }

    private static Character parseCharacter(String value) throws IllegalArgumentException {
        if (value.length() != 1) {
            throw new IllegalArgumentException("invalid character '" + value + "'");
        }
        return Character.valueOf(value.charAt(0));
    }

    private static Integer parseInteger(String value, int min, int max, boolean allowPercentage) throws IllegalArgumentException, NumberFormatException {
        if (allowPercentage && value.endsWith("%")) {
            int percent = UIDefaultsLoader.parsePercentage(value);
            return max * percent / 100;
        }
        Integer integer = UIDefaultsLoader.parseInteger(value);
        if (integer < min || integer > max) {
            throw new NumberFormatException("integer '" + value + "' out of range (" + min + '-' + max + ')');
        }
        return integer;
    }

    private static Integer parseInteger(String value) throws NumberFormatException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("invalid integer '" + value + "'");
        }
    }

    private static Number parseIntegerOrFloat(String value) throws NumberFormatException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            try {
                return Float.valueOf(Float.parseFloat(value));
            } catch (NumberFormatException ex2) {
                throw new NumberFormatException("invalid integer or float '" + value + "'");
            }
        }
    }

    private static Float parseFloat(String value) throws NumberFormatException {
        try {
            return Float.valueOf(Float.parseFloat(value));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("invalid float '" + value + "'");
        }
    }

    private static UIDefaults.ActiveValue parseScaledInteger(String value) throws NumberFormatException {
        int val2 = UIDefaultsLoader.parseInteger(value);
        return t -> UIScale.scale(val2);
    }

    private static UIDefaults.ActiveValue parseScaledFloat(String value) throws NumberFormatException {
        float val2 = UIDefaultsLoader.parseFloat(value).floatValue();
        return t -> Float.valueOf(UIScale.scale(val2));
    }

    private static UIDefaults.ActiveValue parseScaledInsets(String value) throws IllegalArgumentException {
        Insets insets = UIDefaultsLoader.parseInsets(value);
        return t -> UIScale.scale(insets);
    }

    private static UIDefaults.ActiveValue parseScaledDimension(String value) throws IllegalArgumentException {
        Dimension dimension = UIDefaultsLoader.parseDimension(value);
        return t -> UIScale.scale(dimension);
    }

    private static Object parseGrayFilter(String value) throws IllegalArgumentException {
        List<String> numbers = StringUtils.split(value, ',', true, false);
        try {
            int brightness = Integer.parseInt(numbers.get(0));
            int contrast = Integer.parseInt(numbers.get(1));
            int alpha = Integer.parseInt(numbers.get(2));
            return t -> new GrayFilter(brightness, contrast, alpha);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid gray filter '" + value + "'");
        }
    }

    private static List<String> splitFunctionParams(String str, char delim) {
        ArrayList<String> strs = new ArrayList<String>();
        int nestLevel = 0;
        int start = 0;
        int strlen = str.length();
        for (int i = 0; i < strlen; ++i) {
            char ch = str.charAt(i);
            if (ch == '(') {
                ++nestLevel;
                continue;
            }
            if (ch == ')') {
                --nestLevel;
                continue;
            }
            if (nestLevel != 0 || ch != delim) continue;
            strs.add(StringUtils.substringTrimmed(str, start, i));
            start = i + 1;
        }
        String s = StringUtils.substringTrimmed(str, start);
        if (!s.isEmpty() || !strs.isEmpty()) {
            strs.add(s);
        }
        return strs;
    }

    private static Object invokeConstructorOrStaticMethod(Executable[] constructorsOrMethods, List<String> parts, Function<String, String> resolver) throws Exception {
        constructorsOrMethods = (Executable[])constructorsOrMethods.clone();
        Arrays.sort(constructorsOrMethods, (c1, c2) -> {
            Class<?>[] ptypes2;
            Class<?>[] ptypes1 = c1.getParameterTypes();
            if (ptypes1.length != (ptypes2 = c2.getParameterTypes()).length) {
                return ptypes1.length - ptypes2.length;
            }
            for (int i = 0; i < ptypes1.length; ++i) {
                Class<?> pt1 = ptypes1[i];
                Class<?> pt2 = ptypes2[i];
                if (pt1 == pt2) continue;
                if (pt1 == String.class) {
                    return 2;
                }
                if (pt2 == String.class) {
                    return -2;
                }
                if (pt1 == Integer.TYPE) {
                    return -1;
                }
                if (pt2 != Integer.TYPE) continue;
                return 1;
            }
            return 0;
        });
        for (Executable cm : constructorsOrMethods) {
            Object[] params;
            if (cm.getParameterCount() != parts.size() - 1 || (params = UIDefaultsLoader.parseMethodParams(cm.getParameterTypes(), parts, resolver)) == null) continue;
            if (cm instanceof Constructor) {
                return ((Constructor)cm).newInstance(params);
            }
            return ((Method)cm).invoke(null, params);
        }
        return null;
    }

    private static Object[] parseMethodParams(Class<?>[] paramTypes, List<String> parts, Function<String, String> resolver) {
        Object[] params = new Object[paramTypes.length];
        try {
            for (int i = 0; i < params.length; ++i) {
                Class<?> paramType = paramTypes[i];
                String paramValue = parts.get(i + 1);
                if (paramType == String.class) {
                    params[i] = paramValue;
                    continue;
                }
                if (paramType == Boolean.TYPE) {
                    params[i] = UIDefaultsLoader.parseBoolean(paramValue);
                    continue;
                }
                if (paramType == Integer.TYPE) {
                    params[i] = UIDefaultsLoader.parseInteger(paramValue);
                    continue;
                }
                if (paramType == Float.TYPE) {
                    params[i] = UIDefaultsLoader.parseFloat(paramValue);
                    continue;
                }
                if (paramType == Color.class) {
                    params[i] = UIDefaultsLoader.parseColorOrFunction(resolver.apply(paramValue), resolver);
                    continue;
                }
                return null;
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return params;
    }

    static Object lazyUIManagerGet(String uiKey) {
        Object value;
        boolean optional = false;
        if (uiKey.startsWith(OPTIONAL_PREFIX)) {
            uiKey = uiKey.substring(OPTIONAL_PREFIX.length());
            optional = true;
        }
        if ((value = UIManager.get(uiKey)) == null && !optional) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: '" + uiKey + "' not found in UI defaults.", null);
        }
        return value;
    }

    private static IllegalArgumentException newMissingParametersException(String value) {
        return new IllegalArgumentException("missing parameters in function '" + value + "'");
    }

    static {
        fontCache = new SoftCache();
        tempResultValueType = new ValueType[1];
    }

    static enum ValueType {
        UNKNOWN,
        STRING,
        BOOLEAN,
        CHARACTER,
        INTEGER,
        INTEGERORFLOAT,
        FLOAT,
        BORDER,
        ICON,
        INSETS,
        DIMENSION,
        COLOR,
        FONT,
        SCALEDINTEGER,
        SCALEDFLOAT,
        SCALEDINSETS,
        SCALEDDIMENSION,
        INSTANCE,
        CLASS,
        GRAYFILTER,
        NULL,
        LAZY;

    }
}

