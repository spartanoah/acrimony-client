/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import javax.swing.JComponent;

public class JavaCompatibility {
    private static MethodHandle drawStringUnderlineCharAtMethod;
    private static MethodHandle getClippedStringMethod;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, int x, int y) {
        Class<JavaCompatibility> clazz = JavaCompatibility.class;
        synchronized (JavaCompatibility.class) {
            if (drawStringUnderlineCharAtMethod == null) {
                try {
                    Class[] classArray;
                    Class<?> cls = Class.forName(SystemInfo.isJava_9_orLater ? "javax.swing.plaf.basic.BasicGraphicsUtils" : "sun.swing.SwingUtilities2");
                    if (SystemInfo.isJava_9_orLater) {
                        Class[] classArray2 = new Class[6];
                        classArray2[0] = JComponent.class;
                        classArray2[1] = Graphics2D.class;
                        classArray2[2] = String.class;
                        classArray2[3] = Integer.TYPE;
                        classArray2[4] = Float.TYPE;
                        classArray = classArray2;
                        classArray2[5] = Float.TYPE;
                    } else {
                        Class[] classArray3 = new Class[6];
                        classArray3[0] = JComponent.class;
                        classArray3[1] = Graphics.class;
                        classArray3[2] = String.class;
                        classArray3[3] = Integer.TYPE;
                        classArray3[4] = Integer.TYPE;
                        classArray = classArray3;
                        classArray3[5] = Integer.TYPE;
                    }
                    MethodType mt = MethodType.methodType(Void.TYPE, classArray);
                    drawStringUnderlineCharAtMethod = MethodHandles.publicLookup().findStatic(cls, "drawStringUnderlineCharAt", mt);
                } catch (Exception ex) {
                    LoggingFacade.INSTANCE.logSevere(null, ex);
                    throw new RuntimeException(ex);
                }
            }
            // ** MonitorExit[var6_6] (shouldn't be in output)
            try {
                if (SystemInfo.isJava_9_orLater) {
                    drawStringUnderlineCharAtMethod.invoke(c, (Graphics2D)g, text, underlinedIndex, x, y);
                } else {
                    drawStringUnderlineCharAtMethod.invoke(c, g, text, underlinedIndex, x, y);
                }
            } catch (Throwable ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                throw new RuntimeException(ex);
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getClippedString(JComponent c, FontMetrics fm, String string, int availTextWidth) {
        Class<JavaCompatibility> clazz = JavaCompatibility.class;
        synchronized (JavaCompatibility.class) {
            if (getClippedStringMethod == null) {
                try {
                    Class<?> cls = Class.forName(SystemInfo.isJava_9_orLater ? "javax.swing.plaf.basic.BasicGraphicsUtils" : "sun.swing.SwingUtilities2");
                    MethodType mt = MethodType.methodType(String.class, JComponent.class, FontMetrics.class, String.class, Integer.TYPE);
                    getClippedStringMethod = MethodHandles.publicLookup().findStatic(cls, SystemInfo.isJava_9_orLater ? "getClippedString" : "clipStringIfNecessary", mt);
                } catch (Exception ex) {
                    LoggingFacade.INSTANCE.logSevere(null, ex);
                    throw new RuntimeException(ex);
                }
            }
            // ** MonitorExit[var4_4] (shouldn't be in output)
            try {
                return getClippedStringMethod.invoke(c, fm, string, availTextWidth);
            } catch (Throwable ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
                throw new RuntimeException(ex);
            }
        }
    }
}

