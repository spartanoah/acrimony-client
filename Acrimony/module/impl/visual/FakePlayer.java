/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.module.Category;
import Acrimony.module.Module;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.entity.EntityOtherPlayerMP;

public class FakePlayer
extends Module {
    public FakePlayer() {
        super("FakePlayer", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(FakePlayer.mc.theWorld, new GameProfile(UUID.fromString("4f7700aa-93d0-4c6a-b58a-d99b1c7287fd"), mc.getSession().getUsername()));
        fakePlayer.copyLocationAndAnglesFrom(FakePlayer.mc.thePlayer);
        FakePlayer.mc.theWorld.addEntityToWorld(69420, fakePlayer);
    }

    @Override
    public void onDisable() {
        FakePlayer.mc.theWorld.removeEntityFromWorld(69420);
    }
}

