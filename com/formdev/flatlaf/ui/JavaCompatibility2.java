/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;

public class JavaCompatibility2 {
    private static boolean getUIMethodInitialized;
    private static MethodHandle getUIMethod;

    public static ComponentUI getUI(JComponent c) {
        try {
            if (SystemInfo.isJava_9_orLater) {
                if (!getUIMethodInitialized) {
                    getUIMethodInitialized = true;
                    try {
                        MethodType mt = MethodType.methodType(ComponentUI.class, new Class[0]);
                        getUIMethod = MethodHandles.publicLookup().findVirtual(JComponent.class, "getUI", mt);
                    } catch (Exception ex) {
                        LoggingFacade.INSTANCE.logSevere(null, ex);
                    }
                }
                if (getUIMethod != null) {
                    return getUIMethod.invoke(c);
                }
            }
            if (c instanceof JPanel) {
                return ((JPanel)c).getUI();
            }
            if (c instanceof JList) {
                return ((JList)c).getUI();
            }
            if (c instanceof JTable) {
                return ((JTable)c).getUI();
            }
            if (c instanceof JTree) {
                return ((JTree)c).getUI();
            }
            if (c instanceof JTextComponent) {
                return ((JTextComponent)c).getUI();
            }
            Method m = c.getClass().getMethod("getUI", new Class[0]);
            return (ComponentUI)m.invoke(c, new Object[0]);
        } catch (Throwable ex) {
            return null;
        }
    }
}

