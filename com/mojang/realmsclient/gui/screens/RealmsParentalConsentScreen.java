/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.opengl.GL11;

public class RealmsParentalConsentScreen
extends RealmsScreen {
    private final RealmsScreen nextScreen;
    private static final int BUTTON_BACK_ID = 0;
    private static final int BUTTON_OK_ID = 1;
    private final String line1 = "If you have an older Minecraft account (you log in with your username),";
    private final String line2 = "you need to migrate the account to a Mojang account in order to access Realms.";
    private final String line3 = "As you probably know, Mojang is a part of Microsoft. Microsoft implements";
    private final String line4 = "certain procedures to help protect children and their privacy,";
    private final String line5 = "including complying with the Children\u2019s Online Privacy Protection Act (COPPA)";
    private final String line6 = "You may need to obtain parental consent before accessing your Realms account.";
    private boolean onLink = false;

    public RealmsParentalConsentScreen(RealmsScreen nextScreen) {
        this.nextScreen = nextScreen;
    }

    @Override
    public void init() {
        this.buttonsClear();
        this.buttonsAdd(RealmsParentalConsentScreen.newButton(1, this.width() / 2 - 100, RealmsConstants.row(11), 200, 20, "Go to accounts page"));
        this.buttonsAdd(RealmsParentalConsentScreen.newButton(0, this.width() / 2 - 100, RealmsConstants.row(13), 200, 20, "Back"));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        switch (button.id()) {
            case 1: {
                RealmsUtil.browseTo("https://accounts.mojang.com/me/verify/" + Realms.getUUID());
                break;
            }
            case 0: {
                Realms.setScreen(this.nextScreen);
                break;
            }
            default: {
                return;
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int buttonNum) {
        if (this.onLink) {
            RealmsUtil.browseTo("http://www.ftc.gov/enforcement/rules/rulemaking-regulatory-reform-proceedings/childrens-online-privacy-protection-rule");
        }
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.renderBackground();
        this.drawCenteredString("If you have an older Minecraft account (you log in with your username),", this.width() / 2, 30, 0xFFFFFF);
        this.drawCenteredString("you need to migrate the account to a Mojang account in order to access Realms.", this.width() / 2, 45, 0xFFFFFF);
        this.drawCenteredString("As you probably know, Mojang is a part of Microsoft. Microsoft implements", this.width() / 2, 85, 0xFFFFFF);
        this.drawCenteredString("certain procedures to help protect children and their privacy,", this.width() / 2, 100, 0xFFFFFF);
        this.drawCenteredString("including complying with the Children\u2019s Online Privacy Protection Act (COPPA)", this.width() / 2, 115, 0xFFFFFF);
        this.drawCenteredString("You may need to obtain parental consent before accessing your Realms account.", this.width() / 2, 130, 0xFFFFFF);
        this.renderLink(xm, ym);
        super.render(xm, ym, a);
    }

    private void renderLink(int xm, int ym) {
        String text = RealmsParentalConsentScreen.getLocalizedString("Read more about COPPA");
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        int textWidth = this.fontWidth(text);
        int leftPadding = this.width() / 2 - textWidth / 2;
        int topPadding = 145;
        int x1 = leftPadding;
        int x2 = x1 + textWidth + 1;
        int y1 = topPadding;
        int y2 = y1 + this.fontLineHeight();
        GL11.glTranslatef(x1, y1, 0.0f);
        if (x1 <= xm && xm <= x2 && y1 <= ym && ym <= y2) {
            this.onLink = true;
            this.drawString(text, 0, 0, 7107012);
        } else {
            this.onLink = false;
            this.drawString(text, 0, 0, 0x3366BB);
        }
        GL11.glPopMatrix();
    }
}

