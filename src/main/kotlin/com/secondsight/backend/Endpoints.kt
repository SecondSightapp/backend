package com.secondsight.backend

// Sample end points for fetching and creating notes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/notes")
class NoteController {
    @GetMapping()
    fun getNotes(): List<Note> {
        // This is a dummy implementation. In a real application, you would fetch notes from a database
        return listOf(
            Note(
                1,
                "First Note",
                "This is the content of the first note",
            ),
            Note(
                2,
                "Second Note",
                "This is the content of the second note"
            ),
            Note (
                3,
                "Third Note",
                "This is the content of the third note",
        )
        )
    }

    @PostMapping()
    fun createNote(@RequestBody note: Note): Note {
        // This is a dummy implementation. In a real application, you would save the note to a database
        return note
    }
}