/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;

public class FlatToolBarUI
extends BasicToolBarUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected boolean focusableButtons;
    @FlatStylingSupport.Styleable
    protected boolean arrowKeysOnlyNavigation;
    @FlatStylingSupport.Styleable
    protected int hoverButtonGroupArc;
    @FlatStylingSupport.Styleable
    protected Color hoverButtonGroupBackground;
    @FlatStylingSupport.Styleable
    protected Insets borderMargins;
    @FlatStylingSupport.Styleable
    protected Color gripColor;
    @FlatStylingSupport.Styleable
    protected Integer separatorWidth;
    @FlatStylingSupport.Styleable
    protected Color separatorColor;
    private FocusTraversalPolicy focusTraversalPolicy;
    private Boolean oldFloatable;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatToolBarUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installFocusTraversalPolicy();
        this.installStyle();
        if (!this.focusableButtons) {
            this.setButtonsFocusable(false);
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        if (!this.focusableButtons) {
            this.setButtonsFocusable(true);
        }
        this.uninstallFocusTraversalPolicy();
        this.oldStyleValues = null;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.focusableButtons = UIManager.getBoolean("ToolBar.focusableButtons");
        this.arrowKeysOnlyNavigation = UIManager.getBoolean("ToolBar.arrowKeysOnlyNavigation");
        this.hoverButtonGroupArc = UIManager.getInt("ToolBar.hoverButtonGroupArc");
        this.hoverButtonGroupBackground = UIManager.getColor("ToolBar.hoverButtonGroupBackground");
        if (!UIManager.getBoolean("ToolBar.floatable")) {
            this.oldFloatable = this.toolBar.isFloatable();
            this.toolBar.setFloatable(false);
        } else {
            this.oldFloatable = null;
        }
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.hoverButtonGroupBackground = null;
        if (this.oldFloatable != null) {
            this.toolBar.setFloatable(this.oldFloatable);
            this.oldFloatable = null;
        }
    }

    @Override
    protected RootPaneContainer createFloatingWindow(JToolBar toolbar) {
        RootPaneContainer floatingWindow = super.createFloatingWindow(toolbar);
        floatingWindow.getRootPane().putClientProperty("Window.style", "small");
        return floatingWindow;
    }

    @Override
    protected ContainerListener createToolBarContListener() {
        return new BasicToolBarUI.ToolBarContListener(){

            @Override
            public void componentAdded(ContainerEvent e) {
                super.componentAdded(e);
                if (!FlatToolBarUI.this.focusableButtons) {
                    FlatToolBarUI.this.setButtonFocusable(e.getChild(), false);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                super.componentRemoved(e);
                if (!FlatToolBarUI.this.focusableButtons) {
                    FlatToolBarUI.this.setButtonFocusable(e.getChild(), true);
                }
            }
        };
    }

    @Override
    protected PropertyChangeListener createPropertyListener() {
        return FlatStylingSupport.createPropertyChangeListener(this.toolBar, this::installStyle, super.createPropertyListener());
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.toolBar, "ToolBar"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        boolean oldFocusableButtons = this.focusableButtons;
        boolean oldArrowKeysOnlyNavigation = this.arrowKeysOnlyNavigation;
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        if (this.focusableButtons != oldFocusableButtons) {
            this.setButtonsFocusable(this.focusableButtons);
        }
        if (this.arrowKeysOnlyNavigation != oldArrowKeysOnlyNavigation || this.focusableButtons != oldFocusableButtons) {
            if (this.arrowKeysOnlyNavigation) {
                this.installFocusTraversalPolicy();
            } else {
                this.uninstallFocusTraversalPolicy();
            }
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.toolBar, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    protected void setButtonsFocusable(boolean focusable) {
        for (Component c : this.toolBar.getComponents()) {
            this.setButtonFocusable(c, focusable);
        }
    }

    private void setButtonFocusable(Component c, boolean focusable) {
        if (c instanceof AbstractButton && focusable != c.isFocusable()) {
            c.setFocusable(focusable);
        }
    }

    protected void installFocusTraversalPolicy() {
        if (!this.arrowKeysOnlyNavigation || !this.focusableButtons || this.toolBar.getFocusTraversalPolicy() != null) {
            return;
        }
        this.focusTraversalPolicy = this.createFocusTraversalPolicy();
        if (this.focusTraversalPolicy != null) {
            this.toolBar.setFocusTraversalPolicy(this.focusTraversalPolicy);
            this.toolBar.setFocusTraversalPolicyProvider(true);
        }
    }

    protected void uninstallFocusTraversalPolicy() {
        if (this.focusTraversalPolicy != null && this.toolBar.getFocusTraversalPolicy() == this.focusTraversalPolicy) {
            this.toolBar.setFocusTraversalPolicy(null);
            this.toolBar.setFocusTraversalPolicyProvider(false);
        }
        this.focusTraversalPolicy = null;
    }

    protected FocusTraversalPolicy createFocusTraversalPolicy() {
        return new FlatToolBarFocusTraversalPolicy();
    }

    @Override
    protected void navigateFocusedComp(int direction) {
        block9: {
            Component c;
            int add;
            int count = this.toolBar.getComponentCount();
            if (this.focusedCompIndex < 0 || this.focusedCompIndex >= count) {
                return;
            }
            switch (direction) {
                case 3: 
                case 5: {
                    add = 1;
                    break;
                }
                case 1: 
                case 7: {
                    add = -1;
                    break;
                }
                default: {
                    return;
                }
            }
            int i = this.focusedCompIndex;
            do {
                if ((i += add) < 0) {
                    i = count - 1;
                } else if (i >= count) {
                    i = 0;
                }
                if (i == this.focusedCompIndex) break block9;
            } while (!FlatToolBarUI.canBeFocusOwner(c = this.toolBar.getComponentAtIndex(i)));
            c.requestFocus();
            return;
        }
    }

    private static boolean canBeFocusOwner(Component c) {
        if (!(c != null && c.isEnabled() && c.isVisible() && c.isDisplayable() && c.isFocusable())) {
            return false;
        }
        if (c instanceof JComboBox) {
            JComboBox comboBox = (JComboBox)c;
            return comboBox.getUI().isFocusTraversable(comboBox);
        }
        if (c instanceof JComponent) {
            InputMap inputMap;
            for (inputMap = ((JComponent)c).getInputMap(0); inputMap != null && inputMap.size() == 0; inputMap = inputMap.getParent()) {
            }
            if (inputMap == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void setBorderToRollover(Component c) {
    }

    @Override
    protected void setBorderToNonRollover(Component c) {
    }

    @Override
    protected void setBorderToNormal(Component c) {
    }

    @Override
    protected void installRolloverBorders(JComponent c) {
    }

    @Override
    protected void installNonRolloverBorders(JComponent c) {
    }

    @Override
    protected void installNormalBorders(JComponent c) {
    }

    @Override
    protected Border createRolloverBorder() {
        return null;
    }

    @Override
    protected Border createNonRolloverBorder() {
        return null;
    }

    @Override
    public void setOrientation(int orientation) {
        Insets margin;
        Insets newMargin;
        if (orientation != this.toolBar.getOrientation() && !(newMargin = new Insets(margin.left, margin.top, margin.right, margin.bottom)).equals(margin = this.toolBar.getMargin())) {
            this.toolBar.setMargin(newMargin);
        }
        super.setOrientation(orientation);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        this.paintButtonGroup(g);
    }

    protected void paintButtonGroup(Graphics g) {
        Component b;
        if (this.hoverButtonGroupBackground == null) {
            return;
        }
        ButtonGroup group = null;
        Component[] componentArray = this.toolBar.getComponents();
        int n = componentArray.length;
        for (int i = 0; !(i >= n || (b = componentArray[i]) instanceof AbstractButton && ((AbstractButton)b).getModel().isRollover() && (group = this.getButtonGroup((AbstractButton)b)) != null); ++i) {
        }
        if (group == null) {
            return;
        }
        ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
        Enumeration<AbstractButton> e = group.getElements();
        while (e.hasMoreElements()) {
            AbstractButton gb = e.nextElement();
            if (gb.getParent() != this.toolBar) continue;
            rects.add(gb.getBounds());
        }
        boolean horizontal = this.toolBar.getOrientation() == 0;
        rects.sort((r1, r2) -> horizontal ? r1.x - r2.x : r1.y - r2.y);
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        g.setColor(FlatUIUtils.deriveColor(this.hoverButtonGroupBackground, this.toolBar.getBackground()));
        int maxSepWidth = UIScale.scale(10);
        Rectangle gr = null;
        for (Rectangle r : rects) {
            if (gr == null) {
                gr = r;
                continue;
            }
            if (horizontal ? gr.x + gr.width + maxSepWidth >= r.x : gr.y + gr.height + maxSepWidth >= r.y) {
                gr = gr.union(r);
                continue;
            }
            FlatUIUtils.paintComponentBackground((Graphics2D)g, gr.x, gr.y, gr.width, gr.height, 0.0f, UIScale.scale(this.hoverButtonGroupArc));
            gr = r;
        }
        if (gr != null) {
            FlatUIUtils.paintComponentBackground((Graphics2D)g, gr.x, gr.y, gr.width, gr.height, 0.0f, UIScale.scale(this.hoverButtonGroupArc));
        }
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }

    protected void repaintButtonGroup(AbstractButton b) {
        if (this.hoverButtonGroupBackground == null) {
            return;
        }
        ButtonGroup group = this.getButtonGroup(b);
        if (group == null) {
            return;
        }
        Rectangle gr = null;
        Enumeration<AbstractButton> e = group.getElements();
        while (e.hasMoreElements()) {
            AbstractButton gb = e.nextElement();
            Container parent = gb.getParent();
            if (parent != this.toolBar) continue;
            gr = gr != null ? gr.union(gb.getBounds()) : gb.getBounds();
        }
        if (gr != null) {
            this.toolBar.repaint(gr);
        }
    }

    private ButtonGroup getButtonGroup(AbstractButton b) {
        ButtonModel model = b.getModel();
        return model instanceof DefaultButtonModel ? ((DefaultButtonModel)model).getGroup() : null;
    }

    protected class FlatToolBarFocusTraversalPolicy
    extends LayoutFocusTraversalPolicy {
        protected FlatToolBarFocusTraversalPolicy() {
        }

        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            if (!(aComponent instanceof AbstractButton)) {
                return super.getComponentAfter(aContainer, aComponent);
            }
            Component c = aComponent;
            while ((c = super.getComponentAfter(aContainer, c)) != null) {
                if (c instanceof AbstractButton) continue;
                return c;
            }
            return null;
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            if (!(aComponent instanceof AbstractButton)) {
                return super.getComponentBefore(aContainer, aComponent);
            }
            Component c = aComponent;
            while ((c = super.getComponentBefore(aContainer, c)) != null) {
                if (c instanceof AbstractButton) continue;
                return c;
            }
            return null;
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            return this.getRecentComponent(aContainer, true);
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            return this.getRecentComponent(aContainer, false);
        }

        private Component getRecentComponent(Container aContainer, boolean first) {
            if (FlatToolBarUI.this.focusedCompIndex >= 0 && FlatToolBarUI.this.focusedCompIndex < FlatToolBarUI.this.toolBar.getComponentCount()) {
                return FlatToolBarUI.this.toolBar.getComponent(FlatToolBarUI.this.focusedCompIndex);
            }
            return first ? super.getFirstComponent(aContainer) : super.getLastComponent(aContainer);
        }
    }
}

