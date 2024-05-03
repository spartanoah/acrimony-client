/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.icons.FlatHelpButtonIcon;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatToolBarUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class FlatButtonUI
extends BasicButtonUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected int minimumWidth;
    protected int iconTextGap;
    protected Color background;
    protected Color foreground;
    protected Color startBackground;
    protected Color endBackground;
    @FlatStylingSupport.Styleable
    protected Color focusedBackground;
    @FlatStylingSupport.Styleable
    protected Color focusedForeground;
    @FlatStylingSupport.Styleable
    protected Color hoverBackground;
    @FlatStylingSupport.Styleable
    protected Color hoverForeground;
    @FlatStylingSupport.Styleable
    protected Color pressedBackground;
    @FlatStylingSupport.Styleable
    protected Color pressedForeground;
    @FlatStylingSupport.Styleable
    protected Color selectedBackground;
    @FlatStylingSupport.Styleable
    protected Color selectedForeground;
    @FlatStylingSupport.Styleable
    protected Color disabledBackground;
    @FlatStylingSupport.Styleable
    protected Color disabledText;
    @FlatStylingSupport.Styleable
    protected Color disabledSelectedBackground;
    @FlatStylingSupport.Styleable
    protected Color disabledSelectedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultBackground;
    protected Color defaultEndBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultFocusedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultFocusedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultHoverBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultHoverForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultPressedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultPressedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected boolean defaultBoldText;
    @FlatStylingSupport.Styleable
    protected boolean paintShadow;
    @FlatStylingSupport.Styleable
    protected int shadowWidth;
    @FlatStylingSupport.Styleable
    protected Color shadowColor;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color defaultShadowColor;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarHoverBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarHoverForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarPressedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarPressedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarSelectedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarSelectedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarDisabledSelectedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color toolbarDisabledSelectedForeground;
    @FlatStylingSupport.Styleable
    protected String buttonType;
    @FlatStylingSupport.Styleable
    protected boolean squareSize;
    @FlatStylingSupport.Styleable
    protected int minimumHeight;
    private Icon helpButtonIcon;
    private Insets defaultMargin;
    private final boolean shared;
    private boolean helpButtonIconShared = true;
    private boolean defaults_initialized = false;
    private Map<String, Object> oldStyleValues;
    private AtomicBoolean borderShared;
    static final int TYPE_OTHER = -1;
    static final int TYPE_SQUARE = 0;
    static final int TYPE_ROUND_RECT = 1;
    private static Rectangle viewR = new Rectangle();
    private static Rectangle textR = new Rectangle();
    private static Rectangle iconR = new Rectangle();

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) && !FlatUIUtils.needsLightAWTPeer(c) ? FlatUIUtils.createSharedUI(FlatButtonUI.class, () -> new FlatButtonUI(true)) : new FlatButtonUI(false);
    }

    protected FlatButtonUI(boolean shared) {
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
        this.installStyle((AbstractButton)c);
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        Color bg;
        super.installDefaults(b);
        if (!this.defaults_initialized) {
            String prefix = this.getPropertyPrefix();
            this.minimumWidth = UIManager.getInt(prefix + "minimumWidth");
            this.iconTextGap = FlatUIUtils.getUIInt(prefix + "iconTextGap", 4);
            this.background = UIManager.getColor(prefix + "background");
            this.foreground = UIManager.getColor(prefix + "foreground");
            this.startBackground = UIManager.getColor(prefix + "startBackground");
            this.endBackground = UIManager.getColor(prefix + "endBackground");
            this.focusedBackground = UIManager.getColor(prefix + "focusedBackground");
            this.focusedForeground = UIManager.getColor(prefix + "focusedForeground");
            this.hoverBackground = UIManager.getColor(prefix + "hoverBackground");
            this.hoverForeground = UIManager.getColor(prefix + "hoverForeground");
            this.pressedBackground = UIManager.getColor(prefix + "pressedBackground");
            this.pressedForeground = UIManager.getColor(prefix + "pressedForeground");
            this.selectedBackground = UIManager.getColor(prefix + "selectedBackground");
            this.selectedForeground = UIManager.getColor(prefix + "selectedForeground");
            this.disabledBackground = UIManager.getColor(prefix + "disabledBackground");
            this.disabledText = UIManager.getColor(prefix + "disabledText");
            this.disabledSelectedBackground = UIManager.getColor(prefix + "disabledSelectedBackground");
            this.disabledSelectedForeground = UIManager.getColor(prefix + "disabledSelectedForeground");
            this.defaultBackground = FlatUIUtils.getUIColor("Button.default.startBackground", "Button.default.background");
            this.defaultEndBackground = UIManager.getColor("Button.default.endBackground");
            this.defaultForeground = UIManager.getColor("Button.default.foreground");
            this.defaultFocusedBackground = UIManager.getColor("Button.default.focusedBackground");
            this.defaultFocusedForeground = UIManager.getColor("Button.default.focusedForeground");
            this.defaultHoverBackground = UIManager.getColor("Button.default.hoverBackground");
            this.defaultHoverForeground = UIManager.getColor("Button.default.hoverForeground");
            this.defaultPressedBackground = UIManager.getColor("Button.default.pressedBackground");
            this.defaultPressedForeground = UIManager.getColor("Button.default.pressedForeground");
            this.defaultBoldText = UIManager.getBoolean("Button.default.boldText");
            this.paintShadow = UIManager.getBoolean("Button.paintShadow");
            this.shadowWidth = FlatUIUtils.getUIInt("Button.shadowWidth", 2);
            this.shadowColor = UIManager.getColor("Button.shadowColor");
            this.defaultShadowColor = UIManager.getColor("Button.default.shadowColor");
            this.toolbarHoverBackground = UIManager.getColor(prefix + "toolbar.hoverBackground");
            this.toolbarHoverForeground = UIManager.getColor(prefix + "toolbar.hoverForeground");
            this.toolbarPressedBackground = UIManager.getColor(prefix + "toolbar.pressedBackground");
            this.toolbarPressedForeground = UIManager.getColor(prefix + "toolbar.pressedForeground");
            this.toolbarSelectedBackground = UIManager.getColor(prefix + "toolbar.selectedBackground");
            this.toolbarSelectedForeground = UIManager.getColor(prefix + "toolbar.selectedForeground");
            this.toolbarDisabledSelectedBackground = UIManager.getColor(prefix + "toolbar.disabledSelectedBackground");
            this.toolbarDisabledSelectedForeground = UIManager.getColor(prefix + "toolbar.disabledSelectedForeground");
            this.helpButtonIcon = UIManager.getIcon("HelpButton.icon");
            this.defaultMargin = UIManager.getInsets(prefix + "margin");
            this.helpButtonIconShared = true;
            this.defaults_initialized = true;
        }
        if (this.startBackground != null && ((bg = b.getBackground()) == null || bg instanceof UIResource)) {
            b.setBackground(this.startBackground);
        }
        LookAndFeel.installProperty(b, "opaque", false);
        LookAndFeel.installProperty(b, "iconTextGap", UIScale.scale(this.iconTextGap));
        MigLayoutVisualPadding.install(b);
    }

    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        this.oldStyleValues = null;
        this.borderShared = null;
        MigLayoutVisualPadding.uninstall(b);
        this.defaults_initialized = false;
    }

    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new FlatButtonListener(b);
    }

    protected void propertyChange(AbstractButton b, PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "JButton.squareSize": 
            case "JComponent.minimumWidth": 
            case "JComponent.minimumHeight": {
                b.revalidate();
                break;
            }
            case "JButton.buttonType": {
                b.revalidate();
                b.repaint();
                break;
            }
            case "JComponent.outline": {
                b.repaint();
                break;
            }
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
        return "Button";
    }

    protected void applyStyle(AbstractButton b, Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, (key, value) -> this.applyStyleProperty(b, (String)key, value));
    }

    protected Object applyStyleProperty(AbstractButton b, String key, Object value) {
        if (key.startsWith("help.")) {
            if (!(this.helpButtonIcon instanceof FlatHelpButtonIcon)) {
                return new FlatStylingSupport.UnknownStyleException(key);
            }
            if (this.helpButtonIconShared) {
                this.helpButtonIcon = FlatStylingSupport.cloneIcon(this.helpButtonIcon);
                this.helpButtonIconShared = false;
            }
            key = key.substring("help.".length());
            return ((FlatHelpButtonIcon)this.helpButtonIcon).applyStyleProperty(key, value);
        }
        if ("iconTextGap".equals(key) && value instanceof Integer) {
            value = UIScale.scale((Integer)value);
        }
        if (this.borderShared == null) {
            this.borderShared = new AtomicBoolean(true);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, b, this.borderShared);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        Map<String, Class<?>> infos = FlatStylingSupport.getAnnotatedStyleableInfos(this, c.getBorder());
        if (this.helpButtonIcon instanceof FlatHelpButtonIcon) {
            FlatStylingSupport.putAllPrefixKey(infos, "help.", ((FlatHelpButtonIcon)this.helpButtonIcon).getStyleableInfos());
        }
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        if (key.startsWith("help.")) {
            return this.helpButtonIcon instanceof FlatHelpButtonIcon ? ((FlatHelpButtonIcon)this.helpButtonIcon).getStyleableValue(key.substring("help.".length())) : null;
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(this, c.getBorder(), key);
    }

    static boolean isContentAreaFilled(Component c) {
        return !(c instanceof AbstractButton) || ((AbstractButton)c).isContentAreaFilled();
    }

    public static boolean isFocusPainted(Component c) {
        return !(c instanceof AbstractButton) || ((AbstractButton)c).isFocusPainted();
    }

    static boolean isDefaultButton(Component c) {
        return c instanceof JButton && ((JButton)c).isDefaultButton();
    }

    static boolean isIconOnlyOrSingleCharacterButton(Component c) {
        if (!(c instanceof JButton) && !(c instanceof JToggleButton)) {
            return false;
        }
        Icon icon = ((AbstractButton)c).getIcon();
        String text = ((AbstractButton)c).getText();
        return icon != null && (text == null || text.isEmpty()) || icon == null && text != null && ("...".equals(text) || text.length() == 1 || text.length() == 2 && Character.isSurrogatePair(text.charAt(0), text.charAt(1)));
    }

    static int getButtonType(Component c) {
        if (!(c instanceof AbstractButton)) {
            return -1;
        }
        String value = FlatButtonUI.getButtonTypeStr((AbstractButton)c);
        if (value == null) {
            return -1;
        }
        switch (value) {
            case "square": {
                return 0;
            }
            case "roundRect": {
                return 1;
            }
        }
        return -1;
    }

    static boolean isHelpButton(Component c) {
        return c instanceof JButton && "help".equals(FlatButtonUI.getButtonTypeStr((JButton)c));
    }

    static boolean isToolBarButton(Component c) {
        return c.getParent() instanceof JToolBar || c instanceof AbstractButton && "toolBarButton".equals(FlatButtonUI.getButtonTypeStr((AbstractButton)c));
    }

    static boolean isBorderlessButton(Component c) {
        return c instanceof AbstractButton && "borderless".equals(FlatButtonUI.getButtonTypeStr((AbstractButton)c));
    }

    static String getButtonTypeStr(AbstractButton c) {
        Object value = c.getClientProperty("JButton.buttonType");
        if (value instanceof String) {
            return (String)value;
        }
        ButtonUI ui = c.getUI();
        return ui instanceof FlatButtonUI ? ((FlatButtonUI)ui).buttonType : null;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            FlatUIUtils.paintParentBackground(g, c);
        }
        if (FlatButtonUI.isHelpButton(c)) {
            this.helpButtonIcon.paintIcon(c, g, 0, 0);
            return;
        }
        if (FlatButtonUI.isContentAreaFilled(c)) {
            this.paintBackground(g, c);
        }
        this.paint(g, c);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void paintBackground(Graphics g, JComponent c) {
        Color background = this.getBackground(c);
        if (background == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            Color endBg;
            Color shadowColor;
            JTextField textField;
            FlatUIUtils.setRenderingHints(g2);
            boolean def = FlatButtonUI.isDefaultButton(c);
            boolean isToolBarButton = FlatButtonUI.isToolBarButton(c);
            float focusWidth = isToolBarButton ? 0.0f : FlatUIUtils.getBorderFocusWidth(c);
            float arc = FlatUIUtils.getBorderArc(c);
            float textFieldArc = 0.0f;
            if (isToolBarButton && FlatClientProperties.clientProperty(c, "FlatLaf.styleClass", "", String.class).contains("inTextField") && (textField = (JTextField)SwingUtilities.getAncestorOfClass(JTextField.class, c)) != null) {
                textFieldArc = FlatUIUtils.getBorderArc(textField);
            }
            int x = 0;
            int y = 0;
            int width = c.getWidth();
            int height = c.getHeight();
            if (isToolBarButton && c.getBorder() instanceof FlatButtonBorder) {
                Insets spacing = UIScale.scale(((FlatButtonBorder)c.getBorder()).toolbarSpacingInsets);
                x += spacing.left;
                y += spacing.top;
                width -= spacing.left + spacing.right;
                height -= spacing.top + spacing.bottom;
                textFieldArc -= (float)(spacing.top + spacing.bottom);
            }
            if (arc < textFieldArc) {
                arc = textFieldArc;
            }
            Color color = shadowColor = def ? this.defaultShadowColor : this.shadowColor;
            if (!(!this.paintShadow || shadowColor == null || this.shadowWidth <= 0 || !(focusWidth > 0.0f) || !c.isEnabled() || isToolBarButton || FlatButtonUI.isBorderlessButton(c) || FlatButtonUI.isFocusPainted(c) && FlatUIUtils.isPermanentFocusOwner(c))) {
                g2.setColor(shadowColor);
                g2.fill(new RoundRectangle2D.Float(focusWidth, focusWidth + UIScale.scale((float)this.shadowWidth), (float)width - focusWidth * 2.0f, (float)height - focusWidth * 2.0f, arc, arc));
            }
            Color startBg = def ? this.defaultBackground : this.startBackground;
            Color color2 = endBg = def ? this.defaultEndBackground : this.endBackground;
            if (background == startBg && endBg != null && !startBg.equals(endBg)) {
                g2.setPaint(new GradientPaint(0.0f, 0.0f, startBg, 0.0f, height, endBg));
            } else {
                g2.setColor(FlatUIUtils.deriveColor(background, this.getBackgroundBase(c, def)));
            }
            FlatUIUtils.paintComponentBackground(g2, x, y, width, height, focusWidth, arc);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g = FlatLabelUI.createGraphicsHTMLTextYCorrection(g, c);
        AbstractButton b = (AbstractButton)c;
        String clippedText = FlatButtonUI.layout(b, b.getFontMetrics(b.getFont()), b.getWidth(), b.getHeight());
        this.clearTextShiftOffset();
        ButtonModel model = b.getModel();
        if (model.isArmed() && model.isPressed()) {
            this.paintButtonPressed(g, b);
        }
        if (b.getIcon() != null) {
            this.paintIcon(g, b, iconR);
        }
        if (clippedText != null && !clippedText.isEmpty()) {
            View view = (View)b.getClientProperty("html");
            if (view != null) {
                view.paint(g, textR);
            } else {
                this.paintText(g, b, textR, clippedText);
            }
        }
        if (b.isFocusPainted() && b.hasFocus()) {
            this.paintFocus(g, b, viewR, textR, iconR);
        }
    }

    @Override
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        int xOffset = this.defaultBoldPlainWidthDiff(c) / 2;
        if (xOffset > 0) {
            boolean ltr = c.getComponentOrientation().isLeftToRight();
            switch (((AbstractButton)c).getHorizontalTextPosition()) {
                case 4: {
                    iconRect.x -= xOffset;
                    break;
                }
                case 2: {
                    iconRect.x += xOffset;
                    break;
                }
                case 11: {
                    iconRect.x = iconRect.x - (ltr ? xOffset : -xOffset);
                    break;
                }
                case 10: {
                    iconRect.x = iconRect.x + (ltr ? xOffset : -xOffset);
                }
            }
        }
        super.paintIcon(g, c, iconRect);
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        if (FlatButtonUI.isHelpButton(b)) {
            return;
        }
        if (this.defaultBoldText && FlatButtonUI.isDefaultButton(b) && b.getFont() instanceof UIResource) {
            Font boldFont = g.getFont().deriveFont(1);
            g.setFont(boldFont);
            int boldWidth = b.getFontMetrics(boldFont).stringWidth(text);
            if (boldWidth > textRect.width) {
                textRect.x -= (boldWidth - textRect.width) / 2;
                textRect.width = boldWidth;
            }
        }
        FlatButtonUI.paintText(g, b, textRect, text, this.getForeground(b));
    }

    public static void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text, Color foreground) {
        if (foreground == null) {
            foreground = Color.red;
        }
        FontMetrics fm = b.getFontMetrics(b.getFont());
        int mnemonicIndex = FlatLaf.isShowMnemonics() ? b.getDisplayedMnemonicIndex() : -1;
        g.setColor(foreground);
        FlatUIUtils.drawStringUnderlineCharAt(b, g, text, mnemonicIndex, textRect.x, textRect.y + fm.getAscent());
    }

    protected Color getBackground(JComponent c) {
        boolean toolBarButton;
        boolean bl = toolBarButton = FlatButtonUI.isToolBarButton(c) || FlatButtonUI.isBorderlessButton(c);
        if (((AbstractButton)c).isSelected()) {
            return FlatButtonUI.buttonStateColor(c, toolBarButton ? this.toolbarSelectedBackground : this.selectedBackground, toolBarButton ? (this.toolbarDisabledSelectedBackground != null ? this.toolbarDisabledSelectedBackground : this.toolbarSelectedBackground) : this.disabledSelectedBackground, null, null, toolBarButton ? this.toolbarPressedBackground : this.pressedBackground);
        }
        if (toolBarButton) {
            Color bg = c.getBackground();
            return FlatButtonUI.buttonStateColor(c, this.isCustomBackground(bg) ? bg : null, null, null, this.toolbarHoverBackground, this.toolbarPressedBackground);
        }
        boolean def = FlatButtonUI.isDefaultButton(c);
        return FlatButtonUI.buttonStateColor(c, this.getBackgroundBase(c, def), this.disabledBackground, this.isCustomBackground(c.getBackground()) ? null : (def ? this.defaultFocusedBackground : this.focusedBackground), def ? this.defaultHoverBackground : this.hoverBackground, def ? this.defaultPressedBackground : this.pressedBackground);
    }

    protected Color getBackgroundBase(JComponent c, boolean def) {
        if (FlatUIUtils.isAWTPeer(c)) {
            return this.background;
        }
        Color bg = c.getBackground();
        if (this.isCustomBackground(bg)) {
            return bg;
        }
        return def ? this.defaultBackground : bg;
    }

    protected boolean isCustomBackground(Color bg) {
        return bg != this.background && (this.startBackground == null || bg != this.startBackground);
    }

    public static Color buttonStateColor(Component c, Color enabledColor, Color disabledColor, Color focusedColor, Color hoverColor, Color pressedColor) {
        if (c == null) {
            return enabledColor;
        }
        if (!c.isEnabled()) {
            return disabledColor;
        }
        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton)c).getModel();
            if (pressedColor != null && model.isPressed()) {
                return pressedColor;
            }
            if (hoverColor != null && model.isRollover()) {
                return hoverColor;
            }
        }
        if (focusedColor != null && FlatButtonUI.isFocusPainted(c) && FlatUIUtils.isPermanentFocusOwner(c)) {
            return focusedColor;
        }
        return enabledColor;
    }

    protected Color getForeground(JComponent c) {
        boolean toolBarButton;
        Color fg = c.getForeground();
        boolean bl = toolBarButton = FlatButtonUI.isToolBarButton(c) || FlatButtonUI.isBorderlessButton(c);
        if (((AbstractButton)c).isSelected()) {
            return FlatButtonUI.buttonStateColor(c, toolBarButton ? (this.toolbarSelectedForeground != null ? this.toolbarSelectedForeground : fg) : (this.isCustomForeground(fg) ? fg : this.selectedForeground), toolBarButton ? (this.toolbarDisabledSelectedForeground != null ? this.toolbarDisabledSelectedForeground : this.disabledText) : (this.disabledSelectedForeground != null ? this.disabledSelectedForeground : this.disabledText), null, null, toolBarButton ? this.toolbarPressedForeground : this.pressedForeground);
        }
        if (toolBarButton) {
            return FlatButtonUI.buttonStateColor(c, fg, this.disabledText, null, this.toolbarHoverForeground, this.toolbarPressedForeground);
        }
        boolean def = FlatButtonUI.isDefaultButton(c);
        return FlatButtonUI.buttonStateColor(c, this.getForegroundBase(c, def), this.disabledText, this.isCustomForeground(fg) ? null : (def ? this.defaultFocusedForeground : this.focusedForeground), def ? this.defaultHoverForeground : this.hoverForeground, def ? this.defaultPressedForeground : this.pressedForeground);
    }

    protected Color getForegroundBase(JComponent c, boolean def) {
        Color fg = c.getForeground();
        if (this.isCustomForeground(fg)) {
            return fg;
        }
        return def ? this.defaultForeground : fg;
    }

    protected boolean isCustomForeground(Color fg) {
        return fg != this.foreground;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (FlatButtonUI.isHelpButton(c)) {
            return new Dimension(this.helpButtonIcon.getIconWidth(), this.helpButtonIcon.getIconHeight());
        }
        Dimension prefSize = super.getPreferredSize(c);
        if (prefSize == null) {
            return null;
        }
        prefSize.width += this.defaultBoldPlainWidthDiff(c);
        boolean isIconOnlyOrSingleCharacter = FlatButtonUI.isIconOnlyOrSingleCharacterButton(c);
        if (FlatClientProperties.clientPropertyBoolean(c, "JButton.squareSize", this.squareSize)) {
            prefSize.width = prefSize.height = Math.max(prefSize.width, prefSize.height);
        } else if (isIconOnlyOrSingleCharacter && ((AbstractButton)c).getIcon() == null) {
            prefSize.width = Math.max(prefSize.width, prefSize.height);
        } else if (!isIconOnlyOrSingleCharacter && !FlatButtonUI.isToolBarButton(c) && c.getBorder() instanceof FlatButtonBorder && this.hasDefaultMargins(c)) {
            int fw = Math.round(FlatUIUtils.getBorderFocusWidth(c) * 2.0f);
            prefSize.width = Math.max(prefSize.width, UIScale.scale(FlatUIUtils.minimumWidth(c, this.minimumWidth)) + fw);
            prefSize.height = Math.max(prefSize.height, UIScale.scale(FlatUIUtils.minimumHeight(c, this.minimumHeight)) + fw);
        }
        return prefSize;
    }

    private int defaultBoldPlainWidthDiff(JComponent c) {
        if (this.defaultBoldText && FlatButtonUI.isDefaultButton(c) && c.getFont() instanceof UIResource) {
            int plainWidth;
            String text = ((AbstractButton)c).getText();
            if (text == null || text.isEmpty()) {
                return 0;
            }
            Font font = c.getFont();
            Font boldFont = font.deriveFont(1);
            int boldWidth = c.getFontMetrics(boldFont).stringWidth(text);
            if (boldWidth > (plainWidth = c.getFontMetrics(font).stringWidth(text))) {
                return boldWidth - plainWidth;
            }
        }
        return 0;
    }

    private boolean hasDefaultMargins(JComponent c) {
        Insets margin = ((AbstractButton)c).getMargin();
        return margin instanceof UIResource && Objects.equals(margin, this.defaultMargin);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        return FlatButtonUI.getBaselineImpl(c, width, height);
    }

    static int getBaselineImpl(JComponent c, int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        AbstractButton b = (AbstractButton)c;
        String text = b.getText();
        if (text == null || text.isEmpty()) {
            return -1;
        }
        FontMetrics fm = b.getFontMetrics(b.getFont());
        FlatButtonUI.layout(b, fm, width, height);
        View view = (View)b.getClientProperty("html");
        if (view != null) {
            int baseline = BasicHTML.getHTMLBaseline(view, FlatButtonUI.textR.width, FlatButtonUI.textR.height);
            return baseline >= 0 ? FlatButtonUI.textR.y + baseline : baseline;
        }
        return FlatButtonUI.textR.y + fm.getAscent();
    }

    private static String layout(AbstractButton b, FontMetrics fm, int width, int height) {
        Insets insets = b.getInsets();
        viewR.setBounds(insets.left, insets.top, width - insets.left - insets.right, height - insets.top - insets.bottom);
        textR.setBounds(0, 0, 0, 0);
        iconR.setBounds(0, 0, 0, 0);
        String text = b.getText();
        return SwingUtilities.layoutCompoundLabel(b, fm, text, b.getIcon(), b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewR, iconR, textR, text != null ? b.getIconTextGap() : 0);
    }

    protected class FlatButtonListener
    extends BasicButtonListener {
        private final AbstractButton b;

        protected FlatButtonListener(AbstractButton b) {
            super(b);
            this.b = b;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            FlatButtonUI.this.propertyChange(this.b, e);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JToolBar toolBar;
            ToolBarUI ui;
            super.stateChanged(e);
            AbstractButton b = (AbstractButton)e.getSource();
            Container parent = b.getParent();
            if (parent instanceof JToolBar && (ui = (toolBar = (JToolBar)parent).getUI()) instanceof FlatToolBarUI) {
                ((FlatToolBarUI)ui).repaintButtonGroup(b);
            }
        }
    }
}

