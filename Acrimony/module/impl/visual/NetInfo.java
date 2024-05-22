/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.HUDModule;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.util.render.DrawUtil;
import Acrimony.util.render.RenderUtil;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class NetInfo
extends HUDModule {
    private final DoubleSetting speed = new DoubleSetting("Update Speed", 50.0, 10.0, 100.0, 1.0);
    private final DequeManager receivedPacketsManager = new DequeManager();
    public final DequeManager sentPacketsManager = new DequeManager();
    private final DequeManager totalPacketsManager = new DequeManager();

    public NetInfo() {
        super("NetInfo", Category.VISUAL, 8.0, 160.0, 140, 30, AlignType.LEFT);
        this.addSettings(this.speed);
        this.posX.setValue(this.width);
        this.posY.setValue(this.height);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.receivedPacketsManager.clearList();
        this.sentPacketsManager.clearList();
        this.totalPacketsManager.clearList();
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() != null) {
            this.receivedPacketsManager.addOneToList();
            this.totalPacketsManager.addOneToList();
        }
    }

    @Listener
    public void onSendPacket(PacketSendEvent event) {
        if (event.getPacket() != null) {
            this.sentPacketsManager.addOneToList();
            this.totalPacketsManager.addOneToList();
        }
    }

    @Override
    public void onClientStarted() {
    }

    @Override
    @Listener
    protected void renderModule(boolean inChat) {
        boolean canRender = inChat;
        if (inChat && this.isEnabled()) {
            this.renderNet(canRender);
        } else if (this.isEnabled()) {
            this.renderNet(canRender);
        }
    }

    private void renderNet(boolean canRender) {
        if (NetInfo.mc.gameSettings.showDebugInfo) {
            return;
        }
        AcrimonyFont sfsmall = Acrimony.instance.getFontManager().getSfprosmall();
        int x = (int)this.posX.getValue();
        int y = (int)this.posY.getValue();
        this.receivedPacketsManager.iterate(16, (long)this.speed.getValue());
        this.sentPacketsManager.iterate(16, (long)this.speed.getValue());
        this.totalPacketsManager.iterate(16, (long)this.speed.getValue());
        sfsmall.drawStringWithShadow("Incoming packets", x, y - 8, -1);
        sfsmall.drawStringWithShadow("Outgoing packets", x, y + this.height + 4, -1);
        this.drawGraph(x, y, this.width, this.height, 15, this.receivedPacketsManager.getCollection());
        this.drawGraph(x, y + this.height + 12, this.width, this.height, 15, this.sentPacketsManager.getCollection());
        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
    }

    private void drawGraph(int x, int y, int width, int height, int nSamplesDisplayed, Collection<Integer> collection) {
        NetInfoRenderUtils.drawRectCustom(x, y, width, height, 0x40000000);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glEnable(3042);
        GL11.glLineWidth(1.0f);
        GL11.glTranslated(x + width, y, 0.0);
        double n6 = (double)width / (double)nSamplesDisplayed;
        double n7 = 0.0;
        RenderUtil.color(-1);
        int n8 = (int)((double)collection.stream().max(Integer::compareTo).orElse(1).intValue() * 1.25);
        int n9 = 0;
        RenderUtil.color(-1);
        GL11.glBegin(3);
        for (int intValue : collection) {
            GL11.glVertex2d(-n7, (double)height - (double)height * Math.min(1.0, (double)intValue / (double)n8));
            n7 += n6;
            n9 += intValue;
        }
        GL11.glEnd();
        int i = n9 / nSamplesDisplayed;
        double n10 = (double)height * Math.min(1.0, (double)i / (double)n8);
        RenderUtil.color(-16711936);
        GL11.glBegin(1);
        GL11.glVertex2d(-width, (double)height - n10);
        GL11.glVertex2d(0.0, (double)height - n10);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glDisable(3042);
        GL11.glScaled(0.5, 0.5, 1.0);
        NetInfo.mc.fontRendererObj.drawStringWithShadow(i + " Avg", 2.0f, (float)(((double)height - n10) * 2.0) - 4.5f, -1);
        GL11.glScaled(2.0, 2.0, 1.0);
        GL11.glTranslated(-x - width, -y, 0.0);
        NetInfoRenderUtils.rectBordered(x, y, width, height, 0.5f, -1);
    }

    private static class DequeManager {
        private final Deque<Integer> arrayDeque = new ArrayDeque<Integer>();
        private long currentTimeMillis;
        private int currentItems;

        private DequeManager() {
        }

        public void addOneToList() {
            ++this.currentItems;
        }

        public void clearList() {
            this.arrayDeque.clear();
        }

        public void iterate(int n, long speed) {
            if (System.currentTimeMillis() - this.currentTimeMillis > speed) {
                this.arrayDeque.addFirst(this.currentItems);
                this.currentTimeMillis = System.currentTimeMillis();
                this.currentItems = 0;
                if (this.arrayDeque.size() > n) {
                    this.arrayDeque.removeLast();
                }
            }
        }

        public Collection<Integer> getCollection() {
            return this.arrayDeque;
        }
    }

    public static enum NetInfoRenderUtils {
        INSTANCE;


        public static void drawRectCustom(double n, double n2, double n3, double n4, int n5) {
            boolean enable = NetInfoRenderUtils.enable();
            GL11.glDisable(3553);
            RenderUtil.color(n5);
            GL11.glBegin(7);
            GL11.glVertex2d(n, n2);
            GL11.glVertex2d(n, n2 + n4);
            GL11.glVertex2d(n + n3, n2 + n4);
            GL11.glVertex2d(n + n3, n2);
            GL11.glEnd();
            NetInfoRenderUtils.disable(enable);
            GL11.glEnable(3553);
        }

        public static void rectBordered(double n, double n2, double n3, double n4, float n5, int n6) {
            boolean enable = NetInfoRenderUtils.enable();
            GL11.glDisable(3553);
            RenderUtil.color(n6);
            GL11.glLineWidth(n5);
            GL11.glBegin(2);
            GL11.glVertex2d(n, n2);
            GL11.glVertex2d(n, n2 + n4);
            GL11.glVertex2d(n + n3, n2 + n4);
            GL11.glVertex2d(n + n3, n2);
            GL11.glEnd();
            GL11.glEnable(3553);
            NetInfoRenderUtils.disable(enable);
        }

        public static void disable(boolean b) {
            if (!b) {
                GL11.glDisable(3042);
            }
        }

        public static boolean enable() {
            boolean glIsEnabled = GL11.glIsEnabled(3042);
            if (!glIsEnabled) {
                GL11.glEnable(3042);
                GL14.glBlendFuncSeparate(770, 771, 1, 0);
            }
            return glIsEnabled;
        }
    }
}

