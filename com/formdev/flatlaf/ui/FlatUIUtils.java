/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.util.DerivedColor;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

public class FlatUIUtils {
    private static boolean useSharedUIs = true;
    private static final WeakHashMap<LookAndFeel, IdentityHashMap<Object, ComponentUI>> sharedUIinstances = new WeakHashMap();
    private static UIDefaults lightAWTPeerDefaults;
    public static final double MOVE_TO = -1.000000000001E12;
    public static final double QUAD_TO = -1.000000000002E12;
    public static final double CURVE_TO = -1.000000000003E12;
    public static final double ROUNDED = -1.000000000004E12;
    public static final double CLOSE_PATH = -1.000000000005E12;

    public static Rectangle addInsets(Rectangle r, Insets insets) {
        return new Rectangle(r.x - insets.left, r.y - insets.top, r.width + insets.left + insets.right, r.height + insets.top + insets.bottom);
    }

    public static Rectangle subtractInsets(Rectangle r, Insets insets) {
        return new Rectangle(r.x + insets.left, r.y + insets.top, r.width - insets.left - insets.right, r.height - insets.top - insets.bottom);
    }

    public static Dimension addInsets(Dimension dim, Insets insets) {
        return new Dimension(dim.width + insets.left + insets.right, dim.height + insets.top + insets.bottom);
    }

    public static Insets addInsets(Insets insets1, Insets insets2) {
        if (insets1 == null) {
            return insets2;
        }
        if (insets2 == null) {
            return insets1;
        }
        return new Insets(insets1.top + insets2.top, insets1.left + insets2.left, insets1.bottom + insets2.bottom, insets1.right + insets2.right);
    }

    public static void setInsets(Insets dest, Insets src) {
        dest.top = src.top;
        dest.left = src.left;
        dest.bottom = src.bottom;
        dest.right = src.right;
    }

    public static Color getUIColor(String key, int defaultColorRGB) {
        Color color = UIManager.getColor(key);
        return color != null ? color : new Color(defaultColorRGB);
    }

    public static Color getUIColor(String key, Color defaultColor) {
        Color color = UIManager.getColor(key);
        return color != null ? color : defaultColor;
    }

    public static Color getUIColor(String key, String defaultKey) {
        Color color = UIManager.getColor(key);
        return color != null ? color : UIManager.getColor(defaultKey);
    }

    public static boolean getUIBoolean(String key, boolean defaultValue) {
        Object value = UIManager.get(key);
        return value instanceof Boolean ? (Boolean)value : defaultValue;
    }

    public static int getUIInt(String key, int defaultValue) {
        Object value = UIManager.get(key);
        return value instanceof Integer ? (Integer)value : defaultValue;
    }

    public static float getUIFloat(String key, float defaultValue) {
        Object value = UIManager.get(key);
        return value instanceof Number ? ((Number)value).floatValue() : defaultValue;
    }

    public static <T extends Enum<T>> T getUIEnum(String key, Class<T> enumType, T defaultValue) {
        Object value = UIManager.get(key);
        if (value instanceof String) {
            try {
                return Enum.valueOf(enumType, (String)value);
            } catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public static Color getSubUIColor(String key, String subKey) {
        Color value;
        if (subKey != null && (value = UIManager.getColor(FlatUIUtils.buildSubKey(key, subKey))) != null) {
            return value;
        }
        return UIManager.getColor(key);
    }

    public static boolean getSubUIBoolean(String key, String subKey, boolean defaultValue) {
        Object value;
        if (subKey != null && (value = UIManager.get(FlatUIUtils.buildSubKey(key, subKey))) instanceof Boolean) {
            return (Boolean)value;
        }
        return FlatUIUtils.getUIBoolean(key, defaultValue);
    }

    public static int getSubUIInt(String key, String subKey, int defaultValue) {
        Object value;
        if (subKey != null && (value = UIManager.get(FlatUIUtils.buildSubKey(key, subKey))) instanceof Integer) {
            return (Integer)value;
        }
        return FlatUIUtils.getUIInt(key, defaultValue);
    }

    public static Insets getSubUIInsets(String key, String subKey) {
        Insets value;
        if (subKey != null && (value = UIManager.getInsets(FlatUIUtils.buildSubKey(key, subKey))) != null) {
            return value;
        }
        return UIManager.getInsets(key);
    }

    public static Dimension getSubUIDimension(String key, String subKey) {
        Dimension value;
        if (subKey != null && (value = UIManager.getDimension(FlatUIUtils.buildSubKey(key, subKey))) != null) {
            return value;
        }
        return UIManager.getDimension(key);
    }

    public static Icon getSubUIIcon(String key, String subKey) {
        Icon value;
        if (subKey != null && (value = UIManager.getIcon(FlatUIUtils.buildSubKey(key, subKey))) != null) {
            return value;
        }
        return UIManager.getIcon(key);
    }

    public static Font getSubUIFont(String key, String subKey) {
        Font value;
        if (subKey != null && (value = UIManager.getFont(FlatUIUtils.buildSubKey(key, subKey))) != null) {
            return value;
        }
        return UIManager.getFont(key);
    }

    private static String buildSubKey(String key, String subKey) {
        int dot = key.lastIndexOf(46);
        return dot >= 0 ? key.substring(0, dot) + '.' + subKey + '.' + key.substring(dot + 1) : key;
    }

    public static boolean getBoolean(JComponent c, String systemPropertyKey, String clientPropertyKey, String uiKey, boolean defaultValue) {
        Boolean value = FlatSystemProperties.getBooleanStrict(systemPropertyKey, null);
        if (value != null) {
            return value;
        }
        value = FlatClientProperties.clientPropertyBooleanStrict(c, clientPropertyKey, null);
        if (value != null) {
            return value;
        }
        return FlatUIUtils.getUIBoolean(uiKey, defaultValue);
    }

    public static boolean isChevron(String arrowType) {
        return !"triangle".equals(arrowType);
    }

    public static Color nonUIResource(Color c) {
        return c instanceof UIResource ? new Color(c.getRGB(), true) : c;
    }

    public static Font nonUIResource(Font font) {
        return font instanceof UIResource ? font.deriveFont(font.getStyle()) : font;
    }

    public static Border nonUIResource(Border border) {
        return border instanceof UIResource ? new NonUIResourceBorder(border) : border;
    }

    static Border unwrapNonUIResourceBorder(Border border) {
        return border instanceof NonUIResourceBorder ? ((NonUIResourceBorder)border).delegate : border;
    }

    public static int minimumWidth(JComponent c, int minimumWidth) {
        return FlatClientProperties.clientPropertyInt(c, "JComponent.minimumWidth", minimumWidth);
    }

    public static int minimumHeight(JComponent c, int minimumHeight) {
        return FlatClientProperties.clientPropertyInt(c, "JComponent.minimumHeight", minimumHeight);
    }

    public static boolean isCellEditor(Component c) {
        if (c == null) {
            return false;
        }
        Container parent = c.getParent();
        if (parent instanceof JTable && ((JTable)parent).getEditorComponent() == c) {
            return true;
        }
        String name = c.getName();
        if ("Table.editor".equals(name) || "Tree.cellEditor".equals(name)) {
            return true;
        }
        return c instanceof JComponent && Boolean.TRUE.equals(((JComponent)c).getClientProperty("JComboBox.isTableCellEditor"));
    }

    public static boolean isPermanentFocusOwner(Component c) {
        Object value;
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (c instanceof JComponent && (value = ((JComponent)c).getClientProperty("JComponent.focusOwner")) instanceof Predicate) {
            return ((Predicate)value).test((JComponent)c) && FlatUIUtils.isInActiveWindow(c, keyboardFocusManager.getActiveWindow());
        }
        if (c.hasFocus()) {
            return true;
        }
        return keyboardFocusManager.getPermanentFocusOwner() == c && FlatUIUtils.isInActiveWindow(c, keyboardFocusManager.getActiveWindow());
    }

    static boolean isInActiveWindow(Component c, Window activeWindow) {
        Window window = SwingUtilities.windowForComponent(c);
        return window == activeWindow || window != null && window.getType() == Window.Type.POPUP && window.getOwner() == activeWindow;
    }

    static boolean isAWTPeer(Component c) {
        if (SystemInfo.isMacOS) {
            return c.getClass().getName().startsWith("sun.lwawt.LW");
        }
        return false;
    }

    static boolean needsLightAWTPeer(JComponent c) {
        return FlatUIUtils.isAWTPeer(c) && FlatLaf.isLafDark();
    }

    static void runWithLightAWTPeerUIDefaults(Runnable runnable) {
        if (lightAWTPeerDefaults == null) {
            FlatLightLaf lightLaf = UIManager.getInt("Component.focusWidth") >= 2 ? new FlatIntelliJLaf() : new FlatLightLaf();
            lightAWTPeerDefaults = lightLaf.getDefaults();
        }
        FlatLaf.runWithUIDefaultsGetter(key -> {
            Object value = lightAWTPeerDefaults.get(key);
            return value != null ? value : FlatLaf.NULL_VALUE;
        }, runnable);
    }

    public static boolean isFullScreen(Component c) {
        GraphicsConfiguration gc = c.getGraphicsConfiguration();
        GraphicsDevice gd = gc != null ? gc.getDevice() : null;
        Window fullScreenWindow = gd != null ? gd.getFullScreenWindow() : null;
        return fullScreenWindow != null && fullScreenWindow == SwingUtilities.windowForComponent(c);
    }

    public static Boolean isRoundRect(Component c) {
        return c instanceof JComponent ? FlatClientProperties.clientPropertyBooleanStrict((JComponent)c, "JComponent.roundRect", null) : null;
    }

    public static float getBorderFocusWidth(JComponent c) {
        FlatBorder border = FlatUIUtils.getOutsideFlatBorder(c);
        return border != null ? UIScale.scale((float)border.getFocusWidth(c)) : 0.0f;
    }

    public static float getBorderLineWidth(JComponent c) {
        FlatBorder border = FlatUIUtils.getOutsideFlatBorder(c);
        return border != null ? UIScale.scale((float)border.getLineWidth(c)) : 0.0f;
    }

    public static int getBorderFocusAndLineWidth(JComponent c) {
        FlatBorder border = FlatUIUtils.getOutsideFlatBorder(c);
        return border != null ? Math.round(UIScale.scale((float)border.getFocusWidth(c)) + UIScale.scale((float)border.getLineWidth(c))) : 0;
    }

    public static float getBorderArc(JComponent c) {
        FlatBorder border = FlatUIUtils.getOutsideFlatBorder(c);
        return border != null ? UIScale.scale((float)border.getArc(c)) : 0.0f;
    }

    public static boolean hasRoundBorder(JComponent c) {
        return FlatUIUtils.getBorderArc(c) >= (float)c.getHeight();
    }

    public static FlatBorder getOutsideFlatBorder(JComponent c) {
        Border border = c.getBorder();
        while (true) {
            if (border instanceof FlatBorder) {
                return (FlatBorder)border;
            }
            if (!(border instanceof CompoundBorder)) break;
            border = ((CompoundBorder)border).getOutsideBorder();
        }
        return null;
    }

    public static Object[] setRenderingHints(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Object[] oldRenderingHints = new Object[]{g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING), g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL)};
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        return oldRenderingHints;
    }

    public static void resetRenderingHints(Graphics g, Object[] oldRenderingHints) {
        Graphics2D g2 = (Graphics2D)g;
        if (oldRenderingHints[0] != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldRenderingHints[0]);
        }
        if (oldRenderingHints[1] != null) {
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldRenderingHints[1]);
        }
    }

    public static void runWithoutRenderingHints(Graphics g, Object[] oldRenderingHints, Runnable runnable) {
        if (oldRenderingHints == null) {
            runnable.run();
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        Object[] oldRenderingHints2 = new Object[]{g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING), g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL)};
        FlatUIUtils.resetRenderingHints(g2, oldRenderingHints);
        runnable.run();
        FlatUIUtils.resetRenderingHints(g2, oldRenderingHints2);
    }

    public static Color deriveColor(Color color, Color baseColor) {
        return color instanceof DerivedColor ? ((DerivedColor)color).derive(baseColor) : color;
    }

    public static void paintComponentBackground(Graphics2D g, int x, int y, int width, int height, float focusWidth, float arc) {
        FlatUIUtils.paintOutlinedComponent(g, x, y, width, height, focusWidth, 0.0f, 0.0f, 0.0f, arc, null, null, g.getPaint());
    }

    public static void paintOutlinedComponent(Graphics2D g, int x, int y, int width, int height, float focusWidth, float focusWidthFraction, float focusInnerWidth, float borderWidth, float arc, Paint focusColor, Paint borderColor, Paint background) {
        double systemScaleFactor = UIScale.getSystemScaleFactor(g);
        if (systemScaleFactor != 1.0 && systemScaleFactor != 2.0) {
            HiDPIUtils.paintAtScale1x(g, x, y, width, height, (g2d, x2, y2, width2, height2, scaleFactor) -> FlatUIUtils.paintOutlinedComponentImpl(g2d, x2, y2, width2, height2, (float)((double)focusWidth * scaleFactor), focusWidthFraction, (float)((double)focusInnerWidth * scaleFactor), (float)((double)borderWidth * scaleFactor), (float)((double)arc * scaleFactor), focusColor, borderColor, background));
            return;
        }
        FlatUIUtils.paintOutlinedComponentImpl(g, x, y, width, height, focusWidth, focusWidthFraction, focusInnerWidth, borderWidth, arc, focusColor, borderColor, background);
    }

    private static void paintOutlinedComponentImpl(Graphics2D g, int x, int y, int width, int height, float focusWidth, float focusWidthFraction, float focusInnerWidth, float borderWidth, float arc, Paint focusColor, Paint borderColor, Paint background) {
        float x1 = (float)x + focusWidth;
        float y1 = (float)y + focusWidth;
        float w1 = (float)width - focusWidth * 2.0f;
        float h1 = (float)height - focusWidth * 2.0f;
        if (background != null) {
            g.setPaint(background);
            g.fill(FlatUIUtils.createComponentRectangle(x1, y1, w1, h1, arc));
        }
        if (borderColor != null && borderColor.equals(focusColor)) {
            borderColor = null;
            focusInnerWidth = Math.max(focusInnerWidth, borderWidth);
        }
        float paintedFocusWidth = focusWidth * focusWidthFraction + focusInnerWidth;
        if (focusColor != null && paintedFocusWidth != 0.0f) {
            float inset = focusWidth - focusWidth * focusWidthFraction;
            float x2 = (float)x + inset;
            float y2 = (float)y + inset;
            float w2 = (float)width - inset * 2.0f;
            float h2 = (float)height - inset * 2.0f;
            float outerArc = arc + focusWidth * 2.0f;
            float innerArc = arc - focusInnerWidth * 2.0f;
            if (focusWidth > 0.0f && arc > 0.0f && arc < (float)UIScale.scale(10)) {
                outerArc -= UIScale.scale(2.0f);
            }
            if (focusWidthFraction != 1.0f) {
                outerArc = arc + (outerArc - arc) * focusWidthFraction;
            }
            g.setPaint(focusColor);
            FlatUIUtils.paintOutline(g, x2, y2, w2, h2, paintedFocusWidth, outerArc, innerArc);
        }
        if (borderColor != null && borderWidth != 0.0f) {
            g.setPaint(borderColor);
            FlatUIUtils.paintOutline(g, x1, y1, w1, h1, borderWidth, arc);
        }
    }

    public static void paintOutline(Graphics2D g, float x, float y, float w, float h, float lineWidth, float arc) {
        FlatUIUtils.paintOutline(g, x, y, w, h, lineWidth, arc, arc - lineWidth * 2.0f);
    }

    public static void paintOutline(Graphics2D g, float x, float y, float w, float h, float lineWidth, float arc, float innerArc) {
        if (lineWidth == 0.0f || w <= 0.0f || h <= 0.0f) {
            return;
        }
        float t = lineWidth;
        float t2x = t * 2.0f;
        Path2D.Float border = new Path2D.Float(0);
        border.append(FlatUIUtils.createComponentRectangle(x, y, w, h, arc), false);
        border.append(FlatUIUtils.createComponentRectangle(x + t, y + t, w - t2x, h - t2x, innerArc), false);
        g.fill(border);
    }

    public static Shape createComponentRectangle(float x, float y, float w, float h, float arc) {
        if (arc <= 0.0f) {
            return new Rectangle2D.Float(x, y, w, h);
        }
        if (w == h && arc >= w) {
            return new Ellipse2D.Float(x, y, w, h);
        }
        arc = Math.min(arc, Math.min(w, h));
        return new RoundRectangle2D.Float(x, y, w, h, arc, arc);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void paintFilledRectangle(Graphics g, Color color, float x, float y, float w, float h) {
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setColor(color);
            g2.fill(new Rectangle2D.Float(x, y, w, h));
        } finally {
            g2.dispose();
        }
    }

    public static void paintSelection(Graphics2D g, int x, int y, int width, int height, Insets insets, float arcTopLeft, float arcTopRight, float arcBottomLeft, float arcBottomRight, int flags) {
        if (insets != null) {
            x += insets.left;
            y += insets.top;
            width -= insets.left + insets.right;
            height -= insets.top + insets.bottom;
        }
        if (arcTopLeft > 0.0f || arcTopRight > 0.0f || arcBottomLeft > 0.0f || arcBottomRight > 0.0f) {
            double systemScaleFactor = UIScale.getSystemScaleFactor(g);
            if (systemScaleFactor != 1.0 && systemScaleFactor != 2.0) {
                HiDPIUtils.paintAtScale1x(g, x, y, width, height, (g2d, x2, y2, width2, height2, scaleFactor) -> FlatUIUtils.paintRoundedSelectionImpl(g2d, x2, y2, width2, height2, (float)((double)arcTopLeft * scaleFactor), (float)((double)arcTopRight * scaleFactor), (float)((double)arcBottomLeft * scaleFactor), (float)((double)arcBottomRight * scaleFactor)));
            } else {
                FlatUIUtils.paintRoundedSelectionImpl(g, x, y, width, height, arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight);
            }
        } else {
            g.fillRect(x, y, width, height);
        }
    }

    private static void paintRoundedSelectionImpl(Graphics2D g, int x, int y, int width, int height, float arcTopLeft, float arcTopRight, float arcBottomLeft, float arcBottomRight) {
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        g.fill(FlatUIUtils.createRoundRectanglePath(x, y, width, height, arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight));
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }

    public static void paintGrip(Graphics g, int x, int y, int width, int height, boolean horizontal, int dotCount, int dotSize, int gap, boolean centerPrecise) {
        float gy;
        float gx;
        dotSize = UIScale.scale(dotSize);
        gap = UIScale.scale(gap);
        int gripSize = dotSize * dotCount + gap * (dotCount - 1);
        if (horizontal) {
            gx = x + Math.round((float)(width - gripSize) / 2.0f);
            gy = (float)y + (float)(height - dotSize) / 2.0f;
            if (!centerPrecise) {
                gy = Math.round(gy);
            }
        } else {
            gx = (float)x + (float)(width - dotSize) / 2.0f;
            gy = y + Math.round((float)(height - gripSize) / 2.0f);
            if (!centerPrecise) {
                gx = Math.round(gx);
            }
        }
        for (int i = 0; i < dotCount; ++i) {
            ((Graphics2D)g).fill(new Ellipse2D.Float(gx, gy, dotSize, dotSize));
            if (horizontal) {
                gx += (float)(dotSize + gap);
                continue;
            }
            gy += (float)(dotSize + gap);
        }
    }

    public static void paintParentBackground(Graphics g, JComponent c) {
        Color background = FlatUIUtils.getParentBackground(c);
        if (background != null) {
            g.setColor(background);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
    }

    public static Color getParentBackground(JComponent c) {
        Color background;
        Container parent = FlatUIUtils.findOpaqueParent(c);
        Color color = background = parent != null ? parent.getBackground() : null;
        if (background != null) {
            return background;
        }
        if (FlatUIUtils.isAWTPeer(c)) {
            return c instanceof JTextField || c instanceof JScrollPane || c.getBackground() == null ? SystemColor.window : c.getBackground();
        }
        return UIManager.getColor("Panel.background");
    }

    private static Container findOpaqueParent(Container c) {
        while ((c = c.getParent()) != null) {
            if (!c.isOpaque()) continue;
            return c;
        }
        return null;
    }

    public static Path2D createRectangle(float x, float y, float width, float height, float lineWidth) {
        Path2D.Float path = new Path2D.Float(0);
        path.append(new Rectangle2D.Float(x, y, width, height), false);
        path.append(new Rectangle2D.Float(x + lineWidth, y + lineWidth, width - lineWidth * 2.0f, height - lineWidth * 2.0f), false);
        return path;
    }

    public static Path2D createRoundRectangle(float x, float y, float width, float height, float lineWidth, float arcTopLeft, float arcTopRight, float arcBottomLeft, float arcBottomRight) {
        Path2D.Float path = new Path2D.Float(0);
        path.append(FlatUIUtils.createRoundRectanglePath(x, y, width, height, arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight), false);
        path.append(FlatUIUtils.createRoundRectanglePath(x + lineWidth, y + lineWidth, width - lineWidth * 2.0f, height - lineWidth * 2.0f, arcTopLeft - lineWidth, arcTopRight - lineWidth, arcBottomLeft - lineWidth, arcBottomRight - lineWidth), false);
        return path;
    }

    public static Shape createRoundRectanglePath(float x, float y, float width, float height, float arcTopLeft, float arcTopRight, float arcBottomLeft, float arcBottomRight) {
        if (arcTopLeft <= 0.0f && arcTopRight <= 0.0f && arcBottomLeft <= 0.0f && arcBottomRight <= 0.0f) {
            return new Rectangle2D.Float(x, y, width, height);
        }
        float maxArc = Math.min(width, height) / 2.0f;
        arcTopLeft = arcTopLeft > 0.0f ? Math.min(arcTopLeft, maxArc) : 0.0f;
        arcTopRight = arcTopRight > 0.0f ? Math.min(arcTopRight, maxArc) : 0.0f;
        arcBottomLeft = arcBottomLeft > 0.0f ? Math.min(arcBottomLeft, maxArc) : 0.0f;
        arcBottomRight = arcBottomRight > 0.0f ? Math.min(arcBottomRight, maxArc) : 0.0f;
        float x2 = x + width;
        float y2 = y + height;
        double c = 0.5522847498307933;
        double ci = 1.0 - c;
        double ciTopLeft = (double)arcTopLeft * ci;
        double ciTopRight = (double)arcTopRight * ci;
        double ciBottomLeft = (double)arcBottomLeft * ci;
        double ciBottomRight = (double)arcBottomRight * ci;
        Path2D.Float rect = new Path2D.Float(1, 16);
        ((Path2D)rect).moveTo(x2 - arcTopRight, y);
        ((Path2D)rect).curveTo((double)x2 - ciTopRight, y, x2, (double)y + ciTopRight, x2, y + arcTopRight);
        ((Path2D)rect).lineTo(x2, y2 - arcBottomRight);
        ((Path2D)rect).curveTo(x2, (double)y2 - ciBottomRight, (double)x2 - ciBottomRight, y2, x2 - arcBottomRight, y2);
        ((Path2D)rect).lineTo(x + arcBottomLeft, y2);
        ((Path2D)rect).curveTo((double)x + ciBottomLeft, y2, x, (double)y2 - ciBottomLeft, x, y2 - arcBottomLeft);
        ((Path2D)rect).lineTo(x, y + arcTopLeft);
        ((Path2D)rect).curveTo(x, (double)y + ciTopLeft, (double)x + ciTopLeft, y, x + arcTopLeft, y);
        rect.closePath();
        return rect;
    }

    public static Shape createRoundTrianglePath(float x1, float y1, float x2, float y2, float x3, float y3, float arc) {
        double averageSideLength = (FlatUIUtils.distance(x1, y1, x2, y2) + FlatUIUtils.distance(x2, y2, x3, y3) + FlatUIUtils.distance(x3, y3, x1, y1)) / 3.0;
        double t1 = 1.0 / averageSideLength * (double)arc;
        double t2 = 1.0 - t1;
        return FlatUIUtils.createPath(FlatUIUtils.lerp(x3, x1, t2), FlatUIUtils.lerp(y3, y1, t2), -1.000000000002E12, x1, y1, FlatUIUtils.lerp(x1, x2, t1), FlatUIUtils.lerp(y1, y2, t1), FlatUIUtils.lerp(x1, x2, t2), FlatUIUtils.lerp(y1, y2, t2), -1.000000000002E12, x2, y2, FlatUIUtils.lerp(x2, x3, t1), FlatUIUtils.lerp(y2, y3, t1), FlatUIUtils.lerp(x2, x3, t2), FlatUIUtils.lerp(y2, y3, t2), -1.000000000002E12, x3, y3, FlatUIUtils.lerp(x3, x1, t1), FlatUIUtils.lerp(y3, y1, t1));
    }

    public static void paintArrow(Graphics2D g, int x, int y, int width, int height, int direction, boolean chevron, int arrowSize, float arrowThickness, float xOffset, float yOffset) {
        boolean vert;
        float aw = UIScale.scale(arrowSize + (chevron ? -1 : 0));
        float ah = chevron ? aw / 2.0f : (float)UIScale.scale(arrowSize / 2 + 1);
        boolean bl = vert = direction == 1 || direction == 5;
        if (!vert) {
            float temp = aw;
            aw = ah;
            ah = temp;
        }
        boolean extra = chevron;
        float ox = ((float)width - (aw + (float)extra)) / 2.0f + UIScale.scale(xOffset);
        float oy = ((float)height - (ah + (float)extra)) / 2.0f + UIScale.scale(yOffset);
        float ax = (float)x + (direction == 7 ? (float)(-Math.round(-(ox + aw))) - aw : (float)Math.round(ox));
        float ay = (float)y + (direction == 1 ? (float)(-Math.round(-(oy + ah))) - ah : (float)Math.round(oy));
        g.translate(ax, ay);
        Shape arrowShape = FlatUIUtils.createArrowShape(direction, chevron, aw, ah);
        if (chevron) {
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(UIScale.scale(arrowThickness)));
            FlatUIUtils.drawShapePure(g, arrowShape);
            g.setStroke(oldStroke);
        } else {
            g.fill(arrowShape);
        }
        g.translate(-ax, -ay);
    }

    public static Shape createArrowShape(int direction, boolean chevron, float w, float h) {
        switch (direction) {
            case 1: {
                return FlatUIUtils.createPath(!chevron, 0.0, h, w / 2.0f, 0.0, w, h);
            }
            case 5: {
                return FlatUIUtils.createPath(!chevron, 0.0, 0.0, w / 2.0f, h, w, 0.0);
            }
            case 7: {
                return FlatUIUtils.createPath(!chevron, w, 0.0, 0.0, h / 2.0f, w, h);
            }
            case 3: {
                return FlatUIUtils.createPath(!chevron, 0.0, 0.0, w, h / 2.0f, 0.0, h);
            }
        }
        return new Path2D.Float();
    }

    public static Path2D createPath(double ... points) {
        return FlatUIUtils.createPath(true, points);
    }

    public static Path2D createPath(boolean close, double ... points) {
        Path2D.Float path = new Path2D.Float(1, points.length / 2 + (close ? 1 : 0));
        ((Path2D)path).moveTo(points[0], points[1]);
        int i = 2;
        while (i < points.length) {
            double p = points[i];
            if (p == -1.000000000001E12) {
                ((Path2D)path).moveTo(points[i + 1], points[i + 2]);
                i += 3;
                continue;
            }
            if (p == -1.000000000002E12) {
                ((Path2D)path).quadTo(points[i + 1], points[i + 2], points[i + 3], points[i + 4]);
                i += 5;
                continue;
            }
            if (p == -1.000000000003E12) {
                ((Path2D)path).curveTo(points[i + 1], points[i + 2], points[i + 3], points[i + 4], points[i + 5], points[i + 6]);
                i += 7;
                continue;
            }
            if (p == -1.000000000004E12) {
                double x = points[i + 1];
                double y = points[i + 2];
                double arc = points[i + 3];
                int ip2 = i + 4;
                if (points[ip2] == -1.000000000002E12 || points[ip2] == -1.000000000004E12) {
                    ++ip2;
                }
                Point2D p1 = path.getCurrentPoint();
                double x1 = p1.getX();
                double y1 = p1.getY();
                double x2 = points[ip2];
                double y2 = points[ip2 + 1];
                double d1 = FlatUIUtils.distance(x, y, x1, y1);
                double d2 = FlatUIUtils.distance(x, y, x2, y2);
                double t1 = 1.0 - 1.0 / d1 * arc;
                double t2 = 1.0 / d2 * arc;
                ((Path2D)path).lineTo(FlatUIUtils.lerp(x1, x, t1), FlatUIUtils.lerp(y1, y, t1));
                ((Path2D)path).quadTo(x, y, FlatUIUtils.lerp(x, x2, t2), FlatUIUtils.lerp(y, y2, t2));
                i += 4;
                continue;
            }
            if (p == -1.000000000005E12) {
                path.closePath();
                ++i;
                continue;
            }
            ((Path2D)path).lineTo(p, points[i + 1]);
            i += 2;
        }
        if (close) {
            path.closePath();
        }
        return path;
    }

    private static double lerp(double v1, double v2, double t) {
        return v1 * (1.0 - t) + v2 * t;
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static void drawShapePure(Graphics2D g, Shape shape) {
        Object oldStrokeControl = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(0.5, 0.5);
        g.draw(shape);
        g.translate(-0.5, -0.5);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeControl);
    }

    public static void drawString(JComponent c, Graphics g, String text, int x, int y) {
        HiDPIUtils.drawStringWithYCorrection(c, (Graphics2D)g, text, x, y);
    }

    public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, int x, int y) {
        if (underlinedIndex >= 0 && UIScale.getUserScaleFactor() > 1.0f) {
            g = new Graphics2DProxy((Graphics2D)g){

                @Override
                public void fillRect(int x, int y, int width, int height) {
                    if (height == 1) {
                        height = Math.round(UIScale.scale(0.9f));
                        y += height - 1;
                    }
                    super.fillRect(x, y, width, height);
                }
            };
        }
        HiDPIUtils.drawStringUnderlineCharAtWithYCorrection(c, (Graphics2D)g, text, underlinedIndex, x, y);
    }

    public static boolean hasOpaqueBeenExplicitlySet(JComponent c) {
        boolean oldOpaque = c.isOpaque();
        LookAndFeel.installProperty(c, "opaque", !oldOpaque);
        boolean explicitlySet = c.isOpaque() == oldOpaque;
        LookAndFeel.installProperty(c, "opaque", oldOpaque);
        return explicitlySet;
    }

    public static boolean isUseSharedUIs() {
        return useSharedUIs;
    }

    public static boolean setUseSharedUIs(boolean useSharedUIs) {
        boolean old = FlatUIUtils.useSharedUIs;
        FlatUIUtils.useSharedUIs = useSharedUIs;
        return old;
    }

    public static ComponentUI createSharedUI(Object key, Supplier<ComponentUI> newInstanceSupplier) {
        if (!useSharedUIs) {
            return newInstanceSupplier.get();
        }
        return sharedUIinstances.computeIfAbsent(UIManager.getLookAndFeel(), k -> new IdentityHashMap()).computeIfAbsent(key, k -> (ComponentUI)newInstanceSupplier.get());
    }

    public static boolean canUseSharedUI(JComponent c) {
        return !FlatStylingSupport.hasStyleProperty(c);
    }

    private static class NonUIResourceBorder
    implements Border {
        private final Border delegate;

        NonUIResourceBorder(Border delegate) {
            this.delegate = delegate;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            this.delegate.paintBorder(c, g, x, y, width, height);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return this.delegate.getBorderInsets(c);
        }

        @Override
        public boolean isBorderOpaque() {
            return this.delegate.isBorderOpaque();
        }
    }

    public static class RepaintFocusListener
    implements FocusListener {
        private final Component repaintComponent;
        private final Predicate<Component> repaintCondition;

        public RepaintFocusListener(Component repaintComponent, Predicate<Component> repaintCondition) {
            this.repaintComponent = repaintComponent;
            this.repaintCondition = repaintCondition;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (this.repaintCondition == null || this.repaintCondition.test(this.repaintComponent)) {
                this.repaintComponent.repaint();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (this.repaintCondition == null || this.repaintCondition.test(this.repaintComponent)) {
                this.repaintComponent.repaint();
            }
        }
    }
}

