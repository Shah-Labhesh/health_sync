package com.fyp.health_sync.config;

import com.fyp.health_sync.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthenticationFilter filter;

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/swagger-ui/index.html"),
                                new AntPathRequestMatcher("/context-path/**"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/v3/api-docs"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/api/v1/auth/**"),
                                new AntPathRequestMatcher("/api/v1/files/**"),
                                new AntPathRequestMatcher("/api/v1/qualification/**"),
                                new AntPathRequestMatcher("/api/v1/speciality/**"),
                                new AntPathRequestMatcher("/api/v1/user/upload-address/**"),
                                new AntPathRequestMatcher("/api/v1/user/upload-details/**"),
                                new AntPathRequestMatcher("/api/v1/user/khaltiId/**"),
                                new AntPathRequestMatcher("/ws/**")
                        ).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/v1/admin/**")
                        ).hasAuthority(UserRole.ADMIN.name())
                        .anyRequest().authenticated()
                ).exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()
                        )
                ).sessionManagement(
                        sessionManagement -> sessionManagement
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
        ;
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}