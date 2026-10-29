package com.assistant.aiinterviewassistant.infrastructure.security.service.impl;

import com.assistant.aiinterviewassistant.infrastructure.security.config.JwtProperties;
import com.assistant.aiinterviewassistant.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties properties;
    @Override
    public String generateToken(Long userId, String sessionId) {
        return generateToken(userId, sessionId, properties.getValiditySeconds());
    }
    public String generateToken(Long userId, String sessionId,long validitySeconds) {
        if (validitySeconds <= 0) {
            throw new IllegalArgumentException("Token 有效期必须大于 0");
        }
        Instant Now =Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .issuedAt(Now)
                .expiresAt(Now.plusSeconds(validitySeconds))
                .subject(userId.toString())
                .id(sessionId)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);


        Jwt jwt = jwtEncoder.encode(parameters);

        String token = jwt.getTokenValue();
        return token;
    }
}
