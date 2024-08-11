package com.secondsight.backend

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

class FirebaseAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
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
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/public/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder())
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint {
                    it.userService(defaultUserService())
                }
            }
            .oauth2Client(Customizer.withDefaults())
        return http.build()
    }

    @Autowired
    private lateinit var tokenService: TokenService

    @Bean
    fun jwtDecoder(): JwtDecoder = JwtDecoder { jwtToken ->
        val claims = tokenService.getAllClaims(jwtToken)
        val claimsSetBuilder = JwtClaimsSet.builder()
        claims.forEach { claimsSetBuilder.claim(it.key, it.value) }
        val jwtClaimsSet = claimsSetBuilder.build()
        Jwt.withTokenValue(jwtToken)
            .header("alg", "HS256")
            .claims { jwtClaimsSet.claims }
            .subject( jwtClaimsSet.subject )
            .issuedAt(Instant.ofEpochMilli(claims.issuedAt.time))
            .expiresAt(Instant.ofEpochMilli(claims.expiration.time))
            .build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        // Here, you can customize the converter if needed. For example, setting a custom GrantedAuthoritiesMapper.
        return converter
    }

    @Bean
    fun defaultUserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val userService = UserService(UserRepository())

        return userService
    }
}

// Create a user service with use with the User class defined in Classes.kt that I can use with the oauth2login configuration

@Service
class UserService(@Autowired private val userRepository: UserRepository) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = DefaultOAuth2UserService().loadUser(userRequest)
        val email = oAuth2User.getAttribute<String>("email") ?: throw IllegalArgumentException("Email not found from OAuth2 provider")

        val user = runBlocking { userRepository.findByEmail(email) ?: createUser(oAuth2User) }
        return toOAuth2User(user)
    }

    private fun createUser(oAuth2User: OAuth2User): User {
        // Extract necessary information from oAuth2User
        val email = oAuth2User.getAttribute<String>("email")!!
        val name = oAuth2User.getAttribute<String>("name") ?: "Default Name"

        // Create and save the new user
        val newUser = User(
            id = "", // Or leave it unset if it's autogenerated
            identity = name,
            email = email,
            notes = mutableListOf(),
            stars = mutableListOf()
        )
        return runBlocking { userRepository.createUser(newUser) }
    }

    private fun toOAuth2User(user: User): OAuth2User {
        return DefaultOAuth2User(
            Collections.singleton(SimpleGrantedAuthority("ROLE_USER")),
            user.attributes,
            "name"
        )
    }
}