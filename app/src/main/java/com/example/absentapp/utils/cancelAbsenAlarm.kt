package com.example.absentapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.absentapp.worker.ReminderReceiver

/**
 * Membatalkan alarm absen yang sebelumnya disetel menggunakan AlarmManager.
 *
 * Catatan:
 * Alarm yang dibatalkan adalah alarm dengan requestCode = 0.
 * Pastikan requestCode ini sama dengan yang digunakan saat menjadwalkan alarm.
 *
 * @param context Context aplikasi
 */
fun cancelAbsenAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)
}
