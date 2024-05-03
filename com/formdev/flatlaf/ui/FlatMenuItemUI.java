/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatMenuItemRenderer;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

@FlatStylingSupport.StyleableFields(value={@FlatStylingSupport.StyleableField(cls=BasicMenuItemUI.class, key="selectionBackground"), @FlatStylingSupport.StyleableField(cls=BasicMenuItemUI.class, key="selectionForeground"), @FlatStylingSupport.StyleableField(cls=BasicMenuItemUI.class, key="disabledForeground"), @FlatStylingSupport.StyleableField(cls=BasicMenuItemUI.class, key="acceleratorForeground"), @FlatStylingSupport.StyleableField(cls=BasicMenuItemUI.class, key="acceleratorSelectionForeground")})
public class FlatMenuItemUI
extends BasicMenuItemUI
implements FlatStylingSupport.StyleableUI,
FlatStylingSupport.StyleableLookupProvider {
    private FlatMenuItemRenderer renderer;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatMenuItemUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        LookAndFeel.installProperty(this.menuItem, "iconTextGap", FlatUIUtils.getUIInt("MenuItem.iconTextGap", 4));
        this.renderer = this.createRenderer();
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        FlatMenuItemRenderer.clearClientProperties(this.menuItem.getParent());
        this.renderer = null;
        this.oldStyleValues = null;
    }

    protected FlatMenuItemRenderer createRenderer() {
        return new FlatMenuItemRenderer(this.menuItem, this.checkIcon, this.arrowIcon, this.acceleratorFont, this.acceleratorDelimiter);
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener(JComponent c) {
        return FlatStylingSupport.createPropertyChangeListener(c, this::installStyle, super.createPropertyChangeListener(c));
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.menuItem, "MenuItem"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatMenuItemUI.applyStyleProperty(this.menuItem, this, this.renderer, key, value);
    }

    static Object applyStyleProperty(JMenuItem menuItem, BasicMenuItemUI ui, FlatMenuItemRenderer renderer, String key, Object value) {
        try {
            return renderer.applyStyleProperty(key, value);
        } catch (FlatStylingSupport.UnknownStyleException unknownStyleException) {
            return FlatStylingSupport.applyToAnnotatedObjectOrComponent(ui, menuItem, key, value);
        }
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatMenuItemUI.getStyleableInfos(this, this.renderer);
    }

    static Map<String, Class<?>> getStyleableInfos(BasicMenuItemUI ui, FlatMenuItemRenderer renderer) {
        Map<String, Class<?>> infos = FlatStylingSupport.getAnnotatedStyleableInfos(ui);
        infos.putAll(renderer.getStyleableInfos());
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatMenuItemUI.getStyleableValue(this, this.renderer, key);
    }

    static Object getStyleableValue(BasicMenuItemUI ui, FlatMenuItemRenderer renderer, String key) {
        Object value = renderer.getStyleableValue(key);
        if (value == null) {
            value = FlatStylingSupport.getAnnotatedStyleableValue(ui, key);
        }
        return value;
    }

    @Override
    public MethodHandles.Lookup getLookupForStyling() {
        return MethodHandles.lookup();
    }

    @Override
    protected Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {
        return this.renderer.getPreferredMenuItemSize();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        this.renderer.paintMenuItem(g, this.selectionBackground, this.selectionForeground, this.disabledForeground, this.acceleratorForeground, this.acceleratorSelectionForeground);
    }
}

