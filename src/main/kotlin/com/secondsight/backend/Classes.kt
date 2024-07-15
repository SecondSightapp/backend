package com.secondsight.backend

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.persistence.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Entity
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String,
    val identity: String,
    val email: String,

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val notes: List<Note> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val stars: List<StarEntity> = mutableListOf(),
) : OAuth2User {
    init {
        require(identity.isNotBlank()) { "Name must not be blank" }
        require(email.isNotBlank()) { "Email must not be blank" }
    }

    override fun getName(): String {
        return identity
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return mutableMapOf (
            "id" to id,
            "name" to identity,
            "email" to email,
            "notes" to notes,
            "stars" to stars,
        )
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        // Assuming every user has a basic "ROLE_USER" authority
        return mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
    }
}

// Create a user repository interface
@Repository
class UserRepository{

    private val db: Firestore = FirestoreClient.getFirestore();

    suspend fun findByEmail(email: String): User? {
        return db.collection("users").whereEqualTo("email", email).get().get().toObjects(User::class.java).singleOrNull()
    }

    suspend fun createUser(user: User): User {
        println("hi i got reached")
        val newUser = db.collection("users").document()
        user.id = newUser.id
        newUser.set(user).get()
        return user
    }
}

@Entity
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: String,

    val title: String,
    val content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val owner: User,

    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Date = Date(),

    @Temporal(TemporalType.TIMESTAMP)
    val updatedAt: Date = Date()
)

@Entity
data class StarEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: String,

    @Enumerated(EnumType.STRING)
    val mood: Mood,

    @Temporal(TemporalType.DATE)
    val date: Date,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
)

enum class Mood {
    SAD, MEDIUM, HAPPY
}

data class JwtProperties(
    val key: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)

@Service
class TokenService() {
    private val jwtProperties: JwtProperties = JwtProperties(
        "Basedgoat is the best".repeat(13) + "aaaaa",
        1000 * 60 * 60 * 24,
        1000 * 60 * 60 * 24 * 30
    )

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray()
    )

    fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(secretKey)
            .compact()
    fun isValid(token: String, user: OAuth2User): Boolean {
        return true
    }
    fun extractEmail(token: String): String? =
        getAllClaims(token)
            .subject
    fun isExpired(token: String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))
    fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser()
            .verifyWith(secretKey)
            .build()
        return parser
            .parseSignedClaims(token)
            .payload
    }
}

