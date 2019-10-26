package com.starter.demo.support.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Shoven
 * @date 2018-11-01 13:43
 */
public class IdGenerator {
    /**
     * 起始的时间戳
     */
    private static final long START_STAMP = 1508143349995L;
    /**
     * 机器标识位数
     */
    private static final long WORKER_ID_BITS = 5L;
    /**
     * 数据中心标识位数
     */
    private static final long DATACENTER_ID_BITS = 5L;
    /**
     * 毫秒内自增位数
     */
    private static final long SEQUENCE_BITS = 12L;
    /**
     * 机器ID偏左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /**
     * 数据中心ID左移17位
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /**
     * 时间毫秒左移22位
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    /**
     * sequence掩码，确保sequnce不会超出上限
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    /**
     * 上次时间戳
     */
    private long lastTimestamp = -1L;
    /**
     * 序列
     */
    private long sequence = 0L;
    /**
     * 服务器ID
     */
    private long workerId;
    /**
     * 进程编码
     */
    private long processId;

    private IdGenerator() {
        this.workerId = getMachineNum() & ~(-1L << WORKER_ID_BITS);
        this.processId = getProcessNum() & ~(-1L << DATACENTER_ID_BITS);
    }

    private static class InstanceHolder {
        private static final IdGenerator INSTANCE = new IdGenerator();
    }

    public static long get() {
        return InstanceHolder.INSTANCE.nextId();
    }

    private synchronized long nextId() {
        //获取时间戳
        long timestamp = currentTime();
        //如果时间戳小于上次时间戳则报错
        if (timestamp < lastTimestamp) {
            String cause = String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp);
            throw new RuntimeException(cause);
        }
        //如果时间戳与上次时间戳相同
        if (lastTimestamp == timestamp) {
            // 当前毫秒内，则+1，与sequenceMask确保sequence不会超出上限
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 当前毫秒内计数满了，则等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis();
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID，并返回ID
        return ((timestamp - START_STAMP) << TIMESTAMP_LEFT_SHIFT)
                | (processId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 再次获取时间戳直到获取的时间戳与现有的不同
     *
     * @return 下一个时间戳
     */
    private long waitNextMillis() {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 获取进程号编号
     *
     * @return 进程号
     */
    private long getProcessNum() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        return Long.parseLong(bean.getName().split("@")[0]);
    }

    /**
     * 获取机器号编号
     *
     * @return 机器号
     */
    private long getMachineNum() {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            sb.append(ni.toString());
        }
        return sb.toString().hashCode();
    }
}
