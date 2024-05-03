/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

public class FlatCaret
extends DefaultCaret
implements UIResource {
    private static final String KEY_CARET_INFO = "FlatLaf.internal.caretInfo";
    private final String selectAllOnFocusPolicy;
    private final boolean selectAllOnMouseClick;
    private boolean inInstall;
    private boolean wasFocused;
    private boolean wasTemporaryLost;
    private boolean isMousePressed;
    private boolean isWordSelection;
    private boolean isLineSelection;
    private int dragSelectionStart;
    private int dragSelectionEnd;

    public FlatCaret(String selectAllOnFocusPolicy, boolean selectAllOnMouseClick) {
        this.selectAllOnFocusPolicy = selectAllOnFocusPolicy;
        this.selectAllOnMouseClick = selectAllOnMouseClick;
    }

    @Override
    public void install(JTextComponent c) {
        long[] ci = (long[])c.getClientProperty(KEY_CARET_INFO);
        if (ci != null) {
            c.putClientProperty(KEY_CARET_INFO, null);
            if (System.currentTimeMillis() - 500L > ci[3]) {
                ci = null;
            }
        }
        if (ci != null) {
            this.setBlinkRate((int)ci[2]);
        }
        this.inInstall = true;
        try {
            super.install(c);
        } finally {
            this.inInstall = false;
        }
        if (ci != null) {
            this.select((int)ci[1], (int)ci[0]);
            if (this.isSelectionVisible()) {
                EventQueue.invokeLater(() -> {
                    if (this.getComponent() == null) {
                        return;
                    }
                    if (this.isSelectionVisible()) {
                        this.setSelectionVisible(false);
                        this.setSelectionVisible(true);
                    }
                });
            }
        }
    }

    @Override
    public void deinstall(JTextComponent c) {
        c.putClientProperty(KEY_CARET_INFO, new long[]{this.getDot(), this.getMark(), this.getBlinkRate(), System.currentTimeMillis()});
        super.deinstall(c);
    }

    @Override
    protected void adjustVisibility(Rectangle nloc) {
        Rectangle r;
        JTextComponent c = this.getComponent();
        if (c != null && c.getUI() instanceof FlatTextFieldUI && (r = ((FlatTextFieldUI)c.getUI()).getVisibleEditorRect()) != null) {
            nloc.x -= r.x - c.getInsets().left;
        }
        super.adjustVisibility(nloc);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (!(this.inInstall || this.wasTemporaryLost || this.isMousePressed && !this.selectAllOnMouseClick)) {
            this.selectAllOnFocusGained();
        }
        this.wasTemporaryLost = false;
        this.wasFocused = true;
        super.focusGained(e);
    }

    @Override
    public void focusLost(FocusEvent e) {
        this.wasTemporaryLost = e.isTemporary();
        super.focusLost(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.isMousePressed = true;
        super.mousePressed(e);
        JTextComponent c = this.getComponent();
        this.isWordSelection = e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && !e.isConsumed();
        boolean bl = this.isLineSelection = e.getClickCount() == 3 && SwingUtilities.isLeftMouseButton(e) && (!e.isConsumed() || c.getDragEnabled());
        if (this.isLineSelection) {
            Action selectLineAction;
            ActionMap actionMap = c.getActionMap();
            Action action = selectLineAction = actionMap != null ? actionMap.get("select-line") : null;
            if (selectLineAction != null) {
                selectLineAction.actionPerformed(new ActionEvent(c, 1001, null, e.getWhen(), e.getModifiers()));
            }
        }
        if (this.isWordSelection || this.isLineSelection) {
            int mark = this.getMark();
            int dot = this.getDot();
            this.dragSelectionStart = Math.min(dot, mark);
            this.dragSelectionEnd = Math.max(dot, mark);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.isMousePressed = false;
        this.isWordSelection = false;
        this.isLineSelection = false;
        super.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        block7: {
            if ((this.isWordSelection || this.isLineSelection) && !e.isConsumed() && SwingUtilities.isLeftMouseButton(e)) {
                JTextComponent c = this.getComponent();
                int pos = c.viewToModel(e.getPoint());
                if (pos < 0) {
                    return;
                }
                try {
                    if (pos > this.dragSelectionEnd) {
                        this.select(this.dragSelectionStart, this.isWordSelection ? Utilities.getWordEnd(c, pos) : Utilities.getRowEnd(c, pos));
                        break block7;
                    }
                    if (pos < this.dragSelectionStart) {
                        this.select(this.dragSelectionEnd, this.isWordSelection ? Utilities.getWordStart(c, pos) : Utilities.getRowStart(c, pos));
                        break block7;
                    }
                    this.select(this.dragSelectionStart, this.dragSelectionEnd);
                } catch (BadLocationException ex) {
                    UIManager.getLookAndFeel().provideErrorFeedback(c);
                }
            } else {
                super.mouseDragged(e);
            }
        }
    }

    protected void selectAllOnFocusGained() {
        JTextComponent c = this.getComponent();
        Document doc = c.getDocument();
        if (doc == null || !c.isEnabled() || !c.isEditable() || FlatUIUtils.isCellEditor(c)) {
            return;
        }
        Object selectAllOnFocusPolicy = c.getClientProperty("JTextField.selectAllOnFocusPolicy");
        if (selectAllOnFocusPolicy == null) {
            selectAllOnFocusPolicy = this.selectAllOnFocusPolicy;
        }
        if (selectAllOnFocusPolicy == null || "never".equals(selectAllOnFocusPolicy)) {
            return;
        }
        if (!"always".equals(selectAllOnFocusPolicy)) {
            int mark;
            if (this.wasFocused) {
                return;
            }
            int dot = this.getDot();
            if (dot != (mark = this.getMark()) || dot != doc.getLength()) {
                return;
            }
        }
        if (c instanceof JFormattedTextField) {
            EventQueue.invokeLater(() -> {
                JTextComponent c2 = this.getComponent();
                if (c2 == null) {
                    return;
                }
                this.select(0, c2.getDocument().getLength());
            });
        } else {
            this.select(0, doc.getLength());
        }
    }

    private void select(int mark, int dot) {
        if (mark != this.getMark()) {
            this.setDot(mark);
        }
        if (dot != this.getDot()) {
            this.moveDot(dot);
        }
    }

    public void scrollCaretToVisible() {
        JTextComponent c = this.getComponent();
        if (c == null || c.getUI() == null) {
            return;
        }
        try {
            Rectangle loc = c.getUI().modelToView(c, this.getDot(), this.getDotBias());
            if (loc != null) {
                this.adjustVisibility(loc);
                this.damage(loc);
            }
        } catch (BadLocationException badLocationException) {
            // empty catch block
        }
    }
}

