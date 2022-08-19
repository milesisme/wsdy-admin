package com.wsdy.saasops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;


@Configuration
@EnableAsync
public class TheadPoolExecutor {

    /***线程池维护线程的最少数量**/
    private final int minSize = 0;
    /***允许的空闲时间**/
    private final int aliveSeconds = 300;
    /***线程池维护线程的最大数量**/
    private final int maxSize = 6;
    /***缓存队列**/
    private final int queueCapacity = 6000;

    @Bean
    public Executor eventAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, 10, queueCapacity);
    }

    @Bean
    public Executor getPayResultExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, 10, queueCapacity);
    }

    @Bean
    public Executor dispatcherTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 6, queueCapacity * 4);
    }

    @Bean
    public Executor waterActivityTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 2, queueCapacity);
    }

    @Bean
    public Executor sanGongRebateTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 7, queueCapacity);
    }

    @Bean
    public Executor accountRebateCastTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, 10, queueCapacity * 4);
    }

    @Bean
    public Executor smsAlarmTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 7, queueCapacity);
    }

    @Bean
    public Executor vipRedActivityTaskAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, 10, queueCapacity * 4);
    }

    @Bean
    public Executor batchAccountTransferExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, 5, queueCapacity);
    }

    @Bean
    public Executor commissionCastAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 2, queueCapacity * 4);
    }

    @Bean
    public Executor depthCalculationAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 2, queueCapacity);
    }
    @Bean
    public Executor depthCalculationMonthAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 3, queueCapacity);
    }

    @Bean
    public Executor verifyFundDepositAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 2, queueCapacity*8);
    }

    @Bean
    public Executor updateRptBetRcdDayCostAsyncExecutor() {
        return getThreadPoolTaskExecutor(minSize, aliveSeconds, maxSize * 2, queueCapacity*5);
    }

    @Bean
    public ThreadPoolExecutor retentionRateDailyActiveExecutor() {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                this.minSize + 10,
                this.maxSize * 20,
                aliveSeconds,
                TimeUnit.SECONDS,
                queue,
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor(int minSize, int aliveSeconds, int maxSize, int queueCapacity) {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(minSize);//线程池维护线程的最少数量
        pool.setKeepAliveSeconds(aliveSeconds);//允许的空闲时间
        pool.setMaxPoolSize(maxSize);//线程池维护线程的最大数量
        pool.setQueueCapacity(queueCapacity);//缓存队列
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }

    @Bean
    public Executor auditCastExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(minSize);//线程池维护线程的最少数量
        pool.setKeepAliveSeconds(aliveSeconds);//允许的空闲时间
        pool.setMaxPoolSize(10);//线程池维护线程的最大数量
        pool.setQueueCapacity(queueCapacity);//缓存队列
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//线程调用运行该任务的 execute 本身。此策略提供简单的反馈控制机制，能够减缓新任务的提交速度
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }
}
