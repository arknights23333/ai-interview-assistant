package com.assistant.aiinterviewassistant.infrastructure.storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final StorageConfigProperties storageConfig;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                storageConfig.getAccessKey(),
                storageConfig.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(storageConfig.getEndpoint()))
                .region(Region.of(storageConfig.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(true)  // 关键配置：使用路径风格访问
                .build();
    }
}
