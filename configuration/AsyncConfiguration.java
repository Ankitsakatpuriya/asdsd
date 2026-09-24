package com.ing.bankguarantees.configuration;

import com.ing.apisdk.toolkit.connectivity.api.LocalAwareDelegatingExecutorService;
import io.opentelemetry.context.Context;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        return workStealingPool();
    }

    @Bean
    public ExecutorService workStealingPool() {
        return Context.taskWrapping(new LocalAwareDelegatingExecutorService(Executors.newWorkStealingPool()));
    }
}