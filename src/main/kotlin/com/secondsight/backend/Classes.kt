package com.secondsight.backend

import jakarta.persistence.*
import java.util.Date

@Entity
data class User (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    val email: String,

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val notes: List<Note> = mutableListOf()
) {
}

@Entity
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val title: String,
    val content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    // TODO don't make this nullable
    val owner: User? = null,

    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Date = Date(),

    @Temporal(TemporalType.TIMESTAMP)
    val updatedAt: Date = Date()
)

@Entity
data class StarEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,

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

