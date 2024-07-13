package com.secondsight.backend

// Sample end points for fetching and creating notes
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/public/notes")
class NoteController {
    @PostMapping()
    fun createNote(@RequestBody note: Note): Note {
        // This is a dummy implementation. In a real application, you would save the note to a database
        return note
    }
}

@RestController
class UserController {
    @GetMapping("/user")
    fun user(@AuthenticationPrincipal user: OAuth2User): Map<String, Any?> {
        return user.attributes
    }
}