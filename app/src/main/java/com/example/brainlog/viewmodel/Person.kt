package com.example.brainlog.viewmodel

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Person(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) {

}
