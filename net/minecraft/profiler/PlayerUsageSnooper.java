/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.profiler;

import com.google.common.collect.Maps;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.util.HttpUtil;

public class PlayerUsageSnooper {
    private final Map<String, Object> field_152773_a = Maps.newHashMap();
    private final Map<String, Object> field_152774_b = Maps.newHashMap();
    private final String uniqueID = UUID.randomUUID().toString();
    private final URL serverUrl;
    private final IPlayerUsage playerStatsCollector;
    private final Timer threadTrigger = new Timer("Snooper Timer", true);
    private final Object syncLock = new Object();
    private final long minecraftStartTimeMilis;
    private boolean isRunning;
    private int selfCounter;

    public PlayerUsageSnooper(String p_i1563_1_, IPlayerUsage playerStatCollector, long startTime) {
        try {
            this.serverUrl = new URL("http://snoop.minecraft.net/" + p_i1563_1_ + "?version=" + 2);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException();
        }
        this.playerStatsCollector = playerStatCollector;
        this.minecraftStartTimeMilis = startTime;
    }

    public void startSnooper() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.func_152766_h();
            this.threadTrigger.schedule(new TimerTask(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    if (PlayerUsageSnooper.this.playerStatsCollector.isSnooperEnabled()) {
                        HashMap<String, Object> map;
                        Object object = PlayerUsageSnooper.this.syncLock;
                        synchronized (object) {
                            map = Maps.newHashMap(PlayerUsageSnooper.this.field_152774_b);
                            if (PlayerUsageSnooper.this.selfCounter == 0) {
                                map.putAll(PlayerUsageSnooper.this.field_152773_a);
                            }
                            map.put("snooper_count", PlayerUsageSnooper.this.selfCounter++);
                            map.put("snooper_token", PlayerUsageSnooper.this.uniqueID);
                        }
                        HttpUtil.postMap(PlayerUsageSnooper.this.serverUrl, map, true);
                    }
                }
            }, 0L, 900000L);
        }
    }

    private void func_152766_h() {
        this.addJvmArgsToSnooper();
        this.addClientStat("snooper_token", this.uniqueID);
        this.addStatToSnooper("snooper_token", this.uniqueID);
        this.addStatToSnooper("os_name", System.getProperty("os.name"));
        this.addStatToSnooper("os_version", System.getProperty("os.version"));
        this.addStatToSnooper("os_architecture", System.getProperty("os.arch"));
        this.addStatToSnooper("java_version", System.getProperty("java.version"));
        this.addClientStat("version", "1.8.9");
        this.playerStatsCollector.addServerTypeToSnooper(this);
    }

    private void addJvmArgsToSnooper() {
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
        List<String> list = runtimemxbean.getInputArguments();
        int i = 0;
        for (String s : list) {
            if (!s.startsWith("-X")) continue;
            this.addClientStat("jvm_arg[" + i++ + "]", s);
        }
        this.addClientStat("jvm_args", i);
    }

    public void addMemoryStatsToSnooper() {
        this.addStatToSnooper("memory_total", Runtime.getRuntime().totalMemory());
        this.addStatToSnooper("memory_max", Runtime.getRuntime().maxMemory());
        this.addStatToSnooper("memory_free", Runtime.getRuntime().freeMemory());
        this.addStatToSnooper("cpu_cores", Runtime.getRuntime().availableProcessors());
        this.playerStatsCollector.addServerStatsToSnooper(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addClientStat(String p_152768_1_, Object p_152768_2_) {
        Object object = this.syncLock;
        synchronized (object) {
            this.field_152774_b.put(p_152768_1_, p_152768_2_);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addStatToSnooper(String p_152767_1_, Object p_152767_2_) {
        Object object = this.syncLock;
        synchronized (object) {
            this.field_152773_a.put(p_152767_1_, p_152767_2_);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<String, String> getCurrentStats() {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        Object object = this.syncLock;
        synchronized (object) {
            this.addMemoryStatsToSnooper();
            for (Map.Entry<String, Object> entry : this.field_152773_a.entrySet()) {
                map.put(entry.getKey(), entry.getValue().toString());
            }
            for (Map.Entry<String, Object> entry1 : this.field_152774_b.entrySet()) {
                map.put(entry1.getKey(), entry1.getValue().toString());
            }
            return map;
        }
    }

    public boolean isSnooperRunning() {
        return this.isRunning;
    }

    public void stopSnooper() {
        this.threadTrigger.cancel();
    }

    public String getUniqueID() {
        return this.uniqueID;
    }

    public long getMinecraftStartTimeMillis() {
        return this.minecraftStartTimeMilis;
    }
}

