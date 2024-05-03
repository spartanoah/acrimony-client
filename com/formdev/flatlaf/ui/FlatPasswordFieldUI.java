/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatCapsLockIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PasswordView;
import javax.swing.text.View;

public class FlatPasswordFieldUI
extends FlatTextFieldUI {
    private static final String KEY_REVEAL_SELECTED = "FlatLaf.internal.FlatPasswordFieldUI.revealSelected";
    private Character echoChar;
    @FlatStylingSupport.Styleable
    protected boolean showCapsLock;
    @FlatStylingSupport.Styleable
    protected boolean showRevealButton;
    protected Icon capsLockIcon;
    protected Icon revealIcon;
    private KeyListener capsLockListener;
    private boolean capsLockIconShared = true;
    private JToggleButton revealButton;
    private boolean uninstallEchoChar;

    public static ComponentUI createUI(JComponent c) {
        return new FlatPasswordFieldUI();
    }

    @Override
    protected String getPropertyPrefix() {
        return "PasswordField";
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installRevealButton();
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallRevealButton();
        super.uninstallUI(c);
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        String prefix = this.getPropertyPrefix();
        this.echoChar = (Character)UIManager.get(prefix + ".echoChar");
        if (this.echoChar != null) {
            LookAndFeel.installProperty(this.getComponent(), "echoChar", this.echoChar);
        }
        this.showCapsLock = UIManager.getBoolean("PasswordField.showCapsLock");
        this.showRevealButton = UIManager.getBoolean("PasswordField.showRevealButton");
        this.capsLockIcon = UIManager.getIcon("PasswordField.capsLockIcon");
        this.revealIcon = UIManager.getIcon("PasswordField.revealIcon");
        this.capsLockIconShared = true;
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.capsLockIcon = null;
        this.revealIcon = null;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.capsLockListener = new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent e) {
                this.repaint(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                this.repaint(e);
            }

            private void repaint(KeyEvent e) {
                if (e.getKeyCode() == 20) {
                    e.getComponent().repaint();
                    FlatPasswordFieldUI.this.scrollCaretToVisible();
                }
            }
        };
        this.getComponent().addKeyListener(this.capsLockListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.getComponent().removeKeyListener(this.capsLockListener);
        this.capsLockListener = null;
    }

    @Override
    protected void installKeyboardActions() {
        Action selectLineAction;
        super.installKeyboardActions();
        ActionMap map = SwingUtilities.getUIActionMap(this.getComponent());
        if (map != null && map.get("select-word") != null && (selectLineAction = map.get("select-line")) != null) {
            map.put("select-word", selectLineAction);
        }
    }

    @Override
    String getStyleType() {
        return "PasswordField";
    }

    @Override
    protected void applyStyle(Object style) {
        boolean oldShowRevealButton = this.showRevealButton;
        super.applyStyle(style);
        if (this.showRevealButton != oldShowRevealButton) {
            this.uninstallRevealButton();
            this.installRevealButton();
        }
    }

    @Override
    protected Object applyStyleProperty(String key, Object value) {
        if (key.equals("capsLockIconColor") && this.capsLockIcon instanceof FlatCapsLockIcon) {
            if (this.capsLockIconShared) {
                this.capsLockIcon = FlatStylingSupport.cloneIcon(this.capsLockIcon);
                this.capsLockIconShared = false;
            }
            return ((FlatCapsLockIcon)this.capsLockIcon).applyStyleProperty(key, value);
        }
        return super.applyStyleProperty(key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        Map<String, Class<?>> infos = super.getStyleableInfos(c);
        infos.put("capsLockIconColor", Color.class);
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        if (key.equals("capsLockIconColor") && this.capsLockIcon instanceof FlatCapsLockIcon) {
            return ((FlatCapsLockIcon)this.capsLockIcon).getStyleableValue(key);
        }
        return super.getStyleableValue(c, key);
    }

    @Override
    public View create(Element elem) {
        return new PasswordView(elem);
    }

    @Override
    protected void paintIcons(Graphics g, Rectangle r) {
        super.paintIcons(g, r);
        if (this.isCapsLockVisible()) {
            this.paintCapsLock(g, r);
        }
    }

    protected void paintCapsLock(Graphics g, Rectangle r) {
        JTextComponent c = this.getComponent();
        int x = c.getComponentOrientation().isLeftToRight() ? r.x + r.width - this.capsLockIcon.getIconWidth() : r.x;
        int y = r.y + Math.round((float)(r.height - this.capsLockIcon.getIconHeight()) / 2.0f);
        this.capsLockIcon.paintIcon(c, g, x, y);
    }

    @Override
    protected boolean hasTrailingIcon() {
        return super.hasTrailingIcon() || this.isCapsLockVisible();
    }

    @Override
    protected int getTrailingIconWidth() {
        return super.getTrailingIconWidth() + (this.isCapsLockVisible() ? this.capsLockIcon.getIconWidth() + UIScale.scale(this.iconTextGap) : 0);
    }

    protected boolean isCapsLockVisible() {
        if (!this.showCapsLock) {
            return false;
        }
        return FlatUIUtils.isPermanentFocusOwner(this.getComponent()) && Toolkit.getDefaultToolkit().getLockingKeyState(20);
    }

    protected void installRevealButton() {
        if (this.showRevealButton) {
            this.revealButton = this.createRevealButton();
            this.updateRevealButton();
            this.installLayout();
            this.getComponent().add(this.revealButton);
        }
    }

    protected JToggleButton createRevealButton() {
        JPasswordField c = (JPasswordField)this.getComponent();
        JToggleButton button = new JToggleButton(this.revealIcon, !c.echoCharIsSet());
        button.setName("PasswordField.revealButton");
        this.prepareLeadingOrTrailingComponent(button);
        button.putClientProperty("FlatLaf.styleClass", "inTextField revealButton");
        if (FlatClientProperties.clientPropertyBoolean(c, KEY_REVEAL_SELECTED, false)) {
            button.setSelected(true);
            this.updateEchoChar(true);
        }
        button.addActionListener(e -> {
            boolean selected = button.isSelected();
            this.updateEchoChar(selected);
            c.putClientProperty(KEY_REVEAL_SELECTED, selected);
        });
        return button;
    }

    protected void updateRevealButton() {
        if (this.revealButton == null) {
            return;
        }
        JTextComponent c = this.getComponent();
        boolean visible = c.isEnabled();
        if (visible != this.revealButton.isVisible()) {
            this.revealButton.setVisible(visible);
            c.revalidate();
            c.repaint();
            if (!visible) {
                this.revealButton.setSelected(false);
                this.updateEchoChar(false);
                this.getComponent().putClientProperty(KEY_REVEAL_SELECTED, null);
            }
        }
    }

    @Override
    protected void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        switch (e.getPropertyName()) {
            case "enabled": {
                this.updateRevealButton();
            }
        }
    }

    private void updateEchoChar(boolean selected) {
        JPasswordField c;
        char newEchoChar = selected ? (char)'\u0000' : (this.echoChar != null ? (char)this.echoChar.charValue() : (char)'*');
        if (newEchoChar == (c = (JPasswordField)this.getComponent()).getEchoChar()) {
            return;
        }
        LookAndFeel.installProperty(c, "echoChar", Character.valueOf(newEchoChar));
        char actualEchoChar = c.getEchoChar();
        if (actualEchoChar != newEchoChar) {
            if (selected && actualEchoChar != '\u0000') {
                this.echoChar = Character.valueOf(actualEchoChar);
                this.uninstallEchoChar = true;
            }
            c.setEchoChar(newEchoChar);
        }
    }

    protected void uninstallRevealButton() {
        if (this.revealButton != null) {
            if (this.uninstallEchoChar && this.revealButton.isSelected()) {
                ((JPasswordField)this.getComponent()).setEchoChar(this.echoChar.charValue());
            }
            this.getComponent().remove(this.revealButton);
            this.revealButton = null;
        }
    }

    @Override
    protected JComponent[] getTrailingComponents() {
        return new JComponent[]{this.trailingComponent, this.revealButton, this.clearButton};
    }
}

