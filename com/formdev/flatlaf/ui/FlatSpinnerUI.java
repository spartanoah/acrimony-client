/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatArrowButton;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicSpinnerUI;

public class FlatSpinnerUI
extends BasicSpinnerUI
implements FlatStylingSupport.StyleableUI {
    private Handler handler;
    @FlatStylingSupport.Styleable
    protected int minimumWidth;
    @FlatStylingSupport.Styleable
    protected String buttonStyle;
    @FlatStylingSupport.Styleable
    protected String arrowType;
    @FlatStylingSupport.Styleable
    protected Color disabledBackground;
    @FlatStylingSupport.Styleable
    protected Color disabledForeground;
    @FlatStylingSupport.Styleable
    protected Color focusedBackground;
    @FlatStylingSupport.Styleable
    protected Color buttonBackground;
    @FlatStylingSupport.Styleable
    protected float buttonSeparatorWidth;
    @FlatStylingSupport.Styleable
    protected Color buttonSeparatorColor;
    @FlatStylingSupport.Styleable
    protected Color buttonDisabledSeparatorColor;
    @FlatStylingSupport.Styleable
    protected Color buttonArrowColor;
    @FlatStylingSupport.Styleable
    protected Color buttonDisabledArrowColor;
    @FlatStylingSupport.Styleable
    protected Color buttonHoverArrowColor;
    @FlatStylingSupport.Styleable
    protected Color buttonPressedArrowColor;
    @FlatStylingSupport.Styleable
    protected Insets padding;
    private Map<String, Object> oldStyleValues;
    private AtomicBoolean borderShared;
    private static final int MAC_STEPPER_WIDTH = 15;
    private static final int MAC_STEPPER_GAP = 3;

    public static ComponentUI createUI(JComponent c) {
        return new FlatSpinnerUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        LookAndFeel.installProperty(this.spinner, "opaque", false);
        this.minimumWidth = UIManager.getInt("Component.minimumWidth");
        this.buttonStyle = UIManager.getString("Spinner.buttonStyle");
        this.arrowType = UIManager.getString("Component.arrowType");
        this.disabledBackground = UIManager.getColor("Spinner.disabledBackground");
        this.disabledForeground = UIManager.getColor("Spinner.disabledForeground");
        this.focusedBackground = UIManager.getColor("Spinner.focusedBackground");
        this.buttonBackground = UIManager.getColor("Spinner.buttonBackground");
        this.buttonSeparatorWidth = FlatUIUtils.getUIFloat("Spinner.buttonSeparatorWidth", FlatUIUtils.getUIFloat("Component.borderWidth", 1.0f));
        this.buttonSeparatorColor = UIManager.getColor("Spinner.buttonSeparatorColor");
        this.buttonDisabledSeparatorColor = UIManager.getColor("Spinner.buttonDisabledSeparatorColor");
        this.buttonArrowColor = UIManager.getColor("Spinner.buttonArrowColor");
        this.buttonDisabledArrowColor = UIManager.getColor("Spinner.buttonDisabledArrowColor");
        this.buttonHoverArrowColor = UIManager.getColor("Spinner.buttonHoverArrowColor");
        this.buttonPressedArrowColor = UIManager.getColor("Spinner.buttonPressedArrowColor");
        this.padding = UIManager.getInsets("Spinner.padding");
        MigLayoutVisualPadding.install(this.spinner);
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.disabledBackground = null;
        this.disabledForeground = null;
        this.focusedBackground = null;
        this.buttonBackground = null;
        this.buttonSeparatorColor = null;
        this.buttonDisabledSeparatorColor = null;
        this.buttonArrowColor = null;
        this.buttonDisabledArrowColor = null;
        this.buttonHoverArrowColor = null;
        this.buttonPressedArrowColor = null;
        this.padding = null;
        this.oldStyleValues = null;
        this.borderShared = null;
        MigLayoutVisualPadding.uninstall(this.spinner);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.addEditorFocusListener(this.spinner.getEditor());
        this.spinner.addFocusListener(this.getHandler());
        this.spinner.addPropertyChangeListener(this.getHandler());
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.removeEditorFocusListener(this.spinner.getEditor());
        this.spinner.removeFocusListener(this.getHandler());
        this.spinner.removePropertyChangeListener(this.getHandler());
        this.handler = null;
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.spinner, "Spinner"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        this.updateEditorPadding();
        this.updateArrowButtonsStyle();
    }

    protected Object applyStyleProperty(String key, Object value) {
        if (this.borderShared == null) {
            this.borderShared = new AtomicBoolean(true);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, this.spinner, this.borderShared);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this, this.spinner.getBorder());
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, this.spinner.getBorder(), key);
    }

    @Override
    protected JComponent createEditor() {
        JComponent editor = super.createEditor();
        this.configureEditor(editor);
        return editor;
    }

    @Override
    protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
        super.replaceEditor(oldEditor, newEditor);
        this.configureEditor(newEditor);
        this.removeEditorFocusListener(oldEditor);
        this.addEditorFocusListener(newEditor);
    }

    protected void configureEditor(JComponent editor) {
        editor.setOpaque(false);
        JTextField textField = FlatSpinnerUI.getEditorTextField(editor);
        if (textField != null) {
            textField.setOpaque(false);
        }
        this.updateEditorPadding();
        this.updateEditorColors();
    }

    private void addEditorFocusListener(JComponent editor) {
        JTextField textField = FlatSpinnerUI.getEditorTextField(editor);
        if (textField != null) {
            textField.addFocusListener(this.getHandler());
        }
    }

    private void removeEditorFocusListener(JComponent editor) {
        JTextField textField = FlatSpinnerUI.getEditorTextField(editor);
        if (textField != null) {
            textField.removeFocusListener(this.getHandler());
        }
    }

    private void updateEditorPadding() {
        JTextField textField = FlatSpinnerUI.getEditorTextField(this.spinner.getEditor());
        if (textField != null) {
            textField.putClientProperty("JTextField.padding", this.padding);
        }
    }

    private void updateEditorColors() {
        JTextField textField = FlatSpinnerUI.getEditorTextField(this.spinner.getEditor());
        if (textField != null) {
            textField.setForeground(FlatUIUtils.nonUIResource(this.getForeground(true)));
            textField.setDisabledTextColor(FlatUIUtils.nonUIResource(this.getForeground(false)));
        }
    }

    private static JTextField getEditorTextField(JComponent editor) {
        return editor instanceof JSpinner.DefaultEditor ? ((JSpinner.DefaultEditor)editor).getTextField() : null;
    }

    public static boolean isPermanentFocusOwner(JSpinner spinner) {
        if (FlatUIUtils.isPermanentFocusOwner(spinner)) {
            return true;
        }
        JTextField textField = FlatSpinnerUI.getEditorTextField(spinner.getEditor());
        return textField != null && FlatUIUtils.isPermanentFocusOwner(textField);
    }

    protected Color getBackground(boolean enabled) {
        if (enabled) {
            Color background = this.spinner.getBackground();
            if (!(background instanceof UIResource)) {
                return background;
            }
            if (this.focusedBackground != null && FlatSpinnerUI.isPermanentFocusOwner(this.spinner)) {
                return this.focusedBackground;
            }
            return background;
        }
        return this.disabledBackground;
    }

    protected Color getForeground(boolean enabled) {
        return enabled ? this.spinner.getForeground() : this.disabledForeground;
    }

    @Override
    protected LayoutManager createLayout() {
        return this.getHandler();
    }

    @Override
    protected Component createNextButton() {
        return this.createArrowButton(1, "Spinner.nextButton");
    }

    @Override
    protected Component createPreviousButton() {
        return this.createArrowButton(5, "Spinner.previousButton");
    }

    private Component createArrowButton(int direction, String name) {
        FlatArrowButton button = new FlatArrowButton(direction, this.arrowType, this.buttonArrowColor, this.buttonDisabledArrowColor, this.buttonHoverArrowColor, null, this.buttonPressedArrowColor, null){

            @Override
            public int getArrowWidth() {
                return FlatSpinnerUI.this.isMacStyle() ? 7 : super.getArrowWidth();
            }

            @Override
            public float getArrowThickness() {
                return FlatSpinnerUI.this.isMacStyle() ? 1.5f : super.getArrowThickness();
            }

            @Override
            public float getYOffset() {
                return FlatSpinnerUI.this.isMacStyle() ? 0.0f : super.getYOffset();
            }

            @Override
            public boolean isRoundBorderAutoXOffset() {
                return FlatSpinnerUI.this.isMacStyle() ? false : super.isRoundBorderAutoXOffset();
            }
        };
        button.setName(name);
        button.setYOffset(direction == 1 ? 1.25f : -1.25f);
        if (direction == 1) {
            this.installNextButtonListeners(button);
        } else {
            this.installPreviousButtonListeners(button);
        }
        return button;
    }

    private void updateArrowButtonsStyle() {
        for (Component c : this.spinner.getComponents()) {
            if (!(c instanceof FlatArrowButton)) continue;
            ((FlatArrowButton)c).updateStyle(this.arrowType, this.buttonArrowColor, this.buttonDisabledArrowColor, this.buttonHoverArrowColor, null, this.buttonPressedArrowColor, null);
        }
    }

    @Override
    public void update(Graphics g, JComponent c) {
        float focusWidth = FlatUIUtils.getBorderFocusWidth(c);
        float arc = FlatUIUtils.getBorderArc(c);
        if (c.isOpaque() && (focusWidth > 0.0f || arc > 0.0f)) {
            FlatUIUtils.paintParentBackground(g, c);
        }
        Graphics2D g2 = (Graphics2D)g;
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g2);
        int width = c.getWidth();
        int height = c.getHeight();
        boolean enabled = this.spinner.isEnabled();
        boolean ltr = this.spinner.getComponentOrientation().isLeftToRight();
        boolean isMacStyle = this.isMacStyle();
        int macStyleButtonsWidth = isMacStyle ? this.getMacStyleButtonsWidth() : 0;
        g2.setColor(this.getBackground(enabled));
        FlatUIUtils.paintComponentBackground(g2, ltr ? 0 : macStyleButtonsWidth, 0, width - macStyleButtonsWidth, height, focusWidth, arc);
        boolean paintButton = !"none".equals(this.buttonStyle);
        Handler handler = this.getHandler();
        if (paintButton && (handler.nextButton != null || handler.previousButton != null)) {
            Color separatorColor;
            Component button = handler.nextButton != null ? handler.nextButton : handler.previousButton;
            int arrowX = button.getX();
            int arrowWidth = button.getWidth();
            Color color = separatorColor = enabled ? this.buttonSeparatorColor : this.buttonDisabledSeparatorColor;
            if (isMacStyle) {
                Insets insets = this.spinner.getInsets();
                int lineWidth = Math.round(FlatUIUtils.getBorderLineWidth(this.spinner));
                int bx = arrowX;
                int by = insets.top - lineWidth;
                int bw = arrowWidth;
                int bh = height - insets.top - insets.bottom + lineWidth * 2;
                float lw = UIScale.scale(this.buttonSeparatorWidth);
                FlatUIUtils.paintOutlinedComponent(g2, bx, by, bw, bh, 0.0f, 0.0f, 0.0f, lw, UIScale.scale(12), null, separatorColor, this.buttonBackground);
                if (separatorColor != null) {
                    int thickness = UIScale.scale(1);
                    g2.setColor(separatorColor);
                    g2.fill(new Rectangle2D.Float((float)bx + lw, (float)by + (float)(bh - thickness) / 2.0f, (float)bw - lw * 2.0f, thickness));
                }
            } else {
                if (enabled && this.buttonBackground != null) {
                    g2.setColor(this.buttonBackground);
                    Shape oldClip = g2.getClip();
                    if (ltr) {
                        g2.clipRect(arrowX, 0, width - arrowX, height);
                    } else {
                        g2.clipRect(0, 0, arrowX + arrowWidth, height);
                    }
                    FlatUIUtils.paintComponentBackground(g2, 0, 0, width, height, focusWidth, arc);
                    g2.setClip(oldClip);
                }
                if (separatorColor != null && this.buttonSeparatorWidth > 0.0f) {
                    g2.setColor(separatorColor);
                    float lw = UIScale.scale(this.buttonSeparatorWidth);
                    float lx = ltr ? (float)arrowX : (float)(arrowX + arrowWidth) - lw;
                    g2.fill(new Rectangle2D.Float(lx, focusWidth, lw, (float)(height - 1) - focusWidth * 2.0f));
                }
            }
        }
        this.paint(g, c);
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }

    boolean isMacStyle() {
        return "mac".equals(this.buttonStyle);
    }

    int getMacStyleButtonsWidth() {
        return this.handler.nextButton != null || this.handler.previousButton != null ? UIScale.scale(3) + UIScale.scale(15) : 0;
    }

    private class Handler
    implements LayoutManager,
    FocusListener,
    PropertyChangeListener {
        private Component editor = null;
        private Component nextButton;
        private Component previousButton;

        private Handler() {
        }

        @Override
        public void addLayoutComponent(String name, Component c) {
            switch (name) {
                case "Editor": {
                    this.editor = c;
                    break;
                }
                case "Next": {
                    this.nextButton = c;
                    break;
                }
                case "Previous": {
                    this.previousButton = c;
                }
            }
        }

        @Override
        public void removeLayoutComponent(Component c) {
            if (c == this.editor) {
                this.editor = null;
            } else if (c == this.nextButton) {
                this.nextButton = null;
            } else if (c == this.previousButton) {
                this.previousButton = null;
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets insets = parent.getInsets();
            Insets padding = UIScale.scale(FlatSpinnerUI.this.padding);
            Dimension editorSize = this.editor != null ? this.editor.getPreferredSize() : new Dimension(0, 0);
            int minimumWidth = FlatUIUtils.minimumWidth(FlatSpinnerUI.this.spinner, FlatSpinnerUI.this.minimumWidth);
            int innerHeight = editorSize.height + padding.top + padding.bottom;
            float focusWidth = FlatUIUtils.getBorderFocusWidth(FlatSpinnerUI.this.spinner);
            return new Dimension(Math.max(insets.left + insets.right + editorSize.width + padding.left + padding.right + innerHeight, UIScale.scale(minimumWidth) + Math.round(focusWidth * 2.0f)), insets.top + insets.bottom + innerHeight);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return this.preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            int lineWidth;
            int buttonsWidth;
            Dimension size = parent.getSize();
            Insets insets = parent.getInsets();
            Rectangle r = FlatUIUtils.subtractInsets(new Rectangle(size), insets);
            if (this.nextButton == null && this.previousButton == null) {
                if (this.editor != null) {
                    this.editor.setBounds(r);
                }
                return;
            }
            Rectangle editorRect = new Rectangle(r);
            Rectangle buttonsRect = new Rectangle(r);
            FontMetrics fm = FlatSpinnerUI.this.spinner.getFontMetrics(FlatSpinnerUI.this.spinner.getFont());
            int maxButtonWidth = fm.getHeight() + UIScale.scale(FlatSpinnerUI.this.padding.top) + UIScale.scale(FlatSpinnerUI.this.padding.bottom);
            int minButtonWidth = maxButtonWidth * 3 / 4;
            boolean isMacStyle = FlatSpinnerUI.this.isMacStyle();
            int buttonsGap = isMacStyle ? UIScale.scale(3) : 0;
            int prefButtonWidth = isMacStyle ? UIScale.scale(15) : buttonsRect.height;
            buttonsRect.width = buttonsWidth = Math.min(Math.max(prefButtonWidth, minButtonWidth), maxButtonWidth);
            editorRect.width -= buttonsWidth + buttonsGap;
            boolean ltr = parent.getComponentOrientation().isLeftToRight();
            if (ltr) {
                buttonsRect.x += editorRect.width + buttonsGap;
            } else {
                editorRect.x += buttonsWidth + buttonsGap;
            }
            if (isMacStyle && (lineWidth = Math.round(FlatUIUtils.getBorderLineWidth(FlatSpinnerUI.this.spinner))) > 0) {
                buttonsRect.x = buttonsRect.x + (ltr ? lineWidth : -lineWidth);
                buttonsRect.y -= lineWidth;
                buttonsRect.height += lineWidth * 2;
            }
            if (this.editor != null) {
                this.editor.setBounds(editorRect);
            }
            int nextHeight = buttonsRect.height / 2 + buttonsRect.height % 2;
            if (this.nextButton != null) {
                this.nextButton.setBounds(buttonsRect.x, buttonsRect.y, buttonsRect.width, nextHeight);
            }
            if (this.previousButton != null) {
                int previousY = buttonsRect.y + buttonsRect.height - nextHeight;
                this.previousButton.setBounds(buttonsRect.x, previousY, buttonsRect.width, nextHeight);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            JTextField textField;
            FlatSpinnerUI.this.spinner.repaint();
            if (e.getComponent() == FlatSpinnerUI.this.spinner && (textField = FlatSpinnerUI.getEditorTextField(FlatSpinnerUI.this.spinner.getEditor())) != null) {
                textField.requestFocusInWindow();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            FlatSpinnerUI.this.spinner.repaint();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "foreground": 
                case "enabled": {
                    FlatSpinnerUI.this.updateEditorColors();
                    break;
                }
                case "JComponent.roundRect": 
                case "JComponent.outline": {
                    FlatSpinnerUI.this.spinner.repaint();
                    break;
                }
                case "JComponent.minimumWidth": {
                    FlatSpinnerUI.this.spinner.revalidate();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    FlatSpinnerUI.this.installStyle();
                    FlatSpinnerUI.this.spinner.revalidate();
                    FlatSpinnerUI.this.spinner.repaint();
                }
            }
        }
    }
}

