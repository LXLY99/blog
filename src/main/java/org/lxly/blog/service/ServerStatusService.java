package org.lxly.blog.service;

import oshi.SystemInfo;
import oshi.hardware.*;
import org.springframework.stereotype.*;
import oshi.software.os.OperatingSystem;

import java.util.*;

@Service
public class ServerStatusService {

    private final SystemInfo si = new SystemInfo();
    private volatile long[] prevCpuTicks;

    public Map<String, Object> getStatus() {
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        // 1. CPU Load
        long[] ticks = cpu.getSystemCpuLoadTicks();
        Double cpuLoad = 0.0; // Default to 0.0 instead of null
        if (prevCpuTicks != null) {
            cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevCpuTicks) * 100.0;
        }
        prevCpuTicks = ticks;

        // 2. Memory Usage
        GlobalMemory memory = hal.getMemory();
        long total = memory.getTotal();
        double memUsed = total > 0
                ? (total - memory.getAvailable()) * 100.0 / total
                : 0.0;

        // 3. Load Average (Fixing the NPE here)
        double[] load = cpu.getSystemLoadAverage(3);
        // Map.of() throws NPE if value is null. We use 0.0 as a safe default.
        double load1m = (load.length > 0 && load[0] >= 0) ? load[0] : 0.0;

        OperatingSystem os = si.getOperatingSystem();

        Map<String, Object> map = new LinkedHashMap<>();
        // Use Math.round to make it cleaner, and ensure no nulls passed to Map.of
        map.put("cpu", Map.of("usage", Math.round(cpuLoad)));
        map.put("memory", Map.of("usage", Math.round(memUsed)));
        map.put("load", Map.of("average", load1m));
        map.put("system", Map.of("os", os.getFamily(), "arch", System.getProperty("os.arch")));
        return map;
    }
}