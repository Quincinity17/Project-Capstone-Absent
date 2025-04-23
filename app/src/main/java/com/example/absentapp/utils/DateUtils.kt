package com.example.absentapp.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

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
}