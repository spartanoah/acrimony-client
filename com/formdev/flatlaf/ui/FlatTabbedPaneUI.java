/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatArrowButton;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import com.formdev.flatlaf.util.JavaCompatibility;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

public class FlatTabbedPaneUI
extends BasicTabbedPaneUI
implements FlatStylingSupport.StyleableUI {
    protected static final int TAB_TYPE_UNDERLINED = 0;
    protected static final int TAB_TYPE_CARD = 1;
    protected static final int NEVER = 0;
    protected static final int AS_NEEDED = 2;
    protected static final int AS_NEEDED_SINGLE = 3;
    protected static final int BOTH = 100;
    protected static final int FILL = 100;
    protected static final int WIDTH_MODE_PREFERRED = 0;
    protected static final int WIDTH_MODE_EQUAL = 1;
    protected static final int WIDTH_MODE_COMPACT = 2;
    protected static final int NONE = -1;
    protected static final int AUTO = -2;
    private static Set<KeyStroke> focusForwardTraversalKeys;
    private static Set<KeyStroke> focusBackwardTraversalKeys;
    protected Color foreground;
    @FlatStylingSupport.Styleable
    protected Color disabledForeground;
    @FlatStylingSupport.Styleable
    protected Color selectedBackground;
    @FlatStylingSupport.Styleable
    protected Color selectedForeground;
    @FlatStylingSupport.Styleable
    protected Color underlineColor;
    @FlatStylingSupport.Styleable
    protected Color inactiveUnderlineColor;
    @FlatStylingSupport.Styleable
    protected Color disabledUnderlineColor;
    @FlatStylingSupport.Styleable
    protected Color hoverColor;
    @FlatStylingSupport.Styleable
    protected Color hoverForeground;
    @FlatStylingSupport.Styleable
    protected Color focusColor;
    @FlatStylingSupport.Styleable
    protected Color focusForeground;
    @FlatStylingSupport.Styleable
    protected Color tabSeparatorColor;
    @FlatStylingSupport.Styleable
    protected Color contentAreaColor;
    private int textIconGapUnscaled;
    @FlatStylingSupport.Styleable
    protected int minimumTabWidth;
    @FlatStylingSupport.Styleable
    protected int maximumTabWidth;
    @FlatStylingSupport.Styleable
    protected int tabHeight;
    @FlatStylingSupport.Styleable
    protected int tabSelectionHeight;
    @FlatStylingSupport.Styleable
    protected int cardTabSelectionHeight;
    @FlatStylingSupport.Styleable
    protected int tabArc;
    @FlatStylingSupport.Styleable
    protected int tabSelectionArc;
    @FlatStylingSupport.Styleable
    protected int cardTabArc;
    @FlatStylingSupport.Styleable
    protected Insets selectedInsets;
    @FlatStylingSupport.Styleable
    protected Insets tabSelectionInsets;
    @FlatStylingSupport.Styleable
    protected int contentSeparatorHeight;
    @FlatStylingSupport.Styleable
    protected boolean showTabSeparators;
    @FlatStylingSupport.Styleable
    protected boolean tabSeparatorsFullHeight;
    @FlatStylingSupport.Styleable
    protected boolean hasFullBorder;
    @FlatStylingSupport.Styleable
    protected boolean tabsOpaque = true;
    @FlatStylingSupport.Styleable
    protected boolean rotateTabRuns = true;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabType;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabsPopupPolicy;
    @FlatStylingSupport.Styleable(type=String.class)
    private int scrollButtonsPolicy;
    @FlatStylingSupport.Styleable(type=String.class)
    private int scrollButtonsPlacement;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabAreaAlignment;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabAlignment;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabWidthMode;
    @FlatStylingSupport.Styleable(type=String.class)
    private int tabRotation;
    protected Icon closeIcon;
    @FlatStylingSupport.Styleable
    protected String arrowType;
    @FlatStylingSupport.Styleable
    protected Insets buttonInsets;
    @FlatStylingSupport.Styleable
    protected int buttonArc;
    @FlatStylingSupport.Styleable
    protected Color buttonHoverBackground;
    @FlatStylingSupport.Styleable
    protected Color buttonPressedBackground;
    @FlatStylingSupport.Styleable
    protected String moreTabsButtonToolTipText;
    @FlatStylingSupport.Styleable
    protected String tabCloseToolTipText;
    @FlatStylingSupport.Styleable
    protected boolean showContentSeparator = true;
    @FlatStylingSupport.Styleable
    protected boolean hideTabAreaWithOneTab;
    @FlatStylingSupport.Styleable
    protected boolean tabClosable;
    @FlatStylingSupport.Styleable
    protected int tabIconPlacement = 10;
    protected JViewport tabViewport;
    protected FlatWheelTabScroller wheelTabScroller;
    private JButton tabCloseButton;
    private JButton moreTabsButton;
    private Container leadingComponent;
    private Container trailingComponent;
    private Dimension scrollBackwardButtonPrefSize;
    private Handler handler;
    private boolean blockRollover;
    private boolean rolloverTabClose;
    private boolean pressedTabClose;
    private boolean inBasicLayoutContainer;
    private Object[] oldRenderingHints;
    private Map<String, Object> oldStyleValues;
    private boolean closeIconShared = true;
    private boolean repaintRolloverPending;
    private boolean inCalculateEqual;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTabbedPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        String tabLayoutPolicyStr = UIManager.getString("TabbedPane.tabLayoutPolicy");
        if (tabLayoutPolicyStr != null) {
            int tabLayoutPolicy;
            switch (tabLayoutPolicyStr) {
                default: {
                    tabLayoutPolicy = 0;
                    break;
                }
                case "scroll": {
                    tabLayoutPolicy = 1;
                }
            }
            ((JTabbedPane)c).setTabLayoutPolicy(tabLayoutPolicy);
        }
        this.arrowType = UIManager.getString("TabbedPane.arrowType");
        this.foreground = UIManager.getColor("TabbedPane.foreground");
        this.disabledForeground = UIManager.getColor("TabbedPane.disabledForeground");
        this.buttonHoverBackground = UIManager.getColor("TabbedPane.buttonHoverBackground");
        this.buttonPressedBackground = UIManager.getColor("TabbedPane.buttonPressedBackground");
        super.installUI(c);
        FlatSelectedTabRepainter.install();
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        if (UIManager.getBoolean("TabbedPane.tabsOverlapBorder")) {
            Object oldValue = UIManager.put("TabbedPane.tabsOverlapBorder", false);
            super.installDefaults();
            UIManager.put("TabbedPane.tabsOverlapBorder", oldValue);
        } else {
            super.installDefaults();
        }
        this.selectedBackground = UIManager.getColor("TabbedPane.selectedBackground");
        this.selectedForeground = UIManager.getColor("TabbedPane.selectedForeground");
        this.underlineColor = UIManager.getColor("TabbedPane.underlineColor");
        this.inactiveUnderlineColor = FlatUIUtils.getUIColor("TabbedPane.inactiveUnderlineColor", this.underlineColor);
        this.disabledUnderlineColor = UIManager.getColor("TabbedPane.disabledUnderlineColor");
        this.hoverColor = UIManager.getColor("TabbedPane.hoverColor");
        this.hoverForeground = UIManager.getColor("TabbedPane.hoverForeground");
        this.focusColor = UIManager.getColor("TabbedPane.focusColor");
        this.focusForeground = UIManager.getColor("TabbedPane.focusForeground");
        this.tabSeparatorColor = UIManager.getColor("TabbedPane.tabSeparatorColor");
        this.contentAreaColor = UIManager.getColor("TabbedPane.contentAreaColor");
        this.textIconGapUnscaled = UIManager.getInt("TabbedPane.textIconGap");
        this.minimumTabWidth = UIManager.getInt("TabbedPane.minimumTabWidth");
        this.maximumTabWidth = UIManager.getInt("TabbedPane.maximumTabWidth");
        this.tabHeight = UIManager.getInt("TabbedPane.tabHeight");
        this.tabSelectionHeight = UIManager.getInt("TabbedPane.tabSelectionHeight");
        this.cardTabSelectionHeight = UIManager.getInt("TabbedPane.cardTabSelectionHeight");
        this.tabArc = UIManager.getInt("TabbedPane.tabArc");
        this.tabSelectionArc = UIManager.getInt("TabbedPane.tabSelectionArc");
        this.cardTabArc = UIManager.getInt("TabbedPane.cardTabArc");
        this.selectedInsets = UIManager.getInsets("TabbedPane.selectedInsets");
        this.tabSelectionInsets = UIManager.getInsets("TabbedPane.tabSelectionInsets");
        this.contentSeparatorHeight = UIManager.getInt("TabbedPane.contentSeparatorHeight");
        this.showTabSeparators = UIManager.getBoolean("TabbedPane.showTabSeparators");
        this.tabSeparatorsFullHeight = UIManager.getBoolean("TabbedPane.tabSeparatorsFullHeight");
        this.hasFullBorder = UIManager.getBoolean("TabbedPane.hasFullBorder");
        this.tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
        this.rotateTabRuns = FlatUIUtils.getUIBoolean("TabbedPane.rotateTabRuns", true);
        this.tabType = FlatTabbedPaneUI.parseTabType(UIManager.getString("TabbedPane.tabType"));
        this.tabsPopupPolicy = FlatTabbedPaneUI.parseTabsPopupPolicy(UIManager.getString("TabbedPane.tabsPopupPolicy"));
        this.scrollButtonsPolicy = FlatTabbedPaneUI.parseScrollButtonsPolicy(UIManager.getString("TabbedPane.scrollButtonsPolicy"));
        this.scrollButtonsPlacement = FlatTabbedPaneUI.parseScrollButtonsPlacement(UIManager.getString("TabbedPane.scrollButtonsPlacement"));
        this.tabAreaAlignment = FlatTabbedPaneUI.parseAlignment(UIManager.getString("TabbedPane.tabAreaAlignment"), 10);
        this.tabAlignment = FlatTabbedPaneUI.parseAlignment(UIManager.getString("TabbedPane.tabAlignment"), 0);
        this.tabWidthMode = FlatTabbedPaneUI.parseTabWidthMode(UIManager.getString("TabbedPane.tabWidthMode"));
        this.tabRotation = FlatTabbedPaneUI.parseTabRotation(UIManager.getString("TabbedPane.tabRotation"));
        this.closeIcon = UIManager.getIcon("TabbedPane.closeIcon");
        this.closeIconShared = true;
        this.buttonInsets = UIManager.getInsets("TabbedPane.buttonInsets");
        this.buttonArc = UIManager.getInt("TabbedPane.buttonArc");
        Locale l = this.tabPane.getLocale();
        this.moreTabsButtonToolTipText = UIManager.getString((Object)"TabbedPane.moreTabsButtonToolTipText", l);
        this.tabCloseToolTipText = UIManager.getString((Object)"TabbedPane.tabCloseToolTipText", l);
        this.textIconGap = UIScale.scale(this.textIconGapUnscaled);
        if (focusForwardTraversalKeys == null) {
            focusForwardTraversalKeys = Collections.singleton(KeyStroke.getKeyStroke(9, 0));
            focusBackwardTraversalKeys = Collections.singleton(KeyStroke.getKeyStroke(9, 64));
        }
        this.tabPane.setFocusTraversalKeys(0, focusForwardTraversalKeys);
        this.tabPane.setFocusTraversalKeys(1, focusBackwardTraversalKeys);
        MigLayoutVisualPadding.install(this.tabPane, null);
    }

    @Override
    protected void uninstallDefaults() {
        this.tabPane.setFocusTraversalKeys(0, null);
        this.tabPane.setFocusTraversalKeys(1, null);
        super.uninstallDefaults();
        this.foreground = null;
        this.disabledForeground = null;
        this.selectedBackground = null;
        this.selectedForeground = null;
        this.underlineColor = null;
        this.inactiveUnderlineColor = null;
        this.disabledUnderlineColor = null;
        this.hoverColor = null;
        this.hoverForeground = null;
        this.focusColor = null;
        this.focusForeground = null;
        this.tabSeparatorColor = null;
        this.contentAreaColor = null;
        this.closeIcon = null;
        this.buttonHoverBackground = null;
        this.buttonPressedBackground = null;
        this.oldStyleValues = null;
        MigLayoutVisualPadding.uninstall(this.tabPane);
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        this.tabViewport = null;
        if (this.isScrollTabLayout()) {
            for (Component c : this.tabPane.getComponents()) {
                if (!(c instanceof JViewport) || !c.getClass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI$ScrollableTabViewport")) continue;
                this.tabViewport = (JViewport)c;
                break;
            }
        }
        this.installHiddenTabsNavigation();
        this.installLeadingComponent();
        this.installTrailingComponent();
    }

    @Override
    protected void uninstallComponents() {
        this.uninstallHiddenTabsNavigation();
        this.uninstallLeadingComponent();
        this.uninstallTrailingComponent();
        super.uninstallComponents();
        this.tabCloseButton = null;
        this.tabViewport = null;
    }

    protected void installHiddenTabsNavigation() {
        if (!this.isScrollTabLayout() || this.tabViewport == null) {
            return;
        }
        this.tabPane.setLayout(this.createScrollLayoutManager((BasicTabbedPaneUI.TabbedPaneLayout)this.tabPane.getLayout()));
        this.moreTabsButton = this.createMoreTabsButton();
        this.tabPane.add(this.moreTabsButton);
    }

    protected void uninstallHiddenTabsNavigation() {
        if (this.tabPane.getLayout() instanceof FlatTabbedPaneScrollLayout) {
            this.tabPane.setLayout(((FlatTabbedPaneScrollLayout)this.tabPane.getLayout()).delegate);
        }
        if (this.moreTabsButton != null) {
            this.tabPane.remove(this.moreTabsButton);
            this.moreTabsButton = null;
        }
    }

    protected void installLeadingComponent() {
        Object c = this.tabPane.getClientProperty("JTabbedPane.leadingComponent");
        if (c instanceof Component) {
            this.leadingComponent = new ContainerUIResource((Component)c);
            this.tabPane.add(this.leadingComponent);
        }
    }

    protected void uninstallLeadingComponent() {
        if (this.leadingComponent != null) {
            this.tabPane.remove(this.leadingComponent);
            this.leadingComponent = null;
        }
    }

    protected void installTrailingComponent() {
        Object c = this.tabPane.getClientProperty("JTabbedPane.trailingComponent");
        if (c instanceof Component) {
            this.trailingComponent = new ContainerUIResource((Component)c);
            this.tabPane.add(this.trailingComponent);
        }
    }

    protected void uninstallTrailingComponent() {
        if (this.trailingComponent != null) {
            this.tabPane.remove(this.trailingComponent);
            this.trailingComponent = null;
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.getHandler().installListeners();
        if (this.tabViewport != null && (this.wheelTabScroller = this.createWheelTabScroller()) != null) {
            this.tabPane.addMouseWheelListener(this.wheelTabScroller);
            this.tabPane.addMouseMotionListener(this.wheelTabScroller);
            this.tabPane.addMouseListener(this.wheelTabScroller);
        }
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        if (this.handler != null) {
            this.handler.uninstallListeners();
            this.handler = null;
        }
        if (this.wheelTabScroller != null) {
            this.wheelTabScroller.uninstall();
            this.tabPane.removeMouseWheelListener(this.wheelTabScroller);
            this.tabPane.removeMouseMotionListener(this.wheelTabScroller);
            this.tabPane.removeMouseListener(this.wheelTabScroller);
            this.wheelTabScroller = null;
        }
    }

    @Override
    protected void installKeyboardActions() {
        super.installKeyboardActions();
        ActionMap map = SwingUtilities.getUIActionMap(this.tabPane);
        if (map != null) {
            RunWithOriginalLayoutManagerDelegateAction.install(map, "scrollTabsForwardAction");
            RunWithOriginalLayoutManagerDelegateAction.install(map, "scrollTabsBackwardAction");
        }
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected FlatWheelTabScroller createWheelTabScroller() {
        return new FlatWheelTabScroller();
    }

    @Override
    protected MouseListener createMouseListener() {
        Handler handler = this.getHandler();
        handler.mouseDelegate = super.createMouseListener();
        return handler;
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        Handler handler = this.getHandler();
        handler.propertyChangeDelegate = super.createPropertyChangeListener();
        return handler;
    }

    @Override
    protected ChangeListener createChangeListener() {
        Handler handler = this.getHandler();
        handler.changeDelegate = super.createChangeListener();
        return handler;
    }

    @Override
    protected FocusListener createFocusListener() {
        Handler handler = this.getHandler();
        handler.focusDelegate = super.createFocusListener();
        return handler;
    }

    @Override
    protected LayoutManager createLayoutManager() {
        if (this.tabPane.getTabLayoutPolicy() == 0) {
            return new FlatTabbedPaneLayout();
        }
        return super.createLayoutManager();
    }

    protected LayoutManager createScrollLayoutManager(BasicTabbedPaneUI.TabbedPaneLayout delegate) {
        return new FlatTabbedPaneScrollLayout(delegate);
    }

    protected JButton createMoreTabsButton() {
        return new FlatMoreTabsButton();
    }

    @Override
    protected JButton createScrollButton(int direction) {
        return new FlatScrollableTabButton(direction);
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.tabPane, "TabbedPane"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        for (Component c : this.tabPane.getComponents()) {
            if (!(c instanceof FlatTabAreaButton)) continue;
            ((FlatTabAreaButton)c).updateStyle();
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        if (key.startsWith("close")) {
            if (!(this.closeIcon instanceof FlatTabbedPaneCloseIcon)) {
                return new FlatStylingSupport.UnknownStyleException(key);
            }
            if (this.closeIconShared) {
                this.closeIcon = FlatStylingSupport.cloneIcon(this.closeIcon);
                this.closeIconShared = false;
            }
            return ((FlatTabbedPaneCloseIcon)this.closeIcon).applyStyleProperty(key, value);
        }
        if (value instanceof String) {
            switch (key) {
                case "tabType": {
                    value = FlatTabbedPaneUI.parseTabType((String)value);
                    break;
                }
                case "tabsPopupPolicy": {
                    value = FlatTabbedPaneUI.parseTabsPopupPolicy((String)value);
                    break;
                }
                case "scrollButtonsPolicy": {
                    value = FlatTabbedPaneUI.parseScrollButtonsPolicy((String)value);
                    break;
                }
                case "scrollButtonsPlacement": {
                    value = FlatTabbedPaneUI.parseScrollButtonsPlacement((String)value);
                    break;
                }
                case "tabAreaAlignment": {
                    value = FlatTabbedPaneUI.parseAlignment((String)value, 10);
                    break;
                }
                case "tabAlignment": {
                    value = FlatTabbedPaneUI.parseAlignment((String)value, 0);
                    break;
                }
                case "tabWidthMode": {
                    value = FlatTabbedPaneUI.parseTabWidthMode((String)value);
                    break;
                }
                case "tabRotation": {
                    value = FlatTabbedPaneUI.parseTabRotation((String)value);
                    break;
                }
                case "tabIconPlacement": {
                    value = FlatTabbedPaneUI.parseTabIconPlacement((String)value);
                }
            }
        } else {
            switch (key) {
                case "tabInsets": {
                    Insets oldValue = this.tabInsets;
                    this.tabInsets = (Insets)value;
                    return oldValue;
                }
                case "tabAreaInsets": {
                    Insets oldValue = this.tabAreaInsets;
                    this.tabAreaInsets = (Insets)value;
                    return oldValue;
                }
                case "textIconGap": {
                    Integer oldValue = this.textIconGapUnscaled;
                    this.textIconGapUnscaled = (Integer)value;
                    this.textIconGap = UIScale.scale(this.textIconGapUnscaled);
                    return oldValue;
                }
            }
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.tabPane, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        FlatStylingSupport.StyleableInfosMap infos = new FlatStylingSupport.StyleableInfosMap();
        infos.put("tabInsets", Insets.class);
        infos.put("tabAreaInsets", Insets.class);
        infos.put("textIconGap", Integer.TYPE);
        FlatStylingSupport.collectAnnotatedStyleableInfos(this, infos);
        if (this.closeIcon instanceof FlatTabbedPaneCloseIcon) {
            infos.putAll(((FlatTabbedPaneCloseIcon)this.closeIcon).getStyleableInfos());
        }
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        if (key.startsWith("close")) {
            return this.closeIcon instanceof FlatTabbedPaneCloseIcon ? ((FlatTabbedPaneCloseIcon)this.closeIcon).getStyleableValue(key) : null;
        }
        switch (key) {
            case "tabInsets": {
                return this.tabInsets;
            }
            case "tabAreaInsets": {
                return this.tabAreaInsets;
            }
            case "textIconGap": {
                return this.textIconGapUnscaled;
            }
            case "tabType": {
                switch (this.tabType) {
                    default: {
                        return "underlined";
                    }
                    case 1: 
                }
                return "card";
            }
            case "tabsPopupPolicy": {
                switch (this.tabsPopupPolicy) {
                    default: {
                        return "asNeeded";
                    }
                    case 0: 
                }
                return "never";
            }
            case "scrollButtonsPolicy": {
                switch (this.scrollButtonsPolicy) {
                    default: {
                        return "asNeededSingle";
                    }
                    case 2: {
                        return "asNeeded";
                    }
                    case 0: 
                }
                return "never";
            }
            case "scrollButtonsPlacement": {
                switch (this.scrollButtonsPlacement) {
                    default: {
                        return "both";
                    }
                    case 11: 
                }
                return "trailing";
            }
            case "tabAreaAlignment": {
                return FlatTabbedPaneUI.alignmentToString(this.tabAreaAlignment, "leading");
            }
            case "tabAlignment": {
                return FlatTabbedPaneUI.alignmentToString(this.tabAlignment, "center");
            }
            case "tabWidthMode": {
                switch (this.tabWidthMode) {
                    default: {
                        return "preferred";
                    }
                    case 1: {
                        return "equal";
                    }
                    case 2: 
                }
                return "compact";
            }
            case "tabRotation": {
                switch (this.tabRotation) {
                    default: {
                        return "none";
                    }
                    case -2: {
                        return "auto";
                    }
                    case 2: {
                        return "left";
                    }
                    case 4: 
                }
                return "right";
            }
            case "tabIconPlacement": {
                switch (this.tabIconPlacement) {
                    default: {
                        return "leading";
                    }
                    case 11: {
                        return "trailing";
                    }
                    case 1: {
                        return "top";
                    }
                    case 3: 
                }
                return "bottom";
            }
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    protected void setRolloverTab(int x, int y) {
        this.setRolloverTab(this.tabForCoordinate(this.tabPane, x, y));
    }

    @Override
    protected void setRolloverTab(int index) {
        if (this.blockRollover) {
            return;
        }
        int oldIndex = this.getRolloverTab();
        super.setRolloverTab(index);
        if (index != oldIndex) {
            this.repaintRolloverLaterOnce(oldIndex);
        }
    }

    private void repaintRolloverLaterOnce(int oldIndex) {
        if (this.repaintRolloverPending) {
            return;
        }
        this.repaintRolloverPending = true;
        EventQueue.invokeLater(() -> {
            this.repaintRolloverPending = false;
            if (this.tabPane == null) {
                return;
            }
            int index = this.getRolloverTab();
            if (index != oldIndex) {
                this.repaintTab(oldIndex);
                this.repaintTab(index);
            }
        });
    }

    protected boolean isRolloverTabClose() {
        return this.rolloverTabClose;
    }

    protected void setRolloverTabClose(boolean rollover) {
        if (this.rolloverTabClose == rollover) {
            return;
        }
        this.rolloverTabClose = rollover;
        this.repaintTab(this.getRolloverTab());
    }

    protected boolean isPressedTabClose() {
        return this.pressedTabClose;
    }

    protected void setPressedTabClose(boolean pressed) {
        if (this.pressedTabClose == pressed) {
            return;
        }
        this.pressedTabClose = pressed;
        this.repaintTab(this.getRolloverTab());
    }

    private void repaintTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= this.tabPane.getTabCount()) {
            return;
        }
        Rectangle r = this.getTabBounds(this.tabPane, tabIndex);
        if (r == null) {
            return;
        }
        if (this.contentSeparatorHeight > 0 && FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.showContentSeparator", true)) {
            int sh = UIScale.scale(this.contentSeparatorHeight);
            switch (this.tabPane.getTabPlacement()) {
                default: {
                    r.height += sh;
                    break;
                }
                case 3: {
                    r.height += sh;
                    r.y -= sh;
                    break;
                }
                case 2: {
                    r.width += sh;
                    break;
                }
                case 4: {
                    r.width += sh;
                    r.x -= sh;
                }
            }
        }
        this.tabPane.repaint(r);
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return this.getRealTabRotation(tabPlacement) == -1 ? this.calculateTabWidthImpl(tabPlacement, tabIndex, metrics, false) : this.calculateTabHeightImpl(tabPlacement, tabIndex, metrics.getHeight(), true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int calculateTabWidthImpl(int tabPlacement, int tabIndex, FontMetrics metrics, boolean rotated) {
        int tabWidth;
        Icon icon;
        int tabWidthMode = this.getTabWidthMode();
        if (tabWidthMode == 1 && this.isHorizontalOrRotated(tabPlacement) && !this.inCalculateEqual) {
            this.inCalculateEqual = true;
            try {
                int n = this.isHorizontalTabPlacement(tabPlacement) ? this.calculateMaxTabWidth(tabPlacement) : this.calculateMaxTabHeight(tabPlacement);
                return n;
            } finally {
                this.inCalculateEqual = false;
            }
        }
        this.textIconGap = UIScale.scale(this.textIconGapUnscaled);
        if (tabWidthMode == 2 && tabIndex != this.tabPane.getSelectedIndex() && this.isHorizontalOrRotated(tabPlacement) && this.tabPane.getTabComponentAt(tabIndex) == null && (icon = this.getIconForTab(tabIndex)) != null) {
            Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
            tabWidth = icon.getIconWidth() + tabInsets.left + tabInsets.right;
        } else {
            int iconPlacement = FlatClientProperties.clientPropertyInt(this.tabPane, "JTabbedPane.tabIconPlacement", this.tabIconPlacement);
            if ((iconPlacement == 1 || iconPlacement == 3) && this.tabPane.getTabComponentAt(tabIndex) == null && (icon = this.getIconForTab(tabIndex)) != null) {
                tabWidth = icon.getIconWidth();
                View view = this.getTextViewForTab(tabIndex);
                if (view != null) {
                    tabWidth = Math.max(tabWidth, (int)view.getPreferredSpan(0));
                } else {
                    String title = this.tabPane.getTitleAt(tabIndex);
                    if (title != null) {
                        tabWidth = Math.max(tabWidth, metrics.stringWidth(title));
                    }
                }
                Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
                tabWidth += tabInsets.left + tabInsets.right;
            } else {
                Component tabComponent;
                tabWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics) - 3;
                if (rotated && (tabComponent = this.tabPane.getTabComponentAt(tabIndex)) != null) {
                    Dimension prefSize = tabComponent.getPreferredSize();
                    tabWidth = tabWidth - prefSize.width + prefSize.height;
                }
            }
        }
        if (this.isTabClosable(tabIndex)) {
            tabWidth += this.closeIcon.getIconWidth();
        }
        int min = this.getTabClientPropertyInt(tabIndex, "JTabbedPane.minimumTabWidth", this.minimumTabWidth);
        int max = this.getTabClientPropertyInt(tabIndex, "JTabbedPane.maximumTabWidth", this.maximumTabWidth);
        if (min > 0) {
            tabWidth = Math.max(tabWidth, UIScale.scale(min));
        }
        if (max > 0 && this.tabPane.getTabComponentAt(tabIndex) == null) {
            tabWidth = Math.min(tabWidth, UIScale.scale(max));
        }
        return tabWidth;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return this.getRealTabRotation(tabPlacement) == -1 ? this.calculateTabHeightImpl(tabPlacement, tabIndex, fontHeight, false) : this.calculateTabWidthImpl(tabPlacement, tabIndex, this.getFontMetrics(), true);
    }

    private int calculateTabHeightImpl(int tabPlacement, int tabIndex, int fontHeight, boolean rotated) {
        int tabHeight;
        Icon icon;
        int iconPlacement = FlatClientProperties.clientPropertyInt(this.tabPane, "JTabbedPane.tabIconPlacement", this.tabIconPlacement);
        if ((iconPlacement == 1 || iconPlacement == 3) && this.tabPane.getTabComponentAt(tabIndex) == null && (icon = this.getIconForTab(tabIndex)) != null) {
            tabHeight = icon.getIconHeight();
            View view = this.getTextViewForTab(tabIndex);
            if (view != null) {
                tabHeight += (int)view.getPreferredSpan(1) + UIScale.scale(this.textIconGapUnscaled);
            } else if (this.tabPane.getTitleAt(tabIndex) != null) {
                tabHeight += fontHeight + UIScale.scale(this.textIconGapUnscaled);
            }
            Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
            tabHeight += tabInsets.top + tabInsets.bottom;
        } else {
            Component tabComponent;
            tabHeight = super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) - 2;
            if (rotated && (tabComponent = this.tabPane.getTabComponentAt(tabIndex)) != null) {
                Dimension prefSize = tabComponent.getPreferredSize();
                tabHeight = tabHeight - prefSize.height + prefSize.width;
            }
        }
        return Math.max(tabHeight, UIScale.scale(FlatClientProperties.clientPropertyInt(this.tabPane, "JTabbedPane.tabHeight", this.tabHeight)));
    }

    @Override
    protected int calculateMaxTabWidth(int tabPlacement) {
        return this.hideTabArea() ? 0 : super.calculateMaxTabWidth(tabPlacement);
    }

    @Override
    protected int calculateMaxTabHeight(int tabPlacement) {
        return this.hideTabArea() ? 0 : super.calculateMaxTabHeight(tabPlacement);
    }

    @Override
    protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
        return this.hideTabArea() ? 0 : super.calculateTabAreaWidth(tabPlacement, vertRunCount, maxTabWidth);
    }

    @Override
    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
        return this.hideTabArea() ? 0 : super.calculateTabAreaHeight(tabPlacement, horizRunCount, maxTabHeight);
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        Object value = this.getTabClientProperty(tabIndex, "JTabbedPane.tabInsets");
        return UIScale.scale(value instanceof Insets ? (Insets)value : super.getTabInsets(tabPlacement, tabIndex));
    }

    protected Insets getTabInsetsRotated(int tabPlacement, int tabIndex, int rotation) {
        Insets insets = this.getTabInsets(tabPlacement, tabIndex);
        switch (rotation) {
            case 2: {
                return new Insets(insets.right, insets.top, insets.left, insets.bottom);
            }
            case 4: {
                return new Insets(insets.left, insets.bottom, insets.right, insets.top);
            }
        }
        return insets;
    }

    @Override
    protected Insets getSelectedTabPadInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
    }

    protected Insets getRealTabAreaInsets(int tabPlacement) {
        if (this.tabAreaInsets == null) {
            this.tabAreaInsets = new Insets(0, 0, 0, 0);
        }
        Insets currentTabAreaInsets = super.getTabAreaInsets(tabPlacement);
        Insets insets = (Insets)currentTabAreaInsets.clone();
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabAreaInsets");
        if (value instanceof Insets) {
            FlatTabbedPaneUI.rotateInsets((Insets)value, insets, tabPlacement);
        }
        currentTabAreaInsets.left = -10000;
        currentTabAreaInsets.top = -10000;
        insets = UIScale.scale(insets);
        return insets;
    }

    @Override
    protected Insets getTabAreaInsets(int tabPlacement) {
        Insets insets = this.getRealTabAreaInsets(tabPlacement);
        if (this.tabPane.getTabLayoutPolicy() == 0) {
            if (this.isHorizontalTabPlacement(tabPlacement)) {
                insets.left += this.getLeadingPreferredWidth();
                insets.right += this.getTrailingPreferredWidth();
            } else {
                insets.top += this.getLeadingPreferredHeight();
                insets.bottom += this.getTrailingPreferredHeight();
            }
        }
        return insets;
    }

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        if (this.hideTabArea() || this.contentSeparatorHeight == 0 || !FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.showContentSeparator", this.showContentSeparator)) {
            return new Insets(0, 0, 0, 0);
        }
        boolean hasFullBorder = FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.hasFullBorder", this.hasFullBorder);
        int sh = UIScale.scale(this.contentSeparatorHeight);
        Insets insets = hasFullBorder ? new Insets(sh, sh, sh, sh) : new Insets(sh, 0, 0, 0);
        Insets contentBorderInsets = new Insets(0, 0, 0, 0);
        FlatTabbedPaneUI.rotateInsets(insets, contentBorderInsets, tabPlacement);
        return contentBorderInsets;
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        if (this.isTabClosable(tabIndex) && this.getRealTabRotation(tabPlacement) == -1) {
            int shift = this.closeIcon.getIconWidth() / 2;
            return this.isLeftToRight() ? -shift : shift;
        }
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        if (this.isTabClosable(tabIndex) && this.getRealTabRotation(tabPlacement) != -1) {
            int shift = this.closeIcon.getIconHeight() / 2;
            return this.isLeftToRight() ? shift : -shift;
        }
        return 0;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        this.oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        super.update(g, c);
        FlatUIUtils.resetRenderingHints(g, this.oldRenderingHints);
        this.oldRenderingHints = null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (this.hideTabArea()) {
            return;
        }
        this.ensureCurrentLayout();
        int tabPlacement = this.tabPane.getTabPlacement();
        int selectedIndex = this.tabPane.getSelectedIndex();
        this.paintContentBorder(g, tabPlacement, selectedIndex);
        if (this.tabsOpaque && !this.tabPane.isOpaque() && this.tabPane.getTabCount() > 0) {
            Rectangle tr = null;
            if (this.isScrollTabLayout()) {
                tr = this.tabViewport.getBounds();
                for (Component child : this.tabPane.getComponents()) {
                    if (!(child instanceof FlatTabAreaButton) || !child.isVisible()) continue;
                    tr = tr.union(child.getBounds());
                }
            } else {
                for (Rectangle r : this.rects) {
                    tr = tr != null ? tr.union(r) : r;
                }
            }
            if (tr != null) {
                g.setColor(this.tabPane.getBackground());
                if (this.getTabType() == 1 && this.cardTabArc > 0) {
                    ((Graphics2D)g).fill(this.createCardTabOuterPath(tabPlacement, tr.x, tr.y, tr.width, tr.height));
                } else {
                    g.fillRect(tr.x, tr.y, tr.width, tr.height);
                }
            }
        }
        if (!this.isScrollTabLayout()) {
            this.paintTabArea(g, tabPlacement, selectedIndex);
        }
    }

    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        Object[] oldHints = FlatUIUtils.setRenderingHints(g);
        super.paintTabArea(g, tabPlacement, selectedIndex);
        FlatUIUtils.resetRenderingHints(g, oldHints);
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        boolean isCompact;
        boolean isSelected;
        Rectangle tabRect = rects[tabIndex];
        int x = tabRect.x;
        int y = tabRect.y;
        int w = tabRect.width;
        int h = tabRect.height;
        boolean bl = isSelected = tabIndex == this.tabPane.getSelectedIndex();
        if (this.tabsOpaque || this.tabPane.isOpaque()) {
            this.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
        }
        this.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
        if (this.isTabClosable(tabIndex)) {
            this.paintTabCloseButton(g, tabIndex, x, y, w, h);
        }
        if (isSelected) {
            this.paintTabSelection(g, tabPlacement, tabIndex, x, y, w, h);
        }
        if (this.tabPane.getTabComponentAt(tabIndex) != null) {
            return;
        }
        String title = this.tabPane.getTitleAt(tabIndex);
        Icon icon = this.getIconForTab(tabIndex);
        Font font = this.tabPane.getFont();
        FontMetrics metrics = this.tabPane.getFontMetrics(font);
        boolean bl2 = isCompact = icon != null && !isSelected && this.getTabWidthMode() == 2 && this.isHorizontalOrRotated(tabPlacement);
        if (isCompact) {
            title = null;
        }
        String clippedTitle = this.layoutAndClipLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);
        if (this.tabViewport != null && (tabPlacement == 1 || tabPlacement == 3)) {
            Rectangle viewRect = this.tabViewport.getViewRect();
            viewRect.width -= 4;
            if (!viewRect.contains(textRect)) {
                Rectangle r = viewRect.intersection(textRect);
                if (r.x > viewRect.x) {
                    clippedTitle = JavaCompatibility.getClippedString(null, metrics, title, r.width);
                }
            }
        }
        if (!isCompact) {
            this.paintText(g, tabPlacement, font, metrics, tabIndex, clippedTitle, textRect, isSelected);
        }
        this.paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        g.setFont(font);
        FlatUIUtils.runWithoutRenderingHints(g, this.oldRenderingHints, () -> {
            View view = this.getTextViewForTab(tabIndex);
            if (view != null) {
                AffineTransform oldTransform = this.rotateGraphics(g, tabPlacement, textRect);
                Rectangle textRect2 = oldTransform != null ? new Rectangle(textRect.x, textRect.y, textRect.height, textRect.width) : textRect;
                view.paint(g, textRect2);
                if (oldTransform != null) {
                    ((Graphics2D)g).setTransform(oldTransform);
                }
                return;
            }
            AffineTransform oldTransform = this.rotateGraphics(g, tabPlacement, textRect);
            int mnemIndex = FlatLaf.isShowMnemonics() ? this.tabPane.getDisplayedMnemonicIndexAt(tabIndex) : -1;
            g.setColor(this.getTabForeground(tabPlacement, tabIndex, isSelected));
            FlatUIUtils.drawStringUnderlineCharAt(this.tabPane, g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
            if (oldTransform != null) {
                ((Graphics2D)g).setTransform(oldTransform);
            }
        });
    }

    @Override
    protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect, boolean isSelected) {
        if (icon == null) {
            return;
        }
        Shape oldClip = g.getClip();
        ((Graphics2D)g).clip(iconRect);
        AffineTransform oldTransform = this.rotateGraphics(g, tabPlacement, iconRect);
        icon.paintIcon(this.tabPane, g, iconRect.x, iconRect.y);
        if (oldTransform != null) {
            ((Graphics2D)g).setTransform(oldTransform);
        }
        g.setClip(oldClip);
    }

    private AffineTransform rotateGraphics(Graphics g, int tabPlacement, Rectangle r) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform oldTransform = null;
        int rotation = this.getRealTabRotation(tabPlacement);
        if (rotation == 2) {
            oldTransform = g2.getTransform();
            g2.translate(0, r.height);
            g2.rotate(Math.toRadians(270.0), r.x, r.y);
        } else if (rotation == 4) {
            oldTransform = g2.getTransform();
            g2.translate(r.width, 0);
            g2.rotate(Math.toRadians(90.0), r.x, r.y);
        }
        return oldTransform;
    }

    protected Color getTabForeground(int tabPlacement, int tabIndex, boolean isSelected) {
        if (!this.tabPane.isEnabled() || !this.tabPane.isEnabledAt(tabIndex)) {
            return this.disabledForeground;
        }
        if (this.hoverForeground != null && this.getRolloverTab() == tabIndex) {
            return this.hoverForeground;
        }
        Color foreground = this.tabPane.getForegroundAt(tabIndex);
        if (foreground != this.tabPane.getForeground()) {
            return foreground;
        }
        if (this.focusForeground != null && isSelected && FlatUIUtils.isPermanentFocusOwner(this.tabPane)) {
            return this.focusForeground;
        }
        if (this.selectedForeground != null && isSelected) {
            return this.selectedForeground;
        }
        return foreground;
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Color background;
        boolean isCard;
        boolean bl = isCard = this.getTabType() == 1;
        if (!isCard && this.selectedInsets != null) {
            Insets insets = new Insets(0, 0, 0, 0);
            FlatTabbedPaneUI.rotateInsets(this.selectedInsets, insets, this.tabPane.getTabPlacement());
            x += UIScale.scale(insets.left);
            y += UIScale.scale(insets.top);
            w -= UIScale.scale(insets.left + insets.right);
            h -= UIScale.scale(insets.top + insets.bottom);
        }
        if ((background = this.getTabBackground(tabPlacement, tabIndex, isSelected)) != this.tabPane.getBackground()) {
            g.setColor(FlatUIUtils.deriveColor(background, this.tabPane.getBackground()));
            if (!isCard && this.tabArc > 0) {
                float arc = UIScale.scale((float)this.tabArc) / 2.0f;
                FlatUIUtils.paintSelection((Graphics2D)g, x, y, w, h, null, arc, arc, arc, arc, 0);
            } else if (isCard && this.cardTabArc > 0) {
                ((Graphics2D)g).fill(this.createCardTabOuterPath(tabPlacement, x, y, w, h));
            } else {
                g.fillRect(x, y, w, h);
            }
        }
    }

    protected Color getTabBackground(int tabPlacement, int tabIndex, boolean isSelected) {
        Color background = this.tabPane.getBackgroundAt(tabIndex);
        if (!this.tabPane.isEnabled() || !this.tabPane.isEnabledAt(tabIndex)) {
            return background;
        }
        if (this.hoverColor != null && this.getRolloverTab() == tabIndex) {
            return this.hoverColor;
        }
        if (background != this.tabPane.getBackground()) {
            return background;
        }
        if (this.focusColor != null && isSelected && FlatUIUtils.isPermanentFocusOwner(this.tabPane)) {
            return this.focusColor;
        }
        if (this.selectedBackground != null && isSelected) {
            return this.selectedBackground;
        }
        return background;
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        if (FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.showTabSeparators", this.showTabSeparators) && !this.isLastInRun(tabIndex)) {
            if (this.getTabType() == 1) {
                int selectedIndex = this.tabPane.getSelectedIndex();
                if (tabIndex != selectedIndex - 1 && tabIndex != selectedIndex) {
                    this.paintTabSeparator(g, tabPlacement, x, y, w, h);
                }
            } else {
                this.paintTabSeparator(g, tabPlacement, x, y, w, h);
            }
        }
        if (isSelected && this.getTabType() == 1) {
            this.paintCardTabBorder(g, tabPlacement, tabIndex, x, y, w, h);
        }
    }

    protected void paintCardTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        Path2D.Float path = new Path2D.Float(0);
        path.append(this.createCardTabOuterPath(tabPlacement, x, y, w, h), false);
        path.append(this.createCardTabInnerPath(tabPlacement, x, y, w, h), false);
        g.setColor(this.tabSeparatorColor != null ? this.tabSeparatorColor : this.contentAreaColor);
        g2.fill(path);
    }

    protected Shape createCardTabOuterPath(int tabPlacement, int x, int y, int w, int h) {
        float arc = UIScale.scale((float)this.cardTabArc) / 2.0f;
        switch (tabPlacement) {
            default: {
                return FlatUIUtils.createRoundRectanglePath(x, y, w, h, arc, arc, 0.0f, 0.0f);
            }
            case 3: {
                return FlatUIUtils.createRoundRectanglePath(x, y, w, h, 0.0f, 0.0f, arc, arc);
            }
            case 2: {
                return FlatUIUtils.createRoundRectanglePath(x, y, w, h, arc, 0.0f, arc, 0.0f);
            }
            case 4: 
        }
        return FlatUIUtils.createRoundRectanglePath(x, y, w, h, 0.0f, arc, 0.0f, arc);
    }

    protected Shape createCardTabInnerPath(int tabPlacement, int x, int y, int w, int h) {
        float bw = UIScale.scale((float)this.contentSeparatorHeight);
        float arc = UIScale.scale((float)this.cardTabArc) / 2.0f - bw;
        switch (tabPlacement) {
            default: {
                return FlatUIUtils.createRoundRectanglePath((float)x + bw, (float)y + bw, (float)w - bw * 2.0f, (float)h - bw, arc, arc, 0.0f, 0.0f);
            }
            case 3: {
                return FlatUIUtils.createRoundRectanglePath((float)x + bw, y, (float)w - bw * 2.0f, (float)h - bw, 0.0f, 0.0f, arc, arc);
            }
            case 2: {
                return FlatUIUtils.createRoundRectanglePath((float)x + bw, (float)y + bw, (float)w - bw, (float)h - bw * 2.0f, arc, 0.0f, arc, 0.0f);
            }
            case 4: 
        }
        return FlatUIUtils.createRoundRectanglePath(x, (float)y + bw, (float)w - bw, (float)h - bw * 2.0f, 0.0f, arc, 0.0f, arc);
    }

    protected void paintTabCloseButton(Graphics g, int tabIndex, int x, int y, int w, int h) {
        if (this.tabCloseButton == null) {
            this.tabCloseButton = new TabCloseButton();
            this.tabCloseButton.setVisible(false);
        }
        boolean rollover = tabIndex == this.getRolloverTab();
        ButtonModel bm = this.tabCloseButton.getModel();
        bm.setRollover(rollover && this.isRolloverTabClose());
        bm.setPressed(rollover && this.isPressedTabClose());
        this.tabCloseButton.setBackground(this.tabPane.getBackground());
        this.tabCloseButton.setForeground(this.tabPane.getForeground());
        Rectangle tabCloseRect = this.getTabCloseBounds(tabIndex, x, y, w, h, this.calcRect);
        this.closeIcon.paintIcon(this.tabCloseButton, g, tabCloseRect.x, tabCloseRect.y);
    }

    protected void paintTabSeparator(Graphics g, int tabPlacement, int x, int y, int w, int h) {
        float sepWidth = UIScale.scale(1.0f);
        float offset = this.tabSeparatorsFullHeight ? 0.0f : UIScale.scale(5.0f);
        g.setColor(this.tabSeparatorColor != null ? this.tabSeparatorColor : this.contentAreaColor);
        if (tabPlacement == 2 || tabPlacement == 4) {
            ((Graphics2D)g).fill(new Rectangle2D.Float((float)x + offset, (float)(y + h) - sepWidth, (float)w - offset * 2.0f, sepWidth));
        } else if (this.isLeftToRight()) {
            ((Graphics2D)g).fill(new Rectangle2D.Float((float)(x + w) - sepWidth, (float)y + offset, sepWidth, (float)h - offset * 2.0f));
        } else {
            ((Graphics2D)g).fill(new Rectangle2D.Float(x, (float)y + offset, sepWidth, (float)h - offset * 2.0f));
        }
    }

    protected void paintTabSelection(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h) {
        boolean atBottom;
        g.setColor(this.tabPane.isEnabled() ? (this.isTabbedPaneOrChildFocused() ? this.underlineColor : this.inactiveUnderlineColor) : this.disabledUnderlineColor);
        boolean isCard = this.getTabType() == 1;
        boolean bl = atBottom = !isCard;
        Insets contentInsets = atBottom ? (!this.rotateTabRuns && this.runCount > 1 && !this.isScrollTabLayout() && this.getRunForTab(this.tabPane.getTabCount(), tabIndex) > 0 ? new Insets(0, 0, 0, 0) : this.getContentBorderInsets(tabPlacement)) : null;
        int tabSelectionHeight = UIScale.scale(isCard ? this.cardTabSelectionHeight : this.tabSelectionHeight);
        float arc = UIScale.scale((float)(isCard ? this.cardTabArc : this.tabSelectionArc)) / 2.0f;
        int sx = x;
        int sy = y;
        int sw = w;
        int sh = h;
        switch (tabPlacement) {
            default: {
                sy = atBottom ? y + h + contentInsets.top - tabSelectionHeight : y;
                sh = tabSelectionHeight;
                break;
            }
            case 3: {
                sy = atBottom ? y - contentInsets.bottom : y + h - tabSelectionHeight;
                sh = tabSelectionHeight;
                break;
            }
            case 2: {
                sx = atBottom ? x + w + contentInsets.left - tabSelectionHeight : x;
                sw = tabSelectionHeight;
                break;
            }
            case 4: {
                sx = atBottom ? x - contentInsets.right : x + w - tabSelectionHeight;
                sw = tabSelectionHeight;
            }
        }
        if (!isCard && this.tabSelectionInsets != null) {
            Insets insets = new Insets(0, 0, 0, 0);
            FlatTabbedPaneUI.rotateInsets(this.tabSelectionInsets, insets, this.tabPane.getTabPlacement());
            sx += UIScale.scale(insets.left);
            sy += UIScale.scale(insets.top);
            sw -= UIScale.scale(insets.left + insets.right);
            sh -= UIScale.scale(insets.top + insets.bottom);
        }
        if (arc <= 0.0f) {
            g.fillRect(sx, sy, sw, sh);
        } else if (isCard) {
            Area area = new Area(this.createCardTabOuterPath(tabPlacement, x, y, w, h));
            area.intersect(new Area(new Rectangle2D.Float(sx, sy, sw, sh)));
            ((Graphics2D)g).fill(area);
        } else {
            FlatUIUtils.paintSelection((Graphics2D)g, sx, sy, sw, sh, null, arc, arc, arc, arc, 0);
        }
    }

    protected boolean isTabbedPaneOrChildFocused() {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Object value = this.tabPane.getClientProperty("JComponent.focusOwner");
        if (value instanceof Predicate) {
            return ((Predicate)value).test(this.tabPane) && FlatUIUtils.isInActiveWindow(this.tabPane, keyboardFocusManager.getActiveWindow());
        }
        Component focusOwner = keyboardFocusManager.getPermanentFocusOwner();
        return focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, this.tabPane) && FlatUIUtils.isInActiveWindow(focusOwner, keyboardFocusManager.getActiveWindow());
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        if (this.tabPane.getTabCount() <= 0 || this.contentSeparatorHeight == 0 || !FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.showContentSeparator", this.showContentSeparator)) {
            return;
        }
        Insets insets = this.tabPane.getInsets();
        Insets tabAreaInsets = this.getTabAreaInsets(tabPlacement);
        int x = insets.left;
        int y = insets.top;
        int w = this.tabPane.getWidth() - insets.right - insets.left;
        int h = this.tabPane.getHeight() - insets.top - insets.bottom;
        switch (tabPlacement) {
            default: {
                y += this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight);
                h -= (y -= tabAreaInsets.bottom) - insets.top;
                break;
            }
            case 3: {
                h -= this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight);
                h += tabAreaInsets.top;
                break;
            }
            case 2: {
                x += this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth);
                w -= (x -= tabAreaInsets.right) - insets.left;
                break;
            }
            case 4: {
                w -= this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth);
                w += tabAreaInsets.left;
            }
        }
        boolean hasFullBorder = FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.hasFullBorder", this.hasFullBorder);
        int sh = UIScale.scale(this.contentSeparatorHeight * 100);
        Insets ci = new Insets(0, 0, 0, 0);
        FlatTabbedPaneUI.rotateInsets(hasFullBorder ? new Insets(sh, sh, sh, sh) : new Insets(sh, 0, 0, 0), ci, tabPlacement);
        Path2D.Float path = new Path2D.Float(0);
        path.append(new Rectangle2D.Float(x, y, w, h), false);
        path.append(new Rectangle2D.Float((float)x + (float)ci.left / 100.0f, (float)y + (float)ci.top / 100.0f, (float)w - (float)ci.left / 100.0f - (float)ci.right / 100.0f, (float)h - (float)ci.top / 100.0f - (float)ci.bottom / 100.0f), false);
        if (this.getTabType() == 1) {
            float csh = UIScale.scale((float)this.contentSeparatorHeight);
            Rectangle tabRect = this.getTabBounds(this.tabPane, selectedIndex);
            Rectangle2D.Float innerTabRect = new Rectangle2D.Float((float)tabRect.x + csh, (float)tabRect.y + csh, (float)tabRect.width - csh * 2.0f, (float)tabRect.height - csh * 2.0f);
            if (this.tabViewport != null) {
                Rectangle2D.intersect(this.tabViewport.getBounds(), innerTabRect, innerTabRect);
            }
            Rectangle2D.Float gap = null;
            if (this.isHorizontalTabPlacement(tabPlacement)) {
                if (innerTabRect.width > 0.0f) {
                    float y2 = tabPlacement == 1 ? (float)y : (float)(y + h) - csh;
                    gap = new Rectangle2D.Float(innerTabRect.x, y2, innerTabRect.width, csh);
                }
            } else if (innerTabRect.height > 0.0f) {
                float x2 = tabPlacement == 2 ? (float)x : (float)(x + w) - csh;
                gap = new Rectangle2D.Float(x2, innerTabRect.y, csh, innerTabRect.height);
            }
            if (gap != null) {
                path.append(gap, false);
                Color background = this.getTabBackground(tabPlacement, selectedIndex, true);
                g.setColor(FlatUIUtils.deriveColor(background, this.tabPane.getBackground()));
                ((Graphics2D)g).fill(gap);
            }
        }
        g.setColor(this.contentAreaColor);
        ((Graphics2D)g).fill(path);
        if (this.isScrollTabLayout() && selectedIndex >= 0 && this.tabViewport != null) {
            Rectangle tabRect = this.getTabBounds(this.tabPane, selectedIndex);
            Shape oldClip = g.getClip();
            Rectangle vr = this.tabViewport.getBounds();
            if (this.isHorizontalTabPlacement(tabPlacement)) {
                g.clipRect(vr.x, 0, vr.width, this.tabPane.getHeight());
            } else {
                g.clipRect(0, vr.y, this.tabPane.getWidth(), vr.height);
            }
            this.paintTabSelection(g, tabPlacement, selectedIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height);
            g.setClip(oldClip);
        }
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    protected String layoutAndClipLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        int horizontalTextPosition;
        int verticalTextPosition;
        int rotation = this.getRealTabRotation(tabPlacement);
        boolean leftToRight = this.isLeftToRight();
        tabRect = FlatUIUtils.subtractInsets(tabRect, this.getTabInsetsRotated(tabPlacement, tabIndex, rotation));
        if (this.isTabClosable(tabIndex)) {
            if (rotation == -1) {
                int iconWidth = this.closeIcon.getIconWidth();
                tabRect.width -= iconWidth;
                if (!leftToRight) {
                    tabRect.x += iconWidth;
                }
            } else {
                int iconHeight = this.closeIcon.getIconHeight();
                tabRect.height -= iconHeight;
                if (rotation == 2 && leftToRight || rotation == 4 && !leftToRight) {
                    tabRect.y += iconHeight;
                }
            }
        }
        switch (FlatClientProperties.clientPropertyInt(this.tabPane, "JTabbedPane.tabIconPlacement", this.tabIconPlacement)) {
            default: {
                verticalTextPosition = 0;
                horizontalTextPosition = 11;
                break;
            }
            case 11: {
                verticalTextPosition = 0;
                horizontalTextPosition = 10;
                break;
            }
            case 1: {
                verticalTextPosition = 3;
                horizontalTextPosition = 0;
                break;
            }
            case 3: {
                verticalTextPosition = 1;
                horizontalTextPosition = 0;
            }
        }
        textRect.setBounds(0, 0, 0, 0);
        iconRect.setBounds(0, 0, 0, 0);
        View view = this.getTextViewForTab(tabIndex);
        if (view != null) {
            this.tabPane.putClientProperty("html", view);
        }
        String clippedTitle = rotation == -1 ? SwingUtilities.layoutCompoundLabel(this.tabPane, metrics, title, icon, 0, this.getTabAlignment(tabIndex), verticalTextPosition, horizontalTextPosition, tabRect, iconRect, textRect, UIScale.scale(this.textIconGapUnscaled)) : this.layoutVerticalCompoundLabel(rotation, this.tabPane, metrics, title, icon, 0, this.getTabAlignment(tabIndex), verticalTextPosition, horizontalTextPosition, tabRect, iconRect, textRect, UIScale.scale(this.textIconGapUnscaled));
        this.tabPane.putClientProperty("html", null);
        return clippedTitle;
    }

    private String layoutVerticalCompoundLabel(int rotation, JComponent c, FontMetrics fm, String text, Icon icon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewR, Rectangle iconR, Rectangle textR, int textIconGap) {
        Rectangle viewR2 = new Rectangle(viewR.height, viewR.width);
        String clippedTitle = SwingUtilities.layoutCompoundLabel(c, fm, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition, horizontalTextPosition, viewR2, iconR, textR, textIconGap);
        if (rotation == 2) {
            this.rotateLeft(viewR, iconR);
            this.rotateLeft(viewR, textR);
        } else {
            this.rotateRight(viewR, iconR);
            this.rotateRight(viewR, textR);
        }
        return clippedTitle;
    }

    private void rotateLeft(Rectangle viewR, Rectangle r) {
        int x = viewR.x + r.y;
        int y = viewR.y + (viewR.height - (r.x + r.width));
        r.setBounds(x, y, r.height, r.width);
    }

    private void rotateRight(Rectangle viewR, Rectangle r) {
        int x = viewR.x + (viewR.width - (r.y + r.height));
        int y = viewR.y + r.x;
        r.setBounds(x, y, r.height, r.width);
    }

    protected int getRealTabRotation(int tabPlacement) {
        int realRotation;
        int rotation = this.getTabRotation();
        int n = rotation == -2 ? (tabPlacement == 2 ? 2 : (tabPlacement == 4 ? 4 : -1)) : (realRotation = rotation == 2 || rotation == 4 ? rotation : -1);
        assert (realRotation == -1 || realRotation == 2 || realRotation == 4);
        return realRotation;
    }

    @Override
    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        if (this.moreTabsButton != null) {
            Point viewPosition = this.tabViewport.getViewPosition();
            x = x - this.tabViewport.getX() + viewPosition.x;
            y = y - this.tabViewport.getY() + viewPosition.y;
            if (!this.tabViewport.getViewRect().contains(x, y)) {
                return -1;
            }
        }
        return super.tabForCoordinate(pane, x, y);
    }

    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        if (this.moreTabsButton != null) {
            dest.setBounds(this.rects[tabIndex]);
            Point viewPosition = this.tabViewport.getViewPosition();
            dest.x = dest.x + this.tabViewport.getX() - viewPosition.x;
            dest.y = dest.y + this.tabViewport.getY() - viewPosition.y;
            return dest;
        }
        return super.getTabBounds(tabIndex, dest);
    }

    protected Rectangle getTabCloseBounds(int tabIndex, int x, int y, int w, int h, Rectangle dest) {
        int iconWidth = this.closeIcon.getIconWidth();
        int iconHeight = this.closeIcon.getIconHeight();
        int tabPlacement = this.tabPane.getTabPlacement();
        int rotation = this.getRealTabRotation(tabPlacement);
        Insets tabInsets = this.getTabInsetsRotated(tabPlacement, tabIndex, rotation);
        boolean leftToRight = this.isLeftToRight();
        if (rotation == -1) {
            dest.x = leftToRight ? x + w - tabInsets.right / 3 * 2 - iconWidth : x + tabInsets.left / 3 * 2;
            dest.y = y + (h - iconHeight) / 2;
        } else {
            dest.x = x + (w - iconWidth) / 2;
            dest.y = rotation == 4 && leftToRight || rotation == 2 && !leftToRight ? y + h - tabInsets.bottom / 3 * 2 - iconHeight : y + tabInsets.top / 3 * 2;
        }
        dest.width = iconWidth;
        dest.height = iconHeight;
        return dest;
    }

    protected Rectangle getTabCloseHitArea(int tabIndex) {
        Rectangle tabRect = this.getTabBounds(this.tabPane, tabIndex);
        Rectangle tabCloseRect = this.getTabCloseBounds(tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, this.calcRect);
        return this.getRealTabRotation(this.tabPane.getTabPlacement()) == -1 ? new Rectangle(tabCloseRect.x, tabRect.y, tabCloseRect.width, tabRect.height) : new Rectangle(tabRect.x, tabCloseRect.y, tabRect.width, tabCloseRect.height);
    }

    protected boolean isTabClosable(int tabIndex) {
        if (tabIndex < 0) {
            return false;
        }
        Object value = this.getTabClientProperty(tabIndex, "JTabbedPane.tabClosable");
        return value instanceof Boolean ? (Boolean)value : this.tabClosable;
    }

    protected void closeTab(int tabIndex) {
        Object callback = this.getTabClientProperty(tabIndex, "JTabbedPane.tabCloseCallback");
        if (callback instanceof IntConsumer) {
            ((IntConsumer)callback).accept(tabIndex);
        } else if (callback instanceof BiConsumer) {
            ((BiConsumer)callback).accept(this.tabPane, tabIndex);
        } else {
            throw new RuntimeException("Missing tab close callback. Set client property 'JTabbedPane.tabCloseCallback' to a 'java.util.function.IntConsumer' or 'java.util.function.BiConsumer<JTabbedPane, Integer>'");
        }
    }

    protected Object getTabClientProperty(int tabIndex, String key) {
        Object value;
        if (tabIndex < 0) {
            return null;
        }
        Component c = this.tabPane.getComponentAt(tabIndex);
        if (c instanceof JComponent && (value = ((JComponent)c).getClientProperty(key)) != null) {
            return value;
        }
        return this.tabPane.getClientProperty(key);
    }

    protected int getTabClientPropertyInt(int tabIndex, String key, int defaultValue) {
        Object value = this.getTabClientProperty(tabIndex, key);
        return value instanceof Integer ? (Integer)value : defaultValue;
    }

    protected void ensureCurrentLayout() {
        super.getTabRunCount(this.tabPane);
    }

    @Override
    protected boolean shouldRotateTabRuns(int tabPlacement) {
        return this.rotateTabRuns;
    }

    private boolean isLastInRun(int tabIndex) {
        int run = this.getRunForTab(this.tabPane.getTabCount(), tabIndex);
        return this.lastTabInRun(this.tabPane.getTabCount(), run) == tabIndex;
    }

    private boolean isScrollTabLayout() {
        return this.tabPane.getTabLayoutPolicy() == 1;
    }

    private boolean isLeftToRight() {
        return this.tabPane.getComponentOrientation().isLeftToRight();
    }

    protected boolean isHorizontalTabPlacement(int tabPlacement) {
        return tabPlacement == 1 || tabPlacement == 3;
    }

    private boolean isHorizontalOrRotated(int tabPlacement) {
        return this.isHorizontalTabPlacement(tabPlacement) == (this.getRealTabRotation(tabPlacement) == -1);
    }

    protected boolean isSmoothScrollingEnabled() {
        if (!Animator.useAnimation()) {
            return false;
        }
        return UIManager.getBoolean("ScrollPane.smoothScrolling");
    }

    protected boolean hideTabArea() {
        return this.tabPane.getTabCount() == 1 && this.leadingComponent == null && this.trailingComponent == null && FlatClientProperties.clientPropertyBoolean(this.tabPane, "JTabbedPane.hideTabAreaWithOneTab", this.hideTabAreaWithOneTab);
    }

    protected int getTabType() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabType");
        return value instanceof String ? FlatTabbedPaneUI.parseTabType((String)value) : this.tabType;
    }

    protected int getTabsPopupPolicy() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabsPopupPolicy");
        return value instanceof String ? FlatTabbedPaneUI.parseTabsPopupPolicy((String)value) : this.tabsPopupPolicy;
    }

    protected int getScrollButtonsPolicy() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.scrollButtonsPolicy");
        return value instanceof String ? FlatTabbedPaneUI.parseScrollButtonsPolicy((String)value) : this.scrollButtonsPolicy;
    }

    protected int getScrollButtonsPlacement() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.scrollButtonsPlacement");
        return value instanceof String ? FlatTabbedPaneUI.parseScrollButtonsPlacement((String)value) : this.scrollButtonsPlacement;
    }

    protected int getTabAreaAlignment() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabAreaAlignment");
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return value instanceof String ? FlatTabbedPaneUI.parseAlignment((String)value, 10) : this.tabAreaAlignment;
    }

    protected int getTabAlignment(int tabIndex) {
        Object value = this.getTabClientProperty(tabIndex, "JTabbedPane.tabAlignment");
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return value instanceof String ? FlatTabbedPaneUI.parseAlignment((String)value, 0) : this.tabAlignment;
    }

    protected int getTabWidthMode() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabWidthMode");
        return value instanceof String ? FlatTabbedPaneUI.parseTabWidthMode((String)value) : this.tabWidthMode;
    }

    protected int getTabRotation() {
        Object value = this.tabPane.getClientProperty("JTabbedPane.tabRotation");
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return value instanceof String ? FlatTabbedPaneUI.parseTabRotation((String)value) : this.tabRotation;
    }

    protected static int parseTabType(String str) {
        if (str == null) {
            return 0;
        }
        switch (str) {
            default: {
                return 0;
            }
            case "card": 
        }
        return 1;
    }

    protected static int parseTabsPopupPolicy(String str) {
        if (str == null) {
            return 2;
        }
        switch (str) {
            default: {
                return 2;
            }
            case "never": 
        }
        return 0;
    }

    protected static int parseScrollButtonsPolicy(String str) {
        if (str == null) {
            return 3;
        }
        switch (str) {
            default: {
                return 3;
            }
            case "asNeeded": {
                return 2;
            }
            case "never": 
        }
        return 0;
    }

    protected static int parseScrollButtonsPlacement(String str) {
        if (str == null) {
            return 100;
        }
        switch (str) {
            default: {
                return 100;
            }
            case "trailing": 
        }
        return 11;
    }

    protected static int parseAlignment(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        switch (str) {
            case "leading": {
                return 10;
            }
            case "trailing": {
                return 11;
            }
            case "center": {
                return 0;
            }
            case "fill": {
                return 100;
            }
        }
        return defaultValue;
    }

    private static String alignmentToString(int value, String defaultValue) {
        switch (value) {
            case 10: {
                return "leading";
            }
            case 11: {
                return "trailing";
            }
            case 0: {
                return "center";
            }
            case 100: {
                return "fill";
            }
        }
        return defaultValue;
    }

    protected static int parseTabWidthMode(String str) {
        if (str == null) {
            return 0;
        }
        switch (str) {
            default: {
                return 0;
            }
            case "equal": {
                return 1;
            }
            case "compact": 
        }
        return 2;
    }

    protected static int parseTabRotation(String str) {
        if (str == null) {
            return 0;
        }
        switch (str) {
            default: {
                return -1;
            }
            case "auto": {
                return -2;
            }
            case "left": {
                return 2;
            }
            case "right": 
        }
        return 4;
    }

    protected static int parseTabIconPlacement(String str) {
        if (str == null) {
            return 10;
        }
        switch (str) {
            default: {
                return 10;
            }
            case "trailing": {
                return 11;
            }
            case "top": {
                return 1;
            }
            case "bottom": 
        }
        return 3;
    }

    private void runWithOriginalLayoutManager(Runnable runnable) {
        LayoutManager layout = this.tabPane.getLayout();
        if (layout instanceof FlatTabbedPaneScrollLayout) {
            this.tabPane.setLayout(((FlatTabbedPaneScrollLayout)layout).delegate);
            runnable.run();
            this.tabPane.setLayout(layout);
        } else {
            runnable.run();
        }
    }

    protected void ensureSelectedTabIsVisibleLater() {
        if (!this.tabPane.isDisplayable() || !EventQueue.isDispatchThread()) {
            return;
        }
        EventQueue.invokeLater(() -> this.ensureSelectedTabIsVisible());
    }

    protected void ensureSelectedTabIsVisible() {
        if (this.tabPane == null || this.tabViewport == null || !this.tabPane.isDisplayable()) {
            return;
        }
        this.ensureCurrentLayout();
        int selectedIndex = this.tabPane.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= this.rects.length) {
            return;
        }
        ((JComponent)this.tabViewport.getView()).scrollRectToVisible((Rectangle)this.rects[selectedIndex].clone());
    }

    private int getLeadingPreferredWidth() {
        return this.leadingComponent != null ? this.leadingComponent.getPreferredSize().width : 0;
    }

    private int getLeadingPreferredHeight() {
        return this.leadingComponent != null ? this.leadingComponent.getPreferredSize().height : 0;
    }

    private int getTrailingPreferredWidth() {
        return this.trailingComponent != null ? this.trailingComponent.getPreferredSize().width : 0;
    }

    private int getTrailingPreferredHeight() {
        return this.trailingComponent != null ? this.trailingComponent.getPreferredSize().height : 0;
    }

    private void shiftTabs(int sx, int sy) {
        if (sx == 0 && sy == 0) {
            return;
        }
        for (int i = 0; i < this.rects.length; ++i) {
            this.rects[i].x += sx;
            this.rects[i].y += sy;
        }
    }

    private void stretchTabsWidth(int sw, boolean leftToRight) {
        int rsw = sw / this.rects.length;
        int x = this.rects[0].x - (leftToRight ? 0 : rsw);
        for (int i = 0; i < this.rects.length; ++i) {
            this.rects[i].x = x;
            this.rects[i].width += rsw;
            if (leftToRight) {
                x += this.rects[i].width;
                continue;
            }
            if (i + 1 >= this.rects.length) continue;
            x = this.rects[i].x - this.rects[i + 1].width - rsw;
        }
        int diff = sw - rsw * this.rects.length;
        this.rects[this.rects.length - 1].width += diff;
        if (!leftToRight) {
            this.rects[this.rects.length - 1].x -= diff;
        }
    }

    private void stretchTabsHeight(int sh) {
        int rsh = sh / this.rects.length;
        int y = this.rects[0].y;
        for (int i = 0; i < this.rects.length; ++i) {
            this.rects[i].y = y;
            this.rects[i].height += rsh;
            y += this.rects[i].height;
        }
        this.rects[this.rects.length - 1].height += sh - rsh * this.rects.length;
    }

    private int rectsTotalWidth(boolean leftToRight) {
        int last = this.rects.length - 1;
        return leftToRight ? this.rects[last].x + this.rects[last].width - this.rects[0].x : this.rects[0].x + this.rects[0].width - this.rects[last].x;
    }

    private int rectsTotalHeight() {
        int last = this.rects.length - 1;
        return this.rects[last].y + this.rects[last].height - this.rects[0].y;
    }

    private static class FlatSelectedTabRepainter
    implements PropertyChangeListener {
        private static FlatSelectedTabRepainter instance;
        private KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static void install() {
            Class<FlatSelectedTabRepainter> clazz = FlatSelectedTabRepainter.class;
            synchronized (FlatSelectedTabRepainter.class) {
                if (instance != null) {
                    // ** MonitorExit[var0] (shouldn't be in output)
                    return;
                }
                instance = new FlatSelectedTabRepainter();
                // ** MonitorExit[var0] (shouldn't be in output)
                return;
            }
        }

        FlatSelectedTabRepainter() {
            this.keyboardFocusManager.addPropertyChangeListener(this);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void uninstall() {
            Class<FlatSelectedTabRepainter> clazz = FlatSelectedTabRepainter.class;
            synchronized (FlatSelectedTabRepainter.class) {
                if (instance == null) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return;
                }
                this.keyboardFocusManager.removePropertyChangeListener(this);
                this.keyboardFocusManager = null;
                instance = null;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) {
                this.uninstall();
                return;
            }
            switch (e.getPropertyName()) {
                case "permanentFocusOwner": {
                    Object oldValue = e.getOldValue();
                    Object newValue = e.getNewValue();
                    if (oldValue instanceof Component) {
                        this.repaintSelectedTabs((Component)oldValue);
                    }
                    if (!(newValue instanceof Component)) break;
                    this.repaintSelectedTabs((Component)newValue);
                    break;
                }
                case "activeWindow": {
                    Component permanentFocusOwner = this.keyboardFocusManager.getPermanentFocusOwner();
                    if (permanentFocusOwner == null) break;
                    this.repaintSelectedTabs(permanentFocusOwner);
                }
            }
        }

        private void repaintSelectedTabs(Component c) {
            EventQueue.invokeLater(() -> {
                if (!c.isDisplayable()) {
                    return;
                }
                if (c instanceof JTabbedPane) {
                    this.repaintSelectedTab((JTabbedPane)c);
                }
                Component c2 = c;
                while ((c2 = SwingUtilities.getAncestorOfClass(JTabbedPane.class, c2)) != null) {
                    this.repaintSelectedTab((JTabbedPane)c2);
                }
            });
        }

        private void repaintSelectedTab(JTabbedPane tabbedPane) {
            TabbedPaneUI ui = tabbedPane.getUI();
            if (ui instanceof FlatTabbedPaneUI) {
                ((FlatTabbedPaneUI)ui).repaintTab(tabbedPane.getSelectedIndex());
            }
        }
    }

    private static class RunWithOriginalLayoutManagerDelegateAction
    implements Action {
        private final Action delegate;

        static void install(ActionMap map, String key) {
            Action oldAction = map.get(key);
            if (oldAction == null || oldAction instanceof RunWithOriginalLayoutManagerDelegateAction) {
                return;
            }
            map.put(key, new RunWithOriginalLayoutManagerDelegateAction(oldAction));
        }

        private RunWithOriginalLayoutManagerDelegateAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getValue(String key) {
            return this.delegate.getValue(key);
        }

        @Override
        public boolean isEnabled() {
            return this.delegate.isEnabled();
        }

        @Override
        public void putValue(String key, Object value) {
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
            TabbedPaneUI ui = tabbedPane.getUI();
            if (ui instanceof FlatTabbedPaneUI) {
                ((FlatTabbedPaneUI)ui).runWithOriginalLayoutManager(() -> this.delegate.actionPerformed(e));
            } else {
                this.delegate.actionPerformed(e);
            }
        }
    }

    protected class FlatTabbedPaneScrollLayout
    extends FlatTabbedPaneLayout
    implements LayoutManager {
        private final BasicTabbedPaneUI.TabbedPaneLayout delegate;

        protected FlatTabbedPaneScrollLayout(BasicTabbedPaneUI.TabbedPaneLayout delegate) {
            this.delegate = delegate;
        }

        @Override
        public void calculateLayoutInfo() {
            this.delegate.calculateLayoutInfo();
        }

        @Override
        protected Dimension calculateTabAreaSize() {
            Dimension size = super.calculateTabAreaSize();
            if (FlatTabbedPaneUI.this.isHorizontalTabPlacement(FlatTabbedPaneUI.this.tabPane.getTabPlacement())) {
                size.width = Math.min(size.width, UIScale.scale(100));
            } else {
                size.height = Math.min(size.height, UIScale.scale(100));
            }
            return size;
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            if (this.isContentEmpty()) {
                return this.calculateTabAreaSize();
            }
            return this.delegate.preferredLayoutSize(parent);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            if (this.isContentEmpty()) {
                return this.calculateTabAreaSize();
            }
            return this.delegate.minimumLayoutSize(parent);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            this.delegate.addLayoutComponent(name, comp);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            this.delegate.removeLayoutComponent(comp);
        }

        @Override
        protected void layoutContainerImpl() {
            Dimension viewSize;
            Rectangle tr2;
            boolean useTabAreaAlignment;
            Point viewPosition;
            this.layoutChildComponents();
            int tabsPopupPolicy = FlatTabbedPaneUI.this.getTabsPopupPolicy();
            int scrollButtonsPolicy = FlatTabbedPaneUI.this.getScrollButtonsPolicy();
            int scrollButtonsPlacement = FlatTabbedPaneUI.this.getScrollButtonsPlacement();
            boolean useMoreTabsButton = tabsPopupPolicy == 2;
            boolean useScrollButtons = scrollButtonsPolicy == 2 || scrollButtonsPolicy == 3;
            boolean hideDisabledScrollButtons = scrollButtonsPolicy == 3 && scrollButtonsPlacement == 100;
            boolean trailingScrollButtons = scrollButtonsPlacement == 11;
            boolean leftToRight = FlatTabbedPaneUI.this.isLeftToRight();
            if (!leftToRight && FlatTabbedPaneUI.this.isHorizontalTabPlacement(FlatTabbedPaneUI.this.tabPane.getTabPlacement())) {
                useMoreTabsButton = true;
                useScrollButtons = false;
            }
            JButton backwardButton = null;
            JButton forwardButton = null;
            for (Component c : FlatTabbedPaneUI.this.tabPane.getComponents()) {
                if (!(c instanceof FlatScrollableTabButton)) continue;
                int direction = ((FlatScrollableTabButton)c).getDirection();
                if (direction == 7 || direction == 1) {
                    backwardButton = (JButton)c;
                    continue;
                }
                if (direction != 3 && direction != 5) continue;
                forwardButton = (JButton)c;
            }
            if (backwardButton == null || forwardButton == null) {
                return;
            }
            int tabPlacement = FlatTabbedPaneUI.this.tabPane.getTabPlacement();
            int tabAreaAlignment = FlatTabbedPaneUI.this.getTabAreaAlignment();
            Insets tabAreaInsets = FlatTabbedPaneUI.this.getRealTabAreaInsets(tabPlacement);
            boolean moreTabsButtonVisible = false;
            boolean backwardButtonVisible = false;
            boolean forwardButtonVisible = false;
            if (tabAreaInsets.left != 0 || tabAreaInsets.top != 0) {
                FlatTabbedPaneUI.this.shiftTabs(-tabAreaInsets.left, -tabAreaInsets.top);
                Component view = FlatTabbedPaneUI.this.tabViewport.getView();
                Dimension viewSize2 = view.getPreferredSize();
                boolean horizontal = tabPlacement == 1 || tabPlacement == 3;
                view.setPreferredSize(new Dimension(viewSize2.width - (horizontal ? tabAreaInsets.left : 0), viewSize2.height - (horizontal ? 0 : tabAreaInsets.top)));
            }
            Rectangle tr = this.getTabAreaLayoutBounds(tabPlacement, tabAreaInsets);
            if (tabPlacement == 1 || tabPlacement == 3) {
                if (useScrollButtons && hideDisabledScrollButtons) {
                    viewPosition = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
                    if (viewPosition.x <= backwardButton.getPreferredSize().width) {
                        FlatTabbedPaneUI.this.tabViewport.setViewPosition(new Point(0, viewPosition.y));
                    }
                }
                int availWidth = tr.width - FlatTabbedPaneUI.this.getLeadingPreferredWidth() - FlatTabbedPaneUI.this.getTrailingPreferredWidth() - tabAreaInsets.left - tabAreaInsets.right;
                int totalTabWidth = FlatTabbedPaneUI.this.rects.length > 0 ? FlatTabbedPaneUI.this.rectsTotalWidth(leftToRight) : 0;
                useTabAreaAlignment = totalTabWidth < availWidth;
                tr2 = this.layoutLeftAndRightComponents(tr, tabAreaAlignment, tabAreaInsets, useTabAreaAlignment, false, leftToRight);
                if (FlatTabbedPaneUI.this.rects.length > 0) {
                    int x = tr2.x + (leftToRight ? tabAreaInsets.left : tabAreaInsets.right);
                    int w = tr2.width - tabAreaInsets.left - tabAreaInsets.right;
                    int y = tr2.y;
                    int h = tr2.height;
                    if (w < totalTabWidth) {
                        if (useMoreTabsButton) {
                            int buttonWidth = ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).moreTabsButton.getPreferredSize().width;
                            FlatTabbedPaneUI.this.moreTabsButton.setBounds(leftToRight ? x + w - buttonWidth : x, y, buttonWidth, h);
                            x += leftToRight ? 0 : buttonWidth;
                            w -= buttonWidth;
                            moreTabsButtonVisible = true;
                        }
                        if (useScrollButtons) {
                            int buttonWidth;
                            Point viewPosition2 = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
                            viewSize = FlatTabbedPaneUI.this.tabViewport.getViewSize();
                            if (!hideDisabledScrollButtons || viewPosition2.x > 0) {
                                buttonWidth = backwardButton.getPreferredSize().width;
                                if (trailingScrollButtons) {
                                    backwardButton.setBounds(leftToRight ? x + w - buttonWidth : x, y, buttonWidth, h);
                                    x += leftToRight ? 0 : buttonWidth;
                                } else {
                                    backwardButton.setBounds(leftToRight ? x : x + w - buttonWidth, y, buttonWidth, h);
                                    x += leftToRight ? buttonWidth : 0;
                                }
                                w -= buttonWidth;
                                backwardButtonVisible = true;
                            }
                            if (!hideDisabledScrollButtons || viewSize.width - viewPosition2.x > w) {
                                buttonWidth = forwardButton.getPreferredSize().width;
                                forwardButton.setBounds(leftToRight ? x + w - buttonWidth : x, y, buttonWidth, h);
                                x += leftToRight ? 0 : buttonWidth;
                                w -= buttonWidth;
                                forwardButtonVisible = true;
                            }
                        }
                    }
                    FlatTabbedPaneUI.this.tabViewport.setBounds(x, y, w, h);
                    if (!leftToRight) {
                        FlatTabbedPaneUI.this.tabViewport.doLayout();
                        FlatTabbedPaneUI.this.shiftTabs(FlatTabbedPaneUI.this.tabViewport.getView().getWidth() - (((FlatTabbedPaneUI)FlatTabbedPaneUI.this).rects[0].x + ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).rects[0].width), 0);
                    }
                }
            } else {
                if (useScrollButtons && hideDisabledScrollButtons) {
                    viewPosition = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
                    if (viewPosition.y <= backwardButton.getPreferredSize().height) {
                        FlatTabbedPaneUI.this.tabViewport.setViewPosition(new Point(viewPosition.x, 0));
                    }
                }
                int availHeight = tr.height - FlatTabbedPaneUI.this.getLeadingPreferredHeight() - FlatTabbedPaneUI.this.getTrailingPreferredHeight() - tabAreaInsets.top - tabAreaInsets.bottom;
                int totalTabHeight = FlatTabbedPaneUI.this.rects.length > 0 ? FlatTabbedPaneUI.this.rectsTotalHeight() : 0;
                useTabAreaAlignment = totalTabHeight < availHeight;
                tr2 = this.layoutTopAndBottomComponents(tr, tabAreaAlignment, tabAreaInsets, useTabAreaAlignment, false);
                if (FlatTabbedPaneUI.this.rects.length > 0) {
                    int y = tr2.y + tabAreaInsets.top;
                    int h = tr2.height - tabAreaInsets.top - tabAreaInsets.bottom;
                    int x = tr2.x;
                    int w = tr2.width;
                    if (h < totalTabHeight) {
                        if (useMoreTabsButton) {
                            int buttonHeight = ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).moreTabsButton.getPreferredSize().height;
                            FlatTabbedPaneUI.this.moreTabsButton.setBounds(x, y + h - buttonHeight, w, buttonHeight);
                            h -= buttonHeight;
                            moreTabsButtonVisible = true;
                        }
                        if (useScrollButtons) {
                            int buttonHeight;
                            Point viewPosition3 = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
                            viewSize = FlatTabbedPaneUI.this.tabViewport.getViewSize();
                            if (!hideDisabledScrollButtons || viewPosition3.y > 0) {
                                buttonHeight = backwardButton.getPreferredSize().height;
                                if (trailingScrollButtons) {
                                    backwardButton.setBounds(x, y + h - buttonHeight, w, buttonHeight);
                                } else {
                                    backwardButton.setBounds(x, y, w, buttonHeight);
                                    y += buttonHeight;
                                }
                                h -= buttonHeight;
                                backwardButtonVisible = true;
                            }
                            if (!hideDisabledScrollButtons || viewSize.height - viewPosition3.y > h) {
                                buttonHeight = forwardButton.getPreferredSize().height;
                                forwardButton.setBounds(x, y + h - buttonHeight, w, buttonHeight);
                                h -= buttonHeight;
                                forwardButtonVisible = true;
                            }
                        }
                    }
                    FlatTabbedPaneUI.this.tabViewport.setBounds(x, y, w, h);
                }
            }
            Component view = FlatTabbedPaneUI.this.tabViewport.getView();
            if (view instanceof Container && ((Container)view).getComponentCount() > 0) {
                for (Component c : ((Container)view).getComponents()) {
                    if (!this.isTabContainer(c)) continue;
                    this.layoutTabComponents(c);
                    break;
                }
            }
            FlatTabbedPaneUI.this.tabViewport.setVisible(FlatTabbedPaneUI.this.rects.length > 0);
            FlatTabbedPaneUI.this.moreTabsButton.setVisible(moreTabsButtonVisible);
            backwardButton.setVisible(backwardButtonVisible);
            forwardButton.setVisible(forwardButtonVisible);
            FlatTabbedPaneUI.this.scrollBackwardButtonPrefSize = backwardButton.getPreferredSize();
        }

        @Override
        protected void layoutChildComponent(Component c, Rectangle contentAreaBounds) {
            if (c == FlatTabbedPaneUI.this.tabViewport || c instanceof FlatTabAreaButton || c == FlatTabbedPaneUI.this.leadingComponent || c == FlatTabbedPaneUI.this.trailingComponent) {
                return;
            }
            c.setBounds(contentAreaBounds);
        }
    }

    protected class FlatTabbedPaneLayout
    extends BasicTabbedPaneUI.TabbedPaneLayout {
        protected FlatTabbedPaneLayout() {
            super(FlatTabbedPaneUI.this);
        }

        @Override
        protected Dimension calculateSize(boolean minimum) {
            if (this.isContentEmpty()) {
                return this.calculateTabAreaSize();
            }
            return super.calculateSize(minimum);
        }

        protected boolean isContentEmpty() {
            int tabCount = FlatTabbedPaneUI.this.tabPane.getTabCount();
            if (tabCount == 0) {
                return false;
            }
            for (int i = 0; i < tabCount; ++i) {
                Component c = FlatTabbedPaneUI.this.tabPane.getComponentAt(i);
                if (c == null) continue;
                Dimension cs = c.getPreferredSize();
                if (cs.width == 0 && cs.height == 0) continue;
                return false;
            }
            return true;
        }

        protected Dimension calculateTabAreaSize() {
            int tabPlacement = FlatTabbedPaneUI.this.tabPane.getTabPlacement();
            boolean horizontal = FlatTabbedPaneUI.this.isHorizontalTabPlacement(tabPlacement);
            FontMetrics metrics = FlatTabbedPaneUI.this.getFontMetrics();
            int fontHeight = metrics.getHeight();
            int width = 0;
            int height = 0;
            int tabCount = FlatTabbedPaneUI.this.tabPane.getTabCount();
            for (int i = 0; i < tabCount; ++i) {
                if (horizontal) {
                    width += FlatTabbedPaneUI.this.calculateTabWidth(tabPlacement, i, metrics);
                    height = Math.max(height, FlatTabbedPaneUI.this.calculateTabHeight(tabPlacement, i, fontHeight));
                    continue;
                }
                width = Math.max(width, FlatTabbedPaneUI.this.calculateTabWidth(tabPlacement, i, metrics));
                height += FlatTabbedPaneUI.this.calculateTabHeight(tabPlacement, i, fontHeight);
            }
            if (horizontal) {
                height += UIScale.scale(FlatTabbedPaneUI.this.contentSeparatorHeight);
            } else {
                width += UIScale.scale(FlatTabbedPaneUI.this.contentSeparatorHeight);
            }
            Insets insets = FlatTabbedPaneUI.this.tabPane.getInsets();
            Insets tabAreaInsets = FlatTabbedPaneUI.this.getTabAreaInsets(tabPlacement);
            return new Dimension(width + insets.left + insets.right + tabAreaInsets.left + tabAreaInsets.right, height + insets.bottom + insets.top + tabAreaInsets.top + tabAreaInsets.bottom);
        }

        @Override
        public void layoutContainer(Container parent) {
            Action action;
            FlatTabbedPaneUI.this.setRolloverTab(-1);
            this.calculateLayoutInfo();
            boolean shouldChangeFocus = false;
            int selectedIndex = FlatTabbedPaneUI.this.tabPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                Component oldComp = FlatTabbedPaneUI.this.getVisibleComponent();
                Component newComp = FlatTabbedPaneUI.this.tabPane.getComponentAt(selectedIndex);
                if (newComp != null && newComp != oldComp) {
                    shouldChangeFocus = SwingUtilities.findFocusOwner(oldComp) != null;
                    FlatTabbedPaneUI.this.setVisibleComponent(newComp);
                }
            } else {
                FlatTabbedPaneUI.this.setVisibleComponent(null);
            }
            this.layoutContainerImpl();
            if (shouldChangeFocus && (action = FlatTabbedPaneUI.this.tabPane.getActionMap().get("requestFocusForVisibleComponent")) != null) {
                action.actionPerformed(new ActionEvent(FlatTabbedPaneUI.this.tabPane, 1001, null));
            }
        }

        protected void layoutContainerImpl() {
            int tabPlacement = FlatTabbedPaneUI.this.tabPane.getTabPlacement();
            int tabAreaAlignment = FlatTabbedPaneUI.this.getTabAreaAlignment();
            Insets tabAreaInsets = FlatTabbedPaneUI.this.getRealTabAreaInsets(tabPlacement);
            boolean leftToRight = FlatTabbedPaneUI.this.isLeftToRight();
            Rectangle tr = this.getTabAreaLayoutBounds(tabPlacement, tabAreaInsets);
            if (tabPlacement == 1 || tabPlacement == 3) {
                if (!leftToRight) {
                    FlatTabbedPaneUI.this.shiftTabs(((FlatTabbedPaneUI)FlatTabbedPaneUI.this).tabPane.getInsets().left + tabAreaInsets.right + FlatTabbedPaneUI.this.getTrailingPreferredWidth(), 0);
                }
                this.layoutLeftAndRightComponents(tr, tabAreaAlignment, tabAreaInsets, FlatTabbedPaneUI.this.runCount == 1, true, leftToRight);
            } else {
                this.layoutTopAndBottomComponents(tr, tabAreaAlignment, tabAreaInsets, FlatTabbedPaneUI.this.runCount == 1, true);
            }
            this.layoutChildComponents();
        }

        protected void layoutChildComponents() {
            if (FlatTabbedPaneUI.this.tabPane.getComponentCount() == 0) {
                return;
            }
            Rectangle contentAreaBounds = this.getContentAreaLayoutBounds(FlatTabbedPaneUI.this.tabPane.getTabPlacement(), FlatTabbedPaneUI.this.tabAreaInsets);
            for (Component c : FlatTabbedPaneUI.this.tabPane.getComponents()) {
                this.layoutChildComponent(c, contentAreaBounds);
            }
        }

        protected void layoutChildComponent(Component c, Rectangle contentAreaBounds) {
            if (c == FlatTabbedPaneUI.this.leadingComponent || c == FlatTabbedPaneUI.this.trailingComponent) {
                return;
            }
            if (this.isTabContainer(c)) {
                this.layoutTabContainer(c);
            } else {
                c.setBounds(contentAreaBounds);
            }
        }

        boolean isTabContainer(Component c) {
            return c.getClass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI$TabContainer");
        }

        private void layoutTabContainer(Component tabContainer) {
            int tabPlacement = FlatTabbedPaneUI.this.tabPane.getTabPlacement();
            Rectangle bounds = FlatTabbedPaneUI.this.tabPane.getBounds();
            Insets insets = FlatTabbedPaneUI.this.tabPane.getInsets();
            Insets contentInsets = FlatTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            boolean horizontal = FlatTabbedPaneUI.this.isHorizontalTabPlacement(tabPlacement);
            int tabAreaWidth = !horizontal ? FlatTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, FlatTabbedPaneUI.this.runCount, FlatTabbedPaneUI.this.maxTabWidth) : 0;
            int tabAreaHeight = horizontal ? FlatTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, FlatTabbedPaneUI.this.runCount, FlatTabbedPaneUI.this.maxTabHeight) : 0;
            int w = tabAreaWidth != 0 ? tabAreaWidth + insets.left + insets.right + contentInsets.left + contentInsets.right : bounds.width;
            int h = tabAreaHeight != 0 ? tabAreaHeight + insets.top + insets.bottom + contentInsets.top + contentInsets.bottom : bounds.height;
            int x = tabPlacement == 4 ? bounds.width - w : 0;
            int y = tabPlacement == 3 ? bounds.height - h : 0;
            tabContainer.setBounds(x, y, w, h);
            this.layoutTabComponents(tabContainer);
        }

        void layoutTabComponents(Component tabContainer) {
            if (tabContainer instanceof Container && ((Container)tabContainer).getComponentCount() == 0) {
                return;
            }
            int tabPlacement = FlatTabbedPaneUI.this.tabPane.getTabPlacement();
            int selectedTabIndex = FlatTabbedPaneUI.this.tabPane.getSelectedIndex();
            Rectangle r = new Rectangle();
            int deltaX = -tabContainer.getX();
            int deltaY = -tabContainer.getY();
            if (FlatTabbedPaneUI.this.isScrollTabLayout()) {
                Point viewPosition = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
                deltaX = deltaX - FlatTabbedPaneUI.this.tabViewport.getX() + viewPosition.x;
                deltaY = deltaY - FlatTabbedPaneUI.this.tabViewport.getY() + viewPosition.y;
            }
            int tabCount = FlatTabbedPaneUI.this.tabPane.getTabCount();
            for (int i = 0; i < tabCount; ++i) {
                Component c = FlatTabbedPaneUI.this.tabPane.getTabComponentAt(i);
                if (c == null) continue;
                Rectangle tabBounds = FlatTabbedPaneUI.this.getTabBounds(i, r);
                Insets tabInsets = FlatTabbedPaneUI.this.getTabInsets(tabPlacement, i);
                int ox = tabBounds.x + tabInsets.left + deltaX;
                int oy = tabBounds.y + tabInsets.top + deltaY;
                int ow = tabBounds.width - tabInsets.left - tabInsets.right;
                int oh = tabBounds.height - tabInsets.top - tabInsets.bottom;
                Dimension prefSize = c.getPreferredSize();
                int x = ox + (ow - prefSize.width) / 2;
                int y = oy + (oh - prefSize.height) / 2;
                boolean selected = i == selectedTabIndex;
                c.setBounds(x += FlatTabbedPaneUI.this.getTabLabelShiftX(tabPlacement, i, selected), y += FlatTabbedPaneUI.this.getTabLabelShiftY(tabPlacement, i, selected), prefSize.width, prefSize.height);
            }
        }

        Rectangle getContentAreaLayoutBounds(int tabPlacement, Insets tabAreaInsets) {
            int tabPaneWidth = FlatTabbedPaneUI.this.tabPane.getWidth();
            int tabPaneHeight = FlatTabbedPaneUI.this.tabPane.getHeight();
            Insets insets = FlatTabbedPaneUI.this.tabPane.getInsets();
            Insets contentInsets = FlatTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            boolean horizontal = FlatTabbedPaneUI.this.isHorizontalTabPlacement(tabPlacement);
            int tabAreaWidth = !horizontal ? FlatTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, FlatTabbedPaneUI.this.runCount, FlatTabbedPaneUI.this.maxTabWidth) : 0;
            int tabAreaHeight = horizontal ? FlatTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, FlatTabbedPaneUI.this.runCount, FlatTabbedPaneUI.this.maxTabHeight) : 0;
            Rectangle cr = new Rectangle();
            cr.x = insets.left + contentInsets.left;
            cr.y = insets.top + contentInsets.top;
            cr.width = tabPaneWidth - insets.left - insets.right - contentInsets.left - contentInsets.right - tabAreaWidth;
            cr.height = tabPaneHeight - insets.top - insets.bottom - contentInsets.top - contentInsets.bottom - tabAreaHeight;
            if (tabPlacement == 1) {
                cr.y += tabAreaHeight;
            } else if (tabPlacement == 2) {
                cr.x += tabAreaWidth;
            }
            return cr;
        }

        Rectangle getTabAreaLayoutBounds(int tabPlacement, Insets tabAreaInsets) {
            int tabPaneWidth = FlatTabbedPaneUI.this.tabPane.getWidth();
            int tabPaneHeight = FlatTabbedPaneUI.this.tabPane.getHeight();
            Insets insets = FlatTabbedPaneUI.this.tabPane.getInsets();
            Rectangle tr = new Rectangle();
            if (tabPlacement == 1 || tabPlacement == 3) {
                int tabAreaHeight = FlatTabbedPaneUI.this.maxTabHeight > 0 ? FlatTabbedPaneUI.this.maxTabHeight : Math.max(Math.max(FlatTabbedPaneUI.this.getLeadingPreferredHeight(), FlatTabbedPaneUI.this.getTrailingPreferredHeight()), UIScale.scale(FlatClientProperties.clientPropertyInt(FlatTabbedPaneUI.this.tabPane, "JTabbedPane.tabHeight", FlatTabbedPaneUI.this.tabHeight)));
                tr.x = insets.left;
                tr.y = tabPlacement == 1 ? insets.top + tabAreaInsets.top : tabPaneHeight - insets.bottom - tabAreaInsets.bottom - tabAreaHeight;
                tr.width = tabPaneWidth - insets.left - insets.right;
                tr.height = tabAreaHeight;
            } else {
                int tabAreaWidth = FlatTabbedPaneUI.this.maxTabWidth > 0 ? FlatTabbedPaneUI.this.maxTabWidth : Math.max(FlatTabbedPaneUI.this.getLeadingPreferredWidth(), FlatTabbedPaneUI.this.getTrailingPreferredWidth());
                tr.x = tabPlacement == 2 ? insets.left + tabAreaInsets.left : tabPaneWidth - insets.right - tabAreaInsets.right - tabAreaWidth;
                tr.y = insets.top;
                tr.width = tabAreaWidth;
                tr.height = tabPaneHeight - insets.top - insets.bottom;
            }
            return tr;
        }

        Rectangle layoutLeftAndRightComponents(Rectangle tr, int tabAreaAlignment, Insets tabAreaInsets, boolean useTabAreaAlignment, boolean shiftTabs, boolean leftToRight) {
            int rightWidth;
            int leftWidth;
            int leadingWidth = FlatTabbedPaneUI.this.getLeadingPreferredWidth();
            int trailingWidth = FlatTabbedPaneUI.this.getTrailingPreferredWidth();
            if (useTabAreaAlignment && FlatTabbedPaneUI.this.rects.length > 0) {
                int availWidth = tr.width - leadingWidth - trailingWidth - tabAreaInsets.left - tabAreaInsets.right;
                int totalTabWidth = FlatTabbedPaneUI.this.rectsTotalWidth(leftToRight);
                int diff = availWidth - totalTabWidth;
                switch (tabAreaAlignment) {
                    case 10: {
                        trailingWidth += diff;
                        break;
                    }
                    case 11: {
                        if (shiftTabs) {
                            FlatTabbedPaneUI.this.shiftTabs(leftToRight ? diff : -diff, 0);
                        }
                        leadingWidth += diff;
                        break;
                    }
                    case 0: {
                        if (shiftTabs) {
                            FlatTabbedPaneUI.this.shiftTabs((leftToRight ? diff : -diff) / 2, 0);
                        }
                        leadingWidth += diff / 2;
                        trailingWidth += diff - diff / 2;
                        break;
                    }
                    case 100: {
                        FlatTabbedPaneUI.this.stretchTabsWidth(diff, leftToRight);
                    }
                }
            } else if (FlatTabbedPaneUI.this.rects.length == 0) {
                trailingWidth = tr.width - leadingWidth;
            }
            Container leftComponent = leftToRight ? FlatTabbedPaneUI.this.leadingComponent : FlatTabbedPaneUI.this.trailingComponent;
            int n = leftWidth = leftToRight ? leadingWidth : trailingWidth;
            if (leftComponent != null) {
                leftComponent.setBounds(tr.x, tr.y, leftWidth, tr.height);
            }
            Container rightComponent = leftToRight ? FlatTabbedPaneUI.this.trailingComponent : FlatTabbedPaneUI.this.leadingComponent;
            int n2 = rightWidth = leftToRight ? trailingWidth : leadingWidth;
            if (rightComponent != null) {
                rightComponent.setBounds(tr.x + tr.width - rightWidth, tr.y, rightWidth, tr.height);
            }
            Rectangle r = new Rectangle(tr);
            r.x += leftWidth;
            r.width -= leftWidth + rightWidth;
            return r;
        }

        Rectangle layoutTopAndBottomComponents(Rectangle tr, int tabAreaAlignment, Insets tabAreaInsets, boolean useTabAreaAlignment, boolean shiftTabs) {
            int topHeight = FlatTabbedPaneUI.this.getLeadingPreferredHeight();
            int bottomHeight = FlatTabbedPaneUI.this.getTrailingPreferredHeight();
            if (useTabAreaAlignment && FlatTabbedPaneUI.this.rects.length > 0) {
                int availHeight = tr.height - topHeight - bottomHeight - tabAreaInsets.top - tabAreaInsets.bottom;
                int totalTabHeight = FlatTabbedPaneUI.this.rectsTotalHeight();
                int diff = availHeight - totalTabHeight;
                switch (tabAreaAlignment) {
                    case 10: {
                        bottomHeight += diff;
                        break;
                    }
                    case 11: {
                        if (shiftTabs) {
                            FlatTabbedPaneUI.this.shiftTabs(0, diff);
                        }
                        topHeight += diff;
                        break;
                    }
                    case 0: {
                        if (shiftTabs) {
                            FlatTabbedPaneUI.this.shiftTabs(0, diff / 2);
                        }
                        topHeight += diff / 2;
                        bottomHeight += diff - diff / 2;
                        break;
                    }
                    case 100: {
                        FlatTabbedPaneUI.this.stretchTabsHeight(diff);
                    }
                }
            } else if (FlatTabbedPaneUI.this.rects.length == 0) {
                bottomHeight = tr.height - topHeight;
            }
            if (FlatTabbedPaneUI.this.leadingComponent != null) {
                FlatTabbedPaneUI.this.leadingComponent.setBounds(tr.x, tr.y, tr.width, topHeight);
            }
            if (FlatTabbedPaneUI.this.trailingComponent != null) {
                FlatTabbedPaneUI.this.trailingComponent.setBounds(tr.x, tr.y + tr.height - bottomHeight, tr.width, bottomHeight);
            }
            Rectangle r = new Rectangle(tr);
            r.y += topHeight;
            r.height -= topHeight + bottomHeight;
            return r;
        }
    }

    private class Handler
    implements MouseListener,
    MouseMotionListener,
    PropertyChangeListener,
    ChangeListener,
    ComponentListener,
    ContainerListener,
    FocusListener {
        MouseListener mouseDelegate;
        PropertyChangeListener propertyChangeDelegate;
        ChangeListener changeDelegate;
        FocusListener focusDelegate;
        private final PropertyChangeListener contentListener = this::contentPropertyChange;
        private int pressedTabIndex = -1;
        private int lastTipTabIndex = -1;
        private String lastTip;

        private Handler() {
        }

        void installListeners() {
            FlatTabbedPaneUI.this.tabPane.addMouseMotionListener(this);
            FlatTabbedPaneUI.this.tabPane.addComponentListener(this);
            FlatTabbedPaneUI.this.tabPane.addContainerListener(this);
            for (Component c : FlatTabbedPaneUI.this.tabPane.getComponents()) {
                if (c instanceof UIResource) continue;
                c.addPropertyChangeListener(this.contentListener);
            }
        }

        void uninstallListeners() {
            FlatTabbedPaneUI.this.tabPane.removeMouseMotionListener(this);
            FlatTabbedPaneUI.this.tabPane.removeComponentListener(this);
            FlatTabbedPaneUI.this.tabPane.removeContainerListener(this);
            for (Component c : FlatTabbedPaneUI.this.tabPane.getComponents()) {
                if (c instanceof UIResource) continue;
                c.removePropertyChangeListener(this.contentListener);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            this.mouseDelegate.mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.updateRollover(e);
            if (!FlatTabbedPaneUI.this.isPressedTabClose() && SwingUtilities.isLeftMouseButton(e)) {
                this.mouseDelegate.mousePressed(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (FlatTabbedPaneUI.this.isPressedTabClose()) {
                this.updateRollover(e);
                if (this.pressedTabIndex >= 0 && this.pressedTabIndex == FlatTabbedPaneUI.this.getRolloverTab()) {
                    this.restoreTabToolTip();
                    FlatTabbedPaneUI.this.closeTab(this.pressedTabIndex);
                }
            } else {
                this.mouseDelegate.mouseReleased(e);
            }
            this.pressedTabIndex = -1;
            this.updateRollover(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.updateRollover(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.updateRollover(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            this.updateRollover(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            this.updateRollover(e);
        }

        private void updateRollover(MouseEvent e) {
            boolean hitClose;
            int x = e.getX();
            int y = e.getY();
            int tabIndex = FlatTabbedPaneUI.this.tabForCoordinate(FlatTabbedPaneUI.this.tabPane, x, y);
            FlatTabbedPaneUI.this.setRolloverTab(tabIndex);
            boolean bl = hitClose = FlatTabbedPaneUI.this.isTabClosable(tabIndex) && FlatTabbedPaneUI.this.getTabCloseHitArea(tabIndex).contains(x, y);
            if (e.getID() == 501 && SwingUtilities.isLeftMouseButton(e)) {
                this.pressedTabIndex = hitClose ? tabIndex : -1;
            }
            FlatTabbedPaneUI.this.setRolloverTabClose(hitClose);
            FlatTabbedPaneUI.this.setPressedTabClose(hitClose && tabIndex == this.pressedTabIndex);
            if (tabIndex >= 0 && hitClose) {
                Object closeTip = FlatTabbedPaneUI.this.getTabClientProperty(tabIndex, "JTabbedPane.tabCloseToolTipText");
                if (closeTip == null) {
                    closeTip = FlatTabbedPaneUI.this.tabCloseToolTipText;
                }
                if (closeTip instanceof String) {
                    this.setCloseToolTip(tabIndex, (String)closeTip);
                } else {
                    this.restoreTabToolTip();
                }
            } else {
                this.restoreTabToolTip();
            }
        }

        private void setCloseToolTip(int tabIndex, String closeTip) {
            if (tabIndex == this.lastTipTabIndex) {
                return;
            }
            this.restoreTabToolTip();
            this.lastTipTabIndex = tabIndex;
            this.lastTip = FlatTabbedPaneUI.this.tabPane.getToolTipTextAt(this.lastTipTabIndex);
            FlatTabbedPaneUI.this.tabPane.setToolTipTextAt(this.lastTipTabIndex, closeTip);
        }

        private void restoreTabToolTip() {
            if (this.lastTipTabIndex < 0) {
                return;
            }
            if (this.lastTipTabIndex < FlatTabbedPaneUI.this.tabPane.getTabCount()) {
                FlatTabbedPaneUI.this.tabPane.setToolTipTextAt(this.lastTipTabIndex, this.lastTip);
            }
            this.lastTip = null;
            this.lastTipTabIndex = -1;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "tabPlacement": 
                case "opaque": 
                case "background": 
                case "indexForTabComponent": {
                    FlatTabbedPaneUI.this.runWithOriginalLayoutManager(() -> this.propertyChangeDelegate.propertyChange(e));
                    break;
                }
                default: {
                    this.propertyChangeDelegate.propertyChange(e);
                }
            }
            switch (e.getPropertyName()) {
                case "tabPlacement": {
                    if (!(FlatTabbedPaneUI.this.moreTabsButton instanceof FlatMoreTabsButton)) break;
                    ((FlatMoreTabsButton)FlatTabbedPaneUI.this.moreTabsButton).updateDirection();
                    break;
                }
                case "componentOrientation": {
                    FlatTabbedPaneUI.this.ensureSelectedTabIsVisibleLater();
                    break;
                }
                case "JTabbedPane.showTabSeparators": 
                case "JTabbedPane.tabType": {
                    FlatTabbedPaneUI.this.tabPane.repaint();
                    break;
                }
                case "JTabbedPane.showContentSeparator": 
                case "JTabbedPane.hasFullBorder": 
                case "JTabbedPane.hideTabAreaWithOneTab": 
                case "JTabbedPane.minimumTabWidth": 
                case "JTabbedPane.maximumTabWidth": 
                case "JTabbedPane.tabHeight": 
                case "JTabbedPane.tabInsets": 
                case "JTabbedPane.tabAreaInsets": 
                case "JTabbedPane.tabsPopupPolicy": 
                case "JTabbedPane.scrollButtonsPolicy": 
                case "JTabbedPane.scrollButtonsPlacement": 
                case "JTabbedPane.tabAreaAlignment": 
                case "JTabbedPane.tabAlignment": 
                case "JTabbedPane.tabWidthMode": 
                case "JTabbedPane.tabRotation": 
                case "JTabbedPane.tabIconPlacement": 
                case "JTabbedPane.tabClosable": {
                    FlatTabbedPaneUI.this.tabPane.revalidate();
                    FlatTabbedPaneUI.this.tabPane.repaint();
                    break;
                }
                case "JTabbedPane.leadingComponent": {
                    FlatTabbedPaneUI.this.uninstallLeadingComponent();
                    FlatTabbedPaneUI.this.installLeadingComponent();
                    FlatTabbedPaneUI.this.tabPane.revalidate();
                    FlatTabbedPaneUI.this.tabPane.repaint();
                    FlatTabbedPaneUI.this.ensureSelectedTabIsVisibleLater();
                    break;
                }
                case "JTabbedPane.trailingComponent": {
                    FlatTabbedPaneUI.this.uninstallTrailingComponent();
                    FlatTabbedPaneUI.this.installTrailingComponent();
                    FlatTabbedPaneUI.this.tabPane.revalidate();
                    FlatTabbedPaneUI.this.tabPane.repaint();
                    FlatTabbedPaneUI.this.ensureSelectedTabIsVisibleLater();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    FlatTabbedPaneUI.this.installStyle();
                    FlatTabbedPaneUI.this.tabPane.revalidate();
                    FlatTabbedPaneUI.this.tabPane.repaint();
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            this.changeDelegate.stateChanged(e);
            if (FlatTabbedPaneUI.this.moreTabsButton != null) {
                FlatTabbedPaneUI.this.ensureSelectedTabIsVisible();
            }
        }

        protected void contentPropertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "JTabbedPane.minimumTabWidth": 
                case "JTabbedPane.maximumTabWidth": 
                case "JTabbedPane.tabInsets": 
                case "JTabbedPane.tabAlignment": 
                case "JTabbedPane.tabClosable": {
                    FlatTabbedPaneUI.this.tabPane.revalidate();
                    FlatTabbedPaneUI.this.tabPane.repaint();
                }
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            FlatTabbedPaneUI.this.ensureSelectedTabIsVisibleLater();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            if (!(c instanceof UIResource)) {
                c.addPropertyChangeListener(this.contentListener);
            }
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            Component c = e.getChild();
            if (!(c instanceof UIResource)) {
                c.removePropertyChangeListener(this.contentListener);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            this.focusDelegate.focusGained(e);
            FlatTabbedPaneUI.this.repaintTab(FlatTabbedPaneUI.this.tabPane.getSelectedIndex());
        }

        @Override
        public void focusLost(FocusEvent e) {
            this.focusDelegate.focusLost(e);
            FlatTabbedPaneUI.this.repaintTab(FlatTabbedPaneUI.this.tabPane.getSelectedIndex());
        }
    }

    protected class FlatWheelTabScroller
    extends MouseAdapter {
        private int lastMouseX;
        private int lastMouseY;
        private boolean inViewport;
        private boolean scrolled;
        private Timer rolloverTimer;
        private Timer exitedTimer;
        private Animator animator;
        private Point startViewPosition;
        private Point targetViewPosition;

        protected FlatWheelTabScroller() {
        }

        protected void uninstall() {
            if (this.rolloverTimer != null) {
                this.rolloverTimer.stop();
            }
            if (this.exitedTimer != null) {
                this.exitedTimer.stop();
            }
            if (this.animator != null) {
                this.animator.cancel();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (FlatTabbedPaneUI.this.tabPane.getMouseWheelListeners().length > 1) {
                return;
            }
            if (!this.isInViewport(e.getX(), e.getY())) {
                return;
            }
            this.lastMouseX = e.getX();
            this.lastMouseY = e.getY();
            double preciseWheelRotation = e.getPreciseWheelRotation();
            boolean isPreciseWheel = preciseWheelRotation != 0.0 && preciseWheelRotation != (double)e.getWheelRotation();
            int amount = (int)((double)FlatTabbedPaneUI.this.maxTabHeight * preciseWheelRotation);
            if (amount == 0) {
                if (preciseWheelRotation > 0.0) {
                    amount = 1;
                } else if (preciseWheelRotation < 0.0) {
                    amount = -1;
                }
            }
            Point viewPosition = this.targetViewPosition != null ? this.targetViewPosition : FlatTabbedPaneUI.this.tabViewport.getViewPosition();
            Dimension viewSize = FlatTabbedPaneUI.this.tabViewport.getViewSize();
            boolean horizontal = FlatTabbedPaneUI.this.isHorizontalTabPlacement(FlatTabbedPaneUI.this.tabPane.getTabPlacement());
            int x = viewPosition.x;
            int y = viewPosition.y;
            if (horizontal) {
                x += FlatTabbedPaneUI.this.isLeftToRight() ? amount : -amount;
            } else {
                y += amount;
            }
            if (isPreciseWheel && FlatTabbedPaneUI.this.getScrollButtonsPlacement() == 100 && FlatTabbedPaneUI.this.getScrollButtonsPolicy() == 3 && (FlatTabbedPaneUI.this.isLeftToRight() || !horizontal) && FlatTabbedPaneUI.this.scrollBackwardButtonPrefSize != null) {
                if (horizontal) {
                    if (viewPosition.x == 0 && x > 0) {
                        x += ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).scrollBackwardButtonPrefSize.width;
                    } else if (amount < 0 && x <= ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).scrollBackwardButtonPrefSize.width) {
                        x = 0;
                    }
                } else if (viewPosition.y == 0 && y > 0) {
                    y += ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).scrollBackwardButtonPrefSize.height;
                } else if (amount < 0 && y <= ((FlatTabbedPaneUI)FlatTabbedPaneUI.this).scrollBackwardButtonPrefSize.height) {
                    y = 0;
                }
            }
            if (horizontal) {
                x = Math.min(Math.max(x, 0), viewSize.width - FlatTabbedPaneUI.this.tabViewport.getWidth());
            } else {
                y = Math.min(Math.max(y, 0), viewSize.height - FlatTabbedPaneUI.this.tabViewport.getHeight());
            }
            Point newViewPosition = new Point(x, y);
            if (newViewPosition.equals(viewPosition)) {
                return;
            }
            if (isPreciseWheel) {
                if (this.animator != null) {
                    this.animator.stop();
                }
                FlatTabbedPaneUI.this.tabViewport.setViewPosition(newViewPosition);
                this.updateRolloverDelayed();
            } else {
                this.setViewPositionAnimated(newViewPosition);
            }
            this.scrolled = true;
        }

        protected void setViewPositionAnimated(Point viewPosition) {
            if (viewPosition.equals(FlatTabbedPaneUI.this.tabViewport.getViewPosition())) {
                return;
            }
            if (!FlatTabbedPaneUI.this.isSmoothScrollingEnabled()) {
                FlatTabbedPaneUI.this.tabViewport.setViewPosition(viewPosition);
                this.updateRolloverDelayed();
                return;
            }
            this.startViewPosition = FlatTabbedPaneUI.this.tabViewport.getViewPosition();
            this.targetViewPosition = viewPosition;
            if (this.animator == null) {
                int duration = 200;
                int resolution = 10;
                this.animator = new Animator(duration, fraction -> {
                    if (FlatTabbedPaneUI.this.tabViewport == null || !FlatTabbedPaneUI.this.tabViewport.isShowing()) {
                        this.animator.stop();
                        return;
                    }
                    int x = this.startViewPosition.x + Math.round((float)(this.targetViewPosition.x - this.startViewPosition.x) * fraction);
                    int y = this.startViewPosition.y + Math.round((float)(this.targetViewPosition.y - this.startViewPosition.y) * fraction);
                    FlatTabbedPaneUI.this.tabViewport.setViewPosition(new Point(x, y));
                }, () -> {
                    this.targetViewPosition = null;
                    this.startViewPosition = null;
                    if (FlatTabbedPaneUI.this.tabPane != null) {
                        FlatTabbedPaneUI.this.setRolloverTab(this.lastMouseX, this.lastMouseY);
                    }
                });
                this.animator.setResolution(resolution);
                this.animator.setInterpolator(new CubicBezierEasing(0.5f, 0.5f, 0.5f, 1.0f));
            }
            this.animator.restart();
        }

        protected void updateRolloverDelayed() {
            int index;
            FlatTabbedPaneUI.this.blockRollover = true;
            int oldIndex = FlatTabbedPaneUI.this.getRolloverTab();
            if (oldIndex >= 0 && (index = FlatTabbedPaneUI.this.tabForCoordinate(FlatTabbedPaneUI.this.tabPane, this.lastMouseX, this.lastMouseY)) >= 0 && index != oldIndex) {
                FlatTabbedPaneUI.this.blockRollover = false;
                FlatTabbedPaneUI.this.setRolloverTab(-1);
                FlatTabbedPaneUI.this.blockRollover = true;
            }
            if (this.rolloverTimer == null) {
                this.rolloverTimer = new Timer(150, e -> {
                    FlatTabbedPaneUI.this.blockRollover = false;
                    if (FlatTabbedPaneUI.this.tabPane != null) {
                        FlatTabbedPaneUI.this.setRolloverTab(this.lastMouseX, this.lastMouseY);
                    }
                });
                this.rolloverTimer.setRepeats(false);
            }
            this.rolloverTimer.restart();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            this.checkViewportExited(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.checkViewportExited(e.getX(), e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            FlatTabbedPaneUI.this.setRolloverTab(e.getX(), e.getY());
        }

        protected boolean isInViewport(int x, int y) {
            return FlatTabbedPaneUI.this.tabViewport != null && FlatTabbedPaneUI.this.tabViewport.getBounds().contains(x, y);
        }

        protected void checkViewportExited(int x, int y) {
            this.lastMouseX = x;
            this.lastMouseY = y;
            boolean wasInViewport = this.inViewport;
            this.inViewport = this.isInViewport(x, y);
            if (this.inViewport != wasInViewport) {
                if (!this.inViewport) {
                    this.viewportExited();
                } else if (this.exitedTimer != null) {
                    this.exitedTimer.stop();
                }
            }
        }

        protected void viewportExited() {
            if (!this.scrolled) {
                return;
            }
            if (this.exitedTimer == null) {
                this.exitedTimer = new Timer(500, e -> this.ensureSelectedTabVisible());
                this.exitedTimer.setRepeats(false);
            }
            this.exitedTimer.start();
        }

        protected void ensureSelectedTabVisible() {
            if (FlatTabbedPaneUI.this.tabPane == null || FlatTabbedPaneUI.this.tabViewport == null) {
                return;
            }
            if (!this.scrolled) {
                return;
            }
            this.scrolled = false;
            FlatTabbedPaneUI.this.ensureSelectedTabIsVisible();
        }
    }

    protected class FlatScrollableTabButton
    extends FlatTabAreaButton
    implements MouseListener {
        private Timer autoRepeatTimer;

        protected FlatScrollableTabButton(int direction) {
            super(direction);
            this.addMouseListener(this);
        }

        @Override
        protected void fireActionPerformed(ActionEvent event) {
            FlatTabbedPaneUI.this.runWithOriginalLayoutManager(() -> super.fireActionPerformed(event));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && this.isEnabled()) {
                if (this.autoRepeatTimer == null) {
                    this.autoRepeatTimer = new Timer(60, e2 -> {
                        if (this.isEnabled()) {
                            this.doClick();
                        }
                    });
                    this.autoRepeatTimer.setInitialDelay(300);
                }
                this.autoRepeatTimer.start();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (this.autoRepeatTimer != null) {
                this.autoRepeatTimer.stop();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (this.autoRepeatTimer != null && this.isPressed()) {
                this.autoRepeatTimer.start();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (this.autoRepeatTimer != null) {
                this.autoRepeatTimer.stop();
            }
        }
    }

    protected class FlatMoreTabsButton
    extends FlatTabAreaButton
    implements ActionListener,
    PopupMenuListener {
        private boolean popupVisible;

        public FlatMoreTabsButton() {
            super(5);
            this.updateDirection();
            this.setToolTipText(FlatTabbedPaneUI.this.moreTabsButtonToolTipText);
            this.addActionListener(this);
        }

        protected void updateDirection() {
            int direction;
            switch (FlatTabbedPaneUI.this.tabPane.getTabPlacement()) {
                default: {
                    direction = 5;
                    break;
                }
                case 3: {
                    direction = 1;
                    break;
                }
                case 2: {
                    direction = 3;
                    break;
                }
                case 4: {
                    direction = 7;
                }
            }
            this.setDirection(direction);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            boolean horizontal = this.direction == 5 || this.direction == 1;
            int margin = UIScale.scale(8);
            return new Dimension(size.width + (horizontal ? margin : 0), size.height + (horizontal ? 0 : margin));
        }

        @Override
        public void paint(Graphics g) {
            if (this.direction == 3 || this.direction == 7) {
                int xoffset = Math.max(UIScale.unscale((this.getWidth() - this.getHeight()) / 2) - 4, 0);
                this.setXOffset(this.direction == 3 ? (float)xoffset : (float)(-xoffset));
            } else {
                this.setXOffset(0.0f);
            }
            super.paint(g);
        }

        @Override
        protected boolean isHover() {
            return super.isHover() || this.popupVisible;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (FlatTabbedPaneUI.this.tabViewport == null) {
                return;
            }
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.addPopupMenuListener(this);
            Rectangle viewRect = FlatTabbedPaneUI.this.tabViewport.getViewRect();
            int lastIndex = -1;
            for (int i = 0; i < FlatTabbedPaneUI.this.rects.length; ++i) {
                if (viewRect.contains(FlatTabbedPaneUI.this.rects[i])) continue;
                if (lastIndex >= 0 && lastIndex + 1 != i) {
                    popupMenu.addSeparator();
                }
                lastIndex = i;
                popupMenu.add(this.createTabMenuItem(i));
            }
            int buttonWidth = this.getWidth();
            int buttonHeight = this.getHeight();
            Dimension popupSize = popupMenu.getPreferredSize();
            int x = FlatTabbedPaneUI.this.isLeftToRight() ? buttonWidth - popupSize.width : 0;
            int y = buttonHeight - popupSize.height;
            switch (FlatTabbedPaneUI.this.tabPane.getTabPlacement()) {
                default: {
                    y = buttonHeight;
                    break;
                }
                case 3: {
                    y = -popupSize.height;
                    break;
                }
                case 2: {
                    x = buttonWidth;
                    break;
                }
                case 4: {
                    x = -popupSize.width;
                }
            }
            popupMenu.show(this, x, y);
        }

        protected JMenuItem createTabMenuItem(int tabIndex) {
            Color backgroundAt;
            String title = FlatTabbedPaneUI.this.tabPane.getTitleAt(tabIndex);
            if (StringUtils.isEmpty(title)) {
                Component tabComp = FlatTabbedPaneUI.this.tabPane.getTabComponentAt(tabIndex);
                if (tabComp != null) {
                    title = this.findTabTitle(tabComp);
                }
                if (StringUtils.isEmpty(title)) {
                    title = FlatTabbedPaneUI.this.tabPane.getAccessibleContext().getAccessibleChild(tabIndex).getAccessibleContext().getAccessibleName();
                }
                if (StringUtils.isEmpty(title) && tabComp instanceof Accessible) {
                    title = this.findTabTitleInAccessible((Accessible)((Object)tabComp));
                }
                if (StringUtils.isEmpty(title)) {
                    title = tabIndex + 1 + ". Tab";
                }
            }
            JMenuItem menuItem = new JMenuItem(title, FlatTabbedPaneUI.this.tabPane.getIconAt(tabIndex));
            menuItem.setDisabledIcon(FlatTabbedPaneUI.this.tabPane.getDisabledIconAt(tabIndex));
            menuItem.setToolTipText(FlatTabbedPaneUI.this.tabPane.getToolTipTextAt(tabIndex));
            Color foregroundAt = FlatTabbedPaneUI.this.tabPane.getForegroundAt(tabIndex);
            if (foregroundAt != FlatTabbedPaneUI.this.tabPane.getForeground()) {
                menuItem.setForeground(foregroundAt);
            }
            if ((backgroundAt = FlatTabbedPaneUI.this.tabPane.getBackgroundAt(tabIndex)) != FlatTabbedPaneUI.this.tabPane.getBackground()) {
                menuItem.setBackground(backgroundAt);
                menuItem.setOpaque(true);
            }
            if (!FlatTabbedPaneUI.this.tabPane.isEnabled() || !FlatTabbedPaneUI.this.tabPane.isEnabledAt(tabIndex)) {
                menuItem.setEnabled(false);
            }
            menuItem.addActionListener(e -> this.selectTab(tabIndex));
            return menuItem;
        }

        private String findTabTitle(Component c) {
            String title = null;
            if (c instanceof JLabel) {
                title = ((JLabel)c).getText();
            } else if (c instanceof JTextComponent) {
                title = ((JTextComponent)c).getText();
            }
            if (!StringUtils.isEmpty(title)) {
                return title;
            }
            if (c instanceof Container) {
                for (Component child : ((Container)c).getComponents()) {
                    title = this.findTabTitle(child);
                    if (title == null) continue;
                    return title;
                }
            }
            return null;
        }

        private String findTabTitleInAccessible(Accessible accessible) {
            AccessibleContext context = accessible.getAccessibleContext();
            if (context == null) {
                return null;
            }
            String title = context.getAccessibleName();
            if (!StringUtils.isEmpty(title)) {
                return title;
            }
            int childrenCount = context.getAccessibleChildrenCount();
            for (int i = 0; i < childrenCount; ++i) {
                title = this.findTabTitleInAccessible(context.getAccessibleChild(i));
                if (title == null) continue;
                return title;
            }
            return null;
        }

        protected void selectTab(int tabIndex) {
            FlatTabbedPaneUI.this.tabPane.setSelectedIndex(tabIndex);
            FlatTabbedPaneUI.this.ensureSelectedTabIsVisible();
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            this.popupVisible = true;
            this.repaint();
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            this.popupVisible = false;
            this.repaint();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            this.popupVisible = false;
            this.repaint();
        }
    }

    protected class FlatTabAreaButton
    extends FlatArrowButton {
        public FlatTabAreaButton(int direction) {
            super(direction, FlatTabbedPaneUI.this.arrowType, FlatTabbedPaneUI.this.foreground, FlatTabbedPaneUI.this.disabledForeground, null, FlatTabbedPaneUI.this.buttonHoverBackground, null, FlatTabbedPaneUI.this.buttonPressedBackground);
            this.setArrowWidth(11);
        }

        protected void updateStyle() {
            this.updateStyle(FlatTabbedPaneUI.this.arrowType, FlatTabbedPaneUI.this.foreground, FlatTabbedPaneUI.this.disabledForeground, null, FlatTabbedPaneUI.this.buttonHoverBackground, null, FlatTabbedPaneUI.this.buttonPressedBackground);
        }

        @Override
        protected Color deriveBackground(Color background) {
            return FlatUIUtils.deriveColor(background, FlatTabbedPaneUI.this.tabPane.getBackground());
        }

        @Override
        protected void paintBackground(Graphics2D g) {
            Insets insets = new Insets(0, 0, 0, 0);
            FlatTabbedPaneUI.rotateInsets(FlatTabbedPaneUI.this.buttonInsets, insets, FlatTabbedPaneUI.this.tabPane.getTabPlacement());
            int top = UIScale.scale2(insets.top);
            int left = UIScale.scale2(insets.left);
            int bottom = UIScale.scale2(insets.bottom);
            int right = UIScale.scale2(insets.right);
            FlatUIUtils.paintComponentBackground(g, left, top, this.getWidth() - left - right, this.getHeight() - top - bottom, 0.0f, UIScale.scale((float)FlatTabbedPaneUI.this.buttonArc));
        }
    }

    private class ContainerUIResource
    extends JPanel
    implements UIResource {
        private ContainerUIResource(Component c) {
            super(new BorderLayout());
            this.add(c);
        }

        @Override
        public void reshape(int x, int y, int w, int h) {
            if (FlatTabbedPaneUI.this.inBasicLayoutContainer) {
                return;
            }
            super.reshape(x, y, w, h);
        }
    }

    private static class TabCloseButton
    extends JButton
    implements UIResource {
        private TabCloseButton() {
        }
    }
}

