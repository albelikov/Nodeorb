package com.nodeorb.scm.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        .csrf { it.disable() }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/api/v1/auth/**", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
        }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
    
    return http.build()
}
}