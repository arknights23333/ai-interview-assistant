package com.assistant.aiinterviewassistant.infrastructure.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageConfigProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region = "us-east-1";
}