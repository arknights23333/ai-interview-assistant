package com.assistant.aiinterviewassistant.common.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    /**
     * 将配置中的 Base64 密钥转换为 HMAC-SHA256 密钥对象。
     */
    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes = Base64.getDecoder()
                .decode(properties.getSecretKey());

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * 创建 JWT 签发组件。
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * 创建 JWT 校验组件。
     */
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties properties) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        properties.getIssuer()
                )
        );

        return decoder;
    }
}
