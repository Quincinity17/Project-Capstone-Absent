package com.example.absentapp.data.model

data class Comment(
    val commenterID: String = "",  // Email atau nama pemberi komentar
    val commenterEmail: String = "",  // Email atau nama pemberi komentar
    val commentText: String = "",     // Isi komentar
    val commentedAt: Long = 0L        // Waktu komentar
)
