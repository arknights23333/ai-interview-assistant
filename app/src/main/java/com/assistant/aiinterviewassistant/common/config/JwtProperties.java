package com.assistant.aiinterviewassistant.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /** Base64 编码的 JWT 签名密钥。 */
    private String secretKey = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private String issuer;
    /** Token 有效时间，单位为秒，默认 2 小时。 */
    private long validitySeconds = 7200L;
}
