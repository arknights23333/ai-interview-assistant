package com.assistant.aiinterviewassistant.common.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 配置账号密码认证组件，以及基于 Bearer JWT 的接口访问规则。
 *
 * <p>登录业务调用 AuthenticationManager 校验账号密码；
 * 后续请求由 SC 原生 JWT 认证流程校验 Token，并建立当前请求的 SecurityContext。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 构建接口的 JWT 认证过滤器链。
     *
     * <p>登录入口允许匿名访问，其余接口要求已认证；
     * 认证信息只供当前请求使用，不通过 HttpSession 跨请求保存。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API 使用 Authorization 请求头主动携带 Token，关闭默认 CSRF 令牌校验。
                .csrf(AbstractHttpConfigurer::disable)
                // 每次请求根据 Token 建立 SecurityContext，不通过 HttpSession 保存和恢复身份。
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 允许容器内部的 ERROR 分派完成错误响应；普通 URL 请求仍受后续规则约束。
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // 允许未登录用户提交账号密码；账号密码校验由登录业务调用认证管理器完成。
                        .requestMatchers(HttpMethod.POST, "/api/user/auth/login").permitAll()
                        // 其他请求需要前面的 JWT 认证流程建立有效的 Authentication。
                        .anyRequest().authenticated()
                )
                // 关闭 Basic Auth，受保护接口改为接受 Bearer Token 认证。
                .httpBasic(AbstractHttpConfigurer::disable)
                // 接入 SC 原生 JWT 过滤器和认证组件，使用 JwtConfig 中提供的 JwtDecoder。
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    /**
     * 创建供登录业务调用的账号密码认证管理器。
     *
     * <p>通过 UserDetailsService 加载账号信息，使用 PasswordEncoder 校验密码；
     * Bearer Token 请求由资源服务器配置的 JWT 认证组件处理。
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    /**
     * 使用 BCrypt 生成和校验数据库中的密码摘要。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
