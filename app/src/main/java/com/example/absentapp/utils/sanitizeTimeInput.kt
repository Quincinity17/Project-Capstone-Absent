package com.example.absentapp.utils

fun sanitizeTimeInput(raw: String): String {
    return raw
        .replace("“", "") // kutip miring
        .replace("”", "")
        .replace("\"", "") // double quote biasa
        .replace("'", "")  // single quote
        .trim()
}
