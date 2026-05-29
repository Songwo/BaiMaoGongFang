package com.bmgf.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import java.util.concurrent.Executor;
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Bean("taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        // 重点：使用 DelegatingSecurityContextAsyncTaskExecutor 包装线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("VulnEnv-");
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
