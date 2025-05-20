package com.example.brainlog.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserDocument(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val addedMedias: List<String> = emptyList(),
    val photoUrl: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)