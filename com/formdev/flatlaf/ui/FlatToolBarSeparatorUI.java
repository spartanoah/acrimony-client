/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatToolBarUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;

public class FlatToolBarSeparatorUI
extends BasicToolBarSeparatorUI
implements FlatStylingSupport.StyleableUI,
PropertyChangeListener {
    private static final int LINE_WIDTH = 1;
    @FlatStylingSupport.Styleable
    protected int separatorWidth;
    @FlatStylingSupport.Styleable
    protected Color separatorColor;
    private final boolean shared;
    private boolean defaults_initialized = false;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatToolBarSeparatorUI.class, () -> new FlatToolBarSeparatorUI(true)) : new FlatToolBarSeparatorUI(false);
    }

    protected FlatToolBarSeparatorUI(boolean shared) {
        this.shared = shared;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle((JSeparator)c);
    }

    @Override
    protected void installDefaults(JSeparator c) {
        super.installDefaults(c);
        if (!this.defaults_initialized) {
            this.separatorWidth = UIManager.getInt("ToolBar.separatorWidth");
            this.separatorColor = UIManager.getColor("ToolBar.separatorColor");
            this.defaults_initialized = true;
        }
        c.setAlignmentX(0.0f);
    }

    @Override
    protected void uninstallDefaults(JSeparator s) {
        super.uninstallDefaults(s);
        this.defaults_initialized = false;
        this.oldStyleValues = null;
    }

    @Override
    protected void installListeners(JSeparator s) {
        super.installListeners(s);
        s.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(JSeparator s) {
        super.uninstallListeners(s);
        s.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "FlatLaf.style": 
            case "FlatLaf.styleClass": {
                JSeparator s = (JSeparator)e.getSource();
                if (this.shared && FlatStylingSupport.hasStyleProperty(s)) {
                    s.updateUI();
                } else {
                    this.installStyle(s);
                }
                s.revalidate();
                s.repaint();
            }
        }
    }

    protected void installStyle(JSeparator s) {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(s, "ToolBarSeparator"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = ((JToolBar.Separator)c).getSeparatorSize();
        if (size != null) {
            return UIScale.scale(size);
        }
        int separatorWidth = this.separatorWidth;
        FlatToolBarUI toolBarUI = this.getToolBarUI(c);
        if (toolBarUI != null && toolBarUI.separatorWidth != null) {
            separatorWidth = toolBarUI.separatorWidth;
        }
        int sepWidth = UIScale.scale((separatorWidth - 1) / 2) * 2 + UIScale.scale(1);
        boolean vertical = this.isVertical(c);
        return new Dimension(vertical ? sepWidth : 0, vertical ? 0 : sepWidth);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension size = this.getPreferredSize(c);
        if (this.isVertical(c)) {
            return new Dimension(size.width, Short.MAX_VALUE);
        }
        return new Dimension(Short.MAX_VALUE, size.height);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int width = c.getWidth();
        int height = c.getHeight();
        float lineWidth = UIScale.scale(1.0f);
        float offset = UIScale.scale(2.0f);
        Color separatorColor = this.separatorColor;
        FlatToolBarUI toolBarUI = this.getToolBarUI(c);
        if (toolBarUI != null && toolBarUI.separatorColor != null) {
            separatorColor = toolBarUI.separatorColor;
        }
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        g.setColor(separatorColor);
        if (this.isVertical(c)) {
            ((Graphics2D)g).fill(new Rectangle2D.Float(Math.round(((float)width - lineWidth) / 2.0f), offset, lineWidth, (float)height - offset * 2.0f));
        } else {
            ((Graphics2D)g).fill(new Rectangle2D.Float(offset, Math.round(((float)height - lineWidth) / 2.0f), (float)width - offset * 2.0f, lineWidth));
        }
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }

    private boolean isVertical(JComponent c) {
        return ((JToolBar.Separator)c).getOrientation() == 1;
    }

    private FlatToolBarUI getToolBarUI(JComponent c) {
        Container parent = c.getParent();
        return parent instanceof JToolBar && ((JToolBar)parent).getUI() instanceof FlatToolBarUI ? (FlatToolBarUI)((JToolBar)parent).getUI() : null;
    }
}

