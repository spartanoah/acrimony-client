/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.StackUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class FlatTreeUI
extends BasicTreeUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected Color selectionBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionForeground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveForeground;
    @FlatStylingSupport.Styleable
    protected Color selectionBorderColor;
    @FlatStylingSupport.Styleable
    protected Insets selectionInsets;
    @FlatStylingSupport.Styleable
    protected int selectionArc;
    @FlatStylingSupport.Styleable
    protected boolean wideSelection;
    @FlatStylingSupport.Styleable
    protected boolean showCellFocusIndicator;
    protected boolean showDefaultIcons;
    @FlatStylingSupport.Styleable(dot=true)
    public String iconArrowType;
    @FlatStylingSupport.Styleable(dot=true)
    public Color iconExpandedColor;
    @FlatStylingSupport.Styleable(dot=true)
    public Color iconCollapsedColor;
    @FlatStylingSupport.Styleable(dot=true)
    public Color iconLeafColor;
    @FlatStylingSupport.Styleable(dot=true)
    public Color iconClosedColor;
    @FlatStylingSupport.Styleable(dot=true)
    public Color iconOpenColor;
    @FlatStylingSupport.Styleable
    protected boolean paintSelection = true;
    private Icon defaultLeafIcon;
    private Icon defaultClosedIcon;
    private Icon defaultOpenIcon;
    private boolean paintLines;
    private Color defaultCellNonSelectionBackground;
    private Color defaultSelectionBackground;
    private Color defaultSelectionForeground;
    private Color defaultSelectionBorderColor;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTreeUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        LookAndFeel.installBorder(this.tree, "Tree.border");
        this.selectionBackground = UIManager.getColor("Tree.selectionBackground");
        this.selectionForeground = UIManager.getColor("Tree.selectionForeground");
        this.selectionInactiveBackground = UIManager.getColor("Tree.selectionInactiveBackground");
        this.selectionInactiveForeground = UIManager.getColor("Tree.selectionInactiveForeground");
        this.selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
        this.selectionInsets = UIManager.getInsets("Tree.selectionInsets");
        this.selectionArc = UIManager.getInt("Tree.selectionArc");
        this.wideSelection = UIManager.getBoolean("Tree.wideSelection");
        this.showCellFocusIndicator = UIManager.getBoolean("Tree.showCellFocusIndicator");
        this.showDefaultIcons = UIManager.getBoolean("Tree.showDefaultIcons");
        this.defaultLeafIcon = UIManager.getIcon("Tree.leafIcon");
        this.defaultClosedIcon = UIManager.getIcon("Tree.closedIcon");
        this.defaultOpenIcon = UIManager.getIcon("Tree.openIcon");
        this.paintLines = UIManager.getBoolean("Tree.paintLines");
        this.defaultCellNonSelectionBackground = UIManager.getColor("Tree.textBackground");
        this.defaultSelectionBackground = this.selectionBackground;
        this.defaultSelectionForeground = this.selectionForeground;
        this.defaultSelectionBorderColor = this.selectionBorderColor;
        int rowHeight = FlatUIUtils.getUIInt("Tree.rowHeight", 16);
        if (rowHeight > 0) {
            LookAndFeel.installProperty(this.tree, "rowHeight", UIScale.scale(rowHeight));
        }
        this.setLeftChildIndent(UIScale.scale(this.getLeftChildIndent()));
        this.setRightChildIndent(UIScale.scale(this.getRightChildIndent()));
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        LookAndFeel.uninstallBorder(this.tree);
        this.selectionBackground = null;
        this.selectionForeground = null;
        this.selectionInactiveBackground = null;
        this.selectionInactiveForeground = null;
        this.selectionBorderColor = null;
        this.defaultLeafIcon = null;
        this.defaultClosedIcon = null;
        this.defaultOpenIcon = null;
        this.defaultCellNonSelectionBackground = null;
        this.defaultSelectionBackground = null;
        this.defaultSelectionForeground = null;
        this.defaultSelectionBorderColor = null;
        this.oldStyleValues = null;
    }

    @Override
    protected void updateRenderer() {
        DefaultTreeCellRenderer renderer;
        super.updateRenderer();
        if (!this.showDefaultIcons && this.currentCellRenderer instanceof DefaultTreeCellRenderer && (renderer = (DefaultTreeCellRenderer)this.currentCellRenderer).getLeafIcon() == this.defaultLeafIcon && renderer.getClosedIcon() == this.defaultClosedIcon && renderer.getOpenIcon() == this.defaultOpenIcon) {
            renderer.setLeafIcon(null);
            renderer.setClosedIcon(null);
            renderer.setOpenIcon(null);
        }
    }

    @Override
    protected MouseListener createMouseListener() {
        return new BasicTreeUI.MouseHandler(){

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(this.handleWideMouseEvent(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(this.handleWideMouseEvent(e));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(this.handleWideMouseEvent(e));
            }

            private MouseEvent handleWideMouseEvent(MouseEvent e) {
                if (!FlatTreeUI.this.isWideSelection() || !FlatTreeUI.this.tree.isEnabled() || !SwingUtilities.isLeftMouseButton(e) || e.isConsumed()) {
                    return e;
                }
                int x = e.getX();
                int y = e.getY();
                TreePath path = FlatTreeUI.this.getClosestPathForLocation(FlatTreeUI.this.tree, x, y);
                if (path == null || FlatTreeUI.this.isLocationInExpandControl(path, x, y)) {
                    return e;
                }
                Rectangle bounds = FlatTreeUI.this.getPathBounds(FlatTreeUI.this.tree, path);
                if (bounds == null || y < bounds.y || y >= bounds.y + bounds.height) {
                    return e;
                }
                int newX = Math.max(bounds.x, Math.min(x, bounds.x + bounds.width - 1));
                if (newX == x) {
                    return e;
                }
                return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(), newX, e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
            }
        };
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        PropertyChangeListener superListener = super.createPropertyChangeListener();
        return e -> {
            superListener.propertyChange(e);
            if (e.getSource() == this.tree) {
                switch (e.getPropertyName()) {
                    case "JTree.wideSelection": 
                    case "JTree.paintSelection": {
                        this.tree.repaint();
                        break;
                    }
                    case "dropLocation": {
                        if (!this.isWideSelection()) break;
                        JTree.DropLocation oldValue = (JTree.DropLocation)e.getOldValue();
                        this.repaintWideDropLocation(oldValue);
                        this.repaintWideDropLocation(this.tree.getDropLocation());
                        break;
                    }
                    case "FlatLaf.style": 
                    case "FlatLaf.styleClass": {
                        this.installStyle();
                        this.tree.revalidate();
                        this.tree.repaint();
                        break;
                    }
                    case "enabled": {
                        if (this.showDefaultIcons || !(this.currentCellRenderer instanceof DefaultTreeCellRenderer) || this.currentCellRenderer.getClass() == DefaultTreeCellRenderer.class || this.treeState == null) break;
                        this.treeState.invalidateSizes();
                        this.updateSize();
                    }
                }
            }
        };
    }

    private void repaintWideDropLocation(JTree.DropLocation loc) {
        if (loc == null || this.isDropLine(loc)) {
            return;
        }
        Rectangle r = this.tree.getPathBounds(loc.getPath());
        if (r != null) {
            this.tree.repaint(0, r.y, this.tree.getWidth(), r.height);
        }
    }

    @Override
    protected TreeSelectionListener createTreeSelectionListener() {
        TreeSelectionListener superListener = super.createTreeSelectionListener();
        return e -> {
            TreePath[] changedPaths;
            superListener.valueChanged(e);
            if (this.useUnitedRoundedSelection() && this.tree.getSelectionCount() > 1 && (changedPaths = e.getPaths()) != null) {
                if (changedPaths.length > 4) {
                    this.tree.repaint();
                } else {
                    int arc = (int)Math.ceil(UIScale.scale((float)this.selectionArc / 2.0f));
                    for (TreePath path : changedPaths) {
                        Rectangle r = this.getPathBounds(this.tree, path);
                        if (r == null) continue;
                        this.tree.repaint(r.x, r.y - arc, r.width, r.height + arc * 2);
                    }
                }
            }
        };
    }

    @Override
    public Rectangle getPathBounds(JTree tree, TreePath path) {
        Rectangle bounds = super.getPathBounds(tree, path);
        if (bounds != null && this.isWideSelection() && UIManager.getBoolean("FlatLaf.experimental.tree.widePathForLocation") && StackUtils.wasInvokedFrom(JTree.class.getName(), "getPathForLocation", 5)) {
            bounds.x = 0;
            bounds.width = tree.getWidth();
        }
        return bounds;
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.tree, "Tree"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        if ("rowHeight".equals(key) && value instanceof Integer) {
            value = UIScale.scale((Integer)value);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.tree, key, value);
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
    public void paint(Graphics g, JComponent c) {
        if (this.treeState == null) {
            return;
        }
        Rectangle clipBounds = g.getClipBounds();
        TreePath firstPath = this.getClosestPathForLocation(this.tree, 0, clipBounds.y);
        Enumeration<TreePath> visiblePaths = this.treeState.getVisiblePathsFrom(firstPath);
        if (visiblePaths != null) {
            Rectangle bounds;
            TreePath path;
            ArrayList<Runnable> paintExpandControlsLater;
            Insets insets = this.tree.getInsets();
            HashSet<TreePath> verticalLinePaths = this.paintLines ? new HashSet<TreePath>() : null;
            ArrayList<Runnable> paintLinesLater = this.paintLines ? new ArrayList<Runnable>() : null;
            ArrayList<Runnable> arrayList = paintExpandControlsLater = this.paintLines ? new ArrayList<Runnable>() : null;
            if (this.paintLines) {
                for (TreePath path2 = firstPath.getParentPath(); path2 != null; path2 = path2.getParentPath()) {
                    verticalLinePaths.add(path2);
                }
            }
            Rectangle boundsBuffer = new Rectangle();
            boolean rootVisible = this.isRootVisible();
            int row = this.treeState.getRowForPath(firstPath);
            boolean leftToRight = this.tree.getComponentOrientation().isLeftToRight();
            int treeWidth = this.tree.getWidth();
            while (visiblePaths.hasMoreElements() && (path = visiblePaths.nextElement()) != null && (bounds = this.treeState.getBounds(path, boundsBuffer)) != null) {
                bounds.x = leftToRight ? (bounds.x += insets.left) : treeWidth - insets.right - (bounds.x + bounds.width);
                bounds.y += insets.top;
                boolean isLeaf = this.treeModel.isLeaf(path.getLastPathComponent());
                boolean isExpanded = isLeaf ? false : this.treeState.getExpandedState(path);
                boolean hasBeenExpanded = isLeaf ? false : this.tree.hasBeenExpanded(path);
                this.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
                if (this.paintLines) {
                    TreePath parentPath = path.getParentPath();
                    if (parentPath != null) {
                        verticalLinePaths.add(parentPath);
                    }
                    if (parentPath != null || rootVisible && row == 0) {
                        Rectangle bounds2 = new Rectangle(bounds);
                        int row2 = row;
                        paintLinesLater.add(() -> this.paintHorizontalPartOfLeg(g, clipBounds, insets, bounds2, path, row2, isExpanded, hasBeenExpanded, isLeaf));
                    }
                }
                if (this.shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
                    if (this.paintLines) {
                        Rectangle bounds2 = new Rectangle(bounds);
                        int row2 = row;
                        paintExpandControlsLater.add(() -> this.paintExpandControl(g, clipBounds, insets, bounds2, path, row2, isExpanded, hasBeenExpanded, isLeaf));
                    } else {
                        this.paintExpandControl(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
                    }
                }
                if (bounds.y + bounds.height >= clipBounds.y + clipBounds.height) break;
                ++row;
            }
            if (this.paintLines) {
                Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
                for (Runnable r : paintLinesLater) {
                    r.run();
                }
                g.setColor(Color.green);
                for (TreePath path3 : verticalLinePaths) {
                    this.paintVerticalPartOfLeg(g, clipBounds, insets, path3);
                }
                if (oldRenderingHints != null) {
                    FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
                }
                for (Runnable r : paintExpandControlsLater) {
                    r.run();
                }
            }
        }
        this.paintDropLine(g);
        this.rendererPane.removeAll();
    }

    @Override
    protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
        DefaultTreeCellRenderer renderer;
        Color bg;
        boolean cellHasFocus;
        boolean needsSelectionPainting;
        boolean isEditing = this.editingComponent != null && this.editingRow == row;
        boolean isSelected = this.tree.isRowSelected(row);
        boolean isDropRow = this.isDropRow(row);
        boolean bl = needsSelectionPainting = (isSelected || isDropRow) && this.isPaintSelection();
        if (isEditing) {
            if (isSelected && this.isWideSelection()) {
                Color oldColor = g.getColor();
                g.setColor(this.selectionInactiveBackground);
                this.paintWideSelection(g, bounds, row);
                g.setColor(oldColor);
            }
            return;
        }
        boolean hasFocus = FlatUIUtils.isPermanentFocusOwner(this.tree);
        boolean bl2 = cellHasFocus = hasFocus && row == this.getLeadSelectionRow();
        if (!hasFocus && isSelected && this.tree.getParent() instanceof CellRendererPane) {
            hasFocus = FlatUIUtils.isPermanentFocusOwner(this.tree.getParent().getParent());
        }
        Component rendererComponent = this.currentCellRenderer.getTreeCellRendererComponent(this.tree, path.getLastPathComponent(), isSelected, isExpanded, isLeaf, row, cellHasFocus);
        Color oldBackgroundSelectionColor = null;
        if (isSelected && !hasFocus && !isDropRow) {
            oldBackgroundSelectionColor = this.setRendererBackgroundSelectionColor(rendererComponent, this.selectionInactiveBackground);
            this.setRendererForeground(rendererComponent, this.selectionInactiveForeground);
        } else if (isSelected) {
            if (this.selectionBackground != this.defaultSelectionBackground) {
                oldBackgroundSelectionColor = this.setRendererBackgroundSelectionColor(rendererComponent, this.selectionBackground);
            }
            if (this.selectionForeground != this.defaultSelectionForeground) {
                this.setRendererForeground(rendererComponent, this.selectionForeground);
            }
        }
        Color oldBorderSelectionColor = null;
        if (isSelected && hasFocus && (!this.showCellFocusIndicator || this.tree.getMinSelectionRow() == this.tree.getMaxSelectionRow())) {
            oldBorderSelectionColor = this.setRendererBorderSelectionColor(rendererComponent, null);
        } else if (hasFocus && this.selectionBorderColor != this.defaultSelectionBorderColor) {
            oldBorderSelectionColor = this.setRendererBorderSelectionColor(rendererComponent, this.selectionBorderColor);
        }
        if (needsSelectionPainting) {
            Color oldColor = g.getColor();
            g.setColor(isDropRow ? UIManager.getColor("Tree.dropCellBackground") : (rendererComponent instanceof DefaultTreeCellRenderer ? ((DefaultTreeCellRenderer)rendererComponent).getBackgroundSelectionColor() : (hasFocus ? this.selectionBackground : this.selectionInactiveBackground)));
            if (this.isWideSelection()) {
                this.paintWideSelection(g, bounds, row);
            } else {
                this.paintCellBackground(g, rendererComponent, bounds, row, true);
            }
            g.setColor(oldColor);
        } else if (rendererComponent instanceof DefaultTreeCellRenderer && (bg = (renderer = (DefaultTreeCellRenderer)rendererComponent).getBackgroundNonSelectionColor()) != null && !bg.equals(this.defaultCellNonSelectionBackground)) {
            Color oldColor = g.getColor();
            g.setColor(bg);
            this.paintCellBackground(g, rendererComponent, bounds, row, false);
            g.setColor(oldColor);
        }
        this.rendererPane.paintComponent(g, rendererComponent, this.tree, bounds.x, bounds.y, bounds.width, bounds.height, true);
        if (oldBackgroundSelectionColor != null) {
            ((DefaultTreeCellRenderer)rendererComponent).setBackgroundSelectionColor(oldBackgroundSelectionColor);
        }
        if (oldBorderSelectionColor != null) {
            ((DefaultTreeCellRenderer)rendererComponent).setBorderSelectionColor(oldBorderSelectionColor);
        }
    }

    private Color setRendererBackgroundSelectionColor(Component rendererComponent, Color color) {
        Color oldColor = null;
        if (rendererComponent instanceof DefaultTreeCellRenderer) {
            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)rendererComponent;
            if (renderer.getBackgroundSelectionColor() == this.defaultSelectionBackground) {
                oldColor = renderer.getBackgroundSelectionColor();
                renderer.setBackgroundSelectionColor(color);
            }
        } else if (rendererComponent.getBackground() == this.defaultSelectionBackground) {
            rendererComponent.setBackground(color);
        }
        return oldColor;
    }

    private void setRendererForeground(Component rendererComponent, Color color) {
        if (rendererComponent.getForeground() == this.defaultSelectionForeground) {
            rendererComponent.setForeground(color);
        }
    }

    private Color setRendererBorderSelectionColor(Component rendererComponent, Color color) {
        DefaultTreeCellRenderer renderer;
        Color oldColor = null;
        if (rendererComponent instanceof DefaultTreeCellRenderer && (renderer = (DefaultTreeCellRenderer)rendererComponent).getBorderSelectionColor() == this.defaultSelectionBorderColor) {
            oldColor = renderer.getBorderSelectionColor();
            renderer.setBorderSelectionColor(color);
        }
        return oldColor;
    }

    private void paintWideSelection(Graphics g, Rectangle bounds, int row) {
        float arcBottom;
        float arcTop = arcBottom = UIScale.scale((float)this.selectionArc / 2.0f);
        if (this.useUnitedRoundedSelection()) {
            if (row > 0 && this.tree.isRowSelected(row - 1)) {
                arcTop = 0.0f;
            }
            if (row < this.tree.getRowCount() - 1 && this.tree.isRowSelected(row + 1)) {
                arcBottom = 0.0f;
            }
        }
        FlatUIUtils.paintSelection((Graphics2D)g, 0, bounds.y, this.tree.getWidth(), bounds.height, UIScale.scale(this.selectionInsets), arcTop, arcTop, arcBottom, arcBottom, 0);
    }

    private void paintCellBackground(Graphics g, Component rendererComponent, Rectangle bounds, int row, boolean paintSelection) {
        int xOffset = 0;
        int imageOffset = 0;
        if (rendererComponent instanceof JLabel) {
            JLabel label = (JLabel)rendererComponent;
            Icon icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
            imageOffset = icon != null && label.getText() != null ? icon.getIconWidth() + Math.max(label.getIconTextGap() - 1, 0) : 0;
            int n = xOffset = label.getComponentOrientation().isLeftToRight() ? imageOffset : 0;
        }
        if (paintSelection) {
            float arcBottomRight;
            float arcBottomLeft = arcBottomRight = UIScale.scale((float)this.selectionArc / 2.0f);
            float arcTopRight = arcBottomRight;
            float arcTopLeft = arcBottomRight;
            if (this.useUnitedRoundedSelection()) {
                Rectangle r;
                if (row > 0 && this.tree.isRowSelected(row - 1)) {
                    r = this.getPathBounds(this.tree, this.tree.getPathForRow(row - 1));
                    arcTopLeft = Math.min(arcTopLeft, (float)(r.x - bounds.x));
                    arcTopRight = Math.min(arcTopRight, (float)(bounds.x + bounds.width - (r.x + r.width)));
                }
                if (row < this.tree.getRowCount() - 1 && this.tree.isRowSelected(row + 1)) {
                    r = this.getPathBounds(this.tree, this.tree.getPathForRow(row + 1));
                    arcBottomLeft = Math.min(arcBottomLeft, (float)(r.x - bounds.x));
                    arcBottomRight = Math.min(arcBottomRight, (float)(bounds.x + bounds.width - (r.x + r.width)));
                }
            }
            FlatUIUtils.paintSelection((Graphics2D)g, bounds.x + xOffset, bounds.y, bounds.width - imageOffset, bounds.height, UIScale.scale(this.selectionInsets), arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight, 0);
        } else {
            g.fillRect(bounds.x + xOffset, bounds.y, bounds.width - imageOffset, bounds.height);
        }
    }

    private boolean useUnitedRoundedSelection() {
        return this.selectionArc > 0 && (this.selectionInsets == null || this.selectionInsets.top == 0 && this.selectionInsets.bottom == 0);
    }

    @Override
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
        ((Graphics2D)g).fill(new Rectangle2D.Float(x, top, UIScale.scale(1.0f), bottom - top));
    }

    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
        ((Graphics2D)g).fill(new Rectangle2D.Float(left, y, right - left, UIScale.scale(1.0f)));
    }

    private boolean isDropRow(int row) {
        JTree.DropLocation dropLocation = this.tree.getDropLocation();
        return dropLocation != null && dropLocation.getChildIndex() == -1 && this.tree.getRowForPath(dropLocation.getPath()) == row;
    }

    @Override
    protected Rectangle getDropLineRect(JTree.DropLocation loc) {
        Rectangle r = super.getDropLineRect(loc);
        return this.isWideSelection() ? new Rectangle(0, r.y, this.tree.getWidth(), r.height) : r;
    }

    protected boolean isWideSelection() {
        return FlatClientProperties.clientPropertyBoolean(this.tree, "JTree.wideSelection", this.wideSelection);
    }

    protected boolean isPaintSelection() {
        return FlatClientProperties.clientPropertyBoolean(this.tree, "JTree.paintSelection", this.paintSelection);
    }
}

