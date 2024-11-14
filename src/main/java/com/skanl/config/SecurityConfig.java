package com.skanl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.setAllowedOrigins(List.of("*"));
        return corsConfiguration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(httpServletRequest -> buildConfig()))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/**").permitAll().anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
