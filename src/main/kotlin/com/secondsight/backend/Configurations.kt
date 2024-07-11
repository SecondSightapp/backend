package com.secondsight.backend

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.OncePerRequestFilter

class FirebaseAuthenticationTokenFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader("Authorization")?.substring("Bearer ".length)
        token?.let {
            try {
                val decodedToken = FirebaseAuth.getInstance().verifyIdToken(it)
                val auth = UsernamePasswordAuthenticationToken(decodedToken.uid, token, listOf())
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}


class FirebaseAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        // Here, you would add your logic to verify the authentication object with Firebase
        // For simplicity, this example assumes the authentication is always valid
        return authentication
    }

    override fun supports(authentication: Class<*>?): Boolean {
        // This provider supports all types of authentication
        return true
    }
}


@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun firebaseAuthenticationTokenFilter(): FirebaseAuthenticationTokenFilter {
        return FirebaseAuthenticationTokenFilter()
    }

    @Bean
    fun firebaseAuthenticationProvider(): FirebaseAuthenticationProvider {
        return FirebaseAuthenticationProvider()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        // Configure and return your JwtDecoder here
        // This is a placeholder to demonstrate where to configure the JwtDecoder
        // For Firebase, you might need a custom decoder that interfaces with Firebase to validate tokens
        return JwtDecoder { token -> null }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeRequests { authz ->
                authz
                    .requestMatchers("/public/**").permitAll() // Unprotected routes
                    .requestMatchers("/admin/**").hasRole("ADMIN") // Protected: Only accessible by users with the ADMIN role
                    .anyRequest().authenticated() // All other routes are protected
            }
            .oauth2ResourceServer { oauth2 -> oauth2.jwt { jwt -> jwt.decoder(jwtDecoder()) } }
            .addFilterBefore(firebaseAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .csrf { csrf -> csrf.disable() }

        return http.build()
    }

    fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(firebaseAuthenticationProvider())
    }
}