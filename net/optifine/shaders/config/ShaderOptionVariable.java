/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders.config;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.util.StrUtils;

public class ShaderOptionVariable
extends ShaderOption {
    private static final Pattern PATTERN_VARIABLE = Pattern.compile("^\\s*#define\\s+(\\w+)\\s+(-?[0-9\\.Ff]+|\\w+)\\s*(//.*)?$");

    public ShaderOptionVariable(String name, String description, String value, String[] values, String path) {
        super(name, description, value, values, value, path);
        this.setVisible(this.getValues().length > 1);
    }

    @Override
    public String getSourceLine() {
        return "#define " + this.getName() + " " + this.getValue() + " // Shader option " + this.getValue();
    }

    @Override
    public String getValueText(String val2) {
        String s = Shaders.translate("prefix." + this.getName(), "");
        String s1 = super.getValueText(val2);
        String s2 = Shaders.translate("suffix." + this.getName(), "");
        String s3 = s + s1 + s2;
        return s3;
    }

    @Override
    public String getValueColor(String val2) {
        String s = val2.toLowerCase();
        return !s.equals("false") && !s.equals("off") ? "\u00a7a" : "\u00a7c";
    }

    @Override
    public boolean matchesLine(String line) {
        Matcher matcher = PATTERN_VARIABLE.matcher(line);
        if (!matcher.matches()) {
            return false;
        }
        String s = matcher.group(1);
        return s.matches(this.getName());
    }

    public static ShaderOption parseOption(String line, String path) {
        Matcher matcher = PATTERN_VARIABLE.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        String s = matcher.group(1);
        String s1 = matcher.group(2);
        String s2 = matcher.group(3);
        String s3 = StrUtils.getSegment(s2, "[", "]");
        if (s3 != null && s3.length() > 0) {
            s2 = s2.replace(s3, "").trim();
        }
        String[] astring = ShaderOptionVariable.parseValues(s1, s3);
        if (s != null && s.length() > 0) {
            path = StrUtils.removePrefix(path, "/shaders/");
            ShaderOptionVariable shaderoption = new ShaderOptionVariable(s, s2, s1, astring, path);
            return shaderoption;
        }
        return null;
    }

    public static String[] parseValues(String value, String valuesStr) {
        String[] astring = new String[]{value};
        if (valuesStr == null) {
            return astring;
        }
        valuesStr = valuesStr.trim();
        valuesStr = StrUtils.removePrefix(valuesStr, "[");
        valuesStr = StrUtils.removeSuffix(valuesStr, "]");
        if ((valuesStr = valuesStr.trim()).length() <= 0) {
            return astring;
        }
        Object[] astring1 = Config.tokenize(valuesStr, " ");
        if (astring1.length <= 0) {
            return astring;
        }
        if (!Arrays.asList(astring1).contains(value)) {
            astring1 = (String[])Config.addObjectToArray(astring1, value, 0);
        }
        return astring1;
    }
}

