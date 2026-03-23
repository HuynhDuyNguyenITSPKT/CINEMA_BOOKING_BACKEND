package com.movie.cinema_booking_backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ADMIN_ENDPOINTS = {
        "/api/users"
    };

    private final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**"
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
}
