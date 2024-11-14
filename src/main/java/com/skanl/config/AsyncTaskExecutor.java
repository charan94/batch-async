package com.skanl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class AsyncTaskExecutor {

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
