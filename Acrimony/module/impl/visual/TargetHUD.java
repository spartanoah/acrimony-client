/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.HUDModule;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.animation.Animation;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.animation.AnimationUtil;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.render.DrawUtil;
import Acrimony.util.render.FontUtil;
import Acrimony.util.render.RenderUtil;
import Acrimony.util.render.StencilUtil;
import java.awt.Color;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class TargetHUD
extends HUDModule {
    private final ModeSetting mode = new ModeSetting("Mode", "Acrimony", "Acrimony", "Outline", "Blur", "Zeroday", "Novoline", "Lime Recode");
    private final EnumModeSetting<AnimationType> animationType = AnimationUtil.getAnimationType(AnimationType.BOUNCE);
    private final IntegerSetting animationDuration = AnimationUtil.getAnimationDuration(400);
    private final IntegerSetting healthBarDelay = new IntegerSetting("Healh bar delay", 100, 0, 1000, 25);
    private final BooleanSetting blurbg = new BooleanSetting("Blur Background", () -> this.mode.is("Acrimony"), false);
    private final BooleanSetting novoblur = new BooleanSetting("Drop shadow", () -> this.mode.is("Novoline"), true);
    private final BooleanSetting roundedHealth = new BooleanSetting("Rounded health", true);
    private final ModeSetting font = FontUtil.getFontSetting();
    private Animation animation;
    private Killaura killauraModule;
    private ClientTheme theme;
    private AcrimonyFont icons;
    private EntityPlayer target;
    private final TimerUtil barTimer = new TimerUtil();
    private final TimerUtil timer = new TimerUtil();
    private float renderedHealth;
    private boolean hadTarget;
    private boolean out;

    public TargetHUD() {
        super("TargetInfo", Category.VISUAL, 0.0, 0.0, 140, 50, AlignType.LEFT);
        this.addSettings(this.mode, this.blurbg, this.novoblur, this.font, this.animationType, this.animationDuration, this.healthBarDelay, this.roundedHealth);
        ScaledResolution sr = new ScaledResolution(mc);
        this.posX.setValue(sr.getScaledWidth() / 2 - this.width / 2);
        this.posY.setValue(sr.getScaledHeight() / 2 + 20);
        this.animation = new Animation();
        this.animation.setAnimDuration(this.animationDuration.getValue());
        this.animation.setAnimType(this.animationType.getMode());
        this.timer.reset();
    }

    @Override
    public void onClientStarted() {
        this.killauraModule = Acrimony.instance.getModuleManager().getModule(Killaura.class);
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
        this.icons = Acrimony.instance.getFontManager().getIcon1regular();
    }

    @Override
    protected void renderModule(boolean inChat) {
        boolean canRender = this.killauraModule.isEnabled() && this.killauraModule.getTarget() != null && this.killauraModule.getTarget() instanceof EntityPlayer || inChat;
        boolean bl = this.out = !this.timer.hasTimeElapsed(1000L);
        if (inChat && this.isEnabled()) {
            this.timer.reset();
            this.animation.getTimer().setTimeElapsed(this.animationDuration.getValue());
            this.renderTargetHUD(TargetHUD.mc.thePlayer, true);
            this.target = null;
        } else if (this.isEnabled()) {
            if (this.killauraModule.isEnabled() && this.killauraModule.getTarget() != null && this.killauraModule.getTarget() instanceof EntityPlayer) {
                this.target = (EntityPlayer)this.killauraModule.getTarget();
                this.timer.reset();
            }
            this.renderTargetHUD(this.target, canRender);
        } else {
            this.animation.getTimer().setTimeElapsed(0L);
        }
    }

    private void renderTargetHUD(EntityPlayer entity, boolean canRender) {
        int x = (int)this.posX.getValue();
        int y = (int)this.posY.getValue();
        AcrimonyFont sfpro = Acrimony.instance.getFontManager().getSfpro();
        AcrimonyFont sfsmall = Acrimony.instance.getFontManager().getSfprosmall();
        AcrimonyFont sfsverymall = Acrimony.instance.getFontManager().getSfproverysmall();
        AcrimonyFont sfbold = Acrimony.instance.getFontManager().getSfprobold();
        AcrimonyFont geistBold = Acrimony.instance.getFontManager().getGeistbold();
        this.animation.updateState(canRender);
        this.animation.setAnimDuration(this.out ? (long)this.animationDuration.getValue() : 1000L);
        this.animation.setAnimType(this.animationType.getMode());
        if (entity == null) {
            return;
        }
        float size = 38.0f;
        if (this.animation.isRendered() || !this.animation.isAnimDone()) {
            float health;
            float f = health = this.roundedHealth.isEnabled() ? (float)Math.round(entity.getHealth() * 10.0f) / 10.0f : entity.getHealth();
            if (!this.hadTarget) {
                this.renderedHealth = health;
            }
            this.animation.render(() -> {
                double healthMult = this.renderedHealth / entity.getMaxHealth();
                if (health != this.renderedHealth) {
                    this.renderedHealth = (float)((double)this.renderedHealth + (double)(health - this.renderedHealth) * Math.min(1.0, (double)this.barTimer.getTimeElapsed() / (double)this.healthBarDelay.getValue()));
                } else {
                    this.barTimer.reset();
                }
                double endAnimX = (double)(x + 54) + 84.0 * healthMult;
                ScaledResolution sr = new ScaledResolution(mc);
                switch (this.mode.getMode()) {
                    case "Acrimony": {
                        DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height - 8, 2.0, Integer.MIN_VALUE);
                        if (this.blurbg.isEnabled()) {
                            Acrimony.instance.blurHandler.blur((double)x, (double)y, (double)this.width, (double)(this.height - 7), 0.0f);
                            DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height - 8, 2.0, 0x20000000);
                        }
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 4, y + 4, x + 38, y + 38, 2.0, -1);
                        StencilUtil.readStencilBuffer(1);
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 4, y + 4, 34, 34);
                        StencilUtil.uninitStencilBuffer();
                        StencilUtil.readStencilBuffer(1);
                        for (double i = (double)(x + 40); i < endAnimX; i += 1.0) {
                            Gui.drawRect(i, y + 35, i + 1.0, y + 38, this.theme.getColor((int)(200.0 + i * 20.0)));
                        }
                        EntityPlayerSP player = TargetHUD.mc.thePlayer;
                        double playerX = player.posX;
                        double playerY = player.posY;
                        double playerZ = player.posZ;
                        double entityX = entity.posX;
                        double entityY = entity.posY;
                        double entityZ = entity.posZ;
                        double distance = Math.sqrt(Math.pow(playerX - entityX, 2.0) + Math.pow(playerY - entityY, 2.0) + Math.pow(playerZ - entityZ, 2.0));
                        sfbold.drawStringWithShadow(entity.getGameProfile().getName(), x + 42, y + 6, -1);
                        sfsmall.drawStringWithShadow("Target HP : " + health, x + 42, y + 17, -1);
                        sfsmall.drawStringWithShadow("Distance : " + String.format("%.2f", distance) + " m", x + 42, y + 26, -1);
                        break;
                    }
                    case "Outline": {
                        DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height, 1.0, Integer.MIN_VALUE);
                        DrawUtil.drawGradientSideways(x, y + this.height, x + this.width, y + this.height + 2, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        DrawUtil.drawGradientSideways(x, y, x + this.width, y + 2, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        Gui.drawRect(x - 2, y, x, y + this.height + 2, ClientTheme.color1.getRGB());
                        Gui.drawRect(x + this.width, y, x + this.width + 2, y + this.height + 2, ClientTheme.color2.getRGB());
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 8, y + 8, x + 44, y + 44, 5.0, -1);
                        StencilUtil.readStencilBuffer(1);
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 8, y + 8, 36, 36);
                        StencilUtil.uninitStencilBuffer();
                        DrawUtil.drawRoundedRect(x + 52, y + 39, x + 54 + 79, y + 45, 6.0, 0x40000000);
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 52, y + 35, (double)(x + 54) + 79.0 * healthMult, y + 44, 4.0, this.theme.getColor(0));
                        StencilUtil.readStencilBuffer(1);
                        DrawUtil.drawGradientSideways(x + 52, y + 35, (double)(x + 54) + 79.0 * healthMult, y + 44, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        StencilUtil.uninitStencilBuffer();
                        FontUtil.drawStringWithShadow(this.font.getMode(), entity.getGameProfile().getName(), x + 55, y + 10, -1);
                        FontUtil.drawStringWithShadow(this.font.getMode(), health + " Health", x + 55, y + 22, -1);
                        break;
                    }
                    case "Novoline": {
                        if (this.novoblur.isEnabled()) {
                            Acrimony.instance.blurHandler.bloom(x - 1, y - 1, this.width + 2, this.height - 8, 8, new Color(0, 0, 0, 150));
                        }
                        DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height - 10, 1.0, new Color(49, 49, 49).getRGB());
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 4, y + 4, 32, 32);
                        if (this.novoblur.isEnabled()) {
                            Acrimony.instance.blurHandler.bloom(x + 40, y + 22, (int)(14.0 + 82.0 * healthMult), 12, 8, ClientTheme.color1.getRGB());
                        }
                        DrawUtil.drawGradientSideways(x + 40, y + 22, (double)(x + 54) + 82.0 * healthMult, y + 34, ClientTheme.color1.getRGB(), ClientTheme.color1.getRGB());
                        TargetHUD.mc.fontRendererObj.drawStringWithShadow(entity.getGameProfile().getName(), x + 40, y + 8, -1);
                        TargetHUD.mc.fontRendererObj.drawString(health + "%", x + 76, y + 24, -1);
                        break;
                    }
                    case "Blur": {
                        Acrimony.instance.blurHandler.bloom(x, y, this.width, this.height, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 8, y + 8, x + 44, y + 44, 4.0, -1);
                        StencilUtil.readStencilBuffer(1);
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 8, y + 8, 36, 36);
                        StencilUtil.uninitStencilBuffer();
                        Acrimony.instance.blurHandler.bloom(x + 52, y + 36, (int)(79.0 * healthMult), 8, 2, this.theme.getColor(1));
                        FontUtil.drawStringWithShadow(this.font.getMode(), entity.getGameProfile().getName(), x + 55, y + 9, -1);
                        FontUtil.drawStringWithShadow(this.font.getMode(), health + " HP", x + 55, y + 20, -1);
                        break;
                    }
                    case "Zeroday": {
                        Acrimony.instance.blurHandler.bloom(x, y, this.width + 20, this.height + 10, 10, new Color(0, 0, 0, 100));
                        DrawUtil.drawRoundedRect(x, y, x + this.width + 20, y + this.height + 10, 2.0, Integer.MIN_VALUE);
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        sfsmall.drawStringWithShadow("" + Math.round((double)health - 0.5), (float)x + 44.5f, y + 26, new Color(255, 255, 255).getRGB());
                        sfsverymall.drawStringWithShadow(Math.round(entity.posX - 0.5) + "", (double)((float)x + 85.5f), (double)y + 28.4, new Color(255, 255, 255).getRGB());
                        sfsmall.drawStringWithShadow("" + Math.round((double)TargetHUD.mc.thePlayer.getDistanceToEntity(entity) - 0.5), (float)x + 67.2f, y + 26, new Color(255, 255, 255).getRGB());
                        RenderUtil.drawLoadingCircleNormal(x + 50, y + 29, new Color(246, 174, 90, 200));
                        RenderUtil.drawLoadingCircleFast(x + 50, y + 29, new Color(183, 211, 82, 200));
                        RenderUtil.drawLoadingCircleSlow(x + 70, y + 29, new Color(227, 103, 103, 200));
                        RenderUtil.drawLoadingCircleFast(x + 70, y + 29, new Color(182, 182, 84, 200));
                        RenderUtil.drawLoadingCircleNormal(x + 90, y + 29, new Color(199, 19, 19, 200));
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        Gui.drawRect(x, y + this.height + 8, x + 160, y + this.height + 10, new Color(1, 1, 1, 200).getRGB());
                        DrawUtil.drawGradientSideways(x, y + this.height + 8, (double)x + 160.0 * healthMult, y + this.height + 10, new Color(199, 19, 19, 200).getRGB(), new Color(182, 182, 84, 255).getRGB());
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        GuiInventory.drawEntityOnScreen(x + 22, y + 54, 26, -entity.rotationYaw, entity.rotationPitch, entity);
                        sfpro.drawStringWithShadow(entity.getName(), x + 44, y + 8, new Color(255, 255, 255).getRGB());
                        sfpro.drawStringWithShadow(entity.getHealth() < TargetHUD.mc.thePlayer.getHealth() ? "Winning" : "Losing", x + 44, y + 44, new Color(255, 255, 255).getRGB());
                        break;
                    }
                    case "Lime Recode": {
                        Acrimony.instance.blurHandler.bloom(x - 1, y, this.width - 2, this.height - 20, 5, new Color(0, 0, 0, 200));
                        DrawUtil.drawRoundedRect(x, y + 3, (double)(x + this.width) + Math.max(130.0, 42.0 + FontUtil.getStringWidth(this.font.getMode(), entity.getName()) + 3.0) - (double)this.width + 15.0, y + this.height - 12, 1.0, new Color(37, 37, 37, 255).getRGB());
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 4, y + 4, 32, 32);
                        Gui.drawRect(x + 40, y + 27, (double)(x + 54 + 82) + Math.max(130.0, 42.0 + FontUtil.getStringWidth(this.font.getMode(), entity.getName()) + 3.0) - (double)this.width + 15.0, y + 36, new Color(30, 29, 29, 202).getRGB());
                        GL11.glScissor(x + 40, y + 27, (int)((double)((int)((double)(x + 54) + 82.0 * healthMult)) + Math.max(130.0, 42.0 + FontUtil.getStringWidth(this.font.getMode(), entity.getName()) + 3.0) - (double)this.width + 15.0), y + 99);
                        DrawUtil.drawGradientSideways(x + 40, y + 27, (double)(x + 54) + 82.0 * healthMult + Math.max(130.0, 42.0 + FontUtil.getStringWidth(this.font.getMode(), entity.getName()) + 3.0) - (double)this.width + 15.0, y + 36, new Color(14, 176, 236).getRGB(), new Color(78, 221, 246).getRGB());
                        GL11.glScissor(x + 40, y + 27, (int)((double)((int)((double)(x + 54) + 82.0 * healthMult)) + Math.max(130.0, 42.0 + FontUtil.getStringWidth(this.font.getMode(), entity.getName()) + 3.0) - (double)this.width + 15.0), y + 99);
                        FontUtil.drawString(this.font.getMode(), entity.getGameProfile().getName(), x + 40, y + 8, new Color(255, 255, 255).getRGB());
                    }
                }
            }, x, y, x + this.width, y + this.height);
            this.hadTarget = true;
        } else {
            this.hadTarget = false;
        }
    }
}

