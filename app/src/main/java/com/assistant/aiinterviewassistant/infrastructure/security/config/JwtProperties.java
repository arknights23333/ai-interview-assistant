package com.assistant.aiinterviewassistant.infrastructure.security.config;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /** Base64 编码的 JWT 签名密钥。 */
    private String secretKey = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private String issuer;
    /** Token 有效时间，单位为秒，默认 2 小时。 */
    @Positive(message = "Token 有效期必须大于 0")
    private long validitySeconds = 7200L;

}
