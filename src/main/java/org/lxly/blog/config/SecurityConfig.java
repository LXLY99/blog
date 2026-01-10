package org.lxly.blog.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 引入 Slf4j 用于日志
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行 API (注意：已移除 /api/upload/**)
                        .requestMatchers(
                                "/api/user-login",
                                "/api/user-register",
                                "/api/send-verification-code",
                                "/api/reset-password",
                                "/api/settings/**",
                                "/api/post/**", // 获取详情
                                "/api/posts",   // 获取列表
                                "/api/stats",
                                "/api/server-status",
                                "/api/about"
                                // ❌ 已移除上传接口，上传必须认证！
                        ).permitAll()
                        // 2. 放行静态资源
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/*.html", "/BG/**", "/favicon.ico", "/uploads/**").permitAll()
                        // 3. 其他需认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * JWT 过滤器
     */
    @Slf4j // 需要 lombok 支持
    static class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;

        JwtAuthFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Claims claims = jwtUtil.parseToken(token);
                    Long userId = Long.valueOf(claims.getSubject());
                    Boolean isAdmin = claims.get("admin", Boolean.class);

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    if (Boolean.TRUE.equals(isAdmin)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    // ✅ 增加调试日志，便于排查 Token 为什么无效
                    log.debug("Invalid JWT Token: {}", e.getMessage());
                    // 必须清除 Context，防止污染
                    SecurityContextHolder.clearContext();
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}