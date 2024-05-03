/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders.config;

import java.util.ArrayList;
import net.optifine.Lang;
import net.optifine.shaders.ShaderUtils;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderProfile;

public class ShaderOptionProfile
extends ShaderOption {
    private ShaderProfile[] profiles = null;
    private ShaderOption[] options = null;
    private static final String NAME_PROFILE = "<profile>";
    private static final String VALUE_CUSTOM = "<custom>";

    public ShaderOptionProfile(ShaderProfile[] profiles, ShaderOption[] options) {
        super(NAME_PROFILE, "", ShaderOptionProfile.detectProfileName(profiles, options), ShaderOptionProfile.getProfileNames(profiles), ShaderOptionProfile.detectProfileName(profiles, options, true), null);
        this.profiles = profiles;
        this.options = options;
    }

    @Override
    public void nextValue() {
        super.nextValue();
        if (this.getValue().equals(VALUE_CUSTOM)) {
            super.nextValue();
        }
        this.applyProfileOptions();
    }

    public void updateProfile() {
        ShaderProfile shaderprofile = this.getProfile(this.getValue());
        if (shaderprofile == null || !ShaderUtils.matchProfile(shaderprofile, this.options, false)) {
            String s = ShaderOptionProfile.detectProfileName(this.profiles, this.options);
            this.setValue(s);
        }
    }

    private void applyProfileOptions() {
        ShaderProfile shaderprofile = this.getProfile(this.getValue());
        if (shaderprofile != null) {
            String[] astring = shaderprofile.getOptions();
            for (int i = 0; i < astring.length; ++i) {
                String s = astring[i];
                ShaderOption shaderoption = this.getOption(s);
                if (shaderoption == null) continue;
                String s1 = shaderprofile.getValue(s);
                shaderoption.setValue(s1);
            }
        }
    }

    private ShaderOption getOption(String name) {
        for (int i = 0; i < this.options.length; ++i) {
            ShaderOption shaderoption = this.options[i];
            if (!shaderoption.getName().equals(name)) continue;
            return shaderoption;
        }
        return null;
    }

    private ShaderProfile getProfile(String name) {
        for (int i = 0; i < this.profiles.length; ++i) {
            ShaderProfile shaderprofile = this.profiles[i];
            if (!shaderprofile.getName().equals(name)) continue;
            return shaderprofile;
        }
        return null;
    }

    @Override
    public String getNameText() {
        return Lang.get("of.shaders.profile");
    }

    @Override
    public String getValueText(String val2) {
        return val2.equals(VALUE_CUSTOM) ? Lang.get("of.general.custom", VALUE_CUSTOM) : Shaders.translate("profile." + val2, val2);
    }

    @Override
    public String getValueColor(String val2) {
        return val2.equals(VALUE_CUSTOM) ? "\u00a7c" : "\u00a7a";
    }

    @Override
    public String getDescriptionText() {
        String s = Shaders.translate("profile.comment", null);
        if (s != null) {
            return s;
        }
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < this.profiles.length; ++i) {
            String s2;
            String s1 = this.profiles[i].getName();
            if (s1 == null || (s2 = Shaders.translate("profile." + s1 + ".comment", null)) == null) continue;
            stringbuffer.append(s2);
            if (s2.endsWith(". ")) continue;
            stringbuffer.append(". ");
        }
        return stringbuffer.toString();
    }

    private static String detectProfileName(ShaderProfile[] profs, ShaderOption[] opts) {
        return ShaderOptionProfile.detectProfileName(profs, opts, false);
    }

    private static String detectProfileName(ShaderProfile[] profs, ShaderOption[] opts, boolean def) {
        ShaderProfile shaderprofile = ShaderUtils.detectProfile(profs, opts, def);
        return shaderprofile == null ? VALUE_CUSTOM : shaderprofile.getName();
    }

    private static String[] getProfileNames(ShaderProfile[] profs) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < profs.length; ++i) {
            ShaderProfile shaderprofile = profs[i];
            list.add(shaderprofile.getName());
        }
        list.add(VALUE_CUSTOM);
        String[] astring = list.toArray(new String[list.size()]);
        return astring;
    }
}

