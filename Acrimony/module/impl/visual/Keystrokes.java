/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.HUDModule;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.animation.AnimationHolder;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.render.ColorUtil;
import Acrimony.util.render.DrawUtil;
import Acrimony.util.render.FontUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Keystrokes
extends HUDModule {
    private final ModeSetting mode = new ModeSetting("Mode", "Future", "Future", "Blur", "Jello");
    private final BooleanSetting blurbg = new BooleanSetting("Blur Background", () -> this.mode.is("Future"), false);
    private final ModeSetting font = FontUtil.getFontSetting();
    private final ArrayList<AnimationHolder<KeyBinding>> keys = new ArrayList();
    private boolean keysInitiated;
    private AcrimonyFont jellolight;
    private ClientTheme theme;

    public Keystrokes() {
        super("Keystrokes", Category.VISUAL, 15.0, 35.0, 70, 70, AlignType.LEFT);
        this.addSettings(this.mode, this.blurbg, this.font);
    }

    @Override
    public void onEnable() {
        if (!this.keysInitiated) {
            this.initKeys();
            this.keysInitiated = true;
        }
    }

    @Override
    public void onClientStarted() {
        this.jellolight = Acrimony.instance.getFontManager().getJellolight();
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
    }

    private void initKeys() {
        this.keys.clear();
        this.keys.add(new AnimationHolder<KeyBinding>(Keystrokes.mc.gameSettings.keyBindForward));
        this.keys.add(new AnimationHolder<KeyBinding>(Keystrokes.mc.gameSettings.keyBindLeft));
        this.keys.add(new AnimationHolder<KeyBinding>(Keystrokes.mc.gameSettings.keyBindBack));
        this.keys.add(new AnimationHolder<KeyBinding>(Keystrokes.mc.gameSettings.keyBindRight));
        this.keys.add(new AnimationHolder<KeyBinding>(Keystrokes.mc.gameSettings.keyBindJump));
    }

    @Override
    public void renderModule(boolean inChat) {
        if (this.isEnabled()) {
            int x = (int)this.posX.getValue();
            int y = (int)this.posY.getValue();
            if (!this.keysInitiated) {
                this.initKeys();
                this.keysInitiated = true;
            }
            int index = 0;
            int length = 22;
            int spacing = 1;
            int color1 = this.theme.getColor(0);
            int color2 = new Color(255, 255, 255).getRGB();
            Color c = ColorUtil.interpolateColorsBackAndForth(7, 3 + length * 20, color1, color2, false);
            int currentX = x + length + spacing;
            int currentY = y;
            block10: for (AnimationHolder<KeyBinding> key : this.keys) {
                key.setAnimType(AnimationType.POP);
                key.setAnimDuration(150L);
                switch (this.mode.getMode()) {
                    case "Future": {
                        int finalRenderX;
                        int totalLength;
                        if (index == 4) {
                            currentX = x;
                            currentY = y + (length + spacing) * 2;
                            totalLength = length * 3 + spacing * 2;
                            if (this.blurbg.isEnabled()) {
                                Acrimony.instance.blurHandler.blur((double)currentX, (double)currentY, (double)totalLength, (double)length, 0.0f);
                            }
                            DrawUtil.drawRoundedRect(currentX, currentY, currentX + totalLength, currentY + length, 6.0, Integer.MIN_VALUE);
                            finalRenderX = currentX;
                            int finalRenderY = currentY;
                            key.updateState(key.get().isKeyDown());
                            DrawUtil.drawRoundedRect(currentX + totalLength - 62, currentY + 10, currentX + totalLength - 6, currentY + 14, 2.0, new Color(246, 246, 246).getRGB());
                            if (!key.isRendered() && key.isAnimDone()) continue block10;
                            key.render(() -> DrawUtil.drawRoundedRect(finalRenderX, finalRenderY, finalRenderX + totalLength, finalRenderY + length, 6.0, this.theme.getColor(1)), finalRenderX, finalRenderY, finalRenderX + totalLength, (float)finalRenderY + (float)length * 0.85f);
                            break;
                        }
                        if (index == 1) {
                            currentX = x;
                            currentY = y + length + spacing;
                        }
                        if (this.blurbg.isEnabled()) {
                            Acrimony.instance.blurHandler.blur((double)currentX, (double)currentY, (double)length, (double)length, 0.0f);
                        }
                        DrawUtil.drawRoundedRect(currentX, currentY, currentX + length, currentY + length, 6.0, Integer.MIN_VALUE);
                        int finalRenderX2 = currentX;
                        int finalRenderY = currentY;
                        key.updateState(key.get().isKeyDown());
                        if (key.isRendered() || !key.isAnimDone()) {
                            key.render(() -> DrawUtil.drawRoundedRect(finalRenderX2, finalRenderY, finalRenderX2 + length, finalRenderY + length, 6.0, this.theme.getColor(1)), finalRenderX2, finalRenderY, finalRenderX2 + length, finalRenderY + length);
                        }
                        String keyName = Keyboard.getKeyName(key.get().getKeyCode());
                        FontUtil.drawStringWithShadow(this.font.getMode(), keyName, (float)((double)(currentX + length / 2) - FontUtil.getStringWidth(this.font.getMode(), keyName) / 2.0), currentY + 8, -1);
                        currentX += length + spacing;
                        ++index;
                        break;
                    }
                    case "Blur": {
                        int finalRenderX;
                        int totalLength;
                        if (index == 4) {
                            currentX = x;
                            currentY = y + (length + spacing) * 2;
                            totalLength = length * 3 + spacing * 2;
                            Acrimony.instance.blurHandler.bloom(currentX, currentY, totalLength, length, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
                            finalRenderX = currentX;
                            int finalRenderY = currentY;
                            key.updateState(key.get().isKeyDown());
                            DrawUtil.drawRoundedRect(currentX + totalLength - 62, currentY + 10, currentX + totalLength - 6, currentY + 14, 2.0, new Color(246, 246, 246).getRGB());
                            if (!key.isRendered() && key.isAnimDone()) continue block10;
                            key.render(() -> Acrimony.instance.blurHandler.bloom(finalRenderX, finalRenderY, totalLength, length, ClientTheme.blurradius.getValue(), this.theme.getColor(1)), finalRenderX, finalRenderY, finalRenderX + totalLength, (float)finalRenderY + (float)length * 0.85f);
                            break;
                        }
                        if (index == 1) {
                            currentX = x;
                            currentY = y + length + spacing;
                        }
                        Acrimony.instance.blurHandler.bloom(currentX, currentY, length, length, ClientTheme.blurradius.getValue(), new Color(0, 0, 0, 150));
                        int finalRenderX2 = currentX;
                        int finalRenderY = currentY;
                        key.updateState(key.get().isKeyDown());
                        if (key.isRendered() || !key.isAnimDone()) {
                            key.render(() -> Acrimony.instance.blurHandler.bloom(finalRenderX2, finalRenderY, length, length, ClientTheme.blurradius.getValue(), this.theme.getColor(1)), finalRenderX2, finalRenderY, finalRenderX2 + length, finalRenderY + length);
                        }
                        String keyName = Keyboard.getKeyName(key.get().getKeyCode());
                        FontUtil.drawStringWithShadow(this.font.getMode(), keyName, (float)((double)(currentX + length / 2) - FontUtil.getStringWidth(this.font.getMode(), keyName) / 2.0), currentY + 7, -1);
                        currentX += length + spacing;
                        ++index;
                        break;
                    }
                    case "Jello": {
                        if (index == 4) break;
                        if (index == 1) {
                            currentX = x;
                            currentY = y + length + spacing;
                        }
                        Acrimony.instance.blurHandler.bloom(currentX, currentY, length, length, 20, new Color(0, 0, 0, 150));
                        DrawUtil.drawRoundedRect(currentX, currentY, currentX + length, currentY + length, 1.0, 0x60000000);
                        DrawUtil.drawRoundedRect(currentX, currentY, currentX, currentY, 0.0, new Color(253, 253, 253, 250).getRGB());
                        int finalRenderX2 = currentX;
                        int finalRenderY = currentY;
                        key.updateState(key.get().isKeyDown());
                        if (key.isRendered() || !key.isAnimDone()) {
                            key.render(() -> DrawUtil.drawRoundedRect(finalRenderX2, finalRenderY, finalRenderX2 + length, finalRenderY + length, 1.0, new Color(253, 253, 253, 80).getRGB()), finalRenderX2, finalRenderY, finalRenderX2 + length, finalRenderY + length);
                            DrawUtil.drawRoundedRect(currentX, currentY, currentX, currentY, 0.0, new Color(253, 253, 253, 250).getRGB());
                        }
                        String keyName = Keyboard.getKeyName(key.get().getKeyCode());
                        this.jellolight.drawString(keyName, currentX + length / 2 - this.jellolight.getStringWidth(keyName) / 2, currentY + 8, new Color(253, 253, 253, 250).getRGB());
                        currentX += length + spacing;
                        ++index;
                    }
                }
            }
        }
    }
}

