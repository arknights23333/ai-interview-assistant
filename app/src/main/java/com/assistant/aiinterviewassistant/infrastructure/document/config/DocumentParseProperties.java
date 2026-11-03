package com.assistant.aiinterviewassistant.infrastructure.document.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app.document.parse")
public class DocumentParseProperties {
    @DurationMin(seconds = 1)
    private Duration timeout = Duration.ofMinutes(2);
    @Min(1)
    @Max(16)
    private int poolSize=2;
    @Min(1)
    private int queueCapacity=20;
}
