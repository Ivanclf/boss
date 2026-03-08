package com.boss.bosscommon.util;

import com.boss.bosscommon.exception.ClientException;

/**
 * 雪花 ID 生成器。
 */
public class SnowFlakeGenerator {
    // 起始时间戳常量，2023-01-01 00:00:00
    private final static long START_TIMESTAMP = 1672502400L;
    
    // 各部分位数
    private final static long SEQUENCE_BIT = 12;  // 序列号占用的位数
    private final static long MACHINE_BIT = 5;    // 机器标识占用的位数
    private final static long DATA_CENTER_BIT = 5;// 数据中心标识占用的位数
    
    // 各部分最大值
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    
    // 各部分偏移量
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;
    
    private final static long dataCenterId = 1L;  // 数据中心ID，默认为1
    private final static long machineId = 1L;     // 租户ID，默认为1
    private static long sequence = 0L; // 序列号
    private static long lastTimestamp = -1L; // 上一次时间戳
    
    /**
     * 构造函数
     */
    private SnowFlakeGenerator() {
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("数据中心ID不能大于" + MAX_DATA_CENTER_NUM + "或小于0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("机器ID不能大于" + MAX_MACHINE_NUM + "或小于0");
        }
    }
    
    /**
     * 生成下一个ID
     * @return 生成的ID
     */
    public static synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();

        // 如果当前时间小于上一次ID生成时间，说明系统时钟回退过，抛出异常
        if (currentTimestamp < lastTimestamp) {
            throw new ClientException("时钟向后移动，拒绝生成ID");
        }

        // 如果是同一毫秒内生成的，则进行序列号递增。如果序列号已经达到最大值，需要等待下一毫秒
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                do {
                    currentTimestamp = System.currentTimeMillis();
                } while (currentTimestamp <= lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重新从0开始
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 检查时间戳是否有效（避免生成负数ID）
        if (currentTimestamp < START_TIMESTAMP) {
            throw new ClientException("系统时间错误，无法生成有效的ID");
        }

        // 组装ID
        long timestamp = (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT;
        long dataCenterIdShift = dataCenterId << DATA_CENTER_LEFT;
        long machineIdShift = machineId << MACHINE_LEFT;

        return timestamp | dataCenterIdShift | machineIdShift | sequence;
    }
}
