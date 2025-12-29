package org.lxly.blog.service;

import lombok.*;
import oshi.SystemInfo;
import oshi.hardware.*;
import org.springframework.stereotype.*;
import oshi.software.os.OperatingSystem;

import java.util.*;

@Service
public class ServerStatusService {

    private final SystemInfo si = new SystemInfo();

    public Map<String, Object> getStatus() {
        HardwareAbstractionLayer hal = si.getHardware();

        // CPU
        CentralProcessor cpu = hal.getProcessor();
        long[] prevTicks = cpu.getSystemCpuLoadTicks();
        //隔一小段时间或下一次调用时
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // Memory
        GlobalMemory memory = hal.getMemory();
        double memUsed = (memory.getTotal() - memory.getAvailable()) * 100.0 / memory.getTotal();

        // Load average (1 min)
        double[] load = cpu.getSystemLoadAverage(3);
        double loadAvg = load[0]; // 第一个是 1‑minute avg

        // OS 信息
        OperatingSystem os = si.getOperatingSystem();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cpu", Map.of("usage", String.format("%.0f", cpuLoad)));
        map.put("memory", Map.of("usage", String.format("%.0f", memUsed)));
        map.put("load", Map.of("average", String.format("%.0f", loadAvg * 100)));
        map.put("system", Map.of("os", os.getFamily(), "arch", System.getProperty("os.arch")));
        return map;
    }
}
