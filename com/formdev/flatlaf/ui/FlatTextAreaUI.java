/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatCaret;
import com.formdev.flatlaf.ui.FlatEditorPaneUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.Caret;

public class FlatTextAreaUI
extends BasicTextAreaUI
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
    private FocusListener focusListener;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTextAreaUI();
    }

    @Override
    public void installUI(JComponent c) {
        if (FlatUIUtils.needsLightAWTPeer(c)) {
            FlatUIUtils.runWithLightAWTPeerUIDefaults(() -> this.installUIImpl(c));
        } else {
            this.installUIImpl(c);
        }
    }

    private void installUIImpl(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.minimumWidth = UIManager.getInt("Component.minimumWidth");
        this.background = UIManager.getColor("TextArea.background");
        this.disabledBackground = UIManager.getColor("TextArea.disabledBackground");
        this.inactiveBackground = UIManager.getColor("TextArea.inactiveBackground");
        this.focusedBackground = UIManager.getColor("TextArea.focusedBackground");
        this.defaultMargin = UIManager.getInsets("TextArea.margin");
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

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.getComponent(), "TextArea"));
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
        return this.applyMinimumWidth(c, super.getPreferredSize(c));
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return this.applyMinimumWidth(c, super.getMinimumSize(c));
    }

    private Dimension applyMinimumWidth(JComponent c, Dimension size) {
        if (c instanceof JTextArea && ((JTextArea)c).getColumns() > 0) {
            return size;
        }
        return FlatEditorPaneUI.applyMinimumWidth(c, size, this.minimumWidth, this.defaultMargin);
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(HiDPIUtils.createGraphicsTextYCorrection((Graphics2D)g));
    }

    @Override
    protected void paintBackground(Graphics g) {
        FlatEditorPaneUI.paintBackground(g, this.getComponent(), this.focusedBackground);
    }
}

