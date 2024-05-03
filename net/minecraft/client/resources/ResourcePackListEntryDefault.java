/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.resources;

import com.google.gson.JsonParseException;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourcePackListEntryDefault
extends ResourcePackListEntry {
    private static final Logger logger = LogManager.getLogger();
    private final IResourcePack field_148320_d;
    private final ResourceLocation resourcePackIcon;

    public ResourcePackListEntryDefault(GuiScreenResourcePacks resourcePacksGUIIn) {
        super(resourcePacksGUIIn);
        DynamicTexture dynamictexture;
        this.field_148320_d = this.mc.getResourcePackRepository().rprDefaultResourcePack;
        try {
            dynamictexture = new DynamicTexture(this.field_148320_d.getPackImage());
        } catch (IOException var4) {
            dynamictexture = TextureUtil.missingTexture;
        }
        this.resourcePackIcon = this.mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamictexture);
    }

    @Override
    protected int func_183019_a() {
        return 1;
    }

    @Override
    protected String func_148311_a() {
        try {
            PackMetadataSection packmetadatasection = (PackMetadataSection)this.field_148320_d.getPackMetadata(this.mc.getResourcePackRepository().rprMetadataSerializer, "pack");
            if (packmetadatasection != null) {
                return packmetadatasection.getPackDescription().getFormattedText();
            }
        } catch (JsonParseException jsonparseexception) {
            logger.error("Couldn't load metadata info", (Throwable)jsonparseexception);
        } catch (IOException ioexception) {
            logger.error("Couldn't load metadata info", (Throwable)ioexception);
        }
        return (Object)((Object)EnumChatFormatting.RED) + "Missing pack.mcmeta :(";
    }

    @Override
    protected boolean func_148309_e() {
        return false;
    }

    @Override
    protected boolean func_148308_f() {
        return false;
    }

    @Override
    protected boolean func_148314_g() {
        return false;
    }

    @Override
    protected boolean func_148307_h() {
        return false;
    }

    @Override
    protected String func_148312_b() {
        return "Default";
    }

    @Override
    protected void func_148313_c() {
        this.mc.getTextureManager().bindTexture(this.resourcePackIcon);
    }

    @Override
    protected boolean func_148310_d() {
        return false;
    }
}

