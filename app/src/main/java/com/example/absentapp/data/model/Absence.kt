package com.example.absentapp.data.model

data class Absence(
    val id: String = "",
    val reason: String = "", // alasan izin (teks)
// ID unik absensi
    val userEmail: String = "",       // Siapa yang melakukan absen
    val timestamp: Long = 0L,         // Waktu absen (epoch millis)
    val photoUrl: String = "",        // (Opsional) URL foto selfie saat absen
    val comments: List<Comment> = emptyList() // List komentar dari user lain
)