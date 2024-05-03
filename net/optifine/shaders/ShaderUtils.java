/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders;

import net.minecraft.src.Config;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderProfile;

public class ShaderUtils {
    public static ShaderOption getShaderOption(String name, ShaderOption[] opts) {
        if (opts == null) {
            return null;
        }
        for (int i = 0; i < opts.length; ++i) {
            ShaderOption shaderoption = opts[i];
            if (!shaderoption.getName().equals(name)) continue;
            return shaderoption;
        }
        return null;
    }

    public static ShaderProfile detectProfile(ShaderProfile[] profs, ShaderOption[] opts, boolean def) {
        if (profs == null) {
            return null;
        }
        for (int i = 0; i < profs.length; ++i) {
            ShaderProfile shaderprofile = profs[i];
            if (!ShaderUtils.matchProfile(shaderprofile, opts, def)) continue;
            return shaderprofile;
        }
        return null;
    }

    public static boolean matchProfile(ShaderProfile prof, ShaderOption[] opts, boolean def) {
        if (prof == null) {
            return false;
        }
        if (opts == null) {
            return false;
        }
        String[] astring = prof.getOptions();
        for (int i = 0; i < astring.length; ++i) {
            String s2;
            String s1;
            String s = astring[i];
            ShaderOption shaderoption = ShaderUtils.getShaderOption(s, opts);
            if (shaderoption == null || Config.equals(s1 = def ? shaderoption.getValueDefault() : shaderoption.getValue(), s2 = prof.getValue(s))) continue;
            return false;
        }
        return true;
    }
}

