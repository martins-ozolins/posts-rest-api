package com.example.com.posts_rest_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class CustomSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtCookieAuthFilter jwtFilter) {

        /**
         * Since we are using JWT, we disable session creation.
         * Spring Security will NOT create or use HttpSession.
         * Each request must carry authentication information (JWT).
         */
        http.sessionManagement(sessionConfig ->
                sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        /**
         * Disable default form login.
         */
        http.formLogin(fl -> fl.disable());

        /**
        Disables HTTP Basic authentication.
         */
        http.httpBasic(hbc ->
                hbc.disable());

        /**
         Disables CSRF for development.
         */
        http.csrf(csrf -> csrf.disable());

        /**
         * Authorization rules:
         * - Role-based endpoint protection
         * - Some endpoints publicly accessible
         */
        http.authorizeHttpRequests(req -> req
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/favicon.ico").permitAll()

                .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .requestMatchers("/me/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

//        http.exceptionHandling(eh -> eh
//                .authenticationEntryPoint((req, res, ex) -> res.sendError(401))
//                .accessDeniedHandler((req, res, ex) -> res.sendError(403))
//        );

        return http.build();
    }

    /**
     * PasswordEncoder bean.
     * Uses DelegatingPasswordEncoder which supports multiple encoding formats
     * (bcrypt by default).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
