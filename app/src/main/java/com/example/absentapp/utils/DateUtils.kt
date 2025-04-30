package com.example.absentapp.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

<<<<<<< HEAD
=======
/**
 * Extension function untuk mengubah DayOfWeek (enum Java 8) ke nama hari dalam Bahasa Indonesia.
 *
 * Contoh:
 * DayOfWeek.MONDAY.toIndonesian() -> "senin"
 *
 * @receiver DayOfWeek enum (misal: MONDAY, TUESDAY)
 * @return String nama hari dalam bahasa Indonesia (huruf kecil semua)
 */
>>>>>>> 6517416 (Finishing Iterasi 1)
@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.toIndonesian(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "senin"
        DayOfWeek.TUESDAY -> "selasa"
        DayOfWeek.WEDNESDAY -> "rabu"
        DayOfWeek.THURSDAY -> "kamis"
        DayOfWeek.FRIDAY -> "jumat"
        DayOfWeek.SATURDAY -> "sabtu"
        DayOfWeek.SUNDAY -> "minggu"
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 6517416 (Finishing Iterasi 1)
