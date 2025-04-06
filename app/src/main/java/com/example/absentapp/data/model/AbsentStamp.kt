package com.example.absentapp.data.model

data class AbsentStamp(
    val name: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val uid: String = "",
    val photoBase64: String? = null
)
