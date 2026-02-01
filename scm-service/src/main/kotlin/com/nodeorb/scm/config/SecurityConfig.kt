package com.nodeorb.scm.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * Security configuration for Spring Boot 4.0+ with lambda DSL
 * 
 * Key changes from Spring Boot 3:
 * - Uses lambda DSL instead of deprecated methods
 * - No WebSecurityConfigurerAdapter (removed in Spring Security 6+)
 * - Stateless session management for JWT
 */
@Configuration
class SecurityConfig {
    
    /**
     * Password encoder bean for user authentication
     * BCrypt with default strength (10 rounds)
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    
    /**
     * Main security filter chain configuration
     * 
     * Public endpoints:
     * - /api/v1/auth/** - Authentication endpoints
     * - /actuator/health - Health check
     * - /swagger-ui/** - API documentation
     * - /api-docs/** - OpenAPI specs
     * 
     * All other endpoints require authentication
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // Disable CSRF for stateless API
            csrf { 
                disable() 
            }
            
            // Configure authorization rules
            authorizeHttpRequests {
                // Public endpoints
                authorize("/api/v1/auth/**", permitAll)
                authorize("/actuator/health", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/api-docs/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                
                // All other endpoints require authentication
                authorize(anyRequest, authenticated)
            }
            
            // Stateless session management (for JWT)
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            
            // Disable HTTP Basic auth
            httpBasic { 
                disable() 
            }
            
            // Disable form login
            formLogin { 
                disable() 
            }
        }
        
        return http.build()
    }
}
