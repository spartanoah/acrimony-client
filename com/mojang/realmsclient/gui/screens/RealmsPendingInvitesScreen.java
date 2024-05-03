/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class RealmsPendingInvitesScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BUTTON_BACK_ID = 0;
    private static final String ACCEPT_ICON_LOCATION = "realms:textures/gui/realms/accept_icon.png";
    private static final String REJECT_ICON_LOCATION = "realms:textures/gui/realms/reject_icon.png";
    private final RealmsScreen lastScreen;
    private String toolTip = null;
    private boolean loaded = false;
    private PendingInvitationList pendingList;
    private List<PendingInvite> pendingInvites = Lists.newArrayList();

    public RealmsPendingInvitesScreen(RealmsScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void mouseEvent() {
        super.mouseEvent();
        this.pendingList.mouseEvent();
    }

    @Override
    public void init() {
        Keyboard.enableRepeatEvents(true);
        this.buttonsClear();
        this.pendingList = new PendingInvitationList();
        new Thread("Realms-pending-invitations-fetcher"){

            @Override
            public void run() {
                RealmsClient client = RealmsClient.createRealmsClient();
                try {
                    RealmsPendingInvitesScreen.this.pendingInvites = client.pendingInvites().pendingInvites;
                } catch (RealmsServiceException e) {
                    LOGGER.error("Couldn't list invites");
                } finally {
                    RealmsPendingInvitesScreen.this.loaded = true;
                }
            }
        }.start();
        this.buttonsAdd(RealmsPendingInvitesScreen.newButton(0, this.width() / 2 - 75, this.height() - 32, 153, 20, RealmsPendingInvitesScreen.getLocalizedString("gui.done")));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (!button.active()) {
            return;
        }
        switch (button.id()) {
            case 0: {
                Realms.setScreen(new RealmsMainScreen(this.lastScreen));
            }
        }
    }

    @Override
    public void keyPressed(char eventCharacter, int eventKey) {
        if (eventKey == 1) {
            Realms.setScreen(new RealmsMainScreen(this.lastScreen));
        }
    }

    private void updateList(int slot) {
        this.pendingInvites.remove(slot);
    }

    private void reject(final int slot) {
        if (slot < this.pendingInvites.size()) {
            new Thread("Realms-reject-invitation"){

                @Override
                public void run() {
                    try {
                        RealmsClient client = RealmsClient.createRealmsClient();
                        client.rejectInvitation(((PendingInvite)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvites.get((int)slot)).invitationId);
                        RealmsPendingInvitesScreen.this.updateList(slot);
                    } catch (RealmsServiceException e) {
                        LOGGER.error("Couldn't reject invite");
                    }
                }
            }.start();
        }
    }

    private void accept(final int slot) {
        if (slot < this.pendingInvites.size()) {
            new Thread("Realms-accept-invitation"){

                @Override
                public void run() {
                    try {
                        RealmsClient client = RealmsClient.createRealmsClient();
                        client.acceptInvitation(((PendingInvite)((RealmsPendingInvitesScreen)RealmsPendingInvitesScreen.this).pendingInvites.get((int)slot)).invitationId);
                        RealmsPendingInvitesScreen.this.updateList(slot);
                    } catch (RealmsServiceException e) {
                        LOGGER.error("Couldn't accept invite");
                    }
                }
            }.start();
        }
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.toolTip = null;
        this.renderBackground();
        this.pendingList.render(xm, ym, a);
        this.drawCenteredString(RealmsPendingInvitesScreen.getLocalizedString("mco.invites.title"), this.width() / 2, 12, 0xFFFFFF);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, xm, ym);
        }
        if (this.pendingInvites.size() == 0 && this.loaded) {
            this.drawCenteredString(RealmsPendingInvitesScreen.getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 0xFFFFFF);
        }
        super.render(xm, ym, a);
    }

    protected void renderMousehoverTooltip(String msg, int x, int y) {
        if (msg == null) {
            return;
        }
        int rx = x + 12;
        int ry = y - 12;
        int width = this.fontWidth(msg);
        this.fillGradient(rx - 3, ry - 3, rx + width + 3, ry + 8 + 3, -1073741824, -1073741824);
        this.fontDrawShadow(msg, rx, ry, 0xFFFFFF);
    }

    private class PendingInvitationList
    extends RealmsClickableScrolledSelectionList {
        public PendingInvitationList() {
            super(RealmsPendingInvitesScreen.this.width() + 50, RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 40, 36);
        }

        @Override
        public int getItemCount() {
            return RealmsPendingInvitesScreen.this.pendingInvites.size();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public void renderBackground() {
            RealmsPendingInvitesScreen.this.renderBackground();
        }

        @Override
        public void renderSelected(int width, int y, int h, Tezzelator t) {
            int x0 = this.getScrollbarPosition() - 290;
            int x1 = this.getScrollbarPosition() - 10;
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(3553);
            t.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
            t.vertex(x0, y + h + 2, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x1, y + h + 2, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x1, y - 2, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x0, y - 2, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            t.vertex(x0 + 1, y + h + 1, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x1 - 1, y + h + 1, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x1 - 1, y - 1, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            t.vertex(x0 + 1, y - 1, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            t.end();
            GL11.glEnable(3553);
        }

        @Override
        public void renderItem(int i, int x, int y, int h, int mouseX, int mouseY) {
            if (i < RealmsPendingInvitesScreen.this.pendingInvites.size()) {
                this.renderPendingInvitationItem(i, x, y, h);
            }
        }

        private void renderPendingInvitationItem(int i, int x, int y, int h) {
            PendingInvite invite = (PendingInvite)RealmsPendingInvitesScreen.this.pendingInvites.get(i);
            RealmsPendingInvitesScreen.this.drawString(invite.worldName, x + 2, y + 1, 0xFFFFFF);
            RealmsPendingInvitesScreen.this.drawString(invite.worldOwnerName, x + 2, y + 12, 0x6C6C6C);
            RealmsPendingInvitesScreen.this.drawString(RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - invite.date.getTime()), x + 2, y + 24, 0x6C6C6C);
            int dx = this.getScrollbarPosition() - 50;
            this.drawAccept(dx, y, this.xm(), this.ym());
            this.drawReject(dx + 20, y, this.xm(), this.ym());
            RealmsScreen.bindFace(invite.worldOwnerUuid, invite.worldOwnerName);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            RealmsScreen.blit(x - 36, y, 8.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
            RealmsScreen.blit(x - 36, y, 40.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
        }

        private void drawAccept(int x, int y, int xm, int ym) {
            boolean hovered = false;
            if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < RealmsPendingInvitesScreen.this.height() - 40 && ym > 32) {
                hovered = true;
            }
            RealmsScreen.bind(RealmsPendingInvitesScreen.ACCEPT_ICON_LOCATION);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPushMatrix();
            RealmsScreen.blit(x, y, hovered ? 19.0f : 0.0f, 0.0f, 18, 18, 37.0f, 18.0f);
            GL11.glPopMatrix();
            if (hovered) {
                RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.accept");
            }
        }

        private void drawReject(int x, int y, int xm, int ym) {
            boolean hovered = false;
            if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15 && ym < RealmsPendingInvitesScreen.this.height() - 40 && ym > 32) {
                hovered = true;
            }
            RealmsScreen.bind(RealmsPendingInvitesScreen.REJECT_ICON_LOCATION);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPushMatrix();
            RealmsScreen.blit(x, y, hovered ? 19.0f : 0.0f, 0.0f, 18, 18, 37.0f, 18.0f);
            GL11.glPopMatrix();
            if (hovered) {
                RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.reject");
            }
        }

        @Override
        public void itemClicked(int clickSlotPos, int slot, int xm, int ym, int width) {
            int x = this.getScrollbarPosition() - 50;
            int y = clickSlotPos + 30 - this.getScroll();
            if (xm >= x && xm <= x + 15 && ym >= y && ym <= y + 15) {
                RealmsPendingInvitesScreen.this.accept(slot);
            } else if (xm >= x + 20 && xm <= x + 20 + 15 && ym >= y && ym <= y + 15) {
                RealmsPendingInvitesScreen.this.reject(slot);
            }
        }

        @Override
        public void customMouseEvent(int y0, int y1, int headerHeight, float yo, int itemHeight) {
            if (Mouse.isButtonDown(0) && this.ym() >= y0 && this.ym() <= y1) {
                int x0 = this.width() / 2 - 92;
                int x1 = this.width();
                int clickSlotPos = this.ym() - y0 - headerHeight + (int)yo - 4;
                int slot = clickSlotPos / itemHeight;
                if (this.xm() >= x0 && this.xm() <= x1 && slot >= 0 && clickSlotPos >= 0 && slot < this.getItemCount()) {
                    this.itemClicked(clickSlotPos, slot, this.xm(), this.ym(), this.width());
                }
            }
        }
    }
}

