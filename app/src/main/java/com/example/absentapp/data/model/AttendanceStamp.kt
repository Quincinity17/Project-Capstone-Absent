package com.example.absentapp.data.model

data class AttendanceStamp(
    val name: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val uid: String = "",
    val photoBase64: String? = null,
    val type: String? = null,
    val timeNote: String? = null
)

