/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation.glowesp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GlowESPFIleUtils {
    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

