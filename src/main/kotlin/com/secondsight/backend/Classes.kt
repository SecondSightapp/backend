package com.secondsight.backend

import java.util.*

/**
 * User class
 */
class User (
    val id: Int,
    val name: String,
    val email: String,
    val notes: List<Note>,
    
) {

}


/**
 * TODO: implement
 */

class Note (val id: Int, val owner: User, val createdAt: Date, val updatedAt: Date, )