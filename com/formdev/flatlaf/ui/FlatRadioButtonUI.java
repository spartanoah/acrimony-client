/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicRadioButtonUI;

public class FlatRadioButtonUI
extends BasicRadioButtonUI
implements FlatStylingSupport.StyleableUI {
    protected int iconTextGap;
    @FlatStylingSupport.Styleable
    protected Color disabledText;
    private Color defaultBackground;
    private final boolean shared;
    private boolean iconShared = true;
    private boolean defaults_initialized = false;
    private Map<String, Object> oldStyleValues;
    private static final Insets tempInsets = new Insets(0, 0, 0, 0);

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) && !FlatUIUtils.needsLightAWTPeer(c) ? FlatUIUtils.createSharedUI(FlatRadioButtonUI.class, () -> new FlatRadioButtonUI(true)) : new FlatRadioButtonUI(false);
    }

    protected FlatRadioButtonUI(boolean shared) {
        this.shared = shared;
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
        if (FlatUIUtils.isAWTPeer(c)) {
            AWTPeerMouseExitedFix.install(c);
        }
        this.installStyle((AbstractButton)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        if (FlatUIUtils.isAWTPeer(c)) {
            AWTPeerMouseExitedFix.uninstall(c);
        }
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if (!this.defaults_initialized) {
            String prefix = this.getPropertyPrefix();
            this.iconTextGap = FlatUIUtils.getUIInt(prefix + "iconTextGap", 4);
            this.disabledText = UIManager.getColor(prefix + "disabledText");
            this.defaultBackground = UIManager.getColor(prefix + "background");
            this.iconShared = true;
            this.defaults_initialized = true;
        }
        LookAndFeel.installProperty(b, "opaque", false);
        LookAndFeel.installProperty(b, "iconTextGap", UIScale.scale(this.iconTextGap));
        MigLayoutVisualPadding.install(b, null);
    }

    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        this.oldStyleValues = null;
        MigLayoutVisualPadding.uninstall(b);
        this.defaults_initialized = false;
    }

    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new FlatRadioButtonListener(b);
    }

    protected void propertyChange(AbstractButton b, PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "FlatLaf.style": 
            case "FlatLaf.styleClass": {
                if (this.shared && FlatStylingSupport.hasStyleProperty(b)) {
                    b.updateUI();
                } else {
                    this.installStyle(b);
                }
                b.revalidate();
                b.repaint();
            }
        }
    }

    protected void installStyle(AbstractButton b) {
        try {
            this.applyStyle(b, FlatStylingSupport.getResolvedStyle(b, this.getStyleType()));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    String getStyleType() {
        return "RadioButton";
    }

    protected void applyStyle(AbstractButton b, Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, (key, value) -> this.applyStyleProperty(b, (String)key, value));
    }

    protected Object applyStyleProperty(AbstractButton b, String key, Object value) {
        if (key.startsWith("icon.")) {
            if (!(this.icon instanceof FlatCheckBoxIcon)) {
                return new FlatStylingSupport.UnknownStyleException(key);
            }
            if (this.iconShared) {
                this.icon = FlatStylingSupport.cloneIcon(this.icon);
                this.iconShared = false;
            }
            key = key.substring("icon.".length());
            return ((FlatCheckBoxIcon)this.icon).applyStyleProperty(key, value);
        }
        if ("iconTextGap".equals(key) && value instanceof Integer) {
            value = UIScale.scale((Integer)value);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, b, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        Map<String, Class<?>> infos = FlatStylingSupport.getAnnotatedStyleableInfos(this);
        if (this.icon instanceof FlatCheckBoxIcon) {
            for (Map.Entry<String, Class<?>> e : ((FlatCheckBoxIcon)this.icon).getStyleableInfos().entrySet()) {
                infos.put("icon.".concat(e.getKey()), e.getValue());
            }
        }
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        if (key.startsWith("icon.")) {
            return this.icon instanceof FlatCheckBoxIcon ? ((FlatCheckBoxIcon)this.icon).getStyleableValue(key.substring("icon.".length())) : null;
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size == null) {
            return null;
        }
        int focusWidth = this.getIconFocusWidth(c);
        if (focusWidth > 0) {
            Insets insets = c.getInsets(tempInsets);
            size.width += Math.max(focusWidth - insets.left, 0) + Math.max(focusWidth - insets.right, 0);
            size.height += Math.max(focusWidth - insets.top, 0) + Math.max(focusWidth - insets.bottom, 0);
        }
        return size;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int focusWidth;
        if (!c.isOpaque() && ((AbstractButton)c).isContentAreaFilled() && !Objects.equals(c.getBackground(), this.getDefaultBackground(c))) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        if ((focusWidth = this.getIconFocusWidth(c)) > 0) {
            boolean ltr = c.getComponentOrientation().isLeftToRight();
            int halign = ((AbstractButton)c).getHorizontalAlignment();
            if (halign == 10) {
                halign = ltr ? 2 : 4;
            } else if (halign == 11) {
                halign = ltr ? 4 : 2;
            }
            Insets insets = c.getInsets(tempInsets);
            if (!(focusWidth <= insets.left && focusWidth <= insets.right || halign != 2 && halign != 4)) {
                int offset = halign == 2 ? Math.max(focusWidth - insets.left, 0) : -Math.max(focusWidth - insets.right, 0);
                g.translate(offset, 0);
                super.paint(FlatLabelUI.createGraphicsHTMLTextYCorrection(g, c), c);
                g.translate(-offset, 0);
                return;
            }
        }
        super.paint(FlatLabelUI.createGraphicsHTMLTextYCorrection(g, c), c);
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        FlatButtonUI.paintText(g, b, textRect, text, b.isEnabled() ? b.getForeground() : this.disabledText);
    }

    private Color getDefaultBackground(JComponent c) {
        Container parent = c.getParent();
        return parent instanceof CellRendererPane && parent.getParent() != null ? parent.getParent().getBackground() : this.defaultBackground;
    }

    private int getIconFocusWidth(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        Icon icon = b.getIcon();
        if (icon == null) {
            icon = this.getDefaultIcon();
        }
        return icon instanceof FlatCheckBoxIcon ? Math.round(UIScale.scale(((FlatCheckBoxIcon)icon).getFocusWidth())) : 0;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        return FlatButtonUI.getBaselineImpl(c, width, height);
    }

    private static class AWTPeerMouseExitedFix
    extends MouseAdapter
    implements PropertyChangeListener {
        private final JComponent button;

        static void install(JComponent button) {
            AWTPeerMouseExitedFix l = new AWTPeerMouseExitedFix(button);
            button.addPropertyChangeListener("ancestor", l);
            Container parent = button.getParent();
            if (parent != null) {
                parent.addMouseListener(l);
            }
        }

        static void uninstall(JComponent button) {
            for (PropertyChangeListener l : button.getPropertyChangeListeners("ancestor")) {
                if (!(l instanceof AWTPeerMouseExitedFix)) continue;
                button.removePropertyChangeListener("ancestor", l);
                Container parent = button.getParent();
                if (parent == null) break;
                parent.removeMouseListener((AWTPeerMouseExitedFix)l);
                break;
            }
        }

        AWTPeerMouseExitedFix(JComponent button) {
            this.button = button;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getOldValue() instanceof Component) {
                ((Component)e.getOldValue()).removeMouseListener(this);
            }
            if (e.getNewValue() instanceof Component) {
                ((Component)e.getNewValue()).removeMouseListener(this);
                ((Component)e.getNewValue()).addMouseListener(this);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.button.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, this.button));
        }
    }

    protected class FlatRadioButtonListener
    extends BasicButtonListener {
        private final AbstractButton b;

        protected FlatRadioButtonListener(AbstractButton b) {
            super(b);
            this.b = b;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            FlatRadioButtonUI.this.propertyChange(this.b, e);
        }
    }
}

