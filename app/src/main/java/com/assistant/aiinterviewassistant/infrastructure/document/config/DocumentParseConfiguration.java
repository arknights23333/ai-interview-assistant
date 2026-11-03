package com.assistant.aiinterviewassistant.infrastructure.document.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class DocumentParseConfiguration {

    private final DocumentParseProperties p;
    @Bean(name="documentParseThreadPoolExecutor",destroyMethod="shutdown")
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(
                p.getPoolSize(),
                p.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(p.getQueueCapacity()),
                Thread.ofPlatform().name("document_parse_",1).factory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
