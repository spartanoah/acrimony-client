/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.vialoadingbase.platform.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import net.vialoadingbase.ViaLoadingBase;
import net.vialoadingbase.provider.VLBBaseVersionProvider;

public class VLBViaProviders
implements ViaPlatformLoader {
    @Override
    public void load() {
        ViaProviders providers = Via.getManager().getProviders();
        providers.use(VersionProvider.class, new VLBBaseVersionProvider());
        if (ViaLoadingBase.getInstance().getProviders() != null) {
            ViaLoadingBase.getInstance().getProviders().accept(providers);
        }
    }

    @Override
    public void unload() {
    }
}

