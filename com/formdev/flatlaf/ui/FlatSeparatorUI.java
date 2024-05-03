/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

public class FlatSeparatorUI
extends BasicSeparatorUI
implements FlatStylingSupport.StyleableUI,
PropertyChangeListener {
    @FlatStylingSupport.Styleable
    protected int height;
    @FlatStylingSupport.Styleable
    protected int stripeWidth;
    @FlatStylingSupport.Styleable
    protected int stripeIndent;
    private final boolean shared;
    private boolean defaults_initialized = false;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatSeparatorUI.class, () -> new FlatSeparatorUI(true)) : new FlatSeparatorUI(false);
    }

    protected FlatSeparatorUI(boolean shared) {
        this.shared = shared;
    }

    protected String getPropertyPrefix() {
        return "Separator";
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle((JSeparator)c);
    }

    @Override
    protected void installDefaults(JSeparator s) {
        super.installDefaults(s);
        if (!this.defaults_initialized) {
            String prefix = this.getPropertyPrefix();
            this.height = UIManager.getInt(prefix + ".height");
            this.stripeWidth = UIManager.getInt(prefix + ".stripeWidth");
            this.stripeIndent = UIManager.getInt(prefix + ".stripeIndent");
            this.defaults_initialized = true;
        }
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
            this.applyStyle(s, FlatStylingSupport.getResolvedStyle(s, this.getStyleType()));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    String getStyleType() {
        return "Separator";
    }

    protected void applyStyle(JSeparator s, Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, (key, value) -> this.applyStyleProperty(s, (String)key, value));
    }

    protected Object applyStyleProperty(JSeparator s, String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, s, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setColor(c.getForeground());
            float width = UIScale.scale((float)this.stripeWidth);
            float indent = UIScale.scale((float)this.stripeIndent);
            if (((JSeparator)c).getOrientation() == 1) {
                g2.fill(new Rectangle2D.Float(indent, 0.0f, width, c.getHeight()));
            } else {
                g2.fill(new Rectangle2D.Float(0.0f, indent, c.getWidth(), width));
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (((JSeparator)c).getOrientation() == 1) {
            return new Dimension(UIScale.scale(this.height), 0);
        }
        return new Dimension(0, UIScale.scale(this.height));
    }
}

