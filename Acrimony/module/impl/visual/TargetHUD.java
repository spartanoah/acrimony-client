/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
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
import Acrimony.util.render.StencilUtil;
import java.awt.Color;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;

public class TargetHUD
extends HUDModule {
    private final ModeSetting mode = new ModeSetting("Mode", "Acrimony", "Acrimony", "Outline", "Blur");
    private final EnumModeSetting<AnimationType> animationType = AnimationUtil.getAnimationType(AnimationType.BOUNCE);
    private final IntegerSetting animationDuration = AnimationUtil.getAnimationDuration(400);
    private final IntegerSetting healthBarDelay = new IntegerSetting("Healh bar delay", 100, 0, 450, 25);
    private final BooleanSetting roundedHealth = new BooleanSetting("Rounded health", true);
    private final ModeSetting font = FontUtil.getFontSetting();
    private Animation animation;
    private Killaura killauraModule;
    private ClientTheme theme;
    private EntityPlayer target;
    private final TimerUtil barTimer = new TimerUtil();
    private final TimerUtil timer = new TimerUtil();
    private float renderedHealth;
    private boolean hadTarget;
    private boolean out;

    public TargetHUD() {
        super("TargetInfo", Category.VISUAL, 0.0, 0.0, 140, 50, AlignType.LEFT);
        this.addSettings(this.mode, this.font, this.animationType, this.animationDuration, this.healthBarDelay, this.roundedHealth);
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
                double endAnimX = (double)(x + 54) + 79.0 * healthMult;
                switch (this.mode.getMode()) {
                    case "Acrimony": {
                        DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height, 6.0, Integer.MIN_VALUE);
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 8, y + 6, x + 44, y + 42, 10.0, -1);
                        StencilUtil.readStencilBuffer(1);
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 8, y + 6, 36, 36);
                        StencilUtil.uninitStencilBuffer();
                        DrawUtil.drawRoundedRect(x + 52, y + 35, x + 54 + 79, y + 43, 6.0, Integer.MIN_VALUE);
                        StencilUtil.initStencilToWrite();
                        DrawUtil.drawRoundedRect(x + 52, y + 35, (double)(x + 54) + 79.0 * healthMult, y + 43, 6.0, this.theme.getColor(0));
                        StencilUtil.readStencilBuffer(1);
                        for (double i = (double)(x + 52); i < endAnimX; i += 1.0) {
                            Gui.drawRect(i, y + 35, i + 1.0, y + 45, this.theme.getColor((int)(200.0 + i * 5.0)));
                        }
                        StencilUtil.uninitStencilBuffer();
                        FontUtil.drawStringWithShadow(this.font.getMode(), entity.getGameProfile().getName(), x + 55, y + 9, -1);
                        FontUtil.drawStringWithShadow(this.font.getMode(), health + " HP", x + 55, y + 20, -1);
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
                    }
                }
            }, x, y, x + this.width, y + this.height);
            this.hadTarget = true;
        } else {
            this.hadTarget = false;
        }
    }
}

