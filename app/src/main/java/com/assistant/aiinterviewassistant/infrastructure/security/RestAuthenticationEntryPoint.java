package com.assistant.aiinterviewassistant.infrastructure.security;

import com.assistant.aiinterviewassistant.common.response.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 设置真正的 HTTP 状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 设置字符编码和响应内容类型
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 将统一响应对象转换为 JSON，并写入响应体
        objectMapper.writeValue(
                response.getOutputStream(),
                R.fail(401, "未登录或登录状态已失效，请重新登录")
        );




    }
}
