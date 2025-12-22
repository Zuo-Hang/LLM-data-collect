package com.wuxiansheng.shieldarch.marsdata.scheduler;

/**
 * 定时任务接口
 */
public interface Task {
    /**
     * 返回任务名称
     */
    String getName();

    /**
     * 执行任务逻辑
     */
    void execute() throws Exception;

    /**
     * 返回 cron 表达式
     */
    String getSchedule();
}
