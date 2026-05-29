package com.bmgf.Config;
import com.bmgf.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // CORS 允许的源由配置注入，移除硬编码与第三方测试站点（如 hoppscotch.io）
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 禁用CSRF（因使用JWT）
                .csrf(AbstractHttpConfigurer::disable)

                // 3. 会话管理（无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 授权配置
                .authorizeHttpRequests(auth -> auth
                        // 公开路径
                        .requestMatchers(
                                "/user/login",
                                "/user/register",
                                "/admin/login",
                                "/error",
                                "/avatar/**",
                                "/images/**",
                                "/video/**",
                                "/upload/**",
                                "/post/findAll",
                                "/post/findBySection",
                                "/post/findById",
                                "/post/search",
                                "/post/hot",
                                "/comments/find/**",
                                "/user/findAllHole",
                                "/bug/findById/**",
                                "/bug/search",
                                "/api/news/**",

                                "/api/public/**",
                                "/ws/**",  // 放行 WebSocket路径
                                "/actuator/health/**",  // 放行健康探针（细节按授权显示）
                                "/favicon.ico",
                                "/lab/create-compose",// 放行 favicon 请求
                                "vip/create-compose"


                        ).permitAll()

                        // 特定权限要求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 5. 用户详情服务
                .userDetailsService(userDetailsService)

                // 6. 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    // CORS配置（保持原样）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
                "Accept", "X-User-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
