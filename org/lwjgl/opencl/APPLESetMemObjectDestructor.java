/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opencl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLMemObjectDestructorCallback;
import org.lwjgl.opencl.CallbackUtil;

public final class APPLESetMemObjectDestructor {
    private APPLESetMemObjectDestructor() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int clSetMemObjectDestructorAPPLE(CLMem memobj, CLMemObjectDestructorCallback pfn_notify) {
        long function_pointer = CLCapabilities.clSetMemObjectDestructorAPPLE;
        BufferChecks.checkFunctionAddress(function_pointer);
        long user_data = CallbackUtil.createGlobalRef(pfn_notify);
        int __result = 0;
        try {
            int n = __result = APPLESetMemObjectDestructor.nclSetMemObjectDestructorAPPLE(memobj.getPointer(), pfn_notify.getPointer(), user_data, function_pointer);
            return n;
        } finally {
            CallbackUtil.checkCallback(__result, user_data);
        }
    }

    static native int nclSetMemObjectDestructorAPPLE(long var0, long var2, long var4, long var6);
}

