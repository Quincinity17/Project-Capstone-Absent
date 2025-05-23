package com.example.absentapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.absentapp.MainActivity
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.example.absentapp.data.dataStore.helper.dataStore
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Date

val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")

class AbsentReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val preferences = context.dataStore.data.first()
        val isReminderEnabled = preferences[NOTIFICATION_ENABLED_KEY] ?: true
        if (!isReminderEnabled) return Result.success()

        val jadwalPref = JadwalCachePreference(applicationContext)
        val waktuMasukString = jadwalPref.getJamMasukForToday()
//        val waktuMasukString = "20:50"

        val waktuMasuk = LocalTime.parse(waktuMasukString)
        val now = LocalTime.now()
        val selisih = ChronoUnit.MINUTES.between(now, waktuMasuk)

        val hari = android.text.format.DateFormat.format("EEEE", Date()).toString()

//        Log.d("NASIPADANG", "Do Work. jadwalPref: $jadwalPref; waktuMasukString: $waktuMasukString; waktuMasuk: $waktuMasuk; now: $now; selisih: $selisih; hari: $hari")

        if (selisih in 0..10) {
            showNotification()
        } else {
            Log.d("Reminder", "Kondisi tidak terpenuhi, notifikasi tidak ditampilkan.")
        }
        return Result.success()
    }


    private fun showNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "reminder"

        Log.d("NASIPADANG", "Show Notif.")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminder Absen",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ingat Absen!")
            .setContentText("10 menit lagi batas absen, segera login dan absen.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1002, notification)
    }
}