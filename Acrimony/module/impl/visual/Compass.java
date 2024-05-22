/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.util.IMinecraft;
import Acrimony.util.render.StencilUtil;
import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Compass
extends Module {
    public CompassHUD compass = new CompassHUD(325.0f, 325.0f, 1.0f, 2, true);

    public Compass() {
        super("Compass", Category.VISUAL);
    }

    @Listener
    public void onRender(RenderEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float yaw = Compass.mc.thePlayer.rotationYaw % 360.0f * 2.0f + 1080.0f;
        this.compass.draw(new ScaledResolution(mc));
        int count = 10;
        int c = 0;
        int w = 40;
        float yOff = 30.0f;
        float x = (float)sr.getScaledWidth() / 2.0f - (float)count / 2.0f * (float)w;
        StencilUtil.initStencilToWrite();
        Gui.drawRect(x + 10.0f, yOff - 3.0f, x + (float)(count * w) - 10.0f, yOff + 20.0f, Integer.MIN_VALUE);
        StencilUtil.readStencilBuffer(1);
        for (float i = 0.0f; i < 1080.0f; i += 10.0f) {
            int k = (int)i - 360;
            if (k >= 360) {
                k = 720 - k;
            }
            if (k <= 0) {
                k = -k;
            }
            float xx = x + (float)count / 2.0f * (float)w + (float)(c * w) - yaw;
            String kk = k + "";
            AcrimonyFont f = Acrimony.instance.getFontManager().getSfprobold();
            if (k == 90) {
                f = Acrimony.instance.getFontManager().getSfpro23();
                kk = "W";
            } else if (k == 180) {
                f = Acrimony.instance.getFontManager().getSfprobold();
                kk = "N";
            } else if (k == 270) {
                f = Acrimony.instance.getFontManager().getSfprobold();
                kk = "E";
            } else if (k % 360 == 0) {
                f = Acrimony.instance.getFontManager().getSfprobold();
                kk = "S";
            }
            int al = (int)Math.max(230.0f - Math.min(Math.abs(xx - (float)sr.getScaledWidth() / 2.0f), 230.0f), 0.0f);
            f.drawCenteredString(kk, xx, yOff + 10.0f, new Color(255, 255, 255, al).getRGB());
            ++c;
        }
        StencilUtil.uninitStencilBuffer();
        StencilUtil.readStencilBuffer(1);
    }

    public static class CompassHUD {
        public float innerWidth;
        public float outerWidth;
        public boolean shadow;
        public float scale;
        public int accuracy;
        public List<Degree> degrees = Lists.newArrayList();

        public CompassHUD(float i, float o, float s, int a, boolean sh) {
            this.innerWidth = i;
            this.outerWidth = o;
            this.scale = s;
            this.accuracy = a;
            this.shadow = sh;
            this.degrees.add(new Degree("N", 1));
            this.degrees.add(new Degree("195", 2));
            this.degrees.add(new Degree("210", 2));
            this.degrees.add(new Degree("NE", 3));
            this.degrees.add(new Degree("240", 2));
            this.degrees.add(new Degree("255", 2));
            this.degrees.add(new Degree("E", 1));
            this.degrees.add(new Degree("285", 2));
            this.degrees.add(new Degree("300", 2));
            this.degrees.add(new Degree("SE", 3));
            this.degrees.add(new Degree("330", 2));
            this.degrees.add(new Degree("345", 2));
            this.degrees.add(new Degree("S", 1));
            this.degrees.add(new Degree("15", 2));
            this.degrees.add(new Degree("30", 2));
            this.degrees.add(new Degree("SW", 3));
            this.degrees.add(new Degree("60", 2));
            this.degrees.add(new Degree("75", 2));
            this.degrees.add(new Degree("W", 1));
            this.degrees.add(new Degree("105", 2));
            this.degrees.add(new Degree("120", 2));
            this.degrees.add(new Degree("NW", 3));
            this.degrees.add(new Degree("150", 2));
            this.degrees.add(new Degree("165", 2));
        }

        private void drawTexture(float x, float y, float w, float h, float w2, float h2) {
            Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0.0f, 0.0f, (int)w, (int)h, w2, h2);
        }

        public void draw(ScaledResolution sr) {
            float completeLocation;
            float location;
            AcrimonyFont sfpro = Acrimony.instance.getFontManager().getJellosemiboldTitle();
            int counter = 10;
            boolean c = false;
            int w = 40;
            float yOff = 30.0f;
            float x = (float)sr.getScaledWidth() / 2.0f - (float)counter / 2.0f * (float)w;
            StencilUtil.initStencilToWrite();
            Gui.drawRect(x + 70.0f, yOff - 80.0f, x + (float)(counter * w) - 40.0f, yOff + 100.0f, Integer.MIN_VALUE);
            StencilUtil.readStencilBuffer(1);
            float center = (float)sr.getScaledWidth() / 2.0f;
            int count = 0;
            float yaaahhrewindTime = IMinecraft.mc.thePlayer.rotationYaw % 360.0f * 2.0f + 1080.0f;
            for (Degree d : this.degrees) {
                location = center + (float)(count * 30) - yaaahhrewindTime;
                completeLocation = d.type == 1 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : (d.type == 2 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : location - (float)(sfpro.getStringWidth(d.text) / 2));
                int opacity = this.opacity(sr, completeLocation);
                if (d.type == 1 && opacity != 0xFFFFFF) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 6.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 2 && opacity != 0xFFFFFF) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    Gui.drawRect(location - 0.5f, 29.0, location + 0.5f, 34.0, this.opacity2(sr, completeLocation));
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 18.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 3 && opacity != 0xFFFFFF) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 9.0f + (float)sfpro.getHeight() / 2.0f - (float)sfpro.getHeight() / 2.0f - 2.0f, this.opacity(sr, completeLocation));
                }
                ++count;
            }
            for (Degree d : this.degrees) {
                location = center + (float)(count * 30) - yaaahhrewindTime;
                float f = d.type == 1 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : (completeLocation = d.type == 2 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : location - (float)(sfpro.getStringWidth(d.text) / 2));
                if (d.type == 1) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 6.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 2) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    Gui.drawRect(location - 0.5f, 29.0, location + 0.5f, 34.0, this.opacity2(sr, completeLocation));
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 18.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 3) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 9.0f + (float)sfpro.getHeight() / 2.0f - (float)sfpro.getHeight() / 2.0f - 2.0f, this.opacity(sr, completeLocation));
                }
                ++count;
            }
            for (Degree d : this.degrees) {
                location = center + (float)(count * 30) - yaaahhrewindTime;
                float f = d.type == 1 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : (completeLocation = d.type == 2 ? location - (float)(sfpro.getStringWidth(d.text) / 2) : location - (float)(sfpro.getStringWidth(d.text) / 2));
                if (d.type == 1) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 6.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 2) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    Gui.drawRect(location - 0.5f, 29.0, location + 0.5f, 34.0, this.opacity2(sr, completeLocation));
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 18.5f, this.opacity(sr, completeLocation));
                }
                if (d.type == 3) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    sfpro.drawString(d.text, completeLocation, 9.0f + (float)sfpro.getHeight() / 2.0f - (float)sfpro.getHeight() / 2.0f - 2.0f, this.opacity(sr, completeLocation));
                }
                ++count;
            }
            StencilUtil.uninitStencilBuffer();
            StencilUtil.readStencilBuffer(1);
            if (this.shadow) {
                boolean disableAlpha;
                boolean enableBlend = GL11.glIsEnabled(3042);
                boolean bl = disableAlpha = !GL11.glIsEnabled(3008);
                if (!enableBlend) {
                    GL11.glEnable(3042);
                }
                if (!disableAlpha) {
                    GL11.glDisable(3008);
                }
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                IMinecraft.mc.getTextureManager().bindTexture(new ResourceLocation("acrimony/misc/shadow.png"));
                this.drawTexture((int)((float)(sr.getScaledWidth() / 2) - 162.75f), -8.0f, 325.0f, 86.0f, 325.5f, 86.5f);
                if (!enableBlend) {
                    GL11.glDisable(3042);
                }
                if (!disableAlpha) {
                    GL11.glEnable(3008);
                }
            }
        }

        public int opacity(ScaledResolution sr, float offset) {
            float offs = Math.abs((float)sr.getScaledWidth() / 2.0f - offset) * 1.4f;
            int opacity = (int)Math.min(Math.max(0.0f, 255.0f - offs), 200.0f);
            return offs <= 255.0f ? new Color(255, 255, 255, opacity).getRGB() : new Color(255, 255, 255, 5).getRGB();
        }

        public int opacity2(ScaledResolution sr, float offset) {
            float offs = Math.abs((float)sr.getScaledWidth() / 2.0f - offset) * 1.8f;
            int opacity = (int)Math.min(Math.max(0.0f, 255.0f - offs), 200.0f) / 2;
            return offs <= 255.0f ? new Color(255, 255, 255, opacity).getRGB() : new Color(255, 255, 255, 5).getRGB();
        }
    }

    public static class Degree {
        public String text;
        public int type;

        public Degree(String s, int t) {
            this.text = s;
            this.type = t;
        }
    }
}

