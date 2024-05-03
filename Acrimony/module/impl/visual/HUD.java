/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.HUDModule;
import Acrimony.module.Module;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.animation.AnimationHolder;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.network.ServerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class HUD
extends HUDModule {
    private boolean initialised;
    private final ArrayList<AnimationHolder<Module>> modules = new ArrayList();
    private final BooleanSetting showwater = new BooleanSetting("Client WaterMark", true);
    private final BooleanSetting showarray = new BooleanSetting("Client ArrayList", true);
    private final BooleanSetting infomation = new BooleanSetting("Game Infomation", true);
    private final ModeSetting arraymode = new ModeSetting("Array Mode", () -> this.showarray.isEnabled(), "Simple", "Simple", "Outline");
    private final ModeSetting watermode = new ModeSetting("Mark Mode", () -> this.showwater.isEnabled(), "New", "New", "Old");
    private final ModeSetting animation = new ModeSetting("Animation", () -> this.showarray.isEnabled(), "Bounce", "Bounce", "Slide");
    private final BooleanSetting suffix = new BooleanSetting("Tags", () -> this.showarray.isEnabled(), true);
    private final BooleanSetting important = new BooleanSetting("Only Important", () -> this.showarray.isEnabled(), false);
    private final EnumModeSetting<AlignType> alignMode = new EnumModeSetting("Align type", () -> this.showarray.isEnabled(), (Enum)AlignType.RIGHT, (Enum[])AlignType.values());
    private final BooleanSetting bps = new BooleanSetting("BPS", () -> this.infomation.isEnabled(), true);
    private final BooleanSetting balance = new BooleanSetting("Balance", () -> this.infomation.isEnabled(), true);
    private AcrimonyFont sfpro;
    private AcrimonyFont sfproTitle;
    private ClientTheme theme;

    public HUD() {
        super("HUD", Category.VISUAL, 5.0, 5.0, 100, 200, AlignType.RIGHT);
        this.addSettings(this.showwater, this.watermode, this.showarray, this.arraymode, this.animation, this.suffix, this.important, this.alignMode, this.infomation, this.bps, this.balance);
        this.listenType = EventListenType.MANUAL;
        this.setStateHidden(true);
        this.startListening();
        this.setEnabledSilently(true);
    }

    @Override
    public void onClientStarted() {
        Acrimony.instance.getModuleManager().modules.forEach(m -> this.modules.add(new AnimationHolder<Module>((Module)m)));
        this.sfpro = Acrimony.instance.getFontManager().getSfpro();
        this.sfproTitle = Acrimony.instance.getFontManager().getSfproTitle();
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
                }
            }
            if (HUD.mc.gameSettings.showDebugInfo) {
                return;
            }
            switch (this.watermode.getMode()) {
                case "Old": {
                    this.renderOld();
                    break;
                }
                case "New": {
                    this.renderNew();
                }
            }
            if (HUD.mc.gameSettings.showDebugInfo) {
                return;
            }
            float x = 4.0f;
            float y = sr.getScaledHeight() - 13;
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
            this.modules.sort((m1, m2) -> (int)Math.round(this.getStringWidth(((Module)m1.get()).getDisplayName()) * 8.0 - (double)Math.round(this.getStringWidth(((Module)m2.get()).getDisplayName()) * 8.0)));
        } else {
            this.modules.sort((m1, m2) -> (int)Math.round(this.getStringWidth(((Module)m1.get()).getName()) * 8.0 - (double)Math.round(this.getStringWidth(((Module)m2.get()).getName()) * 8.0)));
        }
        Collections.reverse(this.modules);
        Collections.reverseOrder();
    }

    private void renderSimple() {
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
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)((double)sr.getScaledWidth() - this.getStringWidth(name) - (double)x);
            float startY = y;
            float finalLastStartX = lastStartX;
            float endX = this.alignMode.getMode() == AlignType.LEFT ? (float)((double)x + this.getStringWidth(name)) : (float)sr.getScaledWidth() - x;
            float endY = y + offsetY;
            if (Math.abs(endX - startX) > width) {
                width = Math.abs(endX - startX);
            }
            if (this.important.isEnabled() && m.getCategory() == Category.VISUAL) continue;
            holder.setAnimType(this.animation.is("Bounce") ? AnimationType.BOUNCE : AnimationType.SLIDE);
            holder.setAnimDuration(250L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            holder.render(() -> this.drawStringWithShadow(name, startX, startY + 2.0f, this.getColor((int)(startY * -17.0f))), startX - 2.5f, startY, endX, endY);
            y += offsetY * holder.getYMult();
            lastStartX = startX;
        }
        this.width = (int)width + 1;
        this.height = (int)((double)y - this.posY.getValue()) + 1;
    }

    private void renderOutline() {
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
            holder.setAnimType(this.animation.is("Bounce") ? AnimationType.BOUNCE : AnimationType.SLIDE);
            holder.setAnimDuration(300L);
            holder.updateState(m.isEnabled());
            if (holder.isAnimDone() && !holder.isRendered()) continue;
            float startX = this.alignMode.getMode() == AlignType.LEFT ? x : (float)((double)sr.getScaledWidth() - this.getStringWidth(name) - (double)x);
            float startY = y;
            float finalLastStartX = lastStartX;
            float endX = this.alignMode.getMode() == AlignType.LEFT ? (float)((double)x + this.getStringWidth(name)) : (float)sr.getScaledWidth() - x;
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
                this.drawStringWithShadow(name, startX, startY + 2.0f, this.getColor((int)(startY * -17.0f)));
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

    private void renderNew() {
        String clientName = Acrimony.instance.name;
        String formattedClientName = String.valueOf(clientName.charAt(0)) + (Object)((Object)ChatFormatting.WHITE) + clientName.substring(1, clientName.length());
        String watermark = formattedClientName + " " + Acrimony.instance.version;
        double watermarkWidth = this.getStringWidth(watermark);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        this.sfproTitle.drawStringWithShadow(watermark, x + 1.0f, y + 1.0f, this.theme.getColor(0));
        this.width = (int)(watermarkWidth + 3.0);
        this.height = 15;
    }

    private void renderOld() {
        String clientName = Acrimony.instance.name;
        String formattedClientName = String.valueOf(clientName.charAt(0)) + (Object)((Object)ChatFormatting.WHITE) + clientName.substring(1, clientName.length());
        String watermark = formattedClientName + " " + Acrimony.instance.version + " | " + mc.getDebugFPS() + "FPS | " + ServerUtil.getCurrentServer();
        double watermarkWidth = this.getStringWidth(watermark);
        float x = (float)this.posX.getValue();
        float y = (float)this.posY.getValue();
        Gui.drawRect(x + 2.0f, y + 2.0f, x + (float)((int)watermarkWidth) + 8.0f, y + 20.0f, Integer.MIN_VALUE);
        float i = x;
        while ((double)i < (double)(x + 2.0f) + watermarkWidth) {
            Gui.drawRect(i + 2.0f, y + 2.0f, i + 7.0f, y + 3.0f, this.theme.getColor((int)(i * 10.0f)));
            i += 1.0f;
        }
        this.drawStringWithShadow(watermark, x + 5.0f, y + 8.0f, this.theme.getColor(0));
        this.width = (int)(watermarkWidth + 3.0);
        this.height = 15;
    }

    public void drawString(String text, float x, float y, int color) {
        switch (this.arraymode.getMode()) {
            case "Simple": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                return;
            }
            case "Outline": {
                this.sfpro.drawString(text, x, y, color);
                return;
            }
        }
        HUD.mc.fontRendererObj.drawString(text, x, y, color);
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        switch (this.arraymode.getMode()) {
            case "Simple": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Outline": {
                this.sfpro.drawStringWithShadow(text, x, y, color);
            }
        }
        if (this.watermode.is("Old")) {
            this.sfpro.drawStringWithShadow(text, x, y, color);
        }
    }

    public double getStringWidth(String s) {
        switch (this.arraymode.getMode()) {
            case "Simple": {
                return this.sfpro.getStringWidth(s);
            }
            case "Outline": {
                return this.sfpro.getStringWidth(s);
            }
        }
        switch (this.watermode.getMode()) {
            case "New": 
            case "Old": {
                return this.sfpro.getStringWidth(s);
            }
        }
        return HUD.mc.fontRendererObj.getStringWidth(s);
    }

    public int getFontHeight() {
        switch (this.arraymode.getMode()) {
            case "Simple": {
                return this.sfpro.getHeight();
            }
            case "Outline": {
                return this.sfpro.getHeight();
            }
        }
        return HUD.mc.fontRendererObj.FONT_HEIGHT;
    }

    public int getColor(int offset) {
        return this.theme.getColor(offset);
    }
}

