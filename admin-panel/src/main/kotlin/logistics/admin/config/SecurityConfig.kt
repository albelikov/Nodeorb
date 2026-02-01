package logistics.admin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().permitAll()
            }
            .formLogin { form -> form.disable() }
            .csrf { csrf -> csrf.disable() }
            .headers { headers -> headers.frameOptions().disable() }
        
        return http.build()
    }
}