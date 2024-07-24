package com.secondsight.backend

// Sample end points for fetching and creating notes
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*
import javax.crypto.spec.SecretKeySpec

@RestController(value = "/notes")
class NoteController {
    @PostMapping
    fun createNote(@RequestBody note: Note): Note {
        // This is a dummy implementation. In a real application, you would save the note to a database
        return note
    }
}

@RestController
class UserController (@Autowired val tokenService: TokenService){

    @GetMapping("/user")
    fun user(@AuthenticationPrincipal user: OAuth2User): Map<String, Any?> {
        return user.attributes
    }

    @GetMapping("/authenticate")
    fun generateJWT(@AuthenticationPrincipal user: OAuth2User): String {
        return tokenService.generate(expirationDate = Date(), userDetails = user,)
    }
}

@RestController
class StarController {
    @GetMapping("/stars")
    fun getRecentStars(@AuthenticationPrincipal user: OAuth2User):Map<String, Any?>  {
        return mapOf ()
    }
}