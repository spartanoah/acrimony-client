/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetNormalWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsScreenWithCallback;
import com.mojang.realmsclient.gui.screens.RealmsSelectFileToUploadScreen;
import com.mojang.realmsclient.gui.screens.RealmsSelectWorldTemplateScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class RealmsResetWorldScreen
extends RealmsScreenWithCallback<WorldTemplate> {
    private static final Logger LOGGER = LogManager.getLogger();
    private RealmsScreen lastScreen;
    private RealmsServer serverData;
    private RealmsScreen returnScreen;
    private String title = RealmsResetWorldScreen.getLocalizedString("mco.reset.world.title");
    private String subtitle = RealmsResetWorldScreen.getLocalizedString("mco.reset.world.warning");
    private String buttonTitle = RealmsResetWorldScreen.getLocalizedString("gui.cancel");
    private int subtitleColor = 0xFF0000;
    private static final String SLOT_FRAME_LOCATION = "realms:textures/gui/realms/slot_frame.png";
    private static final String UPLOAD_LOCATION = "realms:textures/gui/realms/upload.png";
    private final int BUTTON_CANCEL_ID = 0;
    private boolean loaded = false;
    private List<WorldTemplate> templates = new ArrayList<WorldTemplate>();
    private List<WorldTemplate> adventuremaps = new ArrayList<WorldTemplate>();
    private final Random random = new Random();
    private ResetType selectedType = ResetType.NONE;
    private int templateId;
    private int adventureMapId;
    public int slot = -1;
    private ResetType typeToReset = ResetType.NONE;
    private ResetWorldInfo worldInfoToReset = null;
    private WorldTemplate worldTemplateToReset = null;
    private String resetTitle = null;

    public RealmsResetWorldScreen(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen) {
        this.lastScreen = lastScreen;
        this.serverData = serverData;
        this.returnScreen = returnScreen;
    }

    public RealmsResetWorldScreen(RealmsScreen lastScreen, RealmsServer serverData, RealmsScreen returnScreen, String title, String subtitle, int subtitleColor, String buttonTitle) {
        this(lastScreen, serverData, returnScreen);
        this.title = title;
        this.subtitle = subtitle;
        this.subtitleColor = subtitleColor;
        this.buttonTitle = buttonTitle;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setResetTitle(String title) {
        this.resetTitle = title;
    }

    @Override
    public void init() {
        this.buttonsClear();
        this.buttonsAdd(RealmsResetWorldScreen.newButton(0, this.width() / 2 - 40, RealmsConstants.row(14) - 10, 80, 20, this.buttonTitle));
        if (!this.loaded) {
            new Thread("Realms-reset-world-fetcher"){

                @Override
                public void run() {
                    RealmsClient client = RealmsClient.createRealmsClient();
                    try {
                        for (WorldTemplate wt : client.fetchWorldTemplates().templates) {
                            if (!wt.recommendedPlayers.equals("")) {
                                RealmsResetWorldScreen.this.adventuremaps.add(wt);
                                continue;
                            }
                            RealmsResetWorldScreen.this.templates.add(wt);
                        }
                        RealmsResetWorldScreen.this.templateId = RealmsResetWorldScreen.this.random.nextInt(RealmsResetWorldScreen.this.templates.size());
                        RealmsResetWorldScreen.this.adventureMapId = RealmsResetWorldScreen.this.random.nextInt(RealmsResetWorldScreen.this.adventuremaps.size());
                        RealmsResetWorldScreen.this.loaded = true;
                    } catch (RealmsServiceException e) {
                        LOGGER.error("Couldn't fetch templates in reset world");
                    }
                }
            }.start();
        }
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void keyPressed(char ch, int eventKey) {
        if (eventKey == 1) {
            Realms.setScreen(this.lastScreen);
        }
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (!button.active()) {
            return;
        }
        if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
        }
    }

    @Override
    public void mouseClicked(int x, int y, int buttonNum) {
        switch (this.selectedType) {
            case NONE: {
                break;
            }
            case GENERATE: {
                Realms.setScreen(new RealmsResetNormalWorldScreen(this, this.title));
                break;
            }
            case UPLOAD: {
                Realms.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this));
                break;
            }
            case ADVENTURE: {
                RealmsSelectWorldTemplateScreen screen = new RealmsSelectWorldTemplateScreen(this, null, false, this.adventuremaps);
                screen.setTitle(RealmsResetWorldScreen.getLocalizedString("mco.reset.world.adventure"));
                if (RealmsSharedConstants.VERSION_STRING.equals("1.8.9")) {
                    screen.setWarning(RealmsResetWorldScreen.getLocalizedString("mco.reset.world.updateBreaksAdventure"));
                    screen.setWarningURL("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
                }
                Realms.setScreen(screen);
                break;
            }
            case SURVIVAL_SPAWN: {
                RealmsSelectWorldTemplateScreen templateScreen = new RealmsSelectWorldTemplateScreen(this, null, false, this.templates);
                templateScreen.setTitle(RealmsResetWorldScreen.getLocalizedString("mco.reset.world.template"));
                Realms.setScreen(templateScreen);
                break;
            }
            default: {
                return;
            }
        }
    }

    private int frame(int i) {
        return this.width() / 2 - 80 + (i - 1) * 100;
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.selectedType = ResetType.NONE;
        this.renderBackground();
        this.drawCenteredString(this.title, this.width() / 2, 7, 0xFFFFFF);
        this.drawCenteredString(this.subtitle, this.width() / 2, 22, this.subtitleColor);
        if (this.loaded) {
            this.drawFrame(this.frame(1), RealmsConstants.row(0) + 10, xm, ym, RealmsResetWorldScreen.getLocalizedString("mco.reset.world.generate"), -1L, "textures/gui/title/background/panorama_3.png", ResetType.GENERATE);
            this.drawFrame(this.frame(2), RealmsConstants.row(0) + 10, xm, ym, RealmsResetWorldScreen.getLocalizedString("mco.reset.world.upload"), -1L, UPLOAD_LOCATION, ResetType.UPLOAD);
            this.drawFrame(this.frame(1), RealmsConstants.row(6) + 20, xm, ym, RealmsResetWorldScreen.getLocalizedString("mco.reset.world.adventure"), Long.valueOf(this.adventuremaps.get((int)this.adventureMapId).id), this.adventuremaps.get((int)this.adventureMapId).image, ResetType.ADVENTURE);
            this.drawFrame(this.frame(2), RealmsConstants.row(6) + 20, xm, ym, RealmsResetWorldScreen.getLocalizedString("mco.reset.world.template"), Long.valueOf(this.templates.get((int)this.templateId).id), this.templates.get((int)this.templateId).image, ResetType.SURVIVAL_SPAWN);
        }
        super.render(xm, ym, a);
    }

    private void drawFrame(int x, int y, int xm, int ym, String text, long imageId, String image, ResetType resetType) {
        boolean hovered = false;
        if (xm >= x && xm <= x + 60 && ym >= y - 12 && ym <= y + 60) {
            hovered = true;
            this.selectedType = resetType;
        }
        if (imageId != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
        } else {
            RealmsResetWorldScreen.bind(image);
        }
        if (hovered) {
            GL11.glColor4f(0.56f, 0.56f, 0.56f, 1.0f);
        } else {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        RealmsScreen.blit(x + 2, y + 2, 0.0f, 0.0f, 56, 56, 56.0f, 56.0f);
        RealmsResetWorldScreen.bind(SLOT_FRAME_LOCATION);
        if (hovered) {
            GL11.glColor4f(0.56f, 0.56f, 0.56f, 1.0f);
        } else {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        RealmsScreen.blit(x, y, 0.0f, 0.0f, 60, 60, 60.0f, 60.0f);
        this.drawCenteredString(text, x + 30, y - 12, hovered ? 0xA0A0A0 : 0xFFFFFF);
    }

    @Override
    void callback(WorldTemplate worldTemplate) {
        if (worldTemplate != null) {
            if (this.slot != -1) {
                this.typeToReset = worldTemplate.recommendedPlayers.equals("") ? ResetType.SURVIVAL_SPAWN : ResetType.ADVENTURE;
                this.worldTemplateToReset = worldTemplate;
                this.switchSlot();
            } else {
                this.resetWorldWithTemplate(worldTemplate);
            }
        }
    }

    private void switchSlot() {
        this.switchSlot(this);
    }

    public void switchSlot(RealmsScreen screen) {
        RealmsTasks.SwitchSlotTask switchSlotTask = new RealmsTasks.SwitchSlotTask(this.serverData.id, this.slot, screen, 100);
        RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, switchSlotTask);
        longRunningMcoTaskScreen.start();
        Realms.setScreen(longRunningMcoTaskScreen);
    }

    @Override
    public void confirmResult(boolean result, int id) {
        if (id == 100 && result) {
            switch (this.typeToReset) {
                case ADVENTURE: 
                case SURVIVAL_SPAWN: {
                    if (this.worldTemplateToReset == null) break;
                    this.resetWorldWithTemplate(this.worldTemplateToReset);
                    break;
                }
                case GENERATE: {
                    if (this.worldInfoToReset == null) break;
                    this.triggerResetWorld(this.worldInfoToReset);
                    break;
                }
                default: {
                    return;
                }
            }
            return;
        }
        if (result) {
            Realms.setScreen(this.returnScreen);
        }
    }

    public void resetWorldWithTemplate(WorldTemplate template) {
        RealmsTasks.ResettingWorldTask resettingWorldTask = new RealmsTasks.ResettingWorldTask(this.serverData.id, this.returnScreen, template);
        if (this.resetTitle != null) {
            resettingWorldTask.setResetTitle(this.resetTitle);
        }
        RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, resettingWorldTask);
        longRunningMcoTaskScreen.start();
        Realms.setScreen(longRunningMcoTaskScreen);
    }

    public void resetWorld(ResetWorldInfo resetWorldInfo) {
        if (this.slot != -1) {
            this.typeToReset = ResetType.GENERATE;
            this.worldInfoToReset = resetWorldInfo;
            this.switchSlot();
        } else {
            this.triggerResetWorld(resetWorldInfo);
        }
    }

    private void triggerResetWorld(ResetWorldInfo resetWorldInfo) {
        RealmsTasks.ResettingWorldTask resettingWorldTask = new RealmsTasks.ResettingWorldTask(this.serverData.id, this.returnScreen, resetWorldInfo.seed, resetWorldInfo.levelType, resetWorldInfo.generateStructures);
        if (this.resetTitle != null) {
            resettingWorldTask.setResetTitle(this.resetTitle);
        }
        RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, resettingWorldTask);
        longRunningMcoTaskScreen.start();
        Realms.setScreen(longRunningMcoTaskScreen);
    }

    public static class ResetWorldInfo {
        String seed;
        int levelType;
        boolean generateStructures;

        public ResetWorldInfo(String seed, int levelType, boolean generateStructures) {
            this.seed = seed;
            this.levelType = levelType;
            this.generateStructures = generateStructures;
        }
    }

    static enum ResetType {
        NONE,
        GENERATE,
        UPLOAD,
        ADVENTURE,
        SURVIVAL_SPAWN;

    }
}

