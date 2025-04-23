package com.example.absentapp.utils

/**
 * Membersihkan input waktu dari karakter yang tidak diinginkan seperti:
 * - Kutip miring (“ dan ”)
 * - Double quote (")
 * - Single quote (')
 *
 * Cocok digunakan sebelum melakukan parsing string ke LocalTime.
 *
 * @param rawTimeInput String input mentah dari sumber eksternal (misal: Firestore)
 * @return String bersih yang siap diparsing
 */
fun sanitizeTimeInput(rawTimeInput: String): String {
    return rawTimeInput
        .replace("“", "")  // kutip miring kiri
        .replace("”", "")  // kutip miring kanan
        .replace("\"", "") // double quote biasa
        .replace("'", "")  // single quote
        .trim()            // hapus spasi di awal/akhir
}
