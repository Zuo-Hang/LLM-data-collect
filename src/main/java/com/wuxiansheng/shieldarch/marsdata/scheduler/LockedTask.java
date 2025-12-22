package com.wuxiansheng.shieldarch.marsdata.scheduler;

import java.time.Duration;

/**
 * 需要分布式锁的定时任务接口（可选）
 */
public interface LockedTask extends Task {

    /**
     * 返回分布式锁的键名
     */
    String getLockKey();

    /**
     * 返回分布式锁的过期时间
     */
    Duration getLockTTL();
}


