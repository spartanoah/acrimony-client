/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.ScaledImageIcon;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Function;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import javax.swing.table.TableCellRenderer;

public class FlatFileChooserUI
extends MetalFileChooserUI {
    private final FlatFileView fileView = new FlatFileView();
    private FlatShortcutsPanel shortcutsPanel;

    public static ComponentUI createUI(JComponent c) {
        return new FlatFileChooserUI((JFileChooser)c);
    }

    public FlatFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }

    @Override
    public void installComponents(JFileChooser fc) {
        FlatShortcutsPanel panel;
        super.installComponents(fc);
        this.patchUI(fc);
        if (!UIManager.getBoolean("FileChooser.noPlacesBar") && (panel = this.createShortcutsPanel(fc)).getComponentCount() > 0) {
            this.shortcutsPanel = panel;
            fc.add((Component)this.shortcutsPanel, "Before");
            fc.addPropertyChangeListener(this.shortcutsPanel);
        }
    }

    @Override
    public void uninstallComponents(JFileChooser fc) {
        super.uninstallComponents(fc);
        if (this.shortcutsPanel != null) {
            fc.removePropertyChangeListener(this.shortcutsPanel);
            this.shortcutsPanel = null;
        }
    }

    private void patchUI(JFileChooser fc) {
        Component topButtonPanel;
        Component topPanel = fc.getComponent(0);
        if (topPanel instanceof JPanel && ((JPanel)topPanel).getLayout() instanceof BorderLayout && (topButtonPanel = ((JPanel)topPanel).getComponent(0)) instanceof JPanel && ((JPanel)topButtonPanel).getLayout() instanceof BoxLayout) {
            Insets margin = UIManager.getInsets("Button.margin");
            Component[] comps = ((JPanel)topButtonPanel).getComponents();
            for (int i = comps.length - 1; i >= 0; --i) {
                Component c = comps[i];
                if (c instanceof JButton || c instanceof JToggleButton) {
                    AbstractButton b = (AbstractButton)c;
                    b.putClientProperty("JButton.buttonType", "toolBarButton");
                    b.setMargin(margin);
                    b.setFocusable(false);
                    continue;
                }
                if (!(c instanceof Box.Filler)) continue;
                ((JPanel)topButtonPanel).remove(i);
            }
        }
        try {
            int maximumRowCount;
            Component directoryComboBox = ((JPanel)topPanel).getComponent(2);
            if (directoryComboBox instanceof JComboBox && (maximumRowCount = UIManager.getInt("ComboBox.maximumRowCount")) > 0) {
                ((JComboBox)directoryComboBox).setMaximumRowCount(maximumRowCount);
            }
        } catch (ArrayIndexOutOfBoundsException directoryComboBox) {
            // empty catch block
        }
        LayoutManager layout = fc.getLayout();
        if (layout instanceof BorderLayout) {
            BorderLayout borderLayout = (BorderLayout)layout;
            borderLayout.setHgap(8);
            Component north = borderLayout.getLayoutComponent("North");
            Component lineEnd = borderLayout.getLayoutComponent("After");
            Component center = borderLayout.getLayoutComponent("Center");
            Component south = borderLayout.getLayoutComponent("South");
            if (north != null && lineEnd != null && center != null && south != null) {
                JPanel p = new JPanel(new BorderLayout(0, 11));
                p.add(north, "North");
                p.add(lineEnd, "After");
                p.add(center, "Center");
                p.add(south, "South");
                fc.add((Component)p, "Center");
            }
        }
    }

    @Override
    protected JPanel createDetailsView(JFileChooser fc) {
        JPanel p = super.createDetailsView(fc);
        if (!SystemInfo.isWindows) {
            return p;
        }
        JScrollPane scrollPane = null;
        for (Component c : p.getComponents()) {
            if (!(c instanceof JScrollPane)) continue;
            scrollPane = (JScrollPane)c;
            break;
        }
        if (scrollPane == null) {
            return p;
        }
        Component view = scrollPane.getViewport().getView();
        if (!(view instanceof JTable)) {
            return p;
        }
        JTable table = (JTable)view;
        final TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
        table.setDefaultRenderer(Object.class, new TableCellRenderer(){

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof String && ((String)value).startsWith("\u200e")) {
                    String str = (String)value;
                    char[] buf = new char[str.length()];
                    int j = 0;
                    for (int i = 0; i < buf.length; ++i) {
                        char ch = str.charAt(i);
                        if (ch == '\u200e' || ch == '\u200f') continue;
                        buf[j++] = ch;
                    }
                    value = new String(buf, 0, j);
                }
                return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        return p;
    }

    protected FlatShortcutsPanel createShortcutsPanel(JFileChooser fc) {
        return new FlatShortcutsPanel(fc);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension prefSize = super.getPreferredSize(c);
        Dimension minSize = this.getMinimumSize(c);
        int shortcutsPanelWidth = this.shortcutsPanel != null ? this.shortcutsPanel.getPreferredSize().width : 0;
        return new Dimension(Math.max(prefSize.width, minSize.width + shortcutsPanelWidth), Math.max(prefSize.height, minSize.height));
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return UIScale.scale(super.getMinimumSize(c));
    }

    @Override
    public FileView getFileView(JFileChooser fc) {
        return FlatFileChooserUI.doNotUseSystemIcons() ? super.getFileView(fc) : this.fileView;
    }

    @Override
    public void clearIconCache() {
        if (FlatFileChooserUI.doNotUseSystemIcons()) {
            super.clearIconCache();
        } else {
            this.fileView.clearIconCache();
        }
    }

    private static boolean doNotUseSystemIcons() {
        return SystemInfo.isWindows && SystemInfo.isX86 && SystemInfo.isJava_17_orLater && SystemInfo.javaVersion < SystemInfo.toVersion(17, 0, 3, 0);
    }

    private static class ShortcutIcon
    implements Icon {
        private final Icon icon;
        private final int iconWidth;
        private final int iconHeight;

        ShortcutIcon(Icon icon, int iconWidth, int iconHeight) {
            this.icon = icon;
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = (double)this.getIconWidth() / (double)this.icon.getIconWidth();
                g2.translate(x, y);
                g2.scale(scale, scale);
                this.icon.paintIcon(c, g2, 0, 0);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return UIScale.scale(this.iconWidth);
        }

        @Override
        public int getIconHeight() {
            return UIScale.scale(this.iconHeight);
        }
    }

    public static class FlatShortcutsPanel
    extends JToolBar
    implements PropertyChangeListener {
        private final JFileChooser fc;
        private final Dimension buttonSize;
        private final Dimension iconSize;
        private final Function<File[], File[]> filesFunction;
        private final Function<File, String> displayNameFunction;
        private final Function<File, Icon> iconFunction;
        protected final File[] files;
        protected final JToggleButton[] buttons;
        protected final ButtonGroup buttonGroup = new ButtonGroup();

        public FlatShortcutsPanel(JFileChooser fc) {
            super(1);
            this.fc = fc;
            this.setFloatable(false);
            this.buttonSize = UIScale.scale(this.getUIDimension("FileChooser.shortcuts.buttonSize", 84, 64));
            this.iconSize = this.getUIDimension("FileChooser.shortcuts.iconSize", 32, 32);
            this.filesFunction = (Function)UIManager.get("FileChooser.shortcuts.filesFunction");
            this.displayNameFunction = (Function)UIManager.get("FileChooser.shortcuts.displayNameFunction");
            this.iconFunction = (Function)UIManager.get("FileChooser.shortcuts.iconFunction");
            FileSystemView fsv = fc.getFileSystemView();
            File[] files = this.getChooserShortcutPanelFiles(fsv);
            if (this.filesFunction != null) {
                files = this.filesFunction.apply(files);
            }
            ArrayList<File> filesList = new ArrayList<File>();
            ArrayList<JToggleButton> buttonsList = new ArrayList<JToggleButton>();
            for (File file : files) {
                if (file == null) continue;
                if (fsv.isFileSystemRoot(file)) {
                    file = fsv.createFileObject(file.getAbsolutePath());
                }
                String name = this.getDisplayName(fsv, file);
                Icon icon = this.getIcon(fsv, file);
                if (name == null) continue;
                int lastSepIndex = name.lastIndexOf(File.separatorChar);
                if (lastSepIndex >= 0 && lastSepIndex < name.length() - 1) {
                    name = name.substring(lastSepIndex + 1);
                }
                if (icon instanceof ImageIcon) {
                    icon = new ScaledImageIcon((ImageIcon)icon, this.iconSize.width, this.iconSize.height);
                } else if (icon != null) {
                    icon = new ShortcutIcon(icon, this.iconSize.width, this.iconSize.height);
                }
                JToggleButton button = this.createButton(name, icon);
                File f = file;
                button.addActionListener(e -> fc.setCurrentDirectory(f));
                this.add(button);
                this.buttonGroup.add(button);
                filesList.add(file);
                buttonsList.add(button);
            }
            this.files = filesList.toArray(new File[filesList.size()]);
            this.buttons = buttonsList.toArray(new JToggleButton[buttonsList.size()]);
            this.directoryChanged(fc.getCurrentDirectory());
        }

        private Dimension getUIDimension(String key, int defaultWidth, int defaultHeight) {
            Dimension size = UIManager.getDimension(key);
            if (size == null) {
                size = new Dimension(defaultWidth, defaultHeight);
            }
            return size;
        }

        protected JToggleButton createButton(String name, Icon icon) {
            JToggleButton button = new JToggleButton(name, icon);
            button.setVerticalTextPosition(3);
            button.setHorizontalTextPosition(0);
            button.setAlignmentX(0.5f);
            button.setIconTextGap(0);
            button.setPreferredSize(this.buttonSize);
            button.setMaximumSize(this.buttonSize);
            return button;
        }

        protected File[] getChooserShortcutPanelFiles(FileSystemView fsv) {
            try {
                if (SystemInfo.isJava_12_orLater) {
                    Method m = fsv.getClass().getMethod("getChooserShortcutPanelFiles", new Class[0]);
                    File[] files = (File[])m.invoke(fsv, new Object[0]);
                    if (files.length == 1 && files[0].equals(new File(System.getProperty("user.home")))) {
                        files = new File[]{};
                    }
                    return files;
                }
                if (SystemInfo.isWindows) {
                    Class<?> cls = Class.forName("sun.awt.shell.ShellFolder");
                    Method m = cls.getMethod("get", String.class);
                    return (File[])m.invoke(null, "fileChooserShortcutPanelFolders");
                }
            } catch (IllegalAccessException cls) {
            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
            }
            return new File[0];
        }

        protected String getDisplayName(FileSystemView fsv, File file) {
            String name;
            if (this.displayNameFunction != null && (name = this.displayNameFunction.apply(file)) != null) {
                return name;
            }
            return fsv.getSystemDisplayName(file);
        }

        protected Icon getIcon(FileSystemView fsv, File file) {
            Icon icon;
            if (this.iconFunction != null && (icon = this.iconFunction.apply(file)) != null) {
                return icon;
            }
            if (FlatFileChooserUI.doNotUseSystemIcons()) {
                return new FlatFileViewDirectoryIcon();
            }
            try {
                block9: {
                    try {
                        Class<?> cls;
                        if (SystemInfo.isJava_17_orLater) {
                            Method m = fsv.getClass().getMethod("getSystemIcon", File.class, Integer.TYPE, Integer.TYPE);
                            return (Icon)m.invoke(fsv, file, this.iconSize.width, this.iconSize.height);
                        }
                        if ((this.iconSize.width > 16 || this.iconSize.height > 16) && (cls = Class.forName("sun.awt.shell.ShellFolder")).isInstance(file)) {
                            Method m = file.getClass().getMethod("getIcon", Boolean.TYPE);
                            m.setAccessible(true);
                            Image image = (Image)m.invoke(file, true);
                            if (image != null) {
                                return new ImageIcon(image);
                            }
                        }
                    } catch (Exception ex) {
                        if ("java.lang.reflect.InaccessibleObjectException".equals(ex.getClass().getName())) break block9;
                        LoggingFacade.INSTANCE.logSevere(null, ex);
                    }
                }
                return fsv.getSystemIcon(file);
            } catch (NullPointerException ex) {
                return new FlatFileViewDirectoryIcon();
            }
        }

        protected void directoryChanged(File file) {
            if (file != null) {
                String absolutePath = file.getAbsolutePath();
                for (int i = 0; i < this.files.length; ++i) {
                    if (!this.files[i].equals(file) && !this.files[i].getAbsolutePath().equals(absolutePath)) continue;
                    this.buttons[i].setSelected(true);
                    return;
                }
            }
            this.buttonGroup.clearSelection();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "directoryChanged": {
                    this.directoryChanged(this.fc.getCurrentDirectory());
                }
            }
        }
    }

    private class FlatFileView
    extends BasicFileChooserUI.BasicFileView {
        private FlatFileView() {
            super(FlatFileChooserUI.this);
        }

        @Override
        public Icon getIcon(File f) {
            Icon icon = this.getCachedIcon(f);
            if (icon != null) {
                return icon;
            }
            if (f != null) {
                try {
                    icon = FlatFileChooserUI.this.getFileChooser().getFileSystemView().getSystemIcon(f);
                } catch (NullPointerException nullPointerException) {
                    // empty catch block
                }
                if (icon != null) {
                    if (icon instanceof ImageIcon) {
                        icon = new ScaledImageIcon((ImageIcon)icon);
                    }
                    this.cacheIcon(f, icon);
                    return icon;
                }
            }
            if ((icon = super.getIcon(f)) instanceof ImageIcon) {
                icon = new ScaledImageIcon((ImageIcon)icon);
                this.cacheIcon(f, icon);
            }
            return icon;
        }
    }
}

