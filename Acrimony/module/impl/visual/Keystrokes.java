/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
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
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Keyboard;

public class Keystrokes
extends HUDModule {
    private final ModeSetting font = FontUtil.getFontSetting();
    private final BooleanSetting bloom = new BooleanSetting("Bloom", false);
    private final BooleanSetting blur = new BooleanSetting("Blur", false);
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    private final ArrayList<AnimationHolder<KeyBinding>> keys = new ArrayList();
    private boolean keysInitiated;
    private ClientTheme theme;

    public Keystrokes() {
        super("Keystrokes", Category.VISUAL, 15.0, 35.0, 70, 70, AlignType.LEFT);
        this.addSettings(this.font, this.bloom, this.blur);
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
            int length = 20;
            int spacing = 5;
            int color1 = this.theme.getColor(0);
            int color2 = new Color(255, 255, 255).getRGB();
            Color c = ColorUtil.interpolateColorsBackAndForth(7, 3 + length * 20, color1, color2, false);
            int currentX = x + length + spacing;
            int currentY = y;
            for (AnimationHolder<KeyBinding> key : this.keys) {
                key.setAnimType(AnimationType.POP);
                key.setAnimDuration(150L);
                if (index == 4) {
                    currentX = x;
                    currentY = y + (length + spacing) * 2;
                    int totalLength = length * 3 + spacing * 2;
                    DrawUtil.drawRoundedRect(currentX, currentY, currentX + totalLength, currentY + length, 6.0, Integer.MIN_VALUE);
                    int finalRenderX = currentX;
                    int finalRenderY = currentY;
                    key.updateState(key.get().isKeyDown());
                    DrawUtil.drawRoundedRect(currentX + totalLength - 64, currentY + 8, currentX + totalLength - 6, currentY + 12, 2.0, new Color(246, 246, 246).getRGB());
                    if (!key.isRendered() && key.isAnimDone()) continue;
                    key.render(() -> DrawUtil.drawRoundedRect(finalRenderX, finalRenderY, finalRenderX + totalLength, finalRenderY + length, 6.0, this.theme.getColor(1)), finalRenderX, finalRenderY, finalRenderX + totalLength, (float)finalRenderY + (float)length * 0.85f);
                    continue;
                }
                if (index == 1) {
                    currentX = x;
                    currentY = y + length + spacing;
                }
                DrawUtil.drawRoundedRect(currentX, currentY, currentX + length, currentY + length, 6.0, Integer.MIN_VALUE);
                int finalRenderX = currentX;
                int finalRenderY = currentY;
                key.updateState(key.get().isKeyDown());
                if (key.isRendered() || !key.isAnimDone()) {
                    key.render(() -> DrawUtil.drawRoundedRect(finalRenderX, finalRenderY, finalRenderX + length, finalRenderY + length, 6.0, this.theme.getColor(1)), finalRenderX, finalRenderY, finalRenderX + length, finalRenderY + length);
                }
                String keyName = Keyboard.getKeyName(key.get().getKeyCode());
                FontUtil.drawStringWithShadow(this.font.getMode(), keyName, (float)((double)(currentX + length / 2) - FontUtil.getStringWidth(this.font.getMode(), keyName) / 2.0), currentY + 7, -1);
                currentX += length + spacing;
                ++index;
            }
        }
    }
}

