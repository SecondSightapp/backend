package com.secondsight.backend

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.persistence.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
    val notes: MutableList<Note> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val stars: MutableList<Star> = mutableListOf(),
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
    /**
     * Initializes a connection to the Firestore Database
     */
    private val db: Firestore = FirestoreClient.getFirestore();

    /**
     * @param email the email of the user to find
     * @return the user with the given email, or null if no user is found.
     */
    suspend fun findByEmail(email: String): User? {
        return runBlocking{ db.collection("users").whereEqualTo("email", email).get().get().toObjects(User::class.java).singleOrNull() }
    }

    /**
     * @param id the ID of the user to find
     * @return the user with the given id, or null if no user is found.
     */
    suspend fun findById(id: String): User? {
        return runBlocking { db.collection("users").whereEqualTo("id", id).get().get().toObjects(User::class.java).singleOrNull() }
    }

    /**
     * Creates a user and adds it to the Firestore Database.
     * @param user the User to add to the database. The ID can be anything because it will be automatically generated.
     * @return the created user with the specified ID
     */
    suspend fun createUser(user: User): User {
        val newUser = db.collection("users").document()
        // Use Firebase's ID generation system
        user.id = newUser.id
        runBlocking { newUser.set(user).get() }
        return user
    }

    /**
     * Updates a user
     * @param user the user with the updated information you would like to upload
     */
    suspend fun updateUser(user: User): Boolean {
        val res =  runBlocking { db.collection("users").document(user.id).set(user.attributes).get() }
        return true
    }

    /**
     * Deletes a given user
     * @param id the
     */
    suspend fun deleteUser(id: String): Boolean {
        // ensure user exists
        assert(findById(id) != null)
        db.collection("users").document(id).delete()
        return true
    }
}



@Entity
data class Note(
    /**
     * TODO: There's a better way of doing this.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String = "PLACEHOLDER",

    val title: String,
    val content: String,

    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Date = Date(),

    @Temporal(TemporalType.TIMESTAMP)
    val updatedAt: Date = Date()
)

data class NoteDTO (
    val title: String,
    val content: String,
)

@Repository
class NoteRepository (
    @Autowired val userRepository: UserRepository
){
    /**
     * Initializes a connection to the Firestore Database
     */
    private val db: Firestore = FirestoreClient.getFirestore();

    /**
     * Creates a Note and adds it to the Firestore Database
     * @param note the note to add
     * @return the created note
     */
    suspend fun createNote(user: User, note: Note): Note {
        val newNote = db.collection("notes").document()
        note.id = newNote.id
        user.notes.add(note)
        userRepository.updateUser(user)
        runBlocking { newNote.set(note).get() }
        return note
    }

    /**
     * Updates a note and adds it to the Firestore Database
     * @param note the note to update
     * @return the updated note
     */
    suspend fun updateNote(note: Note): Note {
        val res = runBlocking { db.collection("notes").document(note.id).set(note).get() }
        return note
    }

    /**
     * Returns a note
     * @param id the id of the note
     * @return the note, or null if it is not found
     */
    suspend fun findNoteById(id: String): Note? {
        return runBlocking { db.collection("notes").whereEqualTo("id", id).get().get().firstOrNull()?.toObject(Note::class.java) }
    }

    /**
     * Returns all notes by a user, denoted by its id
     * @param userId the ID of the user that you want to get all the notes from
     * @return a list of notes by that user
     */
    suspend fun getNotesByUserId(userId: String): List<Note> {
        val user = runBlocking { userRepository.findById(userId) }
        return user?.notes ?: listOf()
    }

    /**
     * Returns all notes by a user, denoted by its email
     * @param email the email of the user that you want to get all the notes from
     * @return a list of notes by that user
     */
    suspend fun getNotesByUserEmail(email: String): List<Note> {
        val user = runBlocking { userRepository.findByEmail(email) }
        return user?.notes ?: listOf()
    }

}


@Entity
data class Star(
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

@Repository
class StarRepository (
    @Autowired val userRepository: UserRepository
){
    private val db = FirestoreClient.getFirestore()

    /**
     * Get all stars for a user by their id
     * @param userId id of the user to get the stars for
     * @return a list of stars by that user
     */
    suspend fun getStarsByUserId(userId: String): List<Star> {
        val user = runBlocking { userRepository.findById(userId) }
        return user?.stars ?: listOf()
    }

    /**
     * Get all stars for a user by their email
     * @param email email of the user to get the stars for
     * @return a list of stars by that user
     */
    suspend fun getStarsByEmail(email: String): List<Star> {
        val user = runBlocking { userRepository.findByEmail(email) }
        return user?.stars ?: listOf()
    }

}

enum class Mood {
    SAD, MEDIUM, HAPPY
}

data class JwtProperties(
    val key: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
)

@Service
class TokenService(
    val validTokens: Map<OAuth2User, String> = mutableMapOf()
) {
    private val jwtProperties: JwtProperties = JwtProperties(
        "Basedgoat is the best".repeat(13) + "aaaaa",
        1000 * 60 * 60 * 24,
        1000 * 60 * 60 * 24 * 20
    )

    private val secretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generate(userDetails: OAuth2User, expirationDate: Date, additionalClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims()
            .subject(userDetails.getAttribute("email"))
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(secretKey)
            .compact()
    }

    fun isValid(token: String, user: OAuth2User): Boolean {
        // what the heck
        return true
    }

    fun extractEmail(token: String): String? = getAllClaims(token).subject

    fun isExpired(token: String): Boolean = getAllClaims(token).expiration.before(Date(System.currentTimeMillis()))

    fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser().verifyWith(secretKey).build()
        return parser.parseSignedClaims(token).payload
    }
}

