/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Config;
import net.optifine.reflect.IFieldLocator;
import net.optifine.reflect.ReflectorRaw;

public class FieldLocatorActionKeyF3
implements IFieldLocator {
    @Override
    public Field getField() {
        Class<Minecraft> oclass = Minecraft.class;
        Field field = this.getFieldRenderChunksMany();
        if (field == null) {
            Config.log("(Reflector) Field not present: " + oclass.getName() + ".actionKeyF3 (field renderChunksMany not found)");
            return null;
        }
        Field field1 = ReflectorRaw.getFieldAfter(Minecraft.class, field, Boolean.TYPE, 0);
        if (field1 == null) {
            Config.log("(Reflector) Field not present: " + oclass.getName() + ".actionKeyF3");
            return null;
        }
        return field1;
    }

    private Field getFieldRenderChunksMany() {
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean flag = minecraft.renderChunksMany;
        Field[] afield = Minecraft.class.getDeclaredFields();
        minecraft.renderChunksMany = true;
        Field[] afield1 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.TRUE);
        minecraft.renderChunksMany = false;
        Field[] afield2 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.FALSE);
        minecraft.renderChunksMany = flag;
        HashSet<Field> set = new HashSet<Field>(Arrays.asList(afield1));
        HashSet<Field> set1 = new HashSet<Field>(Arrays.asList(afield2));
        HashSet<Field> set2 = new HashSet<Field>(set);
        set2.retainAll(set1);
        Field[] afield3 = set2.toArray(new Field[set2.size()]);
        return afield3.length != 1 ? null : afield3[0];
    }
}

