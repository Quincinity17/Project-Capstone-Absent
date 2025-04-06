package com.example.absentapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.absentapp.R
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.google.firebase.auth.FirebaseAuth
import com.example.absentapp.data.dataStore.helper.dataStore

import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Date

val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val preferences = context.dataStore.data.first()
        val isReminderEnabled = preferences[NOTIFICATION_ENABLED_KEY] ?: true
        if (!isReminderEnabled) return Result.success()

        val jadwalPref = JadwalCachePreference(applicationContext)
        val waktuMasukString = jadwalPref.getJamMasukForToday() // contoh: "07:30"

        val waktuMasuk = LocalTime.parse(waktuMasukString)

        val now = LocalTime.now()

        val selisih = ChronoUnit.MINUTES.between(now, waktuMasuk)

        val hari = android.text.format.DateFormat.format("EEEE", Date()).toString()

        Log.d("klepon", "Hari ini: $hari | Jam masuk: $waktuMasuk | Sekarang: $now | Selisih: $selisih menit")

        if (selisih in 8..10) {
            showNotification()
        } else {
            Log.d("Reminder", "Kondisi tidak terpenuhi, notifikasi tidak ditampilkan.")
        }

        return Result.success()
    }


    private fun showNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder"

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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1002, notification)
    }
}