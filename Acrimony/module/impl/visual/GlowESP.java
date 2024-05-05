/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.Render2DEvent;
import Acrimony.event.impl.Render3DEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.util.animation.glowesp.DecelerateAnimation;
import Acrimony.util.animation.glowesp.DecelerateAnimationParent;
import Acrimony.util.animation.glowesp.GlowESPShaderUtil;
import Acrimony.util.misc.MathUtils;
import Acrimony.util.render.RenderUtil;
import Acrimony.util.render.StencilUtil;
import Acrimony.util.shader.ShaderUtil;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class GlowESP
extends Module {
    private final DoubleSetting radius = new DoubleSetting("Radius", 4.0, 2.0, 30.0, 2.0);
    private final DoubleSetting exposure = new DoubleSetting("Exposure", 2.2, 0.5, 10.0, 0.1);
    private final BooleanSetting seperate = new BooleanSetting("Seperate Texture", false);
    private final BooleanSetting white = new BooleanSetting("Static White", true);
    private static ClientTheme theme;
    public static boolean renderNameTags;
    private final GlowESPShaderUtil outlineShader = new GlowESPShaderUtil("acrimony/shader/glowesp/outline.frag");
    private final GlowESPShaderUtil glowShader = new GlowESPShaderUtil("acrimony/shader/glowesp/glow.frag");
    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final List<Entity> entities = new ArrayList<Entity>();
    public static DecelerateAnimationParent fadeIn;

    public GlowESP() {
        super("GlowESP", Category.VISUAL);
        this.addSettings(this.radius, this.exposure, this.seperate, this.white);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        fadeIn = new DecelerateAnimation(250, 1.0);
    }

    @Override
    public void onClientStarted() {
        theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
    }

    @Listener
    public final void onRender3D(Render3DEvent e) {
        this.framebuffer = StencilUtil.createFrameBuffer(this.framebuffer);
        this.outlineFrameBuffer = StencilUtil.createFrameBuffer(this.outlineFrameBuffer);
        this.glowFrameBuffer = StencilUtil.createFrameBuffer(this.glowFrameBuffer);
        this.collectEntities();
        this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);
        this.entities.forEach(entity -> mc.getRenderManager().renderEntityStaticNoShadow((Entity)entity, e.getPartialTicks(), false));
        this.framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.disableLighting();
    }

    @Listener
    public final void onRender2D(Render2DEvent e) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (this.framebuffer != null && this.outlineFrameBuffer != null && this.entities.size() > 0) {
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.0f);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            this.outlineFrameBuffer.framebufferClear();
            this.outlineFrameBuffer.bindFramebuffer(true);
            this.outlineShader.init();
            this.setupOutlineUniforms(0.0f, 1.0f);
            StencilUtil.bindTexture(this.framebuffer.framebufferTexture);
            ShaderUtil.drawQuads(new ScaledResolution(mc));
            this.outlineShader.init();
            this.setupOutlineUniforms(1.0f, 0.0f);
            StencilUtil.bindTexture(this.framebuffer.framebufferTexture);
            ShaderUtil.drawQuads(new ScaledResolution(mc));
            this.outlineShader.unload();
            this.outlineFrameBuffer.unbindFramebuffer();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.glowFrameBuffer.framebufferClear();
            this.glowFrameBuffer.bindFramebuffer(true);
            this.glowShader.init();
            this.setupGlowUniforms(1.0f, 0.0f);
            StencilUtil.bindTexture(this.outlineFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads(new ScaledResolution(mc));
            this.glowShader.unload();
            this.glowFrameBuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);
            this.glowShader.init();
            this.setupGlowUniforms(0.0f, 1.0f);
            if (this.seperate.isEnabled()) {
                GL13.glActiveTexture(34000);
                StencilUtil.bindTexture(this.framebuffer.framebufferTexture);
            }
            GL13.glActiveTexture(33984);
            StencilUtil.bindTexture(this.glowFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads(new ScaledResolution(mc));
            this.glowShader.unload();
        }
    }

    public void setupGlowUniforms(float dir1, float dir2) {
        Color color = this.getColor();
        this.glowShader.setUniformi("texture", 0);
        if (this.seperate.isEnabled()) {
            this.glowShader.setUniformi("textureToCheck", 16);
        }
        this.glowShader.setUniformf("radius", (float)this.radius.getValue());
        this.glowShader.setUniformf("texelSize", 1.0f / (float)GlowESP.mc.displayWidth, 1.0f / (float)GlowESP.mc.displayHeight);
        this.glowShader.setUniformf("direction", dir1, dir2);
        this.glowShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
        if (this.exposure != null && fadeIn != null) {
            float exposureValue = (float)(this.exposure.getValue() * fadeIn.getOutput());
            this.glowShader.setUniformf("exposure", exposureValue);
        } else if (this.exposure != null) {
            System.err.println("Error: exposure is null. #" + this.exposure.getValue());
        } else {
            System.err.println("Error: fadeIn is null.");
        }
        this.glowShader.setUniformi("avoidTexture", this.seperate.isEnabled() ? 1 : 0);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        int i = 1;
        while ((double)i <= this.radius.getValue()) {
            buffer.put(MathUtils.calculateGaussianValue(i, (float)this.radius.getValue() / 2.0f));
            ++i;
        }
        buffer.rewind();
        GL20.glUniform1(this.glowShader.getUniform("weights"), buffer);
    }

    public void setupOutlineUniforms(float dir1, float dir2) {
        Color color = this.getColor();
        this.outlineShader.setUniformi("texture", 0);
        this.outlineShader.setUniformf("radius", (float)this.radius.getValue() / 1.5f);
        this.outlineShader.setUniformf("texelSize", 1.0f / (float)GlowESP.mc.displayWidth, 1.0f / (float)GlowESP.mc.displayHeight);
        this.outlineShader.setUniformf("direction", dir1, dir2);
        this.outlineShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
    }

    public void renderEntities(float ticks) {
        this.entities.forEach(entity -> {
            renderNameTags = false;
            mc.getRenderManager().renderEntityStaticNoShadow((Entity)entity, ticks, false);
            renderNameTags = true;
        });
    }

    private Color getColor() {
        return new Color(this.white.isEnabled() ? new Color(255, 255, 255).getRGB() : theme.getColor(0));
    }

    public void collectEntities() {
        this.entities.clear();
        for (Entity entity : GlowESP.mc.theWorld.getLoadedEntityList()) {
            if (!RenderUtil.isInViewFrustrum(entity) || entity == GlowESP.mc.thePlayer && GlowESP.mc.gameSettings.thirdPersonView == 0 || !(entity instanceof EntityPlayer)) continue;
            this.entities.add(entity);
        }
    }

    static {
        renderNameTags = true;
    }
}

