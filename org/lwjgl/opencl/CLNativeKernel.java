/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opencl;

import java.nio.ByteBuffer;
import org.lwjgl.PointerWrapperAbstract;
import org.lwjgl.opencl.CallbackUtil;

public abstract class CLNativeKernel
extends PointerWrapperAbstract {
    protected CLNativeKernel() {
        super(CallbackUtil.getNativeKernelCallback());
    }

    protected abstract void execute(ByteBuffer[] var1);
}

