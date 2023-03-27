package com.orangetoolztech.assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class MultithreadingConfig {

    @Bean
    public TaskExecutor taskExecutor(){
        TaskExecutor taskExecutor=new SimpleAsyncTaskExecutor();
        return taskExecutor;
    }
}
