package com.example.absentapp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.example.absentapp.worker.ReminderReceiver
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Menjadwalkan alarm untuk reminder absen setiap hari dalam seminggu.
 *
 * Alarm akan disetel 10 menit sebelum jam masuk (jamMasuk - 10 menit),
 * dan akan memanggil ReminderReceiver untuk menampilkan notifikasi.
 *
 * Notes:
 * - Hanya bekerja di Android O+ dan sistem yang mengizinkan exact alarm.
 * - Menggunakan coroutine background thread (IO).
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ScheduleExactAlarm")
fun scheduleAllWeekAbsenAlarms(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Validasi apakah perangkat mengizinkan exact alarm (khusus Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("RENDANGAYAM", "Tidak diizinkan menjadwalkan alarm presisi oleh sistem.")
            return@launch
        }

        val jadwalPref = JadwalCachePreference(context)
        val days = listOf("senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu")

        for ((index, hari) in days.withIndex()) {
            val jadwal = jadwalPref.getSchedulesByDays(hari) ?: continue

            val jamMasuk = try {
                LocalTime.parse(jadwal.jamMasuk.replace("\"", "").trim())
            } catch (e: Exception) {
                Log.e("RENDANGAYAM", "Format jamMasuk salah: ${jadwal.jamMasuk}", e)
                continue
            }

            // Waktu pengingat 10 menit sebelum jam masuk
            val reminderTime = jamMasuk.minusMinutes(10)

            // Hitung tanggal target dari hari yang dimaksud (misalnya: Senin berikutnya)
            val now = LocalDate.now()
            val today = now.dayOfWeek.value % 7
            val targetDay = (index + 1) % 7
            val daysUntilTarget = (targetDay - today + 7) % 7
            val targetDate = now.plusDays(daysUntilTarget.toLong())

            // Waktu alarm dalam millis
            val triggerMillis = targetDate
                .atTime(reminderTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Intent untuk menjalankan ReminderReceiver saat alarm berbunyi
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("hari", hari)
            }

            // PendingIntent dengan requestCode unik per hari
            val pendingIntent = PendingIntent.getBroadcast(
                context, index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set alarm presisi, tetap menyala walau device idle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )

//            Log.d("RENDANGAYAM", "Alarm disetel: $hari jam $reminderTime → $triggerMillis")
        }
    }
}
