/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class XPMFile {
    private byte[] bytes;
    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;
    private static final int NUMBER_OF_COLORS = 2;
    private static final int CHARACTERS_PER_PIXEL = 3;
    private static int[] format = new int[4];

    private XPMFile() {
    }

    public static XPMFile load(String file) throws IOException {
        return XPMFile.load(new FileInputStream(new File(file)));
    }

    public static XPMFile load(InputStream is) {
        XPMFile xFile = new XPMFile();
        xFile.readImage(is);
        return xFile;
    }

    public int getHeight() {
        return format[1];
    }

    public int getWidth() {
        return format[0];
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    private void readImage(InputStream is) {
        try {
            int i;
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
            HashMap<String, Integer> colors = new HashMap<String, Integer>();
            format = XPMFile.parseFormat(XPMFile.nextLineOfInterest(reader));
            for (i = 0; i < format[2]; ++i) {
                Object[] colorDefinition = XPMFile.parseColor(XPMFile.nextLineOfInterest(reader));
                colors.put((String)colorDefinition[0], (Integer)colorDefinition[1]);
            }
            this.bytes = new byte[format[0] * format[1] * 4];
            for (i = 0; i < format[1]; ++i) {
                this.parseImageLine(XPMFile.nextLineOfInterest(reader), format, colors, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to parse XPM File");
        }
    }

    private static String nextLineOfInterest(LineNumberReader reader) throws IOException {
        String ret;
        while (!(ret = reader.readLine()).startsWith("\"")) {
        }
        return ret.substring(1, ret.lastIndexOf(34));
    }

    private static int[] parseFormat(String format) {
        StringTokenizer st = new StringTokenizer(format);
        return new int[]{Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())};
    }

    private static Object[] parseColor(String line) {
        String key = line.substring(0, format[3]);
        String color = line.substring(format[3] + 4);
        return new Object[]{key, Integer.parseInt(color, 16)};
    }

    private void parseImageLine(String line, int[] format, HashMap<String, Integer> colors, int index) {
        int offset = index * 4 * format[0];
        for (int i = 0; i < format[0]; ++i) {
            String key = line.substring(i * format[3], i * format[3] + format[3]);
            int color = colors.get(key);
            this.bytes[offset + i * 4] = (byte)((color & 0xFF0000) >> 16);
            this.bytes[offset + (i * 4 + 1)] = (byte)((color & 0xFF00) >> 8);
            this.bytes[offset + (i * 4 + 2)] = (byte)((color & 0xFF) >> 0);
            this.bytes[offset + (i * 4 + 3)] = -1;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage:\nXPMFile <file>");
        }
        try {
            String out = args[0].substring(0, args[0].indexOf(".")) + ".raw";
            XPMFile file = XPMFile.load(args[0]);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(out)));
            bos.write(file.getBytes());
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

