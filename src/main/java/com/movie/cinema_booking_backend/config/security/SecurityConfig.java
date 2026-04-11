package com.movie.cinema_booking_backend.config.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    /**
     * Endpoints chỉ dành cho ADMIN — tài khoản user bình thường không có quyền truy cập.
     */
    private static final String[] ADMIN_ENDPOINTS = {
        "/api/users",
        "/api/users/**",
        "/api/admin/promotions",
        "/api/admin/promotions/**",
        "/api/admin/uploads",
        "/api/admin/uploads/**",
        "/api/admin/extra-services",
        "/api/admin/extra-services/**",
        "/api/admin/payment/**",
        "/api/admin/movies",
        "/api/admin/movies/**",
        "/api/admin/genres",
        "/api/admin/genres/**",
        "/api/admin/showtimes",
        "/api/admin/showtimes/**",
        "/api/admin/reviews",
        "/api/admin/reviews/**"
    };

    /**
     * Endpoints công khai — không yêu cầu xác thực (permit all).
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**", "/api/extra-services/**", "/api/payment/**", "/api/public/cinema/**",
        "/api/genres",
        "/google4213d9ec89513d14.html", "/robots.txt", "/sitemap.xml", "/favicon.ico"
    };


    private static final String[] USER_ENDPOINTS = {
        "/api/users/profile", "/api/users/me/**"
    };

    private final CustomJwtDecoder customJwtDecoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.customJwtDecoder = customJwtDecoder;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(USER_ENDPOINTS).hasAnyRole("USER", "ADMIN")
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .cors(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(customJwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .exceptionHandling( e -> e
                .accessDeniedHandler(customAccessDeniedHandler)
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthorityPrefix("ROLE_");
        gac.setAuthoritiesClaimName("scope");
        JwtAuthenticationConverter jac = new JwtAuthenticationConverter();
        jac.setJwtGrantedAuthoritiesConverter(gac);
        return jac;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl)); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
}
