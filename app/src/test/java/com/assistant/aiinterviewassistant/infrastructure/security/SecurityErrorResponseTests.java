package com.assistant.aiinterviewassistant.infrastructure.security;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.exception.handler.GlobalExceotionHandler;
import com.assistant.aiinterviewassistant.infrastructure.security.config.SecurityConfig;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SC 过滤链与业务异常响应相互独立")
class SecurityErrorResponseTests {

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SecurityConfig.class, TestConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("未持有 Token 时仍由 EntryPoint 返回统一 401")
    void missingToken() throws Exception {
        mvc.perform(get("/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("未登录或登录状态已失效，请重新登录"));
    }

    @Test
    @DisplayName("缺少 Bearer 前缀时仍按未登录处理")
    void tokenWithoutPrefix() throws Exception {
        mvc.perform(get("/test/protected").header("Authorization", "valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("JWT 校验失败仍由 EntryPoint 处理而不是业务异常处理器")
    void invalidToken() throws Exception {
        mvc.perform(get("/test/protected").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("认证后的业务异常保留自己的 HTTP 状态和业务码")
    void authenticatedBusinessFailure() throws Exception {
        mvc.perform(get("/test/protected").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.msg").value("用户不存在"));
    }

    @Test
    @DisplayName("公开登录入口的凭证错误由业务处理器返回而非通用未登录提示")
    void publicLoginBusinessFailure() throws Exception {
        mvc.perform(post("/api/user/auth/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.msg").value("账号或密码错误"));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class TestConfig {

        @Bean
        UserDetailsService userDetailsService() {
            return username -> { throw new UsernameNotFoundException("测试不加载真实用户"); };
        }

        @Bean
        JwtDecoder jwtDecoder() {
            // 仅替换外部令牌解码依赖，过滤链和 EntryPoint 使用生产配置。
            return token -> {
                if (!"valid-token".equals(token)) {
                    throw new BadJwtException("测试用无效 Token");
                }
                return Jwt.withTokenValue(token).header("alg", "HS256")
                        .subject("1").claim("jti", "test-session").build();
            };
        }

        @Bean
        ObjectMapper objectMapper() {
            return JsonMapper.builder().build();
        }

        @Bean
        RestAuthenticationEntryPoint entryPoint(ObjectMapper objectMapper) {
            return new RestAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        GlobalExceotionHandler exceptionHandler() {
            return new GlobalExceotionHandler();
        }

        @Bean
        ErrorController errorController() {
            return new ErrorController();
        }
    }

    /**
     * 仅供 SC 过滤链响应测试使用，不同步到 Apifox。
     *
     * @ignore
     */
    @RestController
    static class ErrorController {
        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @GetMapping("/test/protected")
        public void protectedResource() {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @PostMapping("/api/user/auth/login")
        public void login() {
            throw new BusinessException(LoginErrorCode.BAD_CREDENTIAL);
        }
    }
}
