/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Advapi32Util
 *  com.sun.jna.platform.win32.WinReg
 *  com.sun.jna.platform.win32.WinReg$HKEY
 */
package oshi.software.os.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.util.ArrayList;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Memory;
import oshi.hardware.Processor;
import oshi.software.os.windows.nt.CentralProcessor;
import oshi.software.os.windows.nt.GlobalMemory;

public class WindowsHardwareAbstractionLayer
implements HardwareAbstractionLayer {
    private Processor[] _processors = null;
    private Memory _memory = null;

    public Memory getMemory() {
        if (this._memory == null) {
            this._memory = new GlobalMemory();
        }
        return this._memory;
    }

    public Processor[] getProcessors() {
        if (this._processors == null) {
            String[] processorIds;
            String cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor";
            ArrayList<CentralProcessor> processors = new ArrayList<CentralProcessor>();
            for (String processorId : processorIds = Advapi32Util.registryGetKeys((WinReg.HKEY)WinReg.HKEY_LOCAL_MACHINE, (String)"HARDWARE\\DESCRIPTION\\System\\CentralProcessor")) {
                String cpuRegistryPath = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\" + processorId;
                CentralProcessor cpu = new CentralProcessor();
                cpu.setIdentifier(Advapi32Util.registryGetStringValue((WinReg.HKEY)WinReg.HKEY_LOCAL_MACHINE, (String)cpuRegistryPath, (String)"Identifier"));
                cpu.setName(Advapi32Util.registryGetStringValue((WinReg.HKEY)WinReg.HKEY_LOCAL_MACHINE, (String)cpuRegistryPath, (String)"ProcessorNameString"));
                cpu.setVendor(Advapi32Util.registryGetStringValue((WinReg.HKEY)WinReg.HKEY_LOCAL_MACHINE, (String)cpuRegistryPath, (String)"VendorIdentifier"));
                processors.add(cpu);
            }
            this._processors = processors.toArray(new Processor[0]);
        }
        return this._processors;
    }
}

