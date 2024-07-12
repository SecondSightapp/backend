package com.secondsight.backend

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service


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

@Service
class CustomOidcUserService : OidcUserService() {
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val authorities = oidcUser.authorities
        val attributes = oidcUser.attributes
        val idToken = oidcUser.idToken
        val userInfo = oidcUser.userInfo

        // Create a new DefaultOidcUser with the authorities and attributes
        return DefaultOidcUser(authorities, idToken, userInfo, "sub")
    }
}

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/public/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.userInfoEndpoint {
                    it.oidcUserService(oidcUserService())
                }
            }
            .oauth2Client {}
        return http.build()
    }

    private fun oidcUserService(): CustomOidcUserService {
        return CustomOidcUserService()
    }
}