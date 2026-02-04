package com.nodeorb.freight.marketplace.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver

/**
 * Конфигурация безопасности для UI контроллеров
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Публичные endpoints
                    .requestMatchers("/api/v1/public/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    
                    // Carrier UI endpoints
                    .requestMatchers("/api/v1/carrier/orders").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/orders/*/progress").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/orders/*/bids").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/bids").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/trust-token").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/validate-token").hasAuthority("CARRIER")
                    .requestMatchers("/api/v1/carrier/revoke-token").hasAuthority("CARRIER")
                    
                    // Shipper UI endpoints
                    .requestMatchers("/api/v1/shipper/orders").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/progress").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/bids").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/partial-orders").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/partial-orders/*/assign").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/cancel").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/statistics").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/scm-history").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/carriers/top").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/audit-trail").hasAuthority("SHIPPER")
                    .requestMatchers("/api/v1/shipper/orders/*/export").hasAuthority("SHIPPER")
                    
                    // Admin endpoints
                    .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                    
                    // Все остальные запросы требуют аутентификации
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2
                    .jwt { jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                    }
                    .bearerTokenResolver(bearerTokenResolver())
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles")
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = jwtGrantedAuthoritiesConverter.convert(jwt)
            // Добавляем дополнительные authorities на основе claims
            val customAuthorities = mutableListOf<String>()
            
            // Добавляем authorities на основе типа пользователя
            val userType = jwt.claims["user_type"] as? String
            if (userType != null) {
                customAuthorities.add("ROLE_$userType")
            }
            
            // Добавляем authorities на основе специальных разрешений
            val permissions = jwt.claims["permissions"] as? List<String>
            permissions?.forEach { permission ->
                customAuthorities.add("SCOPE_$permission")
            }
            
            authorities.plus(customAuthorities.map { org.springframework.security.core.GrantedAuthority { it } })
        }
        
        return jwtAuthenticationConverter
    }

    @Bean
    fun bearerTokenResolver(): BearerTokenResolver {
        val bearerTokenResolver = DefaultBearerTokenResolver()
        bearerTokenResolver.setAllowFormEncodedBodyParameter(true)
        bearerTokenResolver.setAllowUriQueryParameter(true)
        return bearerTokenResolver
    }
}