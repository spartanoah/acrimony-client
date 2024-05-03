/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.NumberConversionUtil;
import org.json.XMLParserConfiguration;
import org.json.XMLTokener;
import org.json.XMLXsiTypeConverter;

public class XML {
    public static final Character AMP = Character.valueOf('&');
    public static final Character APOS = Character.valueOf('\'');
    public static final Character BANG = Character.valueOf('!');
    public static final Character EQ = Character.valueOf('=');
    public static final Character GT = Character.valueOf('>');
    public static final Character LT = Character.valueOf('<');
    public static final Character QUEST = Character.valueOf('?');
    public static final Character QUOT = Character.valueOf('\"');
    public static final Character SLASH = Character.valueOf('/');
    public static final String NULL_ATTR = "xsi:nil";
    public static final String TYPE_ATTR = "xsi:type";

    private static Iterable<Integer> codePointIterator(final String string) {
        return new Iterable<Integer>(){

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>(){
                    private int nextIndex = 0;
                    private int length;
                    {
                        this.length = string.length();
                    }

                    @Override
                    public boolean hasNext() {
                        return this.nextIndex < this.length;
                    }

                    @Override
                    public Integer next() {
                        int result = string.codePointAt(this.nextIndex);
                        this.nextIndex += Character.charCount(result);
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static String escape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        block7: for (int cp : XML.codePointIterator(string)) {
            switch (cp) {
                case 38: {
                    sb.append("&amp;");
                    continue block7;
                }
                case 60: {
                    sb.append("&lt;");
                    continue block7;
                }
                case 62: {
                    sb.append("&gt;");
                    continue block7;
                }
                case 34: {
                    sb.append("&quot;");
                    continue block7;
                }
                case 39: {
                    sb.append("&apos;");
                    continue block7;
                }
            }
            if (XML.mustEscape(cp)) {
                sb.append("&#x");
                sb.append(Integer.toHexString(cp));
                sb.append(';');
                continue;
            }
            sb.appendCodePoint(cp);
        }
        return sb.toString();
    }

    private static boolean mustEscape(int cp) {
        return Character.isISOControl(cp) && cp != 9 && cp != 10 && cp != 13 || (cp < 32 || cp > 55295) && (cp < 57344 || cp > 65533) && (cp < 65536 || cp > 0x10FFFF);
    }

    public static String unescape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            if (c == '&') {
                int semic = string.indexOf(59, i);
                if (semic > i) {
                    String entity = string.substring(i + 1, semic);
                    sb.append(XMLTokener.unescapeEntity(entity));
                    i += entity.length() + 1;
                    continue;
                }
                sb.append(c);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static void noSpace(String string) throws JSONException {
        int length = string.length();
        if (length == 0) {
            throw new JSONException("Empty string.");
        }
        for (int i = 0; i < length; ++i) {
            if (!Character.isWhitespace(string.charAt(i))) continue;
            throw new JSONException("'" + string + "' contains a space character.");
        }
    }

    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, int currentNestingDepth) throws JSONException {
        String string;
        JSONObject jsonObject = null;
        Object token = x.nextToken();
        if (token == BANG) {
            char c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token) && x.next() == '[') {
                    String string2 = x.nextCDATA();
                    if (string2.length() > 0) {
                        context.accumulate(config.getcDataTagName(), string2);
                    }
                    return false;
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            int i = 1;
            do {
                if ((token = x.nextMeta()) == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                }
                if (token == LT) {
                    ++i;
                    continue;
                }
                if (token != GT) continue;
                --i;
            } while (i > 0);
            return false;
        }
        if (token == QUEST) {
            x.skipPast("?>");
            return false;
        }
        if (token == SLASH) {
            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;
        }
        if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");
        }
        String tagName = (String)token;
        token = null;
        jsonObject = new JSONObject();
        boolean nilAttributeFound = false;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter = null;
        while (true) {
            if (token == null) {
                token = x.nextToken();
            }
            if (!(token instanceof String)) break;
            string = (String)token;
            token = x.nextToken();
            if (token == EQ) {
                token = x.nextToken();
                if (!(token instanceof String)) {
                    throw x.syntaxError("Missing value");
                }
                if (config.isConvertNilAttributeToNull() && NULL_ATTR.equals(string) && Boolean.parseBoolean((String)token)) {
                    nilAttributeFound = true;
                } else if (config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty() && TYPE_ATTR.equals(string)) {
                    xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                } else if (!nilAttributeFound) {
                    jsonObject.accumulate(string, config.isKeepStrings() ? (String)token : XML.stringToValue((String)token));
                }
                token = null;
                continue;
            }
            jsonObject.accumulate(string, "");
        }
        if (token == SLASH) {
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped tag");
            }
            if (config.getForceList().contains(tagName)) {
                if (nilAttributeFound) {
                    context.append(tagName, JSONObject.NULL);
                } else if (jsonObject.length() > 0) {
                    context.append(tagName, jsonObject);
                } else {
                    context.put(tagName, new JSONArray());
                }
            } else if (nilAttributeFound) {
                context.accumulate(tagName, JSONObject.NULL);
            } else if (jsonObject.length() > 0) {
                context.accumulate(tagName, jsonObject);
            } else {
                context.accumulate(tagName, "");
            }
            return false;
        }
        if (token == GT) {
            while (true) {
                if ((token = x.nextContent()) == null) {
                    if (tagName != null) {
                        throw x.syntaxError("Unclosed tag " + tagName);
                    }
                    return false;
                }
                if (token instanceof String) {
                    string = (String)token;
                    if (string.length() <= 0) continue;
                    if (xmlXsiTypeConverter != null) {
                        jsonObject.accumulate(config.getcDataTagName(), XML.stringToValue(string, xmlXsiTypeConverter));
                        continue;
                    }
                    jsonObject.accumulate(config.getcDataTagName(), config.isKeepStrings() ? string : XML.stringToValue(string));
                    continue;
                }
                if (token != LT) continue;
                if (currentNestingDepth == config.getMaxNestingDepth()) {
                    throw x.syntaxError("Maximum nesting depth of " + config.getMaxNestingDepth() + " reached");
                }
                if (XML.parse(x, jsonObject, tagName, config, currentNestingDepth + 1)) break;
            }
            if (config.getForceList().contains(tagName)) {
                if (jsonObject.length() == 0) {
                    context.put(tagName, new JSONArray());
                } else if (jsonObject.length() == 1 && jsonObject.opt(config.getcDataTagName()) != null) {
                    context.append(tagName, jsonObject.opt(config.getcDataTagName()));
                } else {
                    context.append(tagName, jsonObject);
                }
            } else if (jsonObject.length() == 0) {
                context.accumulate(tagName, "");
            } else if (jsonObject.length() == 1 && jsonObject.opt(config.getcDataTagName()) != null) {
                context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
            } else {
                if (!config.shouldTrimWhiteSpace()) {
                    XML.removeEmpty(jsonObject, config);
                }
                context.accumulate(tagName, jsonObject);
            }
            return false;
        }
        throw x.syntaxError("Misshaped tag");
    }

    private static void removeEmpty(JSONObject jsonObject, XMLParserConfiguration config) {
        if (jsonObject.has(config.getcDataTagName())) {
            Object s = jsonObject.get(config.getcDataTagName());
            if (s instanceof String) {
                if (XML.isStringAllWhiteSpace(s.toString())) {
                    jsonObject.remove(config.getcDataTagName());
                }
            } else if (s instanceof JSONArray) {
                JSONArray sArray = (JSONArray)s;
                for (int k = sArray.length() - 1; k >= 0; --k) {
                    String s1;
                    Object eachString = sArray.get(k);
                    if (!(eachString instanceof String) || !XML.isStringAllWhiteSpace(s1 = (String)eachString)) continue;
                    sArray.remove(k);
                }
                if (sArray.isEmpty()) {
                    jsonObject.remove(config.getcDataTagName());
                }
            }
        }
    }

    private static boolean isStringAllWhiteSpace(String s) {
        for (int k = 0; k < s.length(); ++k) {
            char eachChar = s.charAt(k);
            if (Character.isWhitespace(eachChar)) continue;
            return false;
        }
        return true;
    }

    public static Object stringToValue(String string, XMLXsiTypeConverter<?> typeConverter) {
        if (typeConverter != null) {
            return typeConverter.convert(string);
        }
        return XML.stringToValue(string);
    }

    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }
        if (NumberConversionUtil.potentialNumber(string)) {
            try {
                return NumberConversionUtil.stringToNumber(string);
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return string;
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        return XML.toJSONObject(string, XMLParserConfiguration.ORIGINAL);
    }

    public static JSONObject toJSONObject(Reader reader) throws JSONException {
        return XML.toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }

    public static JSONObject toJSONObject(Reader reader, boolean keepStrings) throws JSONException {
        if (keepStrings) {
            return XML.toJSONObject(reader, XMLParserConfiguration.KEEP_STRINGS);
        }
        return XML.toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }

    public static JSONObject toJSONObject(Reader reader, XMLParserConfiguration config) throws JSONException {
        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader, config);
        while (x.more()) {
            x.skipPast("<");
            if (!x.more()) continue;
            XML.parse(x, jo, null, config, 0);
        }
        return jo;
    }

    public static JSONObject toJSONObject(String string, boolean keepStrings) throws JSONException {
        return XML.toJSONObject((Reader)new StringReader(string), keepStrings);
    }

    public static JSONObject toJSONObject(String string, XMLParserConfiguration config) throws JSONException {
        return XML.toJSONObject((Reader)new StringReader(string), config);
    }

    public static String toString(Object object) throws JSONException {
        return XML.toString(object, null, XMLParserConfiguration.ORIGINAL);
    }

    public static String toString(Object object, String tagName) {
        return XML.toString(object, tagName, XMLParserConfiguration.ORIGINAL);
    }

    public static String toString(Object object, String tagName, XMLParserConfiguration config) throws JSONException {
        return XML.toString(object, tagName, config, 0, 0);
    }

    private static String toString(Object object, String tagName, XMLParserConfiguration config, int indentFactor, int indent) throws JSONException {
        String indentationSuffix;
        StringBuilder sb = new StringBuilder();
        if (object instanceof JSONObject) {
            if (tagName != null) {
                sb.append(XML.indent(indent));
                sb.append('<');
                sb.append(tagName);
                sb.append('>');
                if (indentFactor > 0) {
                    sb.append("\n");
                    indent += indentFactor;
                }
            }
            JSONObject jo = (JSONObject)object;
            for (String key : jo.keySet()) {
                Object val2;
                int i;
                int jaLength;
                JSONArray ja;
                Object value = jo.opt(key);
                if (value == null) {
                    value = "";
                } else if (value.getClass().isArray()) {
                    value = new JSONArray(value);
                }
                if (key.equals(config.getcDataTagName())) {
                    if (value instanceof JSONArray) {
                        ja = (JSONArray)value;
                        jaLength = ja.length();
                        for (i = 0; i < jaLength; ++i) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            val2 = ja.opt(i);
                            sb.append(XML.escape(val2.toString()));
                        }
                        continue;
                    }
                    sb.append(XML.escape(value.toString()));
                    continue;
                }
                if (value instanceof JSONArray) {
                    ja = (JSONArray)value;
                    jaLength = ja.length();
                    for (i = 0; i < jaLength; ++i) {
                        val2 = ja.opt(i);
                        if (val2 instanceof JSONArray) {
                            sb.append('<');
                            sb.append(key);
                            sb.append('>');
                            sb.append(XML.toString(val2, null, config, indentFactor, indent));
                            sb.append("</");
                            sb.append(key);
                            sb.append('>');
                            continue;
                        }
                        sb.append(XML.toString(val2, key, config, indentFactor, indent));
                    }
                    continue;
                }
                if ("".equals(value)) {
                    if (config.isCloseEmptyTag()) {
                        sb.append(XML.indent(indent));
                        sb.append('<');
                        sb.append(key);
                        sb.append(">");
                        sb.append("</");
                        sb.append(key);
                        sb.append(">");
                        if (indentFactor <= 0) continue;
                        sb.append("\n");
                        continue;
                    }
                    sb.append(XML.indent(indent));
                    sb.append('<');
                    sb.append(key);
                    sb.append("/>");
                    if (indentFactor <= 0) continue;
                    sb.append("\n");
                    continue;
                }
                sb.append(XML.toString(value, key, config, indentFactor, indent));
            }
            if (tagName != null) {
                sb.append(XML.indent(indent - indentFactor));
                sb.append("</");
                sb.append(tagName);
                sb.append('>');
                if (indentFactor > 0) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        if (object != null && (object instanceof JSONArray || object.getClass().isArray())) {
            JSONArray ja = object.getClass().isArray() ? new JSONArray(object) : (JSONArray)object;
            int jaLength = ja.length();
            for (int i = 0; i < jaLength; ++i) {
                Object val3 = ja.opt(i);
                sb.append(XML.toString(val3, tagName == null ? "array" : tagName, config, indentFactor, indent));
            }
            return sb.toString();
        }
        String string = object == null ? "null" : XML.escape(object.toString());
        String string2 = indentationSuffix = indentFactor > 0 ? "\n" : "";
        if (tagName == null) {
            return XML.indent(indent) + "\"" + string + "\"" + indentationSuffix;
        }
        if (string.length() == 0) {
            return XML.indent(indent) + "<" + tagName + "/>" + indentationSuffix;
        }
        return XML.indent(indent) + "<" + tagName + ">" + string + "</" + tagName + ">" + indentationSuffix;
    }

    public static String toString(Object object, int indentFactor) {
        return XML.toString(object, null, XMLParserConfiguration.ORIGINAL, indentFactor);
    }

    public static String toString(Object object, String tagName, int indentFactor) {
        return XML.toString(object, tagName, XMLParserConfiguration.ORIGINAL, indentFactor);
    }

    public static String toString(Object object, String tagName, XMLParserConfiguration config, int indentFactor) throws JSONException {
        return XML.toString(object, tagName, config, indentFactor, 0);
    }

    private static final String indent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }
}

