/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.Alternative3DEvent;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.event.impl.Render2DEvent;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.HUDModule;
import Acrimony.module.Module;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.animation.AnimationHolder;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.network.ServerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class HUD
extends HUDModule {
    private boolean initialised;
    private final ArrayList<AnimationHolder<Module>> modules = new ArrayList();
    private final BooleanSetting showwater = new BooleanSetting("Client WaterMark", true);
    private final BooleanSetting showarray = new BooleanSetting("Client ArrayList", true);
    private final BooleanSetting infomation = new BooleanSetting("Game Infomation", true);
    private static BooleanSetting shownoti = new BooleanSetting("Nothifications", true);
    private static BooleanSetting moresetting = new BooleanSetting("Misc setting", true);
    public static final BooleanSetting notification = new BooleanSetting("Enable Noti", () -> shownoti.isEnabled(), true);
    public static ModeSetting notificationmode = new ModeSetting("notification mode", () -> notification.isEnabled() && shownoti.isEnabled(), "Simple", "Simple", "Outline", "Blur", "Jello");
    public static final BooleanSetting notificationsounds = new BooleanSetting("Toggle module Sound", () -> notification.isEnabled() && shownoti.isEnabled(), true);
    private final ModeSetting arraymode = new ModeSetting("Array Mode", () -> this.showarray.isEnabled(), "Simple", "Simple", "Outline", "Blur", "Jello");
    private final ModeSetting arrayfont = new ModeSetting("Array CFont", () -> this.showarray.isEnabled(), "SfPro", "SfPro", "SfBold", "Mc");
    private final ModeSetting watermode = new ModeSetting("Mark Mode", () -> this.showwater.isEnabled(), "New", "New mark", "Old mark", "Blur mark", "Jello");
    private final ModeSetting waterfont = new ModeSetting("Water CFont", () -> this.showwater.isEnabled(), "SfPro", "SfPro", "SfBold", "Mc");
    private final ModeSetting animation = new ModeSetting("Animation", () -> this.showarray.isEnabled(), "Bounce", "Bounce", "Slide");
    private final ModeSetting blurmode = new ModeSetting("Color", () -> this.showarray.isEnabled() && this.arraymode.is("Blur"), "ClientTheme", "ClientTheme", "Black Static", "Custom Static");
    private final BooleanSetting suffix = new BooleanSetting("Tags", () -> this.showarray.isEnabled(), true);
    private final BooleanSetting important = new BooleanSetting("Only Important", () -> this.showarray.isEnabled(), false);
    private final EnumModeSetting<AlignType> alignMode = new EnumModeSetting("Align type", () -> this.showarray.isEnabled(), (Enum)AlignType.RIGHT, (Enum[])AlignType.values());
    private final BooleanSetting bps = new BooleanSetting("BPS", () -> this.infomation.isEnabled(), true);
    private final BooleanSetting balance = new BooleanSetting("Balance", () -> this.infomation.isEnabled(), true);
    private final BooleanSetting infobg = new BooleanSetting("Blur Background", () -> this.infomation.isEnabled(), false);
    private final DoubleSetting colorRedValue = new DoubleSetting("R", () -> this.showarray.isEnabled() && this.arraymode.is("Blur") && this.blurmode.is("Custom Static"), 255.0, 0.0, 255.0, 1.0);
    private final DoubleSetting colorGreenValue = new DoubleSetting("G", () -> this.showarray.isEnabled() && this.arraymode.is("Blur") && this.blurmode.is("Custom Static"), 255.0, 0.0, 255.0, 1.0);
    private final DoubleSetting colorBlueValue = new DoubleSetting("B", () -> this.showarray.isEnabled() && this.arraymode.is("Blur") && this.blurmode.is("Custom Static"), 255.0, 0.0, 255.0, 1.0);
    private AcrimonyFont sfpro;
    private AcrimonyFont sfprobold;
    private AcrimonyFont sfproTitle;
    private AcrimonyFont jellolight;
    private AcrimonyFont jellomedium;
    private AcrimonyFont jelloregular;
    private AcrimonyFont jelloregularSubTitle;
    private AcrimonyFont jelloregularTitle;
    private AcrimonyFont jellosemibold;
    private AcrimonyFont jellosemiboldTitle;
    private ClientTheme theme;
    public static float startY = 0.0f;
    public static float y;

    public HUD() {
        super("HUD", Category.VISUAL, 5.0, 5.0, 100, 200, AlignType.RIGHT);
        this.addSettings(this.showwater, this.watermode, this.waterfont, this.showarray, this.arraymode, this.arrayfont, this.blurmode, this.colorRedValue, this.colorGreenValue, this.colorBlueValue, this.animation, this.suffix, this.important, this.alignMode, this.infomation, this.bps, this.balance, this.infobg, shownoti, notification, notificationmode, notificationsounds, moresetting);
        this.listenType = EventListenType.MANUAL;
        this.setStateHidden(true);
        this.startListening();
        this.setEnabledSilently(true);
    }

    @Override
    public void onClientStarted() {
        Acrimony.instance.getModuleManager().modules.forEach(m -> this.modules.add(new AnimationHolder<Module>((Module)m)));
        this.sfpro = Acrimony.instance.getFontManager().getSfpro();
        this.sfprobold = Acrimony.instance.getFontManager().getSfprobold();
        this.sfproTitle = Acrimony.instance.getFontManager().getSfproTitle();
        this.jellolight = Acrimony.instance.getFontManager().getJellolight();
        this.jellomedium = Acrimony.instance.getFontManager().getJellomedium();
        this.jelloregular = Acrimony.instance.getFontManager().getJelloregular();
        this.jelloregularSubTitle = Acrimony.instance.getFontManager().getJelloregularSubTitle();
        this.jelloregularTitle = Acrimony.instance.getFontManager().getJelloregularTitle();
        this.jellosemibold = Acrimony.instance.getFontManager().getJellosemibold();
        this.jellosemiboldTitle = Acrimony.instance.getFontManager().getJellosemiboldTitle();
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
    }

    @Override
    protected void renderModule(boolean inChat) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (this.isEnabled()) {
            if (!this.initialised) {
                this.sort();
                this.initialised = true;
            }
            this.alignType = this.alignMode.getMode();
            if (HUD.mc.gameSettings.showDebugInfo) {
                return;
            }
            switch (this.arraymode.getMode()) {
                case "Simple": {
                    this.renderSimple();
                    break;
                }
                case "Outline": {
                    this.renderOutline();
                    break;
                }
                case "Blur": {
                    this.renderBlurArray();
                    break;
                }
                case "Jello": {
                    this.renderJelloArray();
                }
            }
            if (HUD.mc.gameSettings.showDebugInfo) {
                return;
            }
            switch (this.watermode.getMode()) {
                case "Old mark": {
                    this.WaterrenderOld();
                    break;
                }
                case "New mark": {
                    this.WaterrenderNew();
                    break;
                }
                case "Blur mark": {
                    this.WaterrenderBlur();
                    break;
                }
                case "Jello": {
                    this.WaterrenderJello();
                }
            }
            if (HUD.mc.gameSettings.showDebugInfo) {
                return;
            }
            float x = 4.0f;
            float y = sr.getScaledHeight() - 13;
            float fully = this.sfpro.fontHeight;
            if (this.infobg.isEnabled()) {
                double WIDTH = this.getStringWidth("Balance : --10000");
                Acrimony.instance.blurHandler.bloom((int)x - 2, (int)((float)((int)y) - fully) + 12, (int)WIDTH, (int)fully - 2, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
            }
            if (this.balance.isEnabled()) {
                this.sfpro.drawStringWithShadow("Balance : " + Acrimony.instance.getBalanceHandler().getBalanceInMS(), x, y, -1);
                y -= 10.0f;
            }
            if (this.bps.isEnabled()) {
                double bpt = Math.hypot(HUD.mc.thePlayer.posX - HUD.mc.thePlayer.lastTickPosX, HUD.mc.thePlayer.posZ - HUD.mc.thePlayer.lastTickPosZ) * (double)HUD.mc.timer.timerSpeed;
                double bps = bpt * 20.0;
                double roundedBPS = (double)Math.round(bps * 100.0) / 100.0;
                this.sfpro.drawStringWithShadow("Speed : " + roundedBPS, x, y, -1);
            }
        }
    }

    @Listener
    public void onPostMotion(PostMotionEvent event) {
        this.sort();
    }

    private void sort() {
        Collections.reverse(this.modules);
        if (this.suffix.isEnabled()) {
            this.modules.sort((m1, m2) -> (int)Math.round(this.arraygetStringWidth(((Module)m1.get()).getDisplayName()) * 8.0 - (double)Math.round(this.arraygetStringWidth(((Module)m2.get()).getDisplayName()) * 8.0)));
        } else {
            this.modules.sort((m1, m2) -> (int)Math.round(this.arraygetStringWidth(((Module)m1.get()).getName()) * 8.0 - (double)Math.round(this.arraygetStringWidth(((Module)m2.get()).getName()) * 8.0)));
        }
        Collections.reverse(this.modules);
        Collections.reverseOrder();
    }

    private void renderSimple() {
        ScaledResolution sr = new ScaledResolution(mc);
        float x = (float)this.posX.getValue();
        y = (float)this.posY.getValue();
        float offsetY = 11.0f;
        float lastStartX = 0.0f;
        float lastEndX = 0.0f;
        boolean firstModule = true;
        float width = 0.0f;
        for (AnimationHolder<Module> holder : this.modules) {
            Module m = holder.get();
            String name = this.suffix.isEnabled() ? m.getDisplayName() : m.getName();
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)((double)sr.getScaledWidth() - this.arraygetStringWidth(name) - (double)x);
            float startY = y;
            float finalLastStartX = lastStartX;
            float endX = this.alignMode.getMode() == AlignType.LEFT ? (float)((double)x + this.arraygetStringWidth(name)) : (float)sr.getScaledWidth() - x;
            float endY = y + offsetY;
            if (Math.abs(endX - startX) > width) {
                width = Math.abs(endX - startX);
            }
            if (this.important.isEnabled() && m.getCategory() == Category.VISUAL) continue;
            holder.setAnimType(this.animation.is("Bounce") ? AnimationType.BOUNCE : AnimationType.SLIDE);
            holder.setAnimDuration(250L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            holder.render(() -> this.arraydrawStringWithShadow(name, startX, startY + 2.0f, this.getColor((int)(startY * -17.0f))), startX - 2.5f, startY, endX, endY);
            y += offsetY * holder.getYMult();
            lastStartX = startX;
        }
        this.width = (int)width + 1;
        this.height = (int)((double)y - this.posY.getValue()) + 1;
    }

    private void renderOutline() {
        ScaledResolution sr = new ScaledResolution(mc);
        float x = (float)this.posX.getValue();
        y = (float)this.posY.getValue();
        float offsetY = 11.0f;
        float lastStartX = 0.0f;
        float lastEndX = 0.0f;
        boolean firstModule = true;
        float width = 0.0f;
        for (AnimationHolder<Module> holder : this.modules) {
            Module m = holder.get();
            String name = this.suffix.isEnabled() ? m.getDisplayName() : m.getName();
            holder.setAnimType(this.animation.is("Bounce") ? AnimationType.BOUNCE : AnimationType.SLIDE);
            holder.setAnimDuration(300L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)((double)sr.getScaledWidth() - this.arraygetStringWidth(name) - (double)x);
            startY = y;
            float finalLastStartX = lastStartX;
            float endX = this.alignMode.getMode() == AlignType.LEFT ? (float)((double)x + this.arraygetStringWidth(name)) : (float)sr.getScaledWidth() - x;
            float endY = y + offsetY;
            if (Math.abs(endX - startX) > width) {
                width = Math.abs(endX - startX);
            }
            if (this.important.isEnabled() && m.getCategory() == Category.VISUAL) continue;
            if (firstModule) {
                Gui.drawRect(startX - 2.0f, y - 1.0f, endX + 3.0f, y, this.getColor((int)(startY * -17.0f)));
            } else {
                double diff = (double)startX - 3.5 - ((double)lastStartX - 1.5);
                if (diff > 1.0) {
                    Gui.drawRect(lastStartX - 2.0f, y, startX - 2.0f, y + 1.0f, this.getColor((int)(startY * -17.0f)));
                }
            }
            Gui.drawRect(endX + 2.0f, startY, endX + 3.0f, startY + offsetY * holder.getYMult(), this.getColor((int)(startY * -17.0f)));
            holder.render(() -> {
                Gui.drawRect(startX - 2.0f, startY, endX + 2.0f, endY, 0x70000000);
                Gui.drawRect(startX - 2.0f, startY, startX - 1.0f, endY, this.getColor((int)(startY * -17.0f)));
                this.arraydrawStringWithShadow(name, startX, startY + 2.0f, this.getColor((int)(startY * -17.0f)));
            }, startX - 2.5f, startY, endX, endY);
            y += offsetY * holder.getYMult();
            lastStartX = startX;
            lastEndX = endX;
            firstModule = false;
        }
        Gui.drawRect(lastStartX - 2.0f, y, lastEndX + 3.0f, y + 1.0f, this.getColor((int)(y * -17.0f)));
        this.width = (int)width + 1;
        this.height = (int)((double)y - this.posY.getValue()) + 1;
    }

    private void renderBlurArray() {
        ScaledResolution sr = new ScaledResolution(mc);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        float offsetY = 11.0f;
        float lastStartX = 0.0f;
        float lastEndX = 0.0f;
        boolean firstModule = true;
        float width = 0.0f;
        for (AnimationHolder<Module> holder : this.modules) {
            Module m = holder.get();
            String name = this.suffix.isEnabled() ? m.getDisplayName() : m.getName();
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)((double)sr.getScaledWidth() - this.arraygetStringWidth(name) - (double)x);
            float startY = y;
            float finalLastStartX = lastStartX;
            float endX = (float)this.arraygetStringWidth(name);
            float endY = offsetY;
            float AniX = this.alignMode.getMode() == AlignType.LEFT ? (float)((double)x + this.arraygetStringWidth(name)) : (float)sr.getScaledWidth() - x;
            float AniY = y + offsetY;
            if (Math.abs(endX - startX) > width) {
                width = Math.abs(endX - startX);
            }
            if (this.important.isEnabled() && m.getCategory() == Category.VISUAL) continue;
            holder.setAnimType(this.animation.is("Bounce") ? AnimationType.BOUNCE : AnimationType.SLIDE);
            holder.setAnimDuration(350L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            holder.render(() -> {
                switch (this.blurmode.getMode()) {
                    case "ClientTheme": {
                        Acrimony.instance.blurHandler.bloom((int)(startX - 4.0f), (int)startY, (int)(endX + 8.0f), (int)endY, ClientTheme.blurradius.getValue(), this.getColor((int)(startY * -17.0f)));
                        break;
                    }
                    case "Black Static": {
                        Acrimony.instance.blurHandler.bloom((int)(startX - 4.0f), (int)startY, (int)(endX + 8.0f), (int)endY, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 10).getRGB());
                        Gui.drawRect(startX - 2.0f, startY, AniX + 2.0f, AniY, new Color(0, 0, 0, 50).getRGB());
                        break;
                    }
                    case "Custom Static": {
                        Acrimony.instance.blurHandler.bloom((int)(startX - 4.0f), (int)startY, (int)(endX + 8.0f), (int)endY, ClientTheme.blurradius.getValue(), new Color((int)this.colorRedValue.getValue(), (int)this.colorGreenValue.getValue(), (int)this.colorBlueValue.getValue(), 150));
                    }
                }
                this.arraydrawStringWithShadow(name, startX, startY + 2.0f, this.getColor((int)(startY * -17.0f)));
            }, startX - 2.5f, startY, AniX, AniY);
            y += offsetY * holder.getYMult();
            lastStartX = startX;
        }
        this.width = (int)width + 1;
        this.height = (int)((double)y - this.posY.getValue()) + 1;
    }

    private void renderJelloArray() {
        ScaledResolution sr = new ScaledResolution(mc);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        float offsetY = 11.0f;
        float lastStartX = 0.0f;
        float lastEndX = 0.0f;
        boolean firstModule = true;
        float width = 0.0f;
        for (AnimationHolder<Module> holder : this.modules) {
            Module m = holder.get();
            String name = this.suffix.isEnabled() ? m.getDisplayName() : m.getName();
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)(sr.getScaledWidth() - this.jellolight.getStringWidth(name)) - x;
            float startY = y;
            float finalLastStartX = lastStartX;
            float endX = this.alignMode.getMode() == AlignType.LEFT ? x + (float)this.jellolight.getStringWidth(name) : (float)sr.getScaledWidth() - x;
            float endY = y + offsetY;
            float blurxS = this.alignMode.getMode() == AlignType.LEFT ? x : (float)(sr.getScaledWidth() - this.jellolight.getStringWidth(name)) - x + 10.0f;
            float bluryS = y + 2.0f;
            float blurx = this.jellolight.getStringWidth(name) - 20;
            float blury = offsetY - 4.0f;
            if (Math.abs(endX - startX) > width) {
                width = Math.abs(endX - startX);
            }
            if (this.important.isEnabled() && m.getCategory() == Category.VISUAL) continue;
            holder.setAnimType(AnimationType.SLIDE);
            holder.setAnimDuration(250L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            holder.render(() -> {
                Acrimony.instance.blurHandler.bloom((int)(blurxS - 4.0f), (int)bluryS, (int)(blurx + 8.0f), (int)blury, 50, new Color(0, 0, 0, 255).getRGB());
                this.jellolight.drawString(name, startX, startY + 2.0f, new Color(255, 255, 255, 250).getRGB());
                this.jellolight.drawString(name, startX, startY + 2.0f, new Color(255, 255, 255, 150).getRGB());
                this.jellolight.drawString(name, startX, startY + 2.0f, new Color(255, 255, 255, 50).getRGB());
            }, startX - 2.5f, startY, endX, endY);
            y += offsetY * holder.getYMult();
            lastStartX = startX;
        }
        this.width = (int)width + 1;
        this.height = (int)((double)y - this.posY.getValue()) + 1;
    }

    private void WaterrenderNew() {
        Objects.requireNonNull(Acrimony.instance);
        String clientName = "Acrimony";
        String formattedClientName = String.valueOf(clientName.charAt(0)) + (Object)((Object)ChatFormatting.WHITE) + clientName.substring(1, clientName.length());
        StringBuilder stringBuilder = new StringBuilder().append(formattedClientName).append(" ");
        Objects.requireNonNull(Acrimony.instance);
        String watermark = stringBuilder.append("v1.0.3").toString();
        double watermarkWidth = this.watergetStringWidth(watermark);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        this.sfproTitle.drawStringWithShadow(watermark, x + 1.0f, y + 1.0f, this.theme.getColor(0));
        this.width = (int)(watermarkWidth + 3.0);
        this.height = 15;
    }

    private void WaterrenderOld() {
        Objects.requireNonNull(Acrimony.instance);
        String clientName = "Acrimony";
        String formattedClientName = String.valueOf(clientName.charAt(0)) + (Object)((Object)ChatFormatting.WHITE) + clientName.substring(1, clientName.length());
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(11);
        String time = hour >= 12 ? new SimpleDateFormat("hh:mma").format(new Date()) : new SimpleDateFormat("hh:mma").format(new Date());
        StringBuilder stringBuilder = new StringBuilder().append(formattedClientName).append(" ");
        Objects.requireNonNull(Acrimony.instance);
        String watermark = stringBuilder.append("v1.0.3").append(" | ").append(time).append(" | ").append(ServerUtil.getCurrentServer()).toString();
        double watermarkWidth = this.watergetStringWidth(watermark);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        Gui.drawRect(x + 2.0f, y + 2.0f, x + (float)((int)watermarkWidth) + 8.0f, y + 20.0f, Integer.MIN_VALUE);
        float i = x;
        while ((double)i < (double)(x + 2.0f) + watermarkWidth) {
            Gui.drawRect(i + 2.0f, y + 2.0f, i + 7.0f, y + 3.0f, this.theme.getColor((int)(i * 10.0f)));
            i += 1.0f;
        }
        this.waterdrawStringWithShadow(watermark, x + 5.0f, y + 8.0f, this.theme.getColor(0));
        this.width = (int)(watermarkWidth + 3.0);
        this.height = 15;
    }

    private void WaterrenderBlur() {
        Objects.requireNonNull(Acrimony.instance);
        String clientName = "Acrimony";
        String formattedClientName = String.valueOf(clientName.charAt(0)) + (Object)((Object)ChatFormatting.WHITE) + clientName.substring(1, clientName.length());
        StringBuilder stringBuilder = new StringBuilder().append(formattedClientName).append(" ");
        Objects.requireNonNull(Acrimony.instance);
        String watermark = stringBuilder.append("v1.0.3").append(" | ").append(mc.getDebugFPS()).append("FPS | ").append(ServerUtil.getCurrentServer()).toString();
        double watermarkWidth = this.watergetStringWidth(watermark);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        Acrimony.instance.blurHandler.bloom((int)x, (int)(y + 2.0f), (int)(watermarkWidth + 10.0), 20, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
        this.waterdrawStringWithShadow(watermark, x + 5.0f, y + 8.0f, this.theme.getColor(0));
        this.width = (int)(watermarkWidth + 3.0);
        this.height = 15;
    }

    private void WaterrenderJello() {
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        this.jelloregularTitle.drawString("Sigma", x, y, new Color(255, 255, 255, 180).getRGB());
        this.jelloregularSubTitle.drawString("Jello", x, y + 26.0f, new Color(255, 255, 255, 180).getRGB());
    }

    public void arraydrawString(String text, float x, float y, int color) {
        switch (this.arrayfont.getMode()) {
            case "SfPro": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                return;
            }
            case "SfBold": {
                this.sfprobold.drawStringWithShadow(text, x, y, color);
                return;
            }
            case "Mc": {
                HUD.mc.fontRendererObj.drawString(text, x, y, color);
                return;
            }
        }
        HUD.mc.fontRendererObj.drawString(text, x, y, color);
    }

    public void arraydrawStringWithShadow(String text, float x, float y, int color) {
        switch (this.arrayfont.getMode()) {
            case "SfPro": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "SfBold": {
                this.sfprobold.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Mc": {
                HUD.mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
            }
        }
    }

    public void waterdrawString(String text, float x, float y, int color) {
        switch (this.waterfont.getMode()) {
            case "SfPro": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                return;
            }
            case "SfBold": {
                this.sfprobold.drawStringWithShadow(text, x, y, color);
                return;
            }
            case "Mc": {
                HUD.mc.fontRendererObj.drawString(text, x, y, color);
                return;
            }
        }
        HUD.mc.fontRendererObj.drawString(text, x, y, color);
    }

    public void waterdrawStringWithShadow(String text, float x, float y, int color) {
        switch (this.waterfont.getMode()) {
            case "SfPro": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "SfBold": {
                this.sfprobold.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Mc": {
                HUD.mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
            }
        }
    }

    public double arraygetStringWidth(String s) {
        switch (this.arrayfont.getMode()) {
            case "SfPro": {
                return this.sfpro.getStringWidth(s);
            }
            case "SfBold": {
                return this.sfprobold.getStringWidth(s);
            }
            case "Mc": {
                return HUD.mc.fontRendererObj.getStringWidth(s);
            }
        }
        return HUD.mc.fontRendererObj.getStringWidth(s);
    }

    public double watergetStringWidth(String s) {
        switch (this.waterfont.getMode()) {
            case "SfPro": {
                return this.sfpro.getStringWidth(s);
            }
            case "SfBold": {
                return this.sfprobold.getStringWidth(s);
            }
            case "Mc": {
                return HUD.mc.fontRendererObj.getStringWidth(s);
            }
        }
        return HUD.mc.fontRendererObj.getStringWidth(s);
    }

    public double getStringWidth(String s) {
        return this.sfpro.getStringWidth(s);
    }

    public int getColor(int offset) {
        return this.theme.getColor(offset);
    }

    @Listener
    public void on2DEvent(Render2DEvent e) {
    }

    @Listener
    public void onAlternative(Alternative3DEvent e) {
    }
}

