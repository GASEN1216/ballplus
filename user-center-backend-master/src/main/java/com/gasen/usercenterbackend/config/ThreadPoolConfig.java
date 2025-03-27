package com.gasen.usercenterbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        // 核心线程数
        int corePoolSize = 6;
        // 最大线程数
        int maxPoolSize = 10;
        // 线程空闲时间
        long keepAliveTime = 60;
        // 时间单位
        TimeUnit unit = TimeUnit.SECONDS;
        // 任务队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(100);
        // 线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        // 拒绝策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        return new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            unit,
            workQueue,
            threadFactory,
            handler
        );
    }

    /**
     * 配置异步任务执行器
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(6);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(500);
        // 线程名前缀
        executor.setThreadNamePrefix("Async-Task-");
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        return executor;
    }
}