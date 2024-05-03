/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatCaret;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

public class FlatEditorPaneUI
extends BasicEditorPaneUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected int minimumWidth;
    private Color background;
    @FlatStylingSupport.Styleable
    protected Color disabledBackground;
    @FlatStylingSupport.Styleable
    protected Color inactiveBackground;
    @FlatStylingSupport.Styleable
    protected Color focusedBackground;
    private Color oldDisabledBackground;
    private Color oldInactiveBackground;
    private Insets defaultMargin;
    private Object oldHonorDisplayProperties;
    private FocusListener focusListener;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatEditorPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        String prefix = this.getPropertyPrefix();
        this.minimumWidth = UIManager.getInt("Component.minimumWidth");
        this.background = UIManager.getColor(prefix + ".background");
        this.disabledBackground = UIManager.getColor(prefix + ".disabledBackground");
        this.inactiveBackground = UIManager.getColor(prefix + ".inactiveBackground");
        this.focusedBackground = UIManager.getColor(prefix + ".focusedBackground");
        this.defaultMargin = UIManager.getInsets(prefix + ".margin");
        this.oldHonorDisplayProperties = this.getComponent().getClientProperty("JEditorPane.honorDisplayProperties");
        this.getComponent().putClientProperty("JEditorPane.honorDisplayProperties", true);
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.background = null;
        this.disabledBackground = null;
        this.inactiveBackground = null;
        this.focusedBackground = null;
        this.oldDisabledBackground = null;
        this.oldInactiveBackground = null;
        this.oldStyleValues = null;
        this.getComponent().putClientProperty("JEditorPane.honorDisplayProperties", this.oldHonorDisplayProperties);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.focusListener = new FlatUIUtils.RepaintFocusListener(this.getComponent(), c -> this.focusedBackground != null);
        this.getComponent().addFocusListener(this.focusListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.getComponent().removeFocusListener(this.focusListener);
        this.focusListener = null;
    }

    @Override
    protected Caret createCaret() {
        return new FlatCaret(null, false);
    }

    @Override
    protected void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if ("editable".equals(propertyName) || "enabled".equals(propertyName)) {
            this.updateBackground();
        }
        super.propertyChange(e);
        FlatEditorPaneUI.propertyChange(this.getComponent(), e, this::installStyle);
    }

    static void propertyChange(JTextComponent c, PropertyChangeEvent e, Runnable installStyle) {
        switch (e.getPropertyName()) {
            case "JComponent.minimumWidth": {
                c.revalidate();
                break;
            }
            case "FlatLaf.style": 
            case "FlatLaf.styleClass": {
                installStyle.run();
                c.revalidate();
                c.repaint();
            }
        }
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.getComponent(), "EditorPane"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldDisabledBackground = this.disabledBackground;
        this.oldInactiveBackground = this.inactiveBackground;
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        this.updateBackground();
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.getComponent(), key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    private void updateBackground() {
        FlatTextFieldUI.updateBackground(this.getComponent(), this.background, this.disabledBackground, this.inactiveBackground, this.oldDisabledBackground, this.oldInactiveBackground);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return FlatEditorPaneUI.applyMinimumWidth(c, super.getPreferredSize(c), this.minimumWidth, this.defaultMargin);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return FlatEditorPaneUI.applyMinimumWidth(c, super.getMinimumSize(c), this.minimumWidth, this.defaultMargin);
    }

    static Dimension applyMinimumWidth(JComponent c, Dimension size, int minimumWidth, Insets defaultMargin) {
        if (!FlatTextFieldUI.hasDefaultMargins(c, defaultMargin)) {
            return size;
        }
        minimumWidth = FlatUIUtils.minimumWidth(c, minimumWidth);
        size.width = Math.max(size.width, UIScale.scale(minimumWidth) - UIScale.scale(1) * 2);
        return size;
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(HiDPIUtils.createGraphicsTextYCorrection((Graphics2D)g));
    }

    @Override
    protected void paintBackground(Graphics g) {
        FlatEditorPaneUI.paintBackground(g, this.getComponent(), this.focusedBackground);
    }

    static void paintBackground(Graphics g, JTextComponent c, Color focusedBackground) {
        g.setColor(FlatTextFieldUI.getBackground(c, focusedBackground));
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
}

