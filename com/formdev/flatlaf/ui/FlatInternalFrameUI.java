/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatDropShadowBorder;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatInternalFrameTitlePane;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatWindowResizer;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class FlatInternalFrameUI
extends BasicInternalFrameUI
implements FlatStylingSupport.StyleableUI {
    protected FlatWindowResizer windowResizer;
    private Map<String, Object> oldStyleValues;
    private AtomicBoolean borderShared;

    public static ComponentUI createUI(JComponent c) {
        return new FlatInternalFrameUI((JInternalFrame)c);
    }

    public FlatInternalFrameUI(JInternalFrame b) {
        super(b);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        LookAndFeel.installProperty(this.frame, "opaque", false);
        this.windowResizer = this.createWindowResizer();
        this.installStyle();
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        if (this.windowResizer != null) {
            this.windowResizer.uninstall();
            this.windowResizer = null;
        }
        this.oldStyleValues = null;
        this.borderShared = null;
    }

    @Override
    protected JComponent createNorthPane(JInternalFrame w) {
        return new FlatInternalFrameTitlePane(w);
    }

    protected FlatWindowResizer createWindowResizer() {
        return new FlatWindowResizer.InternalFrameResizer(this.frame, this::getDesktopManager);
    }

    @Override
    protected MouseInputAdapter createBorderListener(JInternalFrame w) {
        return new FlatBorderListener();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return FlatStylingSupport.createPropertyChangeListener(this.frame, this::installStyle, super.createPropertyChangeListener());
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.frame, "InternalFrame"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        if (this.borderShared == null) {
            this.borderShared = new AtomicBoolean(true);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, this.frame, this.borderShared);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this, this.frame.getBorder());
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, this.frame.getBorder(), key);
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (!c.isOpaque() && !FlatUIUtils.hasOpaqueBeenExplicitlySet(c)) {
            Insets insets = c.getInsets();
            g.setColor(c.getBackground());
            g.fillRect(insets.left, insets.top, c.getWidth() - insets.left - insets.right, c.getHeight() - insets.top - insets.bottom);
        }
        super.update(g, c);
    }

    protected class FlatBorderListener
    extends BasicInternalFrameUI.BorderListener {
        protected FlatBorderListener() {
            super(FlatInternalFrameUI.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Rectangle iconBounds;
            if (e.getClickCount() == 2 && !FlatInternalFrameUI.this.frame.isIcon() && e.getSource() instanceof FlatInternalFrameTitlePane && (iconBounds = ((FlatInternalFrameTitlePane)e.getSource()).getFrameIconBounds()) != null && iconBounds.contains(e.getX(), e.getY())) {
                if (FlatInternalFrameUI.this.frame.isClosable()) {
                    FlatInternalFrameUI.this.frame.doDefaultCloseAction();
                }
                return;
            }
            super.mouseClicked(e);
        }
    }

    public static class FlatInternalFrameBorder
    extends FlatEmptyBorder
    implements FlatStylingSupport.StyleableBorder {
        @FlatStylingSupport.Styleable
        protected Color activeBorderColor = UIManager.getColor("InternalFrame.activeBorderColor");
        @FlatStylingSupport.Styleable
        protected Color inactiveBorderColor = UIManager.getColor("InternalFrame.inactiveBorderColor");
        @FlatStylingSupport.Styleable
        protected int borderLineWidth = FlatUIUtils.getUIInt("InternalFrame.borderLineWidth", 1);
        @FlatStylingSupport.Styleable
        protected boolean dropShadowPainted = UIManager.getBoolean("InternalFrame.dropShadowPainted");
        private final FlatDropShadowBorder activeDropShadowBorder = new FlatDropShadowBorder(UIManager.getColor("InternalFrame.activeDropShadowColor"), UIManager.getInsets("InternalFrame.activeDropShadowInsets"), FlatUIUtils.getUIFloat("InternalFrame.activeDropShadowOpacity", 0.5f));
        private final FlatDropShadowBorder inactiveDropShadowBorder = new FlatDropShadowBorder(UIManager.getColor("InternalFrame.inactiveDropShadowColor"), UIManager.getInsets("InternalFrame.inactiveDropShadowInsets"), FlatUIUtils.getUIFloat("InternalFrame.inactiveDropShadowOpacity", 0.5f));

        public FlatInternalFrameBorder() {
            super(UIManager.getInsets("InternalFrame.borderMargins"));
        }

        @Override
        public Object applyStyleProperty(String key, Object value) {
            switch (key) {
                case "borderMargins": {
                    return this.applyStyleProperty((Insets)value);
                }
                case "activeDropShadowColor": {
                    return this.activeDropShadowBorder.applyStyleProperty("shadowColor", value);
                }
                case "activeDropShadowInsets": {
                    return this.activeDropShadowBorder.applyStyleProperty("shadowInsets", value);
                }
                case "activeDropShadowOpacity": {
                    return this.activeDropShadowBorder.applyStyleProperty("shadowOpacity", value);
                }
                case "inactiveDropShadowColor": {
                    return this.inactiveDropShadowBorder.applyStyleProperty("shadowColor", value);
                }
                case "inactiveDropShadowInsets": {
                    return this.inactiveDropShadowBorder.applyStyleProperty("shadowInsets", value);
                }
                case "inactiveDropShadowOpacity": {
                    return this.inactiveDropShadowBorder.applyStyleProperty("shadowOpacity", value);
                }
            }
            return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
        }

        @Override
        public Map<String, Class<?>> getStyleableInfos() {
            FlatStylingSupport.StyleableInfosMap infos = new FlatStylingSupport.StyleableInfosMap();
            FlatStylingSupport.collectAnnotatedStyleableInfos(this, infos);
            infos.put("borderMargins", Insets.class);
            infos.put("activeDropShadowColor", Color.class);
            infos.put("activeDropShadowInsets", Insets.class);
            infos.put("activeDropShadowOpacity", Float.TYPE);
            infos.put("inactiveDropShadowColor", Color.class);
            infos.put("inactiveDropShadowInsets", Insets.class);
            infos.put("inactiveDropShadowOpacity", Float.TYPE);
            return infos;
        }

        @Override
        public Object getStyleableValue(String key) {
            switch (key) {
                case "borderMargins": {
                    return this.getStyleableValue();
                }
                case "activeDropShadowColor": {
                    return this.activeDropShadowBorder.getStyleableValue("shadowColor");
                }
                case "activeDropShadowInsets": {
                    return this.activeDropShadowBorder.getStyleableValue("shadowInsets");
                }
                case "activeDropShadowOpacity": {
                    return this.activeDropShadowBorder.getStyleableValue("shadowOpacity");
                }
                case "inactiveDropShadowColor": {
                    return this.inactiveDropShadowBorder.getStyleableValue("shadowColor");
                }
                case "inactiveDropShadowInsets": {
                    return this.inactiveDropShadowBorder.getStyleableValue("shadowInsets");
                }
                case "inactiveDropShadowOpacity": {
                    return this.inactiveDropShadowBorder.getStyleableValue("shadowOpacity");
                }
            }
            return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            if (c instanceof JInternalFrame && ((JInternalFrame)c).isMaximum()) {
                insets.left = UIScale.scale(Math.min(this.borderLineWidth, this.left));
                insets.top = UIScale.scale(Math.min(this.borderLineWidth, this.top));
                insets.right = UIScale.scale(Math.min(this.borderLineWidth, this.right));
                insets.bottom = UIScale.scale(Math.min(this.borderLineWidth, this.bottom));
                return insets;
            }
            return super.getBorderInsets(c, insets);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            JInternalFrame f = (JInternalFrame)c;
            Insets insets = this.getBorderInsets(c);
            float lineWidth = UIScale.scale((float)this.borderLineWidth);
            float rx = (float)(x + insets.left) - lineWidth;
            float ry = (float)(y + insets.top) - lineWidth;
            float rwidth = (float)(width - insets.left - insets.right) + lineWidth * 2.0f;
            float rheight = (float)(height - insets.top - insets.bottom) + lineWidth * 2.0f;
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                FlatUIUtils.setRenderingHints(g2);
                g2.setColor(f.isSelected() ? this.activeBorderColor : this.inactiveBorderColor);
                if (this.dropShadowPainted) {
                    FlatDropShadowBorder dropShadowBorder = f.isSelected() ? this.activeDropShadowBorder : this.inactiveDropShadowBorder;
                    Insets dropShadowInsets = dropShadowBorder.getBorderInsets();
                    dropShadowBorder.paintBorder(c, g2, (int)rx - dropShadowInsets.left, (int)ry - dropShadowInsets.top, (int)rwidth + dropShadowInsets.left + dropShadowInsets.right, (int)rheight + dropShadowInsets.top + dropShadowInsets.bottom);
                }
                g2.fill(FlatUIUtils.createRectangle(rx, ry, rwidth, rheight, lineWidth));
            } finally {
                g2.dispose();
            }
        }
    }
}

