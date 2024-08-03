package com.secondsight.backend

// Sample end points for fetching and creating notes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Right now, 30 minutes I believe. This is the time in milliseconds
 */
private const val JWT_EXPIRATION_TIME = 1_800_000

@RestController
class UserController(
    @Autowired val tokenService: TokenService,
    @Autowired val userRepository: UserRepository,
){

    @GetMapping("/user")
    fun testUserOAuth2Endpoint(@AuthenticationPrincipal user: OAuth2User): Map<String, Any?> {
        return user.attributes
    }

    @GetMapping("/authenticate")
    fun generateJWT(@AuthenticationPrincipal user: OAuth2User): String {
        return tokenService.generate(expirationDate = Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME), userDetails = user,)
    }
}

@RestController()
class NoteController(
    @Autowired val userRepository: UserRepository,
    @Autowired val noteRepository: NoteRepository
) {
    @GetMapping("/notes")
    suspend fun getNotes(@AuthenticationPrincipal principal: Jwt): List<Note> {
        val user = userRepository.findByEmail(principal.subject)
        return user?.notes ?: listOf()
    }

    @PostMapping("/notes")
    suspend fun createNote(@AuthenticationPrincipal principal: Jwt, @RequestBody note: NoteDTO): Note {
        // This is a dummy implementation. In a real application, you would save the note to a database
        val user = userRepository.findByEmail(principal.subject) ?: throw AuthenticationServiceException("User from JWT not found. ")
        return noteRepository.createNote(user, Note(title = note.title, content = note.content))
    }

    @PutMapping("/notes/{id}")
    suspend fun updateNote(@AuthenticationPrincipal principal: Jwt, @RequestBody note: NoteDTO, @PathVariable id: String): Note {
        return noteRepository.updateNote(Note(id = id, title = note.title, content = note.content))
    }
}

@RestController
class StarController (
    @Autowired val userRepository: UserRepository,
    @Autowired val starRepository: StarRepository
) {
    @GetMapping("/stars")
    suspend fun getRecentStars(@AuthenticationPrincipal principal: Jwt):Map<String, Any?>  {
        val user = userRepository.findByEmail(principal.subject)
        return mapOf("stars" to user?.stars)
    }

    @PostMapping("/stars")
    suspend fun createStar(@AuthenticationPrincipal principal: Jwt, @RequestBody star: StarDTO): Star {
        val user = userRepository.findByEmail(principal.subject) ?: throw AuthenticationServiceException("User from JWT not found. ")
        return starRepository.createStar(user, star)
    }

    @PutMapping("/stars/{id}")
    suspend fun updateStar(@AuthenticationPrincipal principal: Jwt, @RequestBody star: StarDTO, @PathVariable id: String): Star {
        val user = userRepository.findByEmail(principal.subject) ?: throw AuthenticationServiceException("User from JWT not found. ")
        return starRepository.updateStar(user, id, star)
    }
}