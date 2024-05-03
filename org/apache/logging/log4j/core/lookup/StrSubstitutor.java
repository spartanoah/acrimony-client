/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationAware;
import org.apache.logging.log4j.core.lookup.LookupResult;
import org.apache.logging.log4j.core.lookup.PropertiesLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrMatcher;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public class StrSubstitutor
implements ConfigurationAware {
    public static final char DEFAULT_ESCAPE = '$';
    public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
    public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
    public static final String DEFAULT_VALUE_DELIMITER_STRING = ":-";
    public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
    public static final String ESCAPE_DELIMITER_STRING = ":\\-";
    public static final StrMatcher DEFAULT_VALUE_ESCAPE_DELIMITER = StrMatcher.stringMatcher(":\\-");
    private static final int BUF_SIZE = 256;
    private char escapeChar;
    private StrMatcher prefixMatcher;
    private StrMatcher suffixMatcher;
    private String valueDelimiterString;
    private StrMatcher valueDelimiterMatcher;
    private StrMatcher valueEscapeDelimiterMatcher;
    private StrLookup variableResolver;
    private boolean enableSubstitutionInVariables = true;
    private Configuration configuration;

    public StrSubstitutor() {
        this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(Map<String, String> valueMap) {
        this((StrLookup)new PropertiesLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(Map<String, String> valueMap, String prefix, String suffix) {
        this((StrLookup)new PropertiesLookup(valueMap), prefix, suffix, '$');
    }

    public StrSubstitutor(Map<String, String> valueMap, String prefix, String suffix, char escape) {
        this((StrLookup)new PropertiesLookup(valueMap), prefix, suffix, escape);
    }

    public StrSubstitutor(Map<String, String> valueMap, String prefix, String suffix, char escape, String valueDelimiter) {
        this((StrLookup)new PropertiesLookup(valueMap), prefix, suffix, escape, valueDelimiter);
    }

    public StrSubstitutor(Properties properties) {
        this(StrSubstitutor.toTypeSafeMap(properties));
    }

    public StrSubstitutor(StrLookup variableResolver) {
        this(variableResolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(StrLookup variableResolver, String prefix, String suffix, char escape) {
        this.setVariableResolver(variableResolver);
        this.setVariablePrefix(prefix);
        this.setVariableSuffix(suffix);
        this.setEscapeChar(escape);
    }

    public StrSubstitutor(StrLookup variableResolver, String prefix, String suffix, char escape, String valueDelimiter) {
        this.setVariableResolver(variableResolver);
        this.setVariablePrefix(prefix);
        this.setVariableSuffix(suffix);
        this.setEscapeChar(escape);
        this.setValueDelimiter(valueDelimiter);
    }

    public StrSubstitutor(StrLookup variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape) {
        this(variableResolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER, DEFAULT_VALUE_ESCAPE_DELIMITER);
        this.valueDelimiterString = DEFAULT_VALUE_DELIMITER_STRING;
    }

    public StrSubstitutor(StrLookup variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape, StrMatcher valueDelimiterMatcher) {
        this.setVariableResolver(variableResolver);
        this.setVariablePrefixMatcher(prefixMatcher);
        this.setVariableSuffixMatcher(suffixMatcher);
        this.setEscapeChar(escape);
        this.setValueDelimiterMatcher(valueDelimiterMatcher);
    }

    public StrSubstitutor(StrLookup variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape, StrMatcher valueDelimiterMatcher, StrMatcher valueEscapeMatcher) {
        this.setVariableResolver(variableResolver);
        this.setVariablePrefixMatcher(prefixMatcher);
        this.setVariableSuffixMatcher(suffixMatcher);
        this.setEscapeChar(escape);
        this.setValueDelimiterMatcher(valueDelimiterMatcher);
        this.valueEscapeDelimiterMatcher = valueEscapeMatcher;
    }

    StrSubstitutor(StrSubstitutor other) {
        Objects.requireNonNull(other, "other");
        this.setVariableResolver(other.getVariableResolver());
        this.setVariablePrefixMatcher(other.getVariablePrefixMatcher());
        this.setVariableSuffixMatcher(other.getVariableSuffixMatcher());
        this.setEscapeChar(other.getEscapeChar());
        this.setValueDelimiterMatcher(other.valueDelimiterMatcher);
        this.valueEscapeDelimiterMatcher = other.valueEscapeDelimiterMatcher;
        this.configuration = other.configuration;
        this.enableSubstitutionInVariables = other.isEnableSubstitutionInVariables();
        this.valueDelimiterString = other.valueDelimiterString;
    }

    public static String replace(Object source, Map<String, String> valueMap) {
        return new StrSubstitutor(valueMap).replace(source);
    }

    public static String replace(Object source, Map<String, String> valueMap, String prefix, String suffix) {
        return new StrSubstitutor(valueMap, prefix, suffix).replace(source);
    }

    public static String replace(Object source, Properties valueProperties) {
        if (valueProperties == null) {
            return Objects.toString(source, null);
        }
        HashMap<String, String> valueMap = new HashMap<String, String>();
        Enumeration<?> propNames = valueProperties.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String)propNames.nextElement();
            String propValue = valueProperties.getProperty(propName);
            valueMap.put(propName, propValue);
        }
        return StrSubstitutor.replace(source, valueMap);
    }

    private static Map<String, String> toTypeSafeMap(Properties properties) {
        HashMap<String, String> map = new HashMap<String, String>(properties.size());
        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return map;
    }

    private static String handleFailedReplacement(String input, Throwable throwable) {
        StatusLogger.getLogger().error("Replacement failed on {}", (Object)input, (Object)throwable);
        return input;
    }

    public String replace(String source) {
        return this.replace(null, source);
    }

    public String replace(LogEvent event, String source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source);
        try {
            if (!this.substitute(event, buf, 0, source.length())) {
                return source;
            }
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source, t);
        }
        return buf.toString();
    }

    public String replace(String source, int offset, int length) {
        return this.replace(null, source, offset, length);
    }

    public String replace(LogEvent event, String source, int offset, int length) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(length).append(source, offset, length);
        try {
            if (!this.substitute(event, buf, 0, length)) {
                return source.substring(offset, offset + length);
            }
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source, t);
        }
        return buf.toString();
    }

    public String replace(char[] source) {
        return this.replace((LogEvent)null, source);
    }

    public String replace(LogEvent event, char[] source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length).append(source);
        try {
            this.substitute(event, buf, 0, source.length);
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(new String(source), t);
        }
        return buf.toString();
    }

    public String replace(char[] source, int offset, int length) {
        return this.replace(null, source, offset, length);
    }

    public String replace(LogEvent event, char[] source, int offset, int length) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(length).append(source, offset, length);
        try {
            this.substitute(event, buf, 0, length);
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(new String(source, offset, length), t);
        }
        return buf.toString();
    }

    public String replace(StringBuffer source) {
        return this.replace(null, source);
    }

    public String replace(LogEvent event, StringBuffer source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length()).append(source);
        try {
            this.substitute(event, buf, 0, buf.length());
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source.toString(), t);
        }
        return buf.toString();
    }

    public String replace(StringBuffer source, int offset, int length) {
        return this.replace(null, source, offset, length);
    }

    public String replace(LogEvent event, StringBuffer source, int offset, int length) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(length).append(source, offset, length);
        try {
            this.substitute(event, buf, 0, length);
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source.substring(offset, offset + length), t);
        }
        return buf.toString();
    }

    public String replace(StringBuilder source) {
        return this.replace(null, source);
    }

    public String replace(LogEvent event, StringBuilder source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length()).append((CharSequence)source);
        try {
            this.substitute(event, buf, 0, buf.length());
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source.toString(), t);
        }
        return buf.toString();
    }

    public String replace(StringBuilder source, int offset, int length) {
        return this.replace(null, source, offset, length);
    }

    public String replace(LogEvent event, StringBuilder source, int offset, int length) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(length).append(source, offset, length);
        try {
            this.substitute(event, buf, 0, length);
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(source.substring(offset, offset + length), t);
        }
        return buf.toString();
    }

    public String replace(Object source) {
        return this.replace(null, source);
    }

    public String replace(LogEvent event, Object source) {
        if (source == null) {
            return null;
        }
        String stringValue = String.valueOf(source);
        StringBuilder buf = new StringBuilder(stringValue.length()).append(stringValue);
        try {
            this.substitute(event, buf, 0, buf.length());
        } catch (Throwable t) {
            return StrSubstitutor.handleFailedReplacement(stringValue, t);
        }
        return buf.toString();
    }

    public boolean replaceIn(StringBuffer source) {
        if (source == null) {
            return false;
        }
        return this.replaceIn(source, 0, source.length());
    }

    public boolean replaceIn(StringBuffer source, int offset, int length) {
        return this.replaceIn(null, source, offset, length);
    }

    public boolean replaceIn(LogEvent event, StringBuffer source, int offset, int length) {
        if (source == null) {
            return false;
        }
        StringBuilder buf = new StringBuilder(length).append(source, offset, length);
        try {
            if (!this.substitute(event, buf, 0, length)) {
                return false;
            }
        } catch (Throwable t) {
            StatusLogger.getLogger().error("Replacement failed on {}", (Object)source, (Object)t);
            return false;
        }
        source.replace(offset, offset + length, buf.toString());
        return true;
    }

    public boolean replaceIn(StringBuilder source) {
        return this.replaceIn(null, source);
    }

    public boolean replaceIn(LogEvent event, StringBuilder source) {
        if (source == null) {
            return false;
        }
        return this.substitute(event, source, 0, source.length());
    }

    public boolean replaceIn(StringBuilder source, int offset, int length) {
        return this.replaceIn(null, source, offset, length);
    }

    public boolean replaceIn(LogEvent event, StringBuilder source, int offset, int length) {
        if (source == null) {
            return false;
        }
        return this.substitute(event, source, offset, length);
    }

    protected boolean substitute(LogEvent event, StringBuilder buf, int offset, int length) {
        return this.substitute(event, buf, offset, length, null) > 0;
    }

    private int substitute(LogEvent event, StringBuilder buf, int offset, int length, List<String> priorVariables) {
        StrMatcher prefixMatcher = this.getVariablePrefixMatcher();
        StrMatcher suffixMatcher = this.getVariableSuffixMatcher();
        char escape = this.getEscapeChar();
        StrMatcher valueDelimiterMatcher = this.getValueDelimiterMatcher();
        boolean substitutionInVariablesEnabled = this.isEnableSubstitutionInVariables();
        boolean top = priorVariables == null;
        boolean altered = false;
        int lengthChange = 0;
        char[] chars = this.getChars(buf);
        int bufEnd = offset + length;
        int pos = offset;
        block0: while (pos < bufEnd) {
            int startMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd);
            if (startMatchLen == 0) {
                ++pos;
                continue;
            }
            if (pos > offset && chars[pos - 1] == escape) {
                buf.deleteCharAt(pos - 1);
                chars = this.getChars(buf);
                --lengthChange;
                altered = true;
                --bufEnd;
                continue;
            }
            int startPos = pos;
            pos += startMatchLen;
            int endMatchLen = 0;
            int nestedVarCount = 0;
            while (pos < bufEnd) {
                if (substitutionInVariablesEnabled && (endMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd)) != 0) {
                    ++nestedVarCount;
                    pos += endMatchLen;
                    continue;
                }
                endMatchLen = suffixMatcher.isMatch(chars, pos, offset, bufEnd);
                if (endMatchLen == 0) {
                    ++pos;
                    continue;
                }
                if (nestedVarCount == 0) {
                    String varValue;
                    boolean isCyclic;
                    String varNameExpr = new String(chars, startPos + startMatchLen, pos - startPos - startMatchLen);
                    if (substitutionInVariablesEnabled) {
                        if (priorVariables == null) {
                            priorVariables = new ArrayList<String>();
                        }
                        StringBuilder bufName = new StringBuilder(varNameExpr);
                        this.substitute(event, bufName, 0, bufName.length(), priorVariables);
                        varNameExpr = bufName.toString();
                    }
                    int endPos = pos += endMatchLen;
                    String varName = varNameExpr;
                    String varDefaultValue = null;
                    if (valueDelimiterMatcher != null) {
                        char[] varNameExprChars = varNameExpr.toCharArray();
                        int valueDelimiterMatchLen = 0;
                        block2: for (int i = 0; i < varNameExprChars.length && (substitutionInVariablesEnabled || prefixMatcher.isMatch(varNameExprChars, i, i, varNameExprChars.length) == 0); ++i) {
                            if (this.valueEscapeDelimiterMatcher != null) {
                                int matchLen = this.valueEscapeDelimiterMatcher.isMatch(varNameExprChars, i);
                                if (matchLen != 0) {
                                    String varNamePrefix = varNameExpr.substring(0, i) + ':';
                                    varName = varNamePrefix + varNameExpr.substring(i + matchLen - 1);
                                    for (int j = i + matchLen; j < varNameExprChars.length; ++j) {
                                        valueDelimiterMatchLen = valueDelimiterMatcher.isMatch(varNameExprChars, j);
                                        if (valueDelimiterMatchLen == 0) continue;
                                        varName = varNamePrefix + varNameExpr.substring(i + matchLen, j);
                                        varDefaultValue = varNameExpr.substring(j + valueDelimiterMatchLen);
                                        break block2;
                                    }
                                    break;
                                }
                                valueDelimiterMatchLen = valueDelimiterMatcher.isMatch(varNameExprChars, i);
                                if (valueDelimiterMatchLen == 0) continue;
                                varName = varNameExpr.substring(0, i);
                                varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                break;
                            }
                            valueDelimiterMatchLen = valueDelimiterMatcher.isMatch(varNameExprChars, i);
                            if (valueDelimiterMatchLen == 0) continue;
                            varName = varNameExpr.substring(0, i);
                            varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                            break;
                        }
                    }
                    if (priorVariables == null) {
                        priorVariables = new ArrayList<String>();
                        priorVariables.add(new String(chars, offset, length + lengthChange));
                    }
                    LookupResult resolvedResult = (isCyclic = this.isCyclicSubstitution(varName, priorVariables)) ? null : this.resolveVariable(event, varName, buf, startPos, endPos);
                    String string = varValue = resolvedResult == null ? null : resolvedResult.value();
                    if (varValue == null) {
                        varValue = varDefaultValue;
                    }
                    if (varValue != null) {
                        int varLen = varValue.length();
                        buf.replace(startPos, endPos, varValue);
                        altered = true;
                        int change = resolvedResult != null && resolvedResult.isLookupEvaluationAllowedInValue() ? this.substitute(event, buf, startPos, varLen, priorVariables) : 0;
                        pos += (change += varLen - (endPos - startPos));
                        bufEnd += change;
                        lengthChange += change;
                        chars = this.getChars(buf);
                    }
                    if (isCyclic) continue block0;
                    priorVariables.remove(priorVariables.size() - 1);
                    continue block0;
                }
                --nestedVarCount;
                pos += endMatchLen;
            }
        }
        if (top) {
            return altered ? 1 : 0;
        }
        return lengthChange;
    }

    private boolean isCyclicSubstitution(String varName, List<String> priorVariables) {
        if (!priorVariables.contains(varName)) {
            priorVariables.add(varName);
            return false;
        }
        StringBuilder buf = new StringBuilder(256);
        buf.append("Infinite loop in property interpolation of ");
        this.appendWithSeparators(buf, priorVariables, "->");
        StatusLogger.getLogger().warn(buf);
        return true;
    }

    protected LookupResult resolveVariable(LogEvent event, String variableName, StringBuilder buf, int startPos, int endPos) {
        StrLookup resolver = this.getVariableResolver();
        if (resolver == null) {
            return null;
        }
        try {
            return resolver.evaluate(event, variableName);
        } catch (Throwable t) {
            StatusLogger.getLogger().error("Resolver failed to lookup {}", (Object)variableName, (Object)t);
            return null;
        }
    }

    public char getEscapeChar() {
        return this.escapeChar;
    }

    public void setEscapeChar(char escapeCharacter) {
        this.escapeChar = escapeCharacter;
    }

    public StrMatcher getVariablePrefixMatcher() {
        return this.prefixMatcher;
    }

    public StrSubstitutor setVariablePrefixMatcher(StrMatcher prefixMatcher) {
        if (prefixMatcher == null) {
            throw new IllegalArgumentException("Variable prefix matcher must not be null!");
        }
        this.prefixMatcher = prefixMatcher;
        return this;
    }

    public StrSubstitutor setVariablePrefix(char prefix) {
        return this.setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
    }

    public StrSubstitutor setVariablePrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Variable prefix must not be null!");
        }
        return this.setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
    }

    public StrMatcher getVariableSuffixMatcher() {
        return this.suffixMatcher;
    }

    public StrSubstitutor setVariableSuffixMatcher(StrMatcher suffixMatcher) {
        if (suffixMatcher == null) {
            throw new IllegalArgumentException("Variable suffix matcher must not be null!");
        }
        this.suffixMatcher = suffixMatcher;
        return this;
    }

    public StrSubstitutor setVariableSuffix(char suffix) {
        return this.setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
    }

    public StrSubstitutor setVariableSuffix(String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("Variable suffix must not be null!");
        }
        return this.setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
    }

    public StrMatcher getValueDelimiterMatcher() {
        return this.valueDelimiterMatcher;
    }

    public StrSubstitutor setValueDelimiterMatcher(StrMatcher valueDelimiterMatcher) {
        this.valueDelimiterMatcher = valueDelimiterMatcher;
        return this;
    }

    public StrSubstitutor setValueDelimiter(char valueDelimiter) {
        return this.setValueDelimiterMatcher(StrMatcher.charMatcher(valueDelimiter));
    }

    public StrSubstitutor setValueDelimiter(String valueDelimiter) {
        if (Strings.isEmpty(valueDelimiter)) {
            this.setValueDelimiterMatcher(null);
            return this;
        }
        String escapeValue = valueDelimiter.substring(0, valueDelimiter.length() - 1) + "\\" + valueDelimiter.substring(valueDelimiter.length() - 1);
        this.valueEscapeDelimiterMatcher = StrMatcher.stringMatcher(escapeValue);
        return this.setValueDelimiterMatcher(StrMatcher.stringMatcher(valueDelimiter));
    }

    public StrLookup getVariableResolver() {
        return this.variableResolver;
    }

    public void setVariableResolver(StrLookup variableResolver) {
        if (variableResolver instanceof ConfigurationAware && this.configuration != null) {
            ((ConfigurationAware)((Object)variableResolver)).setConfiguration(this.configuration);
        }
        this.variableResolver = variableResolver;
    }

    public boolean isEnableSubstitutionInVariables() {
        return this.enableSubstitutionInVariables;
    }

    public void setEnableSubstitutionInVariables(boolean enableSubstitutionInVariables) {
        this.enableSubstitutionInVariables = enableSubstitutionInVariables;
    }

    private char[] getChars(StringBuilder sb) {
        char[] chars = new char[sb.length()];
        sb.getChars(0, sb.length(), chars, 0);
        return chars;
    }

    public void appendWithSeparators(StringBuilder sb, Iterable<?> iterable, String separator) {
        if (iterable != null) {
            separator = separator == null ? "" : separator;
            Iterator<?> it = iterable.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (!it.hasNext()) continue;
                sb.append(separator);
            }
        }
    }

    public String toString() {
        return "StrSubstitutor(" + this.variableResolver.toString() + ')';
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        if (this.variableResolver instanceof ConfigurationAware) {
            ((ConfigurationAware)((Object)this.variableResolver)).setConfiguration(this.configuration);
        }
    }
}

