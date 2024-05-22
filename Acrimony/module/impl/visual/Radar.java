/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.HUDModule;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.render.DrawUtil;
import Acrimony.util.render.StencilUtil;
import java.awt.Color;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;

public class Radar
extends HUDModule {
    private AcrimonyFont sfpro;
    private ClientTheme theme;
    private final ModeSetting mode = new ModeSetting("Mode", "Future", "Future", "Outline", "Blur");
    private final BooleanSetting blurbg = new BooleanSetting("Blur Background", () -> this.mode.is("Future"), false);
    public BooleanSetting showtarget = new BooleanSetting("Render Setting", false);
    public BooleanSetting players = new BooleanSetting("Players", () -> this.showtarget.isEnabled(), true);
    public BooleanSetting animals = new BooleanSetting("Animals", () -> this.showtarget.isEnabled(), false);
    public BooleanSetting mobs = new BooleanSetting("Mobs", () -> this.showtarget.isEnabled(), false);
    public BooleanSetting invisible = new BooleanSetting("Invisibles", () -> this.showtarget.isEnabled(), true);
    private boolean initialised;

    public Radar() {
        super("MiniMap", Category.VISUAL, 50.0, 80.0, 80, 80, AlignType.LEFT);
        this.addSettings(this.mode, this.blurbg, this.showtarget, this.players, this.animals, this.mobs);
        this.posX.setValue(this.width);
        this.posY.setValue(this.height);
    }

    @Override
    @Listener
    protected void renderModule(boolean inChat) {
        boolean canRender = inChat;
        if (inChat && this.isEnabled()) {
            this.renderRadar(canRender);
        } else if (this.isEnabled()) {
            this.renderRadar(canRender);
        }
    }

    private void renderRadar(boolean canRender) {
        if (!this.initialised) {
            this.initialise();
            this.initialised = true;
        }
        if (Radar.mc.gameSettings.showDebugInfo) {
            return;
        }
        AcrimonyFont icon1 = Acrimony.instance.getFontManager().getIconregular();
        AcrimonyFont sfpro = Acrimony.instance.getFontManager().getSfprobold();
        int x = (int)this.posX.getValue();
        int y = (int)this.posY.getValue();
        switch (this.mode.getMode()) {
            case "Future": {
                if (this.blurbg.isEnabled()) {
                    Acrimony.instance.blurHandler.blur((double)x, (double)y, (double)this.width, (double)this.height, 0.0f);
                }
                DrawUtil.drawRoundedRect(x, y + 8, x + this.width + 1, y + this.height + 1, 2.0, Integer.MIN_VALUE);
                DrawUtil.drawRoundedRect(x, (double)y + (double)this.height / 2.0 - 0.5, x + this.width + 1, (double)y + (double)this.height / 2.0 + 0.5, 2.0, Integer.MIN_VALUE);
                DrawUtil.drawRoundedRect((double)x + (double)this.width / 2.0 - 0.5, y + 8, (double)x + (double)this.width / 2.0 + 0.5, y + this.height + 1, 0.0, Integer.MIN_VALUE);
                StencilUtil.initStencilToWrite();
                DrawUtil.drawRoundedRect(x, y + 8, x + this.width + 1, y + this.height + 1, 2.0, Integer.MIN_VALUE);
                StencilUtil.readStencilBuffer(1);
                GlStateManager.translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
                for (Entity entity : Radar.mc.theWorld.loadedEntityList) {
                    if (!(entity instanceof EntityLivingBase)) continue;
                    int color = 0;
                    if (entity.isInvisible() && !this.invisible.isEnabled()) continue;
                    if (this.mobs.isEnabled() && entity instanceof EntityMob) {
                        color = new Color(255, 0, 0).getRGB();
                    }
                    if (this.animals.isEnabled() && entity instanceof EntityAnimal) {
                        color = new Color(77, 182, 77).getRGB();
                    }
                    if (this.players.isEnabled() && entity instanceof EntityPlayer) {
                        color = new Color(182, 182, 84).getRGB();
                    }
                    double Ex = (double)x + Radar.mc.thePlayer.posX - entity.posX;
                    double Ey = (double)y + Radar.mc.thePlayer.posZ - entity.posZ;
                    DrawUtil.drawRoundedRect(Ex - 1.0, Ey - 1.0, Ex + 1.0, Ey + 1.0, 0.8, color);
                }
                GlStateManager.translate(-((float)this.width / 2.0f), -((float)this.height / 2.0f), 0.0f);
                StencilUtil.uninitStencilBuffer();
                DrawUtil.drawRoundedRect(x, y - 6, x + this.width + 1, y + 8, 2.0, Integer.MIN_VALUE);
                for (float i = (float)x; i < (float)(x + this.width); i += 1.0f) {
                    Gui.drawRect(i, y + 8, i + 2.0f, y + 9, this.theme.getColor((int)(i * 10.0f)));
                }
                icon1.drawStringWithShadow("m", x + 2, y - 1, this.theme.getColor(1));
                sfpro.drawStringWithShadow("Radar", x + 16, y - 2, -1);
                break;
            }
            case "Outline": {
                DrawUtil.drawRoundedRect(x, y, x + this.width + 1, y + this.height + 1, 5.0, Integer.MIN_VALUE);
                DrawUtil.drawGradientSideways(x, y + this.height, x + this.width, y + this.height + 2, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                DrawUtil.drawGradientSideways(x, y, x + this.width, y + 2, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                Gui.drawRect(x - 2, y, x, y + this.height + 2, ClientTheme.color1.getRGB());
                Gui.drawRect(x + this.width, y, x + this.width + 2, y + this.height + 2, ClientTheme.color2.getRGB());
                StencilUtil.initStencilToWrite();
                DrawUtil.drawRoundedRect(x, y, x + this.width + 1, y + this.height + 1, 5.0, Integer.MIN_VALUE);
                StencilUtil.readStencilBuffer(1);
                GlStateManager.translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
                for (Entity entity : Radar.mc.theWorld.loadedEntityList) {
                    if (!(entity instanceof EntityLivingBase)) continue;
                    int color = 0;
                    if (entity.isInvisible() && !this.invisible.isEnabled()) continue;
                    if (this.mobs.isEnabled() && entity instanceof EntityMob) {
                        color = new Color(255, 0, 0).getRGB();
                    }
                    if (this.animals.isEnabled() && entity instanceof EntityAnimal) {
                        color = new Color(77, 182, 77).getRGB();
                    }
                    if (this.players.isEnabled() && entity instanceof EntityPlayer) {
                        color = new Color(182, 182, 84).getRGB();
                    }
                    double Ex = (double)x + Radar.mc.thePlayer.posX - entity.posX;
                    double Ey = (double)y + Radar.mc.thePlayer.posZ - entity.posZ;
                    DrawUtil.drawRoundedRect(Ex - 1.0, Ey - 1.0, Ex + 1.0, Ey + 1.0, 1.0, color);
                }
                GlStateManager.translate(-((float)this.width / 2.0f), -((float)this.height / 2.0f), 0.0f);
                StencilUtil.uninitStencilBuffer();
                DrawUtil.drawRoundedRect((double)x + (double)this.width / 2.0 - 2.5, (double)y + (double)this.height / 2.0 - 2.5, (double)x + (double)this.width / 2.0 - 2.5 + 5.0, (double)y + (double)this.height / 2.0 - 2.5 + 5.0, 5.0, -1);
                break;
            }
            case "Blur": {
                Acrimony.instance.blurHandler.bloom(x, y, this.width, this.height, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
                StencilUtil.initStencilToWrite();
                DrawUtil.drawRoundedRect(x, y, x + this.width, y + this.height, 1.0, Integer.MIN_VALUE);
                StencilUtil.readStencilBuffer(1);
                GlStateManager.translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
                for (Entity entity : Radar.mc.theWorld.loadedEntityList) {
                    if (!(entity instanceof EntityLivingBase)) continue;
                    double Ex = (double)x + Radar.mc.thePlayer.posX - entity.posX;
                    double Ey = (double)y + Radar.mc.thePlayer.posZ - entity.posZ;
                    Acrimony.instance.blurHandler.bloom((int)(Ex - 2.0), (int)(Ey - 2.0), 3, 3, 2, ClientTheme.color1);
                }
                GlStateManager.translate(-((float)this.width / 2.0f), -((float)this.height / 2.0f), 0.0f);
                StencilUtil.uninitStencilBuffer();
                Acrimony.instance.blurHandler.bloom((int)((double)x + (double)this.width / 2.0 - 2.5), (int)((double)y + (double)this.height / 2.0 - 2.5), 5, 5, 3, new Color(253, 253, 253, 250));
            }
        }
    }

    private void initialise() {
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
        this.sfpro = Acrimony.instance.getFontManager().getSfprosmall();
    }
}

