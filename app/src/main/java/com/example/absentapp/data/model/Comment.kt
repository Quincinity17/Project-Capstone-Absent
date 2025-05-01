package com.example.absentapp.data.model

data class Comment(
    val commenterID: String = "",
    val commenterEmail: String = "",
    val commentText: String = "",
    val commentedAt: Long = 0L
)
