/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony;

import Acrimony.command.CommandManager;
import Acrimony.event.EventManager;
import Acrimony.filesystem.FileSystem;
import Acrimony.font.FontManager;
import Acrimony.handler.client.BalanceHandler;
import Acrimony.handler.client.CameraHandler;
import Acrimony.handler.client.KeybindHandler;
import Acrimony.handler.client.SlotSpoofHandler;
import Acrimony.handler.packet.PacketBlinkHandler;
import Acrimony.handler.packet.PacketDelayHandler;
import Acrimony.module.ModuleManager;
import Acrimony.ui.menu.AcrimonyMenu;
import Acrimony.ui.notification.NotificationHandler;
import Acrimony.util.AcrimonyClientUtil;
import Acrimony.util.IMinecraft;
import Acrimony.util.render.FontUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.io.IOException;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.ViaMCP;

public class Acrimony
implements IMinecraft {
    public static final Acrimony instance = new Acrimony();
    public final String name = "Acrimony";
    public final String version = "v1.0";
    private EventManager eventManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private PacketDelayHandler packetDelayHandler;
    private PacketBlinkHandler packetBlinkHandler;
    private AcrimonyClientUtil acrimonyClientUtil;
    private NotificationHandler notificationHandler;
    private KeybindHandler keybindHandler;
    private BalanceHandler balanceHandler;
    private CameraHandler cameraHandler;
    private SlotSpoofHandler slotSpoofHandler;
    private FileSystem fileSystem;
    private FontManager fontManager;
    private boolean destructed;

    public void start() throws IOException {
        this.eventManager = new EventManager();
        this.moduleManager = new ModuleManager();
        this.commandManager = new CommandManager();
        this.packetDelayHandler = new PacketDelayHandler();
        this.packetBlinkHandler = new PacketBlinkHandler();
        this.acrimonyClientUtil = new AcrimonyClientUtil();
        this.notificationHandler = new NotificationHandler();
        this.keybindHandler = new KeybindHandler();
        this.balanceHandler = new BalanceHandler();
        this.slotSpoofHandler = new SlotSpoofHandler();
        this.cameraHandler = new CameraHandler();
        this.fileSystem = new FileSystem();
        this.fontManager = new FontManager();
        this.fileSystem.loadDefaultConfig();
        this.fileSystem.loadKeybinds();
        this.moduleManager.modules.forEach(m -> m.onClientStarted());
        FontUtil.initFonts();
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
            ViaLoadingBase.getInstance().reload(ProtocolVersion.v1_12_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (!this.destructed) {
            Acrimony.instance.fileSystem.saveDefaultConfig();
            Acrimony.instance.fileSystem.saveKeybinds();
        }
    }

    public GuiScreen getMainMenu() {
        return this.destructed ? new GuiMainMenu() : new AcrimonyMenu();
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public PacketDelayHandler getPacketDelayHandler() {
        return this.packetDelayHandler;
    }

    public PacketBlinkHandler getPacketBlinkHandler() {
        return this.packetBlinkHandler;
    }

    public AcrimonyClientUtil getAcrimonyClientUtil() {
        return this.acrimonyClientUtil;
    }

    public NotificationHandler getNotificationHandler() {
        return this.notificationHandler;
    }

    public KeybindHandler getKeybindHandler() {
        return this.keybindHandler;
    }

    public BalanceHandler getBalanceHandler() {
        return this.balanceHandler;
    }

    public CameraHandler getCameraHandler() {
        return this.cameraHandler;
    }

    public SlotSpoofHandler getSlotSpoofHandler() {
        return this.slotSpoofHandler;
    }

    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    public FontManager getFontManager() {
        return this.fontManager;
    }

    public boolean isDestructed() {
        return this.destructed;
    }

    public void setDestructed(boolean destructed) {
        this.destructed = destructed;
    }
}

