/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class RealmsTermsScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BUTTON_AGREE_ID = 1;
    private static final int BUTTON_DISAGREE_ID = 2;
    private final RealmsScreen lastScreen;
    private final RealmsServer realmsServer;
    private RealmsButton agreeButton;
    private boolean onLink = false;
    private String realmsToSUrl = "https://minecraft.net/realms/terms";

    public RealmsTermsScreen(RealmsScreen lastScreen, RealmsServer realmsServer) {
        this.lastScreen = lastScreen;
        this.realmsServer = realmsServer;
    }

    @Override
    public void init() {
        Keyboard.enableRepeatEvents(true);
        this.buttonsClear();
        int column1_x = this.width() / 4;
        int column_width = this.width() / 4 - 2;
        int column2_x = this.width() / 2 + 4;
        this.agreeButton = RealmsTermsScreen.newButton(1, column1_x, RealmsConstants.row(12), column_width, 20, RealmsTermsScreen.getLocalizedString("mco.terms.buttons.agree"));
        this.buttonsAdd(this.agreeButton);
        this.buttonsAdd(RealmsTermsScreen.newButton(2, column2_x, RealmsConstants.row(12), column_width, 20, RealmsTermsScreen.getLocalizedString("mco.terms.buttons.disagree")));
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (!button.active()) {
            return;
        }
        switch (button.id()) {
            case 2: {
                Realms.setScreen(this.lastScreen);
                break;
            }
            case 1: {
                this.agreedToTos();
                break;
            }
            default: {
                return;
            }
        }
    }

    @Override
    public void keyPressed(char eventCharacter, int eventKey) {
        if (eventKey == 1) {
            Realms.setScreen(this.lastScreen);
        }
    }

    private void agreedToTos() {
        RealmsClient client = RealmsClient.createRealmsClient();
        try {
            client.agreeToTos();
            RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, new RealmsTasks.RealmsConnectTask(this.lastScreen, this.realmsServer));
            longRunningMcoTaskScreen.start();
            Realms.setScreen(longRunningMcoTaskScreen);
        } catch (RealmsServiceException e) {
            LOGGER.error("Couldn't agree to TOS");
        }
    }

    @Override
    public void mouseClicked(int x, int y, int buttonNum) {
        super.mouseClicked(x, y, buttonNum);
        if (this.onLink) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(this.realmsToSUrl), null);
            RealmsUtil.browseTo(this.realmsToSUrl);
        }
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.renderBackground();
        this.drawCenteredString(RealmsTermsScreen.getLocalizedString("mco.terms.title"), this.width() / 2, 17, 0xFFFFFF);
        this.drawString(RealmsTermsScreen.getLocalizedString("mco.terms.sentence.1"), this.width() / 2 - 120, RealmsConstants.row(5), 0xFFFFFF);
        int firstPartWidth = this.fontWidth(RealmsTermsScreen.getLocalizedString("mco.terms.sentence.1"));
        int x1 = this.width() / 2 - 121 + firstPartWidth;
        int y1 = RealmsConstants.row(5);
        int x2 = x1 + this.fontWidth("mco.terms.sentence.2") + 1;
        int y2 = y1 + 1 + this.fontLineHeight();
        if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
            this.onLink = true;
            this.drawString(" " + RealmsTermsScreen.getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, RealmsConstants.row(5), 7107012);
        } else {
            this.onLink = false;
            this.drawString(" " + RealmsTermsScreen.getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + firstPartWidth, RealmsConstants.row(5), 0x3366BB);
        }
        super.render(xm, ym, a);
    }
}

