/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

public class FlatSliderUI
extends BasicSliderUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected int trackWidth;
    @FlatStylingSupport.Styleable
    protected Dimension thumbSize;
    @FlatStylingSupport.Styleable
    protected int focusWidth;
    @FlatStylingSupport.Styleable
    protected float thumbBorderWidth;
    @FlatStylingSupport.Styleable
    protected Color trackValueColor;
    @FlatStylingSupport.Styleable
    protected Color trackColor;
    @FlatStylingSupport.Styleable
    protected Color thumbColor;
    @FlatStylingSupport.Styleable
    protected Color thumbBorderColor;
    protected Color focusBaseColor;
    @FlatStylingSupport.Styleable
    protected Color focusedColor;
    @FlatStylingSupport.Styleable
    protected Color focusedThumbBorderColor;
    @FlatStylingSupport.Styleable
    protected Color hoverThumbColor;
    @FlatStylingSupport.Styleable
    protected Color pressedThumbColor;
    @FlatStylingSupport.Styleable
    protected Color disabledTrackColor;
    @FlatStylingSupport.Styleable
    protected Color disabledThumbColor;
    @FlatStylingSupport.Styleable
    protected Color disabledThumbBorderColor;
    @FlatStylingSupport.Styleable
    protected Color tickColor;
    private Color defaultBackground;
    private Color defaultForeground;
    protected boolean thumbHover;
    protected boolean thumbPressed;
    private Object[] oldRenderingHints;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatSliderUI();
    }

    public FlatSliderUI() {
        super(null);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults(JSlider slider) {
        super.installDefaults(slider);
        LookAndFeel.installProperty(slider, "opaque", false);
        this.trackWidth = UIManager.getInt("Slider.trackWidth");
        this.thumbSize = UIManager.getDimension("Slider.thumbSize");
        if (this.thumbSize == null) {
            int thumbWidth = UIManager.getInt("Slider.thumbWidth");
            this.thumbSize = new Dimension(thumbWidth, thumbWidth);
        }
        this.focusWidth = FlatUIUtils.getUIInt("Slider.focusWidth", 4);
        this.thumbBorderWidth = FlatUIUtils.getUIFloat("Slider.thumbBorderWidth", 1.0f);
        this.trackValueColor = FlatUIUtils.getUIColor("Slider.trackValueColor", "Slider.thumbColor");
        this.trackColor = UIManager.getColor("Slider.trackColor");
        this.thumbColor = UIManager.getColor("Slider.thumbColor");
        this.thumbBorderColor = UIManager.getColor("Slider.thumbBorderColor");
        this.focusBaseColor = UIManager.getColor("Component.focusColor");
        this.focusedColor = FlatUIUtils.getUIColor("Slider.focusedColor", this.focusBaseColor);
        this.focusedThumbBorderColor = FlatUIUtils.getUIColor("Slider.focusedThumbBorderColor", "Component.focusedBorderColor");
        this.hoverThumbColor = UIManager.getColor("Slider.hoverThumbColor");
        this.pressedThumbColor = UIManager.getColor("Slider.pressedThumbColor");
        this.disabledTrackColor = UIManager.getColor("Slider.disabledTrackColor");
        this.disabledThumbColor = UIManager.getColor("Slider.disabledThumbColor");
        this.disabledThumbBorderColor = FlatUIUtils.getUIColor("Slider.disabledThumbBorderColor", "Component.disabledBorderColor");
        this.tickColor = FlatUIUtils.getUIColor("Slider.tickColor", Color.BLACK);
        this.defaultBackground = UIManager.getColor("Slider.background");
        this.defaultForeground = UIManager.getColor("Slider.foreground");
    }

    @Override
    protected void uninstallDefaults(JSlider slider) {
        super.uninstallDefaults(slider);
        this.trackValueColor = null;
        this.trackColor = null;
        this.thumbColor = null;
        this.thumbBorderColor = null;
        this.focusBaseColor = null;
        this.focusedColor = null;
        this.focusedThumbBorderColor = null;
        this.hoverThumbColor = null;
        this.pressedThumbColor = null;
        this.disabledTrackColor = null;
        this.disabledThumbColor = null;
        this.disabledThumbBorderColor = null;
        this.tickColor = null;
        this.defaultBackground = null;
        this.defaultForeground = null;
        this.oldStyleValues = null;
    }

    @Override
    protected BasicSliderUI.TrackListener createTrackListener(JSlider slider) {
        return new FlatTrackListener();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener(JSlider slider) {
        return FlatStylingSupport.createPropertyChangeListener(slider, this::installStyle, super.createPropertyChangeListener(slider));
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.slider, "Slider"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.slider, key, value);
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
    public int getBaseline(JComponent c, int width, int height) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        if (this.slider.getOrientation() == 1) {
            return -1;
        }
        Font font = UIManager.getFont("defaultFont");
        if (font == null) {
            font = this.slider.getFont();
        }
        FontMetrics fm = this.slider.getFontMetrics(font);
        Insets insets = this.slider.getInsets();
        int thumbHeight = this.getThumbSize().height;
        int contentHeight = height - insets.top - insets.bottom - this.focusInsets.top - this.focusInsets.bottom;
        int centerSpacing = thumbHeight + (this.slider.getPaintTicks() ? this.getTickLength() : 0) + (this.slider.getPaintLabels() ? this.getHeightOfTallestLabel() : 0);
        int trackY = insets.top + this.focusInsets.top + (contentHeight - centerSpacing - 1) / 2;
        int trackHeight = thumbHeight;
        return trackY + Math.round((float)(trackHeight - fm.getHeight()) / 2.0f) + fm.getAscent() - 1;
    }

    @Override
    public Dimension getPreferredHorizontalSize() {
        return UIScale.scale(super.getPreferredHorizontalSize());
    }

    @Override
    public Dimension getPreferredVerticalSize() {
        return UIScale.scale(super.getPreferredVerticalSize());
    }

    @Override
    public Dimension getMinimumHorizontalSize() {
        return UIScale.scale(super.getMinimumHorizontalSize());
    }

    @Override
    public Dimension getMinimumVerticalSize() {
        return UIScale.scale(super.getMinimumVerticalSize());
    }

    @Override
    protected int getTickLength() {
        return UIScale.scale(super.getTickLength());
    }

    @Override
    protected Dimension getThumbSize() {
        return FlatSliderUI.calcThumbSize(this.slider, this.thumbSize, this.focusWidth);
    }

    public static Dimension calcThumbSize(JSlider slider, Dimension thumbSize, int focusWidth) {
        int fw = UIScale.scale(focusWidth);
        int w = UIScale.scale(thumbSize.width) + fw + fw;
        int h = UIScale.scale(thumbSize.height) + fw + fw;
        return slider.getOrientation() == 0 ? new Dimension(w, h) : new Dimension(h, w);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        this.oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        super.paint(g, c);
        FlatUIUtils.resetRenderingHints(g, this.oldRenderingHints);
        this.oldRenderingHints = null;
    }

    @Override
    public void paintLabels(Graphics g) {
        FlatUIUtils.runWithoutRenderingHints(g, this.oldRenderingHints, () -> super.paintLabels(g));
    }

    @Override
    public void paintFocus(Graphics g) {
    }

    @Override
    public void paintTrack(Graphics g) {
        RoundRectangle2D.Float track;
        float tw;
        boolean enabled = this.slider.isEnabled();
        float arc = tw = UIScale.scale((float)this.trackWidth);
        RoundRectangle2D.Float coloredTrack = null;
        if (this.slider.getOrientation() == 0) {
            float y = (float)this.trackRect.y + ((float)this.trackRect.height - tw) / 2.0f;
            if (enabled && this.isRoundThumb()) {
                if (this.slider.getComponentOrientation().isLeftToRight()) {
                    int cw = this.thumbRect.x + this.thumbRect.width / 2 - this.trackRect.x;
                    coloredTrack = new RoundRectangle2D.Float(this.trackRect.x, y, cw, tw, arc, arc);
                    track = new RoundRectangle2D.Float(this.trackRect.x + cw, y, this.trackRect.width - cw, tw, arc, arc);
                } else {
                    int cw = this.trackRect.x + this.trackRect.width - this.thumbRect.x - this.thumbRect.width / 2;
                    coloredTrack = new RoundRectangle2D.Float(this.trackRect.x + this.trackRect.width - cw, y, cw, tw, arc, arc);
                    track = new RoundRectangle2D.Float(this.trackRect.x, y, this.trackRect.width - cw, tw, arc, arc);
                }
            } else {
                track = new RoundRectangle2D.Float(this.trackRect.x, y, this.trackRect.width, tw, arc, arc);
            }
        } else {
            float x = (float)this.trackRect.x + ((float)this.trackRect.width - tw) / 2.0f;
            if (enabled && this.isRoundThumb()) {
                int ch = this.thumbRect.y + this.thumbRect.height / 2 - this.trackRect.y;
                track = new RoundRectangle2D.Float(x, this.trackRect.y, tw, ch, arc, arc);
                coloredTrack = new RoundRectangle2D.Float(x, this.trackRect.y + ch, tw, this.trackRect.height - ch, arc, arc);
            } else {
                track = new RoundRectangle2D.Float(x, this.trackRect.y, tw, this.trackRect.height, arc, arc);
            }
        }
        if (coloredTrack != null) {
            if (this.slider.getInverted()) {
                RoundRectangle2D.Float temp = track;
                track = coloredTrack;
                coloredTrack = temp;
            }
            g.setColor(this.getTrackValueColor());
            ((Graphics2D)g).fill(coloredTrack);
        }
        g.setColor(enabled ? this.getTrackColor() : this.disabledTrackColor);
        ((Graphics2D)g).fill(track);
    }

    @Override
    public void paintTicks(Graphics g) {
        super.paintTicks(new Graphics2DProxy((Graphics2D)g){

            @Override
            public void setColor(Color c) {
                super.setColor(FlatSliderUI.this.tickColor);
            }
        });
    }

    @Override
    public void paintThumb(Graphics g) {
        Color thumbColor = this.getThumbColor();
        Color color = FlatSliderUI.stateColor(this.slider, this.thumbHover, this.thumbPressed, thumbColor, this.disabledThumbColor, null, this.hoverThumbColor, this.pressedThumbColor);
        color = FlatUIUtils.deriveColor(color, thumbColor);
        Color foreground = this.slider.getForeground();
        Color borderColor = this.thumbBorderColor != null && foreground == this.defaultForeground ? FlatSliderUI.stateColor(this.slider, false, false, this.thumbBorderColor, this.disabledThumbBorderColor, this.focusedThumbBorderColor, null, null) : null;
        Color focusedColor = FlatUIUtils.deriveColor(this.focusedColor, foreground != this.defaultForeground ? foreground : this.focusBaseColor);
        FlatSliderUI.paintThumb(g, this.slider, this.thumbRect, this.isRoundThumb(), color, borderColor, focusedColor, this.thumbBorderWidth, this.focusWidth);
    }

    public static void paintThumb(Graphics g, JSlider slider, Rectangle thumbRect, boolean roundThumb, Color thumbColor, Color thumbBorderColor, Color focusedColor, float thumbBorderWidth, int focusWidth) {
        double systemScaleFactor = UIScale.getSystemScaleFactor((Graphics2D)g);
        if (systemScaleFactor != 1.0 && systemScaleFactor != 2.0) {
            HiDPIUtils.paintAtScale1x((Graphics2D)g, thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, (g2d, x2, y2, width2, height2, scaleFactor) -> FlatSliderUI.paintThumbImpl(g, slider, x2, y2, width2, height2, roundThumb, thumbColor, thumbBorderColor, focusedColor, (float)((double)thumbBorderWidth * scaleFactor), (float)((double)focusWidth * scaleFactor)));
            return;
        }
        FlatSliderUI.paintThumbImpl(g, slider, thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, roundThumb, thumbColor, thumbBorderColor, focusedColor, thumbBorderWidth, focusWidth);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void paintThumbImpl(Graphics g, JSlider slider, int x, int y, int width, int height, boolean roundThumb, Color thumbColor, Color thumbBorderColor, Color focusedColor, float thumbBorderWidth, float focusWidth) {
        int fw = Math.round(UIScale.scale(focusWidth));
        int tx = x + fw;
        int ty = y + fw;
        int tw = width - fw - fw;
        int th = height - fw - fw;
        boolean focused = FlatUIUtils.isPermanentFocusOwner(slider);
        if (roundThumb) {
            if (focused) {
                g.setColor(focusedColor);
                ((Graphics2D)g).fill(FlatSliderUI.createRoundThumbShape(x, y, width, height));
            }
            if (thumbBorderColor != null) {
                g.setColor(thumbBorderColor);
                ((Graphics2D)g).fill(FlatSliderUI.createRoundThumbShape(tx, ty, tw, th));
                float lw = UIScale.scale(thumbBorderWidth);
                g.setColor(thumbColor);
                ((Graphics2D)g).fill(FlatSliderUI.createRoundThumbShape((float)tx + lw, (float)ty + lw, (float)tw - lw - lw, (float)th - lw - lw));
            } else {
                g.setColor(thumbColor);
                ((Graphics2D)g).fill(FlatSliderUI.createRoundThumbShape(tx, ty, tw, th));
            }
        } else {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                g2.translate(x, y);
                if (slider.getOrientation() == 1) {
                    if (slider.getComponentOrientation().isLeftToRight()) {
                        g2.translate(0, height);
                        g2.rotate(Math.toRadians(270.0));
                    } else {
                        g2.translate(width, 0);
                        g2.rotate(Math.toRadians(90.0));
                    }
                    int temp = tw;
                    tw = th;
                    th = temp;
                }
                if (focused) {
                    g2.setColor(focusedColor);
                    g2.fill(FlatSliderUI.createDirectionalThumbShape(0.0f, 0.0f, tw + fw + fw, (float)(th + fw + fw) + (float)fw * 0.4142f, fw));
                }
                if (thumbBorderColor != null) {
                    g2.setColor(thumbBorderColor);
                    g2.fill(FlatSliderUI.createDirectionalThumbShape(fw, fw, tw, th, 0.0f));
                    float lw = UIScale.scale(thumbBorderWidth);
                    g2.setColor(thumbColor);
                    g2.fill(FlatSliderUI.createDirectionalThumbShape((float)fw + lw, (float)fw + lw, (float)tw - lw - lw, (float)th - lw - lw - lw * 0.4142f, 0.0f));
                } else {
                    g2.setColor(thumbColor);
                    g2.fill(FlatSliderUI.createDirectionalThumbShape(fw, fw, tw, th, 0.0f));
                }
            } finally {
                g2.dispose();
            }
        }
    }

    public static Shape createRoundThumbShape(float x, float y, float w, float h) {
        if (w == h) {
            return new Ellipse2D.Float(x, y, w, h);
        }
        float arc = Math.min(w, h);
        return new RoundRectangle2D.Float(x, y, w, h, arc, arc);
    }

    public static Shape createDirectionalThumbShape(float x, float y, float w, float h, float arc) {
        float wh = w / 2.0f;
        Path2D.Float path = new Path2D.Float(1, 9);
        ((Path2D)path).moveTo(x + wh, y + h);
        ((Path2D)path).lineTo(x, y + (h - wh));
        ((Path2D)path).lineTo(x, y + arc);
        ((Path2D)path).quadTo(x, y, x + arc, y);
        ((Path2D)path).lineTo(x + (w - arc), y);
        ((Path2D)path).quadTo(x + w, y, x + w, y + arc);
        ((Path2D)path).lineTo(x + w, y + (h - wh));
        path.closePath();
        return path;
    }

    protected Color getTrackValueColor() {
        Color foreground = this.slider.getForeground();
        return foreground != this.defaultForeground ? foreground : this.trackValueColor;
    }

    protected Color getTrackColor() {
        Color backround = this.slider.getBackground();
        return backround != this.defaultBackground ? backround : this.trackColor;
    }

    protected Color getThumbColor() {
        Color foreground = this.slider.getForeground();
        return foreground != this.defaultForeground ? foreground : this.thumbColor;
    }

    public static Color stateColor(JSlider slider, boolean hover, boolean pressed, Color enabledColor, Color disabledColor, Color focusedColor, Color hoverColor, Color pressedColor) {
        if (disabledColor != null && !slider.isEnabled()) {
            return disabledColor;
        }
        if (pressedColor != null && pressed) {
            return pressedColor;
        }
        if (hoverColor != null && hover) {
            return hoverColor;
        }
        if (focusedColor != null && FlatUIUtils.isPermanentFocusOwner(slider)) {
            return focusedColor;
        }
        return enabledColor;
    }

    protected boolean isRoundThumb() {
        return !this.slider.getPaintTicks() && !this.slider.getPaintLabels();
    }

    @Override
    public void setThumbLocation(int x, int y) {
        if (!this.isRoundThumb()) {
            Rectangle r = new Rectangle(this.thumbRect);
            this.thumbRect.setLocation(x, y);
            SwingUtilities.computeUnion(this.thumbRect.x, this.thumbRect.y, this.thumbRect.width, this.thumbRect.height, r);
            int extra = (int)Math.ceil((float)UIScale.scale(this.focusWidth) * 0.4142f);
            if (this.slider.getOrientation() == 0) {
                r.height += extra;
            } else {
                r.width += extra;
                if (!this.slider.getComponentOrientation().isLeftToRight()) {
                    r.x -= extra;
                }
            }
            this.slider.repaint(r);
        } else {
            super.setThumbLocation(x, y);
        }
    }

    protected class FlatTrackListener
    extends BasicSliderUI.TrackListener {
        protected FlatTrackListener() {
            super(FlatSliderUI.this);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.setThumbHover(this.isOverThumb(e));
            super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.setThumbHover(false);
            super.mouseExited(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            this.setThumbHover(this.isOverThumb(e));
            super.mouseMoved(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.setThumbPressed(this.isOverThumb(e));
            if (!FlatSliderUI.this.slider.isEnabled()) {
                return;
            }
            if (UIManager.getBoolean("Slider.scrollOnTrackClick")) {
                super.mousePressed(e);
                return;
            }
            int x = e.getX();
            int y = e.getY();
            FlatSliderUI.this.calculateGeometry();
            if (FlatSliderUI.this.thumbRect.contains(x, y)) {
                super.mousePressed(e);
                return;
            }
            if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && !SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            int tx = ((FlatSliderUI)FlatSliderUI.this).thumbRect.x + ((FlatSliderUI)FlatSliderUI.this).thumbRect.width / 2 - x;
            int ty = ((FlatSliderUI)FlatSliderUI.this).thumbRect.y + ((FlatSliderUI)FlatSliderUI.this).thumbRect.height / 2 - y;
            e.translatePoint(tx, ty);
            super.mousePressed(e);
            e.translatePoint(-tx, -ty);
            this.mouseDragged(e);
            this.setThumbPressed(true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.setThumbPressed(false);
            super.mouseReleased(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (FlatSliderUI.this.isDragging() && FlatSliderUI.this.slider.getSnapToTicks() && FlatSliderUI.this.slider.isEnabled() && !UIManager.getBoolean("Slider.snapToTicksOnReleased")) {
                FlatSliderUI.this.calculateThumbLocation();
                FlatSliderUI.this.slider.repaint();
            }
        }

        protected void setThumbHover(boolean hover) {
            if (hover != FlatSliderUI.this.thumbHover) {
                FlatSliderUI.this.thumbHover = hover;
                FlatSliderUI.this.slider.repaint(FlatSliderUI.this.thumbRect);
            }
        }

        protected void setThumbPressed(boolean pressed) {
            if (pressed != FlatSliderUI.this.thumbPressed) {
                FlatSliderUI.this.thumbPressed = pressed;
                FlatSliderUI.this.slider.repaint(FlatSliderUI.this.thumbRect);
            }
        }

        protected boolean isOverThumb(MouseEvent e) {
            return e != null && FlatSliderUI.this.slider.isEnabled() && FlatSliderUI.this.thumbRect.contains(e.getX(), e.getY());
        }
    }
}

