package com.ashraf.jobapptracker.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LocalSecurityConfig {
    @Bean
    SecurityFilterChain localSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .csrf(Customizer.withDefaults())
                .exceptionHandling(errors -> errors.accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(403);
                    if (request.getServletPath().startsWith("/applications")) {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\":\"Request could not be verified. "
                                + "Fetch /csrf with the same session and try again.\"}");
                    } else {
                        request.getRequestDispatcher("/request-expired").forward(request, response);
                    }
                }));
        return http.build();
    }
}
