package com.ing.bankguarantees.configuration.testsupport;

import com.ing.apisdk.toolkit.connectivity.api.LocalAwareDelegatingExecutorService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TestConfiguration
@EnableAsync
public class ExecutorConfig {

    @Bean
    public static ExecutorService workStealingPool() {
        return new LocalAwareDelegatingExecutorService(Executors.newWorkStealingPool());
    }

}