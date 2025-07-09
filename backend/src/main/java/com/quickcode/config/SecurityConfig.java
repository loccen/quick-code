package com.quickcode.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.quickcode.security.handler.CustomAccessDeniedHandler;
import com.quickcode.security.jwt.JwtAuthenticationEntryPoint;
import com.quickcode.security.jwt.JwtAuthenticationFilter;
import com.quickcode.security.service.CustomUserDetailsService;

/**
 * Spring Security配置类 配置认证、授权、CORS等安全相关设置
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfig(CustomUserDetailsService userDetailsService,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      CustomAccessDeniedHandler accessDeniedHandler,
      JwtAuthenticationFilter jwtAuthenticationFilter, PasswordEncoder passwordEncoder) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * 配置安全过滤器链
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // 禁用CSRF（使用JWT不需要CSRF保护）
        .csrf(AbstractHttpConfigurer::disable)

        // 配置CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 配置会话管理（无状态）
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 配置异常处理
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))

        // 配置请求授权
        .authorizeHttpRequests(authz -> authz
            // 公开接口
            .requestMatchers("/api/auth/**").permitAll().requestMatchers("/api/public/**")
            .permitAll()

            // 健康检查和监控
            .requestMatchers("/actuator/health").permitAll().requestMatchers("/actuator/**")
            .hasRole("ADMIN")

            // API文档
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // 静态资源
            .requestMatchers("/favicon.ico", "/error").permitAll()

            // 其他所有请求都需要认证
            .anyRequest().authenticated())

        // 添加JWT过滤器
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * 认证管理器
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * 认证提供者
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  /**
   * CORS配置
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 允许的源
    configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", // 用户端前端
        "http://localhost:3001", // 管理后台前端
        "http://127.0.0.1:3000", "http://127.0.0.1:3001"));

    // 允许的HTTP方法
    configuration
        .setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // 允许的请求头
    configuration
        .setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));

    // 暴露的响应头
    configuration.setExposedHeaders(
        Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

    // 允许凭证
    configuration.setAllowCredentials(true);

    // 预检请求缓存时间
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
