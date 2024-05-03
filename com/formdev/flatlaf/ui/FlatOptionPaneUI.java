/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SwingUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicOptionPaneUI;

public class FlatOptionPaneUI
extends BasicOptionPaneUI {
    protected boolean showIcon;
    protected int iconMessageGap;
    protected int messagePadding;
    protected int maxCharactersPerLine;
    private int focusWidth;
    private boolean sameSizeButtons;

    public static ComponentUI createUI(JComponent c) {
        return new FlatOptionPaneUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.showIcon = UIManager.getBoolean("OptionPane.showIcon");
        this.iconMessageGap = UIManager.getInt("OptionPane.iconMessageGap");
        this.messagePadding = UIManager.getInt("OptionPane.messagePadding");
        this.maxCharactersPerLine = UIManager.getInt("OptionPane.maxCharactersPerLine");
        this.focusWidth = UIManager.getInt("Component.focusWidth");
        this.sameSizeButtons = FlatUIUtils.getUIBoolean("OptionPane.sameSizeButtons", true);
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        PropertyChangeListener superListener = super.createPropertyChangeListener();
        return e -> {
            JRootPane rootPane;
            superListener.propertyChange(e);
            if (!this.showIcon && "ancestor".equals(e.getPropertyName()) && e.getNewValue() != null && (rootPane = SwingUtilities.getRootPane(this.optionPane)) != null && rootPane.getContentPane().getComponentCount() > 0 && rootPane.getContentPane().getComponent(0) == this.optionPane) {
                rootPane.putClientProperty("JRootPane.titleBarShowIcon", false);
            }
        };
    }

    @Override
    public Dimension getMinimumOptionPaneSize() {
        return UIScale.scale(super.getMinimumOptionPaneSize());
    }

    @Override
    protected int getMaxCharactersPerLineCount() {
        int max = super.getMaxCharactersPerLineCount();
        return this.maxCharactersPerLine > 0 && max == Integer.MAX_VALUE ? this.maxCharactersPerLine : max;
    }

    @Override
    protected Container createMessageArea() {
        Object iconMessageSeparator;
        Container messageArea = super.createMessageArea();
        this.updateAreaPanel(messageArea);
        this.updateKnownChildPanels(messageArea);
        if (this.iconMessageGap > 0 && (iconMessageSeparator = SwingUtils.getComponentByName(messageArea, "OptionPane.separator")) != null) {
            ((Component)iconMessageSeparator).setPreferredSize(new Dimension(UIScale.scale(this.iconMessageGap), 1));
        }
        return messageArea;
    }

    @Override
    protected Container createButtonArea() {
        Container buttonArea = super.createButtonArea();
        this.updateAreaPanel(buttonArea);
        if (buttonArea.getLayout() instanceof BasicOptionPaneUI.ButtonAreaLayout) {
            BasicOptionPaneUI.ButtonAreaLayout layout = (BasicOptionPaneUI.ButtonAreaLayout)buttonArea.getLayout();
            layout.setPadding(UIScale.scale(layout.getPadding() - this.focusWidth * 2));
        }
        return buttonArea;
    }

    @Override
    protected void addMessageComponents(Container container, GridBagConstraints cons, Object msg, int maxll, boolean internallyCreated) {
        Box box;
        if (this.messagePadding > 0) {
            cons.insets.bottom = UIScale.scale(this.messagePadding);
        }
        if (msg != null && !(msg instanceof Component) && !(msg instanceof Object[]) && !(msg instanceof Icon) && BasicHTML.isHTMLString((String)(msg = msg.toString()))) {
            maxll = Integer.MAX_VALUE;
        }
        if (msg instanceof Box && "OptionPane.verticalBox".equals((box = (Box)msg).getName()) && box.getLayout() instanceof BoxLayout && ((BoxLayout)box.getLayout()).getAxis() == 1) {
            box.addPropertyChangeListener("componentOrientation", e -> {
                float alignX = box.getComponentOrientation().isLeftToRight() ? 0.0f : 1.0f;
                for (Component c : box.getComponents()) {
                    if (!(c instanceof JLabel) || !"OptionPane.label".equals(c.getName())) continue;
                    ((JLabel)c).setAlignmentX(alignX);
                }
            });
        }
        super.addMessageComponents(container, cons, msg, maxll, internallyCreated);
    }

    private void updateAreaPanel(Container area) {
        if (!(area instanceof JPanel)) {
            return;
        }
        JPanel panel = (JPanel)area;
        panel.setBorder(FlatUIUtils.nonUIResource(panel.getBorder()));
        panel.setOpaque(false);
    }

    private void updateKnownChildPanels(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JPanel && child.getName() != null) {
                switch (child.getName()) {
                    case "OptionPane.realBody": 
                    case "OptionPane.body": 
                    case "OptionPane.separator": 
                    case "OptionPane.break": {
                        ((JPanel)child).setOpaque(false);
                    }
                }
            }
            if (!(child instanceof Container)) continue;
            this.updateKnownChildPanels((Container)child);
        }
    }

    @Override
    protected boolean getSizeButtonsToSameWidth() {
        return this.sameSizeButtons;
    }
}

