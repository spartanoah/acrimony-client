/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.menu;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.ui.menu.AltLoginScreen;
import Acrimony.ui.menu.Changelog;
import Acrimony.ui.menu.ChangelogType;
import Acrimony.ui.menu.components.Button;
import Acrimony.util.glsl.GLSLSandboxShader;
import Acrimony.util.render.DrawUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class AcrimonyMenu
extends GuiScreen {
    private long initTime = System.currentTimeMillis();
    private GLSLSandboxShader backgroundShader;
    private final ArrayList<Changelog> changelogs = new ArrayList();
    private final Button[] buttons = new Button[]{new Button("Singleplayer"), new Button("Multiplayer"), new Button("AltManager"), new Button("Settings"), new Button("Quit")};

    @Override
    public void initGui() {
        super.initGui();
        try {
            this.backgroundShader = new GLSLSandboxShader("/assets/minecraft/acrimony/shader/shader.fsh");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.initTime = System.currentTimeMillis();
        this.buttonList.clear();
        this.changelogs.clear();
        int initHeight = this.height / 4 + 48;
        int initWidth = this.width / 2 - 51;
        this.changelogs.add(new Changelog(new String[]{"Added changelog", "Added a new main menu design", "Added damageparticles", "Added projectiles", "Added blur", "Added custom compresion & radius blur", "Added shader on main menu", "Added jesus", "Added fast eat", "Added killaura strafe mode (silent, none, strict)", "Added new full watchdog sprint scaffold", "Added session info (draggable)", "Added ncp/watchdog step"}, ChangelogType.ADD));
        this.changelogs.add(new Changelog(new String[]{"Fixed shaders crashing", "Fixed Scaffold pitch", "Fixed killaura rotations", "Fixed rainbow color"}, ChangelogType.FIXED));
        this.changelogs.add(new Changelog(new String[]{"Removed chat font in better visuals", "Removed spotify", "Removed remove chat background", "Removed vulcan glide fly", "Removed discord rpc"}, ChangelogType.REMOVE));
        this.buttonList.add(new GuiButton(0, initWidth - 40, initHeight + 20, "Singleplayer"));
        this.buttonList.add(new GuiButton(1, initWidth - 40, initHeight + 42, "Multiplayer"));
        this.buttonList.add(new GuiButton(2, initWidth - 40, initHeight + 64, "AltManager"));
        this.buttonList.add(new GuiButton(3, initWidth - 40, initHeight + 90, 98, 20, "Options.."));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 11, initHeight + 90, 98, 20, "Exit Game"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 0: {
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            }
            case 1: {
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            }
            case 2: {
                this.mc.displayGuiScreen(new AltLoginScreen());
                break;
            }
            case 3: {
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            }
            case 4: {
                this.mc.shutdown();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        AcrimonyFont reguler = Acrimony.instance.getFontManager().getSfpro();
        AcrimonyFont bigFont = Acrimony.instance.getFontManager().getSfpro23();
        AcrimonyFont titleFont = Acrimony.instance.getFontManager().getSfproTitle();
        GlStateManager.disableCull();
        this.backgroundShader.useShader(this.width, this.height, mouseX, mouseY, (float)(System.currentTimeMillis() - this.initTime) / 1000.0f);
        GL11.glBegin(7);
        GL11.glVertex2f(-1.0f, -1.0f);
        GL11.glVertex2f(-1.0f, 1.0f);
        GL11.glVertex2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -1.0f);
        GL11.glEnd();
        GL20.glUseProgram(0);
        GlStateManager.disableAlpha();
        ScaledResolution sr = new ScaledResolution(this.mc);
        for (GuiButton g : this.buttonList) {
            g.drawButton(this.mc, mouseX, mouseY);
        }
        titleFont.drawString("Changelog [#" + Acrimony.instance.version + "]", 5.0, 5.0, -1);
        int changeY = 0;
        for (Changelog c : this.changelogs) {
            int set = 0;
            String text = "";
            switch (c.getType()) {
                case ADD: {
                    text = (Object)((Object)ChatFormatting.DARK_GRAY) + "[" + (Object)((Object)ChatFormatting.GREEN) + "+" + (Object)((Object)ChatFormatting.DARK_GRAY) + "] " + (Object)((Object)ChatFormatting.RESET);
                    break;
                }
                case FIXED: {
                    text = (Object)((Object)ChatFormatting.DARK_GRAY) + "[" + (Object)((Object)ChatFormatting.YELLOW) + "*" + (Object)((Object)ChatFormatting.DARK_GRAY) + "] " + (Object)((Object)ChatFormatting.RESET);
                    break;
                }
                case REMOVE: {
                    text = (Object)((Object)ChatFormatting.DARK_GRAY) + "[" + (Object)((Object)ChatFormatting.RED) + "-" + (Object)((Object)ChatFormatting.DARK_GRAY) + "] " + (Object)((Object)ChatFormatting.RESET);
                }
            }
            for (String description : c.getDescription()) {
                int before = 14 + changeY;
                reguler.drawString(text + description, 10.0, (double)(before + (set += 12)), new Color(190, 190, 190).getRGB());
            }
            changeY += c.getDescription().length * 12;
        }
        reguler.drawStringWithShadow("Developed with " + (Object)((Object)ChatFormatting.RED) + "<3" + (Object)((Object)ChatFormatting.RESET) + " by kitxk1 & sophia_yu", (double)(this.width - reguler.getStringWidth("Developed with " + (Object)((Object)ChatFormatting.RED) + "<3" + (Object)((Object)ChatFormatting.RESET) + " by kitxk1 & sophia_yu") - 13), (double)(this.height - 15), -1);
        reguler.drawStringWithShadow(Acrimony.instance.name + " Client [#" + Acrimony.instance.version + "]", (double)((float)(this.width - reguler.getStringWidth(Acrimony.instance.name + " Client [#" + Acrimony.instance.version + "]")) / 110.0f), (double)(this.height - 15), -1);
        DrawUtil.drawImage(new ResourceLocation("minecraft", "acrimony/Logo.png"), sr.getScaledWidth() / 2 - 70, this.height / 4 - 55, 160, 160);
    }
}

