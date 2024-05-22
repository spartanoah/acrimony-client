/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.click.dropdown;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.visual.ClickGuiModule;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.AbstractSetting;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ColorSetting;
import Acrimony.setting.impl.CustomDoubleSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.EnumModeSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.ui.click.dropdown.holder.CategoryHolder;
import Acrimony.ui.click.dropdown.holder.ModuleHolder;
import Acrimony.ui.click.dropdown.holder.SettingHolder;
import Acrimony.util.misc.LogUtil;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.render.DrawUtil;
import Acrimony.util.render.FontUtil;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class DropdownClickGUI
extends GuiScreen {
    private final ClickGuiModule module;
    private AcrimonyFont icons;
    private ClientTheme theme;
    private final ArrayList<CategoryHolder> categories = new ArrayList();
    private final int categoryXOffset = 120;
    private final int categoryYOffset = 18;
    private final int moduleYOffset = 18;
    private final int settingYOffset = 18;
    private final Color moduleDisabledColor = new Color(47, 47, 47, 255);
    private final Color boolSettingEnabledColor = new Color(225, 225, 225);
    private final Color boolSettingBox = new Color(51, 51, 51, 255);
    private final int mouseHoverColor = 0x30000000;
    private int lastMouseX;
    private int lastMouseY;
    private CustomDoubleSetting customDSetting;
    private String customDSettingText;
    private final TimerUtil cursorTimer = new TimerUtil();
    private boolean deleteKeyPressed;
    private final TimerUtil deleteTimer = new TimerUtil();
    private final TimerUtil deleteSpeedTimer = new TimerUtil();
    private boolean changingKeybind;
    private Module keyChangeModule;
    public float alp;
    private int scrollY;
    private float targetScrollY = 0.0f;

    public DropdownClickGUI(ClickGuiModule module) {
        this.module = module;
        int x = 20;
        int y = 20;
        for (Category category : Category.values()) {
            ArrayList<ModuleHolder> modules = new ArrayList<ModuleHolder>();
            Acrimony.instance.getModuleManager().modules.stream().filter(m -> m.getCategory() == category).sorted(Comparator.comparing(Module::getName)).forEach(m -> modules.add(new ModuleHolder((Module)m)));
            this.categories.add(new CategoryHolder(category, modules, x, y, true));
            x += 128;
        }
    }

    @Override
    public void initGui() {
        this.categories.forEach(c -> c.getModules().forEach(m -> m.updateState()));
        this.scrollY = 0;
        this.targetScrollY = 0.0f;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float offsetYDelta = (this.targetScrollY - (float)this.scrollY) * 0.1f;
        this.scrollY += (int)offsetYDelta;
        GL11.glTranslatef(0.0f, this.scrollY, 0.0f);
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
        AcrimonyFont fr = Acrimony.instance.getFontManager().getSfpro();
        AcrimonyFont frs = Acrimony.instance.getFontManager().getSfprosmall();
        AcrimonyFont frr = Acrimony.instance.getFontManager().getSfprobold22();
        this.icons = Acrimony.instance.getFontManager().getIconregular();
        for (CategoryHolder category : this.categories) {
            if (!category.isShown()) continue;
            if (category.isHolded()) {
                category.setX(category.getX() + mouseX - this.lastMouseX);
                category.setY(category.getY() + mouseY - this.lastMouseY);
            }
            int x = category.getX();
            int y = category.getY();
            DrawUtil.drawGradientSideways(x - 2, y, x + 120 + 2, y + 18, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
            Gui.drawRect(x - 2, y, x + 120 + 2, y + 18, 0);
            String categoryName = category.getCategory().toString().toLowerCase();
            float categorywidth = fr.getStringWidth(categoryName);
            float categoryX = (float)x + (120.0f - categorywidth) / 2.0f;
            frr.drawStringWithShadow(categoryName, categoryX - 5.0f, y + 5, -1);
            if (category.getCategory() == Category.COMBAT) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("c", x + 4, y + 6, -1);
            }
            if (category.getCategory() == Category.MOVEMENT) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("f", x + 4, y + 6, -1);
            }
            if (category.getCategory() == Category.PLAYER) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("e", x + 5, y + 6, -1);
            }
            if (category.getCategory() == Category.EXPLOIT) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("s", x + 4, y + 6, -1);
            }
            if (category.getCategory() == Category.VISUAL) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("d", x + 4, y + 6, -1);
            }
            if (category.getCategory() == Category.GHOST) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("a", x + 4, y + 6, -1);
            }
            if (category.getCategory() == Category.CONFIG) {
                DrawUtil.drawOutlinedRoundedRect((double)x + 0.5, (double)y + 1.5, 18.0, 14.5, 14.0, 4.0f, 0x20000000);
                this.icons.drawStringWithShadow("b", x + 4, y + 6, -1);
            }
            float startX = x;
            float endX = startX + 120.0f;
            y += 18;
            boolean firstModule = true;
            boolean firstModuleEnabled = false;
            for (ModuleHolder holder : category.getModules()) {
                Module m = holder.getModule();
                float startY = y;
                float endY = startY + 18.0f;
                if (this.module.boxOnHover.isEnabled() && !m.isEnabled() && (float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startY && (float)mouseY < endY) {
                    Gui.drawRect(startX, startY, endX, endY, 0x30000000);
                }
                double mult = (double)holder.getTimer().getTimeElapsed() / 200.0;
                DrawUtil.drawGradientSideways(startX - 2.0f, startY, endX + 2.0f, endY + 2.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                Gui.drawRect(startX - 1.0f, startY, endX + 1.0f, endY + 1.0f, this.moduleDisabledColor.getRGB());
                if (m.isEnabled()) {
                    DrawUtil.drawGradientSideways(startX - 1.0f, startY, startX + 120.0f + 1.0f, startY + 18.0f + 1.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                }
                String moduleName = m.getName();
                float textWidth = fr.getStringWidth(moduleName);
                float textHeight = FontUtil.getFontHeight(Acrimony.instance.getFontManager().getSfpro().toString());
                float centerX = startX + (120.0f - textWidth) / 2.0f;
                float centerY = startY + (18.0f - textHeight) / 2.0f;
                fr.drawStringWithShadow(moduleName, (double)(centerX - 2.0f), (double)centerY + 1.5, new Color(255, 255, 255).getRGB());
                y += 18;
                if (firstModule && (m.isEnabled() || mult < 1.0)) {
                    firstModuleEnabled = true;
                }
                if (holder.isSettingsShown()) {
                    float startKeybindY = y;
                    float endKeybindY = y + 18;
                    DrawUtil.drawGradientSideways(startX - 2.0f, startKeybindY, endX + 2.0f, endKeybindY + 1.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                    Gui.drawRect(startX - 1.0f, startKeybindY, endX + 1.0f, endKeybindY, new Color(38, 38, 38, 255).getRGB());
                    if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && (float)mouseX > startX + 1.0f && (float)mouseX < endX - 1.0f && (float)mouseY > startKeybindY && (float)mouseY < endKeybindY) {
                        Gui.drawRect(startX, startKeybindY, endX, endKeybindY, 0x30000000);
                    }
                    frs.drawStringWithShadow(this.keyChangeModule == m ? "Waiting..." : "Keybind : " + Keyboard.getKeyName(m.getKey()), startX + 18.0f, startKeybindY + 6.0f, -1);
                    this.icons.drawStringWithShadow("t", startX + 3.0f, startKeybindY + 6.0f, -1);
                    y += 18;
                    for (SettingHolder settingHolder : holder.getSettings()) {
                        String toRender;
                        AbstractSetting setting;
                        boolean hoveringSetting;
                        if (!((AbstractSetting)settingHolder.getSetting()).getVisibility().get().booleanValue()) continue;
                        float startSettingY = y;
                        float endSettingY = y + 18;
                        DrawUtil.drawGradientSideways(startX - 2.0f, startSettingY, endX + 2.0f, endSettingY + 2.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        Gui.drawRect(startX - 1.0f, startSettingY, endX + 1.0f, endSettingY + 1.0f, this.moduleDisabledColor.getRGB());
                        boolean bl = hoveringSetting = (float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startSettingY && (float)mouseY < endSettingY;
                        if (settingHolder.getSetting() instanceof ModeSetting) {
                            setting = (ModeSetting)settingHolder.getSetting();
                            toRender = setting.getName() + " : " + ((ModeSetting)setting).getMode();
                            Gui.drawRect(startX, endSettingY, endX, endSettingY, this.moduleDisabledColor.getRGB());
                            if (frs.getStringWidth(toRender) > 117) {
                                if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && (float)mouseX > startX + 1.0f && (float)mouseX < endX - 1.0f && (float)mouseY > startSettingY && (float)mouseY < endSettingY + 6.0f) {
                                    Gui.drawRect(startX, startSettingY, endX, endSettingY + 6.0f, 0x30000000);
                                }
                                frs.drawStringWithShadow(setting.getName() + " :", startX + 3.0f, startSettingY + 4.5f, -1);
                                frs.drawStringWithShadow(((ModeSetting)setting).getMode(), startX + 3.0f, startSettingY + 15.0f, -1);
                                y += 6;
                            } else {
                                Gui.drawRect(startX, startSettingY, startX + 120.0f, startSettingY + 18.0f - 2.0f, 0x50000000);
                                if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && hoveringSetting) {
                                    Gui.drawRect(startX, startSettingY, endX, endSettingY, 0x30000000);
                                }
                                frs.drawStringWithShadow(toRender, startX + 3.0f, startSettingY + 5.0f, -1);
                            }
                        } else {
                            double numberX;
                            double thing;
                            double mousePos;
                            float length;
                            if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && hoveringSetting) {
                                Gui.drawRect(startX, startSettingY, endX, endSettingY, 0x30000000);
                            }
                            if (settingHolder.getSetting() instanceof BooleanSetting) {
                                setting = (BooleanSetting)settingHolder.getSetting();
                                Gui.drawRect(startX + 2.0f, startSettingY + 2.0f, startX + 6.0f, startSettingY + 18.0f - 4.0f, Integer.MIN_VALUE);
                                frs.drawStringWithShadow(setting.getName(), startX + 10.0f, startSettingY + 5.0f, -1);
                                DrawUtil.drawOutlinedRoundedRect(endX - 15.0f, startSettingY + 2.0f, 11.0, 11.0, 8.0, 1.0f, -1);
                                DrawUtil.drawOutlinedRoundedRect(endX - 15.0f, startSettingY + 2.0f, 11.0, 11.0, 8.0, 1.0f, -1);
                                if (((BooleanSetting)setting).isEnabled()) {
                                    this.icons.drawStringWithShadow("o", endX - 15.0f, startSettingY + 5.0f, -1);
                                }
                            } else if (settingHolder.getSetting() instanceof EnumModeSetting) {
                                setting = (EnumModeSetting)settingHolder.getSetting();
                                frs.drawStringWithShadow(setting.getName() + " : " + ((Enum)((EnumModeSetting)setting).getMode()).name(), startX + 3.0f, startSettingY + 3.0f, -1);
                            } else if (settingHolder.getSetting() instanceof DoubleSetting) {
                                setting = (DoubleSetting)settingHolder.getSetting();
                                float startSettingX = startX + 1.0f;
                                float endSettingX = endX - 1.0f;
                                length = endSettingX - startSettingX;
                                if (settingHolder.isHoldingMouse() && mouseX >= x && (float)mouseX <= startSettingX + endSettingX && (float)mouseY > startSettingY && (float)mouseY < endSettingY) {
                                    mousePos = (float)mouseX - startSettingX;
                                    thing = mousePos / (double)length;
                                    ((DoubleSetting)setting).setValue(thing * (((DoubleSetting)setting).getMax() - ((DoubleSetting)setting).getMin()) + ((DoubleSetting)setting).getMin());
                                }
                                numberX = (((DoubleSetting)setting).getValue() - ((DoubleSetting)setting).getMin()) * (double)length / (((DoubleSetting)setting).getMax() - ((DoubleSetting)setting).getMin());
                                Gui.drawRect(startSettingX, startSettingY + 12.0f, endSettingX, startSettingY + 14.0f, Integer.MIN_VALUE);
                                DrawUtil.drawGradientSideways(startSettingX, startSettingY + 12.0f, (double)startSettingX + numberX, startSettingY + 14.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                                DrawUtil.drawRoundedRect((double)startSettingX + numberX - 4.0, startSettingY + 11.0f, (double)startSettingX + numberX, startSettingY + 15.0f, 4.0, ClientTheme.color2.getRGB());
                                frs.drawStringWithShadow(setting.getName() + " : " + ((DoubleSetting)setting).getStringValue(), startSettingX + 2.0f, startSettingY + 3.0f, -1);
                            } else if (settingHolder.getSetting() instanceof IntegerSetting) {
                                setting = (IntegerSetting)settingHolder.getSetting();
                                float startSettingX = startX + 1.0f;
                                float endSettingX = endX - 1.0f;
                                length = endSettingX - startSettingX;
                                if (settingHolder.isHoldingMouse() && mouseX >= x && (float)mouseX <= startSettingX + endSettingX && (float)mouseY > startSettingY && (float)mouseY < endSettingY) {
                                    mousePos = (float)mouseX - startSettingX;
                                    thing = mousePos / (double)length;
                                    int value = (int)(thing * (double)(((IntegerSetting)setting).getMax() - ((IntegerSetting)setting).getMin()) + (double)((IntegerSetting)setting).getMin());
                                    ((IntegerSetting)setting).setValue(value);
                                }
                                numberX = (float)(((IntegerSetting)setting).getValue() - ((IntegerSetting)setting).getMin()) * length / (float)(((IntegerSetting)setting).getMax() - ((IntegerSetting)setting).getMin());
                                Gui.drawRect(startSettingX, startSettingY + 12.0f, endSettingX, startSettingY + 14.0f, Integer.MIN_VALUE);
                                DrawUtil.drawGradientSideways(startSettingX, startSettingY + 12.0f, (double)startSettingX + numberX, startSettingY + 14.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                                DrawUtil.drawRoundedRect((double)startSettingX + numberX - 4.0, startSettingY + 11.0f, (double)startSettingX + numberX, startSettingY + 15.0f, 4.0, ClientTheme.color2.getRGB());
                                DrawUtil.drawRoundedRect((double)startSettingX + numberX - 4.0, startSettingY + 11.0f, (double)startSettingX + numberX, startSettingY + 15.0f, 4.0, ClientTheme.color2.getRGB());
                                frs.drawStringWithShadow(setting.getName() + " : " + ((IntegerSetting)setting).getValue(), (double)(startSettingX + 2.0f), (double)startSettingY + 2.8, -1);
                            } else if (settingHolder.getSetting() instanceof CustomDoubleSetting) {
                                CustomDoubleSetting doubleSetting = (CustomDoubleSetting)settingHolder.getSetting();
                                float startSettingX = startX + 1.0f;
                                String aaa = doubleSetting.getDisplayName() + " : " + (this.customDSetting == doubleSetting ? this.customDSettingText : Double.valueOf(doubleSetting.getValue()));
                                frs.drawStringWithShadow(aaa, startSettingX + 2.0f, startSettingY + 6.0f, -1);
                                this.deleteKeyPressed = Keyboard.isKeyDown(14);
                                if (this.deleteKeyPressed) {
                                    if (this.deleteTimer.getTimeElapsed() >= 600L && this.deleteSpeedTimer.getTimeElapsed() >= 30L) {
                                        this.deleteCustomDoubleCharacter();
                                        this.deleteSpeedTimer.reset();
                                    }
                                } else {
                                    this.deleteTimer.reset();
                                }
                                if (this.cursorTimer.getTimeElapsed() % 1200L < 600L && this.customDSetting == doubleSetting) {
                                    Gui.drawRect(startSettingX + (float)fr.getStringWidth(aaa) + 2.0f, startSettingY + 3.0f, startSettingX + (float)fr.getStringWidth(aaa) + 3.0f, startSettingY + 14.5f, this.theme.getColor((int)(endX - 12.0f + (float)y)));
                                }
                            } else if (settingHolder.getSetting() instanceof ColorSetting) {
                                setting = (ColorSetting)settingHolder.getSetting();
                                toRender = setting.getName() + " : " + ((ColorSetting)setting).getMode();
                                Gui.drawRect(startX, endSettingY, endX, endSettingY, this.moduleDisabledColor.getRGB());
                                if (frs.getStringWidth(toRender) > 117) {
                                    if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && (float)mouseX > startX + 1.0f && (float)mouseX < endX - 1.0f && (float)mouseY > startSettingY && (float)mouseY < endSettingY + 6.0f) {
                                        Gui.drawRect(startX, startSettingY, endX, endSettingY + 6.0f, 0x30000000);
                                    }
                                    frs.drawStringWithShadow(setting.getName() + " :", startX + 3.0f, startSettingY + 4.5f, -1);
                                    frs.drawStringWithShadow(((ColorSetting)setting).getMode(), startX + 3.0f, startSettingY + 15.0f, -1);
                                    y += 6;
                                } else {
                                    Gui.drawRect(startX, startSettingY, startX + 120.0f, startSettingY + 18.0f - 2.0f, 0x50000000);
                                    if (this.module.boxOnHover.isEnabled() && this.module.boxOnSettings.isEnabled() && hoveringSetting) {
                                        Gui.drawRect(startX, startSettingY, endX, endSettingY, 0x30000000);
                                    }
                                    DrawUtil.drawGradientSideways(endX - 40.0f, startSettingY + 2.0f, endX - 1.0f, startSettingY + 7.0f, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                                    DrawUtil.drawGradientSideways(endX - 40.0f, startSettingY + 9.0f, endX - 1.0f, startSettingY + 14.0f, this.theme.getColor((int)(endX - 12.0f + (float)y)), this.theme.getColor((int)(endX - 12.0f + (float)y)));
                                    frs.drawStringWithShadow(toRender, startX + 3.0f, startSettingY + 5.0f, -1);
                                }
                            }
                        }
                        y += 18;
                    }
                }
                firstModule = false;
            }
        }
        GL11.glTranslatef(0.0f, -this.scrollY, 0.0f);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        AcrimonyFont fr = Acrimony.instance.getFontManager().getSfpro();
        AcrimonyFont frs = Acrimony.instance.getFontManager().getSfprosmall();
        this.icons = Acrimony.instance.getFontManager().getIcon();
        for (CategoryHolder category : this.categories) {
            if (!category.isShown()) continue;
            int x = category.getX();
            int y = category.getY() + this.scrollY;
            if (mouseX > x && mouseX < x + 120 && mouseY > y && mouseY < y + 18) {
                category.setHolded(true);
            }
            float startX = x;
            float endX = startX + 120.0f;
            y += 18;
            for (ModuleHolder holder : category.getModules()) {
                Module m = holder.getModule();
                float startY = y;
                float endY = startY + 18.0f;
                if ((float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startY && (float)mouseY < endY) {
                    if (button == 0) {
                        m.toggle();
                        holder.updateState();
                    } else if (button == 1) {
                        holder.setSettingsShown(!holder.isSettingsShown());
                    }
                }
                y += 18;
                if (!holder.isSettingsShown()) continue;
                float startKeybindY = y;
                float endKeybindY = y + 18;
                if (button == 0 && (float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startKeybindY && (float)mouseY < endKeybindY) {
                    this.keyChangeModule = m;
                }
                y += 18;
                for (SettingHolder settingHolder : holder.getSettings()) {
                    String toRender;
                    AbstractSetting setting;
                    boolean hovering;
                    float endSettingY;
                    if (!((AbstractSetting)settingHolder.getSetting()).getVisibility().get().booleanValue()) continue;
                    float startSettingY = y;
                    float realEndSettingY = endSettingY = (float)(y + 18);
                    boolean bl = hovering = (float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startSettingY && (float)mouseY < realEndSettingY;
                    if (settingHolder.getSetting() instanceof BooleanSetting) {
                        setting = (BooleanSetting)settingHolder.getSetting();
                        if (button == 0 && hovering) {
                            ((BooleanSetting)setting).setEnabled(!((BooleanSetting)setting).isEnabled());
                        }
                    } else if (settingHolder.getSetting() instanceof ModeSetting) {
                        setting = (ModeSetting)settingHolder.getSetting();
                        toRender = setting.getName() + " : " + ((ModeSetting)setting).getMode();
                        if (frs.getStringWidth(toRender) > 117) {
                            if ((float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startSettingY && (float)mouseY < endSettingY + 6.0f) {
                                realEndSettingY = endSettingY + 6.0f;
                                if (button == 0) {
                                    ((ModeSetting)setting).increment();
                                } else if (button == 1) {
                                    ((ModeSetting)setting).decrement();
                                }
                            }
                            y += 6;
                        } else if (hovering) {
                            if (button == 0) {
                                ((ModeSetting)setting).increment();
                            } else if (button == 1) {
                                ((ModeSetting)setting).decrement();
                            }
                        }
                    } else if (settingHolder.getSetting() instanceof EnumModeSetting) {
                        setting = (EnumModeSetting)settingHolder.getSetting();
                        if (hovering) {
                            if (button == 0) {
                                ((EnumModeSetting)setting).increment();
                            } else if (button == 1) {
                                ((EnumModeSetting)setting).decrement();
                            }
                        }
                    } else if (settingHolder.getSetting() instanceof CustomDoubleSetting) {
                        CustomDoubleSetting doubleSetting = (CustomDoubleSetting)settingHolder.getSetting();
                        float endSettingX = startX + 100.0f;
                        int length = frs.getStringWidth(doubleSetting.getDisplayName() + " : " + (this.customDSetting == doubleSetting ? this.customDSettingText : Double.valueOf(doubleSetting.getValue())));
                        if ((float)mouseX >= startX && (float)mouseX <= startX + Math.max(endSettingX, (float)(length + 20)) && (float)mouseY > startSettingY && (float)mouseY < endSettingY) {
                            LogUtil.addChatMessage("Setting name : " + doubleSetting.getName());
                            if (this.customDSetting != doubleSetting) {
                                if (this.customDSetting != null) {
                                    try {
                                        this.customDSetting.setValue(Double.parseDouble(this.customDSettingText));
                                    } catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                                this.customDSetting = doubleSetting;
                                this.customDSettingText = "" + doubleSetting.getValue();
                                this.cursorTimer.reset();
                                this.deleteTimer.reset();
                                this.deleteSpeedTimer.reset();
                                this.deleteKeyPressed = Keyboard.isKeyDown(14);
                            }
                        } else if (this.customDSetting == doubleSetting) {
                            this.setCustomDoubleSetting();
                        }
                    } else if (settingHolder.getSetting() instanceof ColorSetting) {
                        setting = (ColorSetting)settingHolder.getSetting();
                        toRender = setting.getName() + " : " + ((ColorSetting)setting).getMode();
                        if (frs.getStringWidth(toRender) > 117) {
                            if ((float)mouseX > startX && (float)mouseX < endX && (float)mouseY > startSettingY && (float)mouseY < endSettingY + 6.0f) {
                                realEndSettingY = endSettingY + 6.0f;
                                if (button == 0) {
                                    ((ColorSetting)setting).increment();
                                } else if (button == 1) {
                                    ((ColorSetting)setting).decrement();
                                }
                            }
                            y += 6;
                        } else if (hovering) {
                            if (button == 0) {
                                ((ColorSetting)setting).increment();
                            } else if (button == 1) {
                                ((ColorSetting)setting).decrement();
                            }
                        }
                    }
                    if (hovering && button == 0) {
                        settingHolder.setHoldingMouse(true);
                    }
                    y += 18;
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.categories.forEach(c -> {
            c.setHolded(false);
            c.getModules().forEach(m -> m.getSettings().forEach(s -> s.setHoldingMouse(false)));
        });
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Integer.signum(Mouse.getEventDWheel());
        this.targetScrollY -= (float)(i * 20);
        this.targetScrollY = Math.max(-200.0f, Math.min(200.0f, this.targetScrollY));
    }

    @Override
    protected void keyTyped(char typedChar, int key) throws IOException {
        if (key == 1) {
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
            this.setCustomDoubleSetting();
        } else if (this.customDSetting != null) {
            if (key == 28) {
                this.setCustomDoubleSetting();
            } else if (key == 14) {
                this.deleteCustomDoubleCharacter();
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                this.customDSettingText = this.customDSettingText + typedChar;
            }
        }
        if (this.keyChangeModule != null) {
            this.keyChangeModule.setKey(key == 14 ? 0 : key);
            this.keyChangeModule = null;
        }
    }

    private void setCustomDoubleSetting() {
        if (this.customDSetting != null) {
            try {
                this.customDSetting.setValue(Double.parseDouble(this.customDSettingText));
            } catch (Exception exception) {
                // empty catch block
            }
        }
        this.customDSetting = null;
        this.customDSettingText = "";
        this.cursorTimer.reset();
        this.deleteTimer.reset();
        this.deleteKeyPressed = false;
    }

    private void deleteCustomDoubleCharacter() {
        this.customDSettingText = this.customDSettingText.length() >= 2 ? this.customDSettingText.substring(0, this.customDSettingText.length() - 1) : "";
        this.cursorTimer.reset();
        this.deleteTimer.reset();
        this.deleteSpeedTimer.reset();
    }

    @Override
    public void onGuiClosed() {
        this.module.setEnabled(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

