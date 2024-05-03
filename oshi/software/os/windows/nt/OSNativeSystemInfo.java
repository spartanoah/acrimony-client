/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Kernel32
 *  com.sun.jna.platform.win32.WinBase$SYSTEM_INFO
 *  com.sun.jna.platform.win32.WinNT$HANDLE
 */
package oshi.software.os.windows.nt;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public class OSNativeSystemInfo {
    private WinBase.SYSTEM_INFO _si = null;

    public OSNativeSystemInfo() {
        WinBase.SYSTEM_INFO si = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(si);
        try {
            IntByReference isWow64 = new IntByReference();
            WinNT.HANDLE hProcess = Kernel32.INSTANCE.GetCurrentProcess();
            if (Kernel32.INSTANCE.IsWow64Process(hProcess, isWow64) && isWow64.getValue() > 0) {
                Kernel32.INSTANCE.GetNativeSystemInfo(si);
            }
        } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            // empty catch block
        }
        this._si = si;
    }

    public OSNativeSystemInfo(WinBase.SYSTEM_INFO si) {
        this._si = si;
    }

    public int getNumberOfProcessors() {
        return this._si.dwNumberOfProcessors.intValue();
    }
}

