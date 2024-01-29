package com.personal.microart.rest.configuration;

import com.personal.microart.rest.auth.BasicAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.DisableEncodeUrlFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true) //TODO remove
public class SecurityConfiguration {
    private final BasicAuthFilter basicAuthFilter;

    private final String[] swaggerPatterns = new String[]{
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/swagger-ui/**",
            "/configuration/security",
            "/webjars/**",
            "/swagger-ui.html"
    };

    @Bean
//    @SneakyThrows
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .addFilterBefore(basicAuthFilter, DisableEncodeUrlFilter.class)
                .authorizeHttpRequests(customizer -> customizer
                                .requestMatchers("/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/mvn/**").permitAll()
//                                .requestMatchers(HttpMethod.PUT, "/mvn/**").authenticated()
//                                .requestMatchers(HttpMethod.POST, "/user/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/browse/**").authenticated()
//
//                                .requestMatchers(HttpMethod.GET, this.swaggerPatterns).permitAll()
//                        .requestMatchers(HttpMethod.POST, this.swaggerPatterns).authenticated()
                )
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
