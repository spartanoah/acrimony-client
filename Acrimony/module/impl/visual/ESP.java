/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.Render3DEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Antibot;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.util.render.RenderUtil;
import java.awt.Color;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

public class ESP
extends Module {
    private final DoubleSetting lineWidth = new DoubleSetting("Line width", 3.25, 0.5, 4.0, 0.25);
    private final DoubleSetting alpha = new DoubleSetting("Alpha", 0.8, 0.2, 1.0, 0.05);
    private final BooleanSetting renderInvisibles = new BooleanSetting("Render invisibles", false);
    private ClientTheme theme;
    private Antibot antibotModule;

    public ESP() {
        super("ESP", Category.VISUAL);
        this.addSettings(this.lineWidth, this.alpha, this.renderInvisibles);
    }

    @Listener
    public void onRender3D(Render3DEvent event) {
        if (this.theme == null) {
            this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
        }
        if (this.antibotModule == null) {
            this.antibotModule = Acrimony.instance.getModuleManager().getModule(Antibot.class);
        }
        Color color = new Color(this.theme.getColor(100));
        RenderUtil.prepareBoxRender((float)this.lineWidth.getValue(), (double)color.getRed() / 255.0, (double)color.getGreen() / 255.0, (double)color.getBlue() / 255.0, this.alpha.getValue());
        RenderManager rm = mc.getRenderManager();
        float partialTicks = event.getPartialTicks();
        ESP.mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity != ESP.mc.thePlayer && (!entity.isInvisible() && !entity.isInvisibleToPlayer(ESP.mc.thePlayer) || this.renderInvisibles.isEnabled()) && entity instanceof EntityPlayer && this.antibotModule.canAttack((EntityPlayer)entity, this)).forEach(entity -> RenderUtil.renderEntityBox(rm, partialTicks, entity));
        RenderUtil.stopBoxRender();
    }
}

