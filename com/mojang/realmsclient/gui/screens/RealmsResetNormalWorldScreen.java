/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class RealmsResetNormalWorldScreen
extends RealmsScreen {
    private RealmsResetWorldScreen lastScreen;
    private RealmsEditBox seedEdit;
    private Boolean generateStructures = true;
    private Integer levelTypeIndex = 0;
    String[] levelTypes;
    private final int BUTTON_CANCEL_ID = 0;
    private final int BUTTON_RESET_ID = 1;
    private static final int BUTTON_LEVEL_TYPE_ID = 2;
    private static final int BUTTON_GENERATE_STRUCTURES_ID = 3;
    private final int SEED_EDIT_BOX = 4;
    private RealmsButton resetButton;
    private RealmsButton levelTypeButton;
    private RealmsButton generateStructuresButton;
    private String buttonTitle = RealmsResetNormalWorldScreen.getLocalizedString("mco.backup.button.reset");

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen lastScreen, String buttonTitle) {
        this(lastScreen);
        this.buttonTitle = buttonTitle;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.levelTypes = new String[]{RealmsResetNormalWorldScreen.getLocalizedString("generator.default"), RealmsResetNormalWorldScreen.getLocalizedString("generator.flat"), RealmsResetNormalWorldScreen.getLocalizedString("generator.largeBiomes"), RealmsResetNormalWorldScreen.getLocalizedString("generator.amplified")};
        Keyboard.enableRepeatEvents(true);
        this.buttonsClear();
        this.buttonsAdd(RealmsResetNormalWorldScreen.newButton(0, this.width() / 2 + 8, RealmsConstants.row(12), 97, 20, RealmsResetNormalWorldScreen.getLocalizedString("gui.back")));
        this.resetButton = RealmsResetNormalWorldScreen.newButton(1, this.width() / 2 - 102, RealmsConstants.row(12), 97, 20, this.buttonTitle);
        this.buttonsAdd(this.resetButton);
        this.seedEdit = this.newEditBox(4, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20);
        this.seedEdit.setFocus(true);
        this.seedEdit.setMaxLength(32);
        this.seedEdit.setValue("");
        this.levelTypeButton = RealmsResetNormalWorldScreen.newButton(2, this.width() / 2 - 102, RealmsConstants.row(4), 205, 20, this.levelTypeTitle());
        this.buttonsAdd(this.levelTypeButton);
        this.generateStructuresButton = RealmsResetNormalWorldScreen.newButton(3, this.width() / 2 - 102, RealmsConstants.row(6) - 2, 205, 20, this.generateStructuresTitle());
        this.buttonsAdd(this.generateStructuresButton);
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void keyPressed(char ch, int eventKey) {
        this.seedEdit.keyPressed(ch, eventKey);
        if (eventKey == 28 || eventKey == 156) {
            this.buttonClicked(this.resetButton);
        }
        if (eventKey == 1) {
            Realms.setScreen(this.lastScreen);
        }
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (!button.active()) {
            return;
        }
        switch (button.id()) {
            case 0: {
                Realms.setScreen(this.lastScreen);
                break;
            }
            case 1: {
                this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures));
                break;
            }
            case 2: {
                this.levelTypeIndex = (this.levelTypeIndex + 1) % this.levelTypes.length;
                button.msg(this.levelTypeTitle());
                break;
            }
            case 3: {
                this.generateStructures = this.generateStructures == false;
                button.msg(this.generateStructuresTitle());
                break;
            }
            default: {
                return;
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int buttonNum) {
        super.mouseClicked(x, y, buttonNum);
        this.seedEdit.mouseClicked(x, y, buttonNum);
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.renderBackground();
        this.drawCenteredString(RealmsResetNormalWorldScreen.getLocalizedString("mco.reset.world.generate"), this.width() / 2, 17, 0xFFFFFF);
        this.drawString(RealmsResetNormalWorldScreen.getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, RealmsConstants.row(1), 0xA0A0A0);
        this.seedEdit.render();
        super.render(xm, ym, a);
    }

    private String levelTypeTitle() {
        String levelType = RealmsResetNormalWorldScreen.getLocalizedString("selectWorld.mapType");
        return levelType + " " + this.levelTypes[this.levelTypeIndex];
    }

    private String generateStructuresTitle() {
        return RealmsResetNormalWorldScreen.getLocalizedString("selectWorld.mapFeatures") + " " + (this.generateStructures != false ? RealmsResetNormalWorldScreen.getLocalizedString("mco.configure.world.on") : RealmsResetNormalWorldScreen.getLocalizedString("mco.configure.world.off"));
    }
}

