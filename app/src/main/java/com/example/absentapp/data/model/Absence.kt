package com.example.absentapp.data.model

data class Absence(
    val id: String = "",
    val reason: String = "",
    val userEmail: String = "",
    val timestamp: Long = 0L,
    val photoUrl: String = "",        //next progress
    val comments: List<Comment> = emptyList()
)