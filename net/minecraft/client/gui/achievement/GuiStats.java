/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui.achievement;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class GuiStats
extends GuiScreen
implements IProgressMeter {
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private StatsGeneral generalStats;
    private StatsItem itemStats;
    private StatsBlock blockStats;
    private StatsMobsList mobStats;
    private StatFileWriter field_146546_t;
    private GuiSlot displaySlot;
    private boolean doesGuiPauseGame = true;

    public GuiStats(GuiScreen p_i1071_1_, StatFileWriter p_i1071_2_) {
        this.parentScreen = p_i1071_1_;
        this.field_146546_t = p_i1071_2_;
    }

    @Override
    public void initGui() {
        this.screenTitle = I18n.format("gui.stats", new Object[0]);
        this.doesGuiPauseGame = true;
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.displaySlot != null) {
            this.displaySlot.handleMouseInput();
        }
    }

    public void func_175366_f() {
        this.generalStats = new StatsGeneral(this.mc);
        this.generalStats.registerScrollButtons(1, 1);
        this.itemStats = new StatsItem(this.mc);
        this.itemStats.registerScrollButtons(1, 1);
        this.blockStats = new StatsBlock(this.mc);
        this.blockStats.registerScrollButtons(1, 1);
        this.mobStats = new StatsMobsList(this.mc);
        this.mobStats.registerScrollButtons(1, 1);
    }

    public void createButtons() {
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 160, this.height - 52, 80, 20, I18n.format("stat.generalButton", new Object[0])));
        GuiButton guibutton = new GuiButton(2, this.width / 2 - 80, this.height - 52, 80, 20, I18n.format("stat.blocksButton", new Object[0]));
        this.buttonList.add(guibutton);
        GuiButton guibutton1 = new GuiButton(3, this.width / 2, this.height - 52, 80, 20, I18n.format("stat.itemsButton", new Object[0]));
        this.buttonList.add(guibutton1);
        GuiButton guibutton2 = new GuiButton(4, this.width / 2 + 80, this.height - 52, 80, 20, I18n.format("stat.mobsButton", new Object[0]));
        this.buttonList.add(guibutton2);
        if (this.blockStats.getSize() == 0) {
            guibutton.enabled = false;
        }
        if (this.itemStats.getSize() == 0) {
            guibutton1.enabled = false;
        }
        if (this.mobStats.getSize() == 0) {
            guibutton2.enabled = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (button.id == 1) {
                this.displaySlot = this.generalStats;
            } else if (button.id == 3) {
                this.displaySlot = this.itemStats;
            } else if (button.id == 2) {
                this.displaySlot = this.blockStats;
            } else if (button.id == 4) {
                this.displaySlot = this.mobStats;
            } else {
                this.displaySlot.actionPerformed(button);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.doesGuiPauseGame) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats", new Object[0]), this.width / 2, this.height / 2, 0xFFFFFF);
            this.drawCenteredString(this.fontRendererObj, lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % (long)lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 0xFFFFFF);
        } else {
            this.displaySlot.drawScreen(mouseX, mouseY, partialTicks);
            this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void doneLoading() {
        if (this.doesGuiPauseGame) {
            this.func_175366_f();
            this.createButtons();
            this.displaySlot = this.generalStats;
            this.doesGuiPauseGame = false;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return !this.doesGuiPauseGame;
    }

    private void drawStatsScreen(int p_146521_1_, int p_146521_2_, Item p_146521_3_) {
        this.drawButtonBackground(p_146521_1_ + 1, p_146521_2_ + 1);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemIntoGUI(new ItemStack(p_146521_3_, 1, 0), p_146521_1_ + 2, p_146521_2_ + 2);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void drawButtonBackground(int p_146531_1_, int p_146531_2_) {
        this.drawSprite(p_146531_1_, p_146531_2_, 0, 0);
    }

    private void drawSprite(int p_146527_1_, int p_146527_2_, int p_146527_3_, int p_146527_4_) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(statIcons);
        float f = 0.0078125f;
        float f1 = 0.0078125f;
        int i = 18;
        int j = 18;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b(p_146527_1_ + 0, p_146527_2_ + 18, this.zLevel).func_181673_a((float)(p_146527_3_ + 0) * 0.0078125f, (float)(p_146527_4_ + 18) * 0.0078125f).func_181675_d();
        worldrenderer.func_181662_b(p_146527_1_ + 18, p_146527_2_ + 18, this.zLevel).func_181673_a((float)(p_146527_3_ + 18) * 0.0078125f, (float)(p_146527_4_ + 18) * 0.0078125f).func_181675_d();
        worldrenderer.func_181662_b(p_146527_1_ + 18, p_146527_2_ + 0, this.zLevel).func_181673_a((float)(p_146527_3_ + 18) * 0.0078125f, (float)(p_146527_4_ + 0) * 0.0078125f).func_181675_d();
        worldrenderer.func_181662_b(p_146527_1_ + 0, p_146527_2_ + 0, this.zLevel).func_181673_a((float)(p_146527_3_ + 0) * 0.0078125f, (float)(p_146527_4_ + 0) * 0.0078125f).func_181675_d();
        tessellator.draw();
    }

    class StatsGeneral
    extends GuiSlot {
        public StatsGeneral(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 10);
            this.setShowSelectionBox(false);
        }

        @Override
        protected int getSize() {
            return StatList.generalStats.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
        }

        @Override
        protected int getContentHeight() {
            return this.getSize() * 10;
        }

        @Override
        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatBase statbase = StatList.generalStats.get(entryID);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, statbase.getStatName().getUnformattedText(), p_180791_2_ + 2, p_180791_3_ + 1, entryID % 2 == 0 ? 0xFFFFFF : 0x909090);
            String s = statbase.format(GuiStats.this.field_146546_t.readStat(statbase));
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_180791_2_ + 2 + 213 - GuiStats.this.fontRendererObj.getStringWidth(s), p_180791_3_ + 1, entryID % 2 == 0 ? 0xFFFFFF : 0x909090);
        }
    }

    class StatsItem
    extends Stats {
        public StatsItem(Minecraft mcIn) {
            super(mcIn);
            this.statsHolder = Lists.newArrayList();
            for (StatCrafting statcrafting : StatList.itemStats) {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());
                if (GuiStats.this.field_146546_t.readStat(statcrafting) > 0) {
                    flag = true;
                } else if (StatList.objectBreakStats[i] != null && GuiStats.this.field_146546_t.readStat(StatList.objectBreakStats[i]) > 0) {
                    flag = true;
                } else if (StatList.objectCraftStats[i] != null && GuiStats.this.field_146546_t.readStat(StatList.objectCraftStats[i]) > 0) {
                    flag = true;
                }
                if (!flag) continue;
                this.statsHolder.add(statcrafting);
            }
            this.statSorter = new Comparator<StatCrafting>(){

                @Override
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_) {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;
                    if (StatsItem.this.field_148217_o == 0) {
                        statbase = StatList.objectBreakStats[j];
                        statbase1 = StatList.objectBreakStats[k];
                    } else if (StatsItem.this.field_148217_o == 1) {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    } else if (StatsItem.this.field_148217_o == 2) {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }
                    if (statbase != null || statbase1 != null) {
                        int i1;
                        if (statbase == null) {
                            return 1;
                        }
                        if (statbase1 == null) {
                            return -1;
                        }
                        int l = GuiStats.this.field_146546_t.readStat(statbase);
                        if (l != (i1 = GuiStats.this.field_146546_t.readStat(statbase1))) {
                            return (l - i1) * StatsItem.this.field_148215_p;
                        }
                    }
                    return j - k;
                }
            };
        }

        @Override
        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);
            if (this.field_148218_l == 0) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 72, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 72, 18);
            }
            if (this.field_148218_l == 1) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 18, 18);
            }
            if (this.field_148218_l == 2) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 36, 18);
            }
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatCrafting statcrafting = this.func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            GuiStats.this.drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            this.func_148209_a(StatList.objectBreakStats[i], p_180791_2_ + 115, p_180791_3_, entryID % 2 == 0);
            this.func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 165, p_180791_3_, entryID % 2 == 0);
            this.func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, entryID % 2 == 0);
        }

        @Override
        protected String func_148210_b(int p_148210_1_) {
            return p_148210_1_ == 1 ? "stat.crafted" : (p_148210_1_ == 2 ? "stat.used" : "stat.depleted");
        }
    }

    class StatsBlock
    extends Stats {
        public StatsBlock(Minecraft mcIn) {
            super(mcIn);
            this.statsHolder = Lists.newArrayList();
            for (StatCrafting statcrafting : StatList.objectMineStats) {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());
                if (GuiStats.this.field_146546_t.readStat(statcrafting) > 0) {
                    flag = true;
                } else if (StatList.objectUseStats[i] != null && GuiStats.this.field_146546_t.readStat(StatList.objectUseStats[i]) > 0) {
                    flag = true;
                } else if (StatList.objectCraftStats[i] != null && GuiStats.this.field_146546_t.readStat(StatList.objectCraftStats[i]) > 0) {
                    flag = true;
                }
                if (!flag) continue;
                this.statsHolder.add(statcrafting);
            }
            this.statSorter = new Comparator<StatCrafting>(){

                @Override
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_) {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;
                    if (StatsBlock.this.field_148217_o == 2) {
                        statbase = StatList.mineBlockStatArray[j];
                        statbase1 = StatList.mineBlockStatArray[k];
                    } else if (StatsBlock.this.field_148217_o == 0) {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    } else if (StatsBlock.this.field_148217_o == 1) {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }
                    if (statbase != null || statbase1 != null) {
                        int i1;
                        if (statbase == null) {
                            return 1;
                        }
                        if (statbase1 == null) {
                            return -1;
                        }
                        int l = GuiStats.this.field_146546_t.readStat(statbase);
                        if (l != (i1 = GuiStats.this.field_146546_t.readStat(statbase1))) {
                            return (l - i1) * StatsBlock.this.field_148215_p;
                        }
                    }
                    return j - k;
                }
            };
        }

        @Override
        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);
            if (this.field_148218_l == 0) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 18, 18);
            }
            if (this.field_148218_l == 1) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 36, 18);
            }
            if (this.field_148218_l == 2) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 54, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 54, 18);
            }
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatCrafting statcrafting = this.func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            GuiStats.this.drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            this.func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 115, p_180791_3_, entryID % 2 == 0);
            this.func_148209_a(StatList.objectUseStats[i], p_180791_2_ + 165, p_180791_3_, entryID % 2 == 0);
            this.func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, entryID % 2 == 0);
        }

        @Override
        protected String func_148210_b(int p_148210_1_) {
            return p_148210_1_ == 0 ? "stat.crafted" : (p_148210_1_ == 1 ? "stat.used" : "stat.mined");
        }
    }

    class StatsMobsList
    extends GuiSlot {
        private final List<EntityList.EntityEggInfo> field_148222_l;

        public StatsMobsList(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, ((GuiStats)GuiStats.this).fontRendererObj.FONT_HEIGHT * 4);
            this.field_148222_l = Lists.newArrayList();
            this.setShowSelectionBox(false);
            for (EntityList.EntityEggInfo entitylist$entityegginfo : EntityList.entityEggs.values()) {
                if (GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151512_d) <= 0 && GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151513_e) <= 0) continue;
                this.field_148222_l.add(entitylist$entityegginfo);
            }
        }

        @Override
        protected int getSize() {
            return this.field_148222_l.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
        }

        @Override
        protected int getContentHeight() {
            return this.getSize() * ((GuiStats)GuiStats.this).fontRendererObj.FONT_HEIGHT * 4;
        }

        @Override
        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            EntityList.EntityEggInfo entitylist$entityegginfo = this.field_148222_l.get(entryID);
            String s = I18n.format("entity." + EntityList.getStringFromID(entitylist$entityegginfo.spawnedID) + ".name", new Object[0]);
            int i = GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151512_d);
            int j = GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151513_e);
            String s1 = I18n.format("stat.entityKills", i, s);
            String s2 = I18n.format("stat.entityKilledBy", s, j);
            if (i == 0) {
                s1 = I18n.format("stat.entityKills.none", s);
            }
            if (j == 0) {
                s2 = I18n.format("stat.entityKilledBy.none", s);
            }
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_180791_2_ + 2 - 10, p_180791_3_ + 1, 0xFFFFFF);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s1, p_180791_2_ + 2, p_180791_3_ + 1 + ((GuiStats)GuiStats.this).fontRendererObj.FONT_HEIGHT, i == 0 ? 0x606060 : 0x909090);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s2, p_180791_2_ + 2, p_180791_3_ + 1 + ((GuiStats)GuiStats.this).fontRendererObj.FONT_HEIGHT * 2, j == 0 ? 0x606060 : 0x909090);
        }
    }

    abstract class Stats
    extends GuiSlot {
        protected int field_148218_l;
        protected List<StatCrafting> statsHolder;
        protected Comparator<StatCrafting> statSorter;
        protected int field_148217_o;
        protected int field_148215_p;

        protected Stats(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 20);
            this.field_148218_l = -1;
            this.field_148217_o = -1;
            this.setShowSelectionBox(false);
            this.setHasListHeader(true, 20);
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
        }

        @Override
        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        @Override
        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            if (!Mouse.isButtonDown(0)) {
                this.field_148218_l = -1;
            }
            if (this.field_148218_l == 0) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 18);
            }
            if (this.field_148218_l == 1) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 18);
            }
            if (this.field_148218_l == 2) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 18);
            }
            if (this.field_148217_o != -1) {
                int i = 79;
                int j = 18;
                if (this.field_148217_o == 1) {
                    i = 129;
                } else if (this.field_148217_o == 2) {
                    i = 179;
                }
                if (this.field_148215_p == 1) {
                    j = 36;
                }
                GuiStats.this.drawSprite(p_148129_1_ + i, p_148129_2_ + 1, j, 0);
            }
        }

        @Override
        protected void func_148132_a(int p_148132_1_, int p_148132_2_) {
            this.field_148218_l = -1;
            if (p_148132_1_ >= 79 && p_148132_1_ < 115) {
                this.field_148218_l = 0;
            } else if (p_148132_1_ >= 129 && p_148132_1_ < 165) {
                this.field_148218_l = 1;
            } else if (p_148132_1_ >= 179 && p_148132_1_ < 215) {
                this.field_148218_l = 2;
            }
            if (this.field_148218_l >= 0) {
                this.func_148212_h(this.field_148218_l);
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0f));
            }
        }

        @Override
        protected final int getSize() {
            return this.statsHolder.size();
        }

        protected final StatCrafting func_148211_c(int p_148211_1_) {
            return this.statsHolder.get(p_148211_1_);
        }

        protected abstract String func_148210_b(int var1);

        protected void func_148209_a(StatBase p_148209_1_, int p_148209_2_, int p_148209_3_, boolean p_148209_4_) {
            if (p_148209_1_ != null) {
                String s = p_148209_1_.format(GuiStats.this.field_146546_t.readStat(p_148209_1_));
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_148209_2_ - GuiStats.this.fontRendererObj.getStringWidth(s), p_148209_3_ + 5, p_148209_4_ ? 0xFFFFFF : 0x909090);
            } else {
                String s1 = "-";
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, s1, p_148209_2_ - GuiStats.this.fontRendererObj.getStringWidth(s1), p_148209_3_ + 5, p_148209_4_ ? 0xFFFFFF : 0x909090);
            }
        }

        @Override
        protected void func_148142_b(int p_148142_1_, int p_148142_2_) {
            if (p_148142_2_ >= this.top && p_148142_2_ <= this.bottom) {
                int i = this.getSlotIndexFromScreenCoords(p_148142_1_, p_148142_2_);
                int j = this.width / 2 - 92 - 16;
                if (i >= 0) {
                    if (p_148142_1_ < j + 40 || p_148142_1_ > j + 40 + 20) {
                        return;
                    }
                    StatCrafting statcrafting = this.func_148211_c(i);
                    this.func_148213_a(statcrafting, p_148142_1_, p_148142_2_);
                } else {
                    String s = "";
                    if (p_148142_1_ >= j + 115 - 18 && p_148142_1_ <= j + 115) {
                        s = this.func_148210_b(0);
                    } else if (p_148142_1_ >= j + 165 - 18 && p_148142_1_ <= j + 165) {
                        s = this.func_148210_b(1);
                    } else {
                        if (p_148142_1_ < j + 215 - 18 || p_148142_1_ > j + 215) {
                            return;
                        }
                        s = this.func_148210_b(2);
                    }
                    s = ("" + I18n.format(s, new Object[0])).trim();
                    if (s.length() > 0) {
                        int k = p_148142_1_ + 12;
                        int l = p_148142_2_ - 12;
                        int i1 = GuiStats.this.fontRendererObj.getStringWidth(s);
                        GuiStats.this.drawGradientRect(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
                        GuiStats.this.fontRendererObj.drawStringWithShadow(s, k, l, -1);
                    }
                }
            }
        }

        protected void func_148213_a(StatCrafting p_148213_1_, int p_148213_2_, int p_148213_3_) {
            if (p_148213_1_ != null) {
                Item item = p_148213_1_.func_150959_a();
                ItemStack itemstack = new ItemStack(item);
                String s = itemstack.getUnlocalizedName();
                String s1 = ("" + I18n.format(s + ".name", new Object[0])).trim();
                if (s1.length() > 0) {
                    int i = p_148213_2_ + 12;
                    int j = p_148213_3_ - 12;
                    int k = GuiStats.this.fontRendererObj.getStringWidth(s1);
                    GuiStats.this.drawGradientRect(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
                    GuiStats.this.fontRendererObj.drawStringWithShadow(s1, i, j, -1);
                }
            }
        }

        protected void func_148212_h(int p_148212_1_) {
            if (p_148212_1_ != this.field_148217_o) {
                this.field_148217_o = p_148212_1_;
                this.field_148215_p = -1;
            } else if (this.field_148215_p == -1) {
                this.field_148215_p = 1;
            } else {
                this.field_148217_o = -1;
                this.field_148215_p = 0;
            }
            Collections.sort(this.statsHolder, this.statSorter);
        }
    }
}

