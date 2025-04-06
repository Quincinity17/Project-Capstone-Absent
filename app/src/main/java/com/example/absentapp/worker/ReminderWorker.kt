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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.temporal.ChronoUnit

val Context.dataStore by preferencesDataStore(name = "settings")
val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.d("Rendang", "doWork() DIPANGGIL 🚀")

        val batasAbsen = LocalTime.now().plusMinutes(10)
        val now = LocalTime.now()
        val selisih = ChronoUnit.MINUTES.between(now, batasAbsen)

        Log.d("Rendang", "Waktu sekarang: $now")
        Log.d("Rendang", "Batas absen: $batasAbsen")
        Log.d("Rendang", "Selisih menit: $selisih")


        val preferences = context.dataStore.data.first()
        val isReminderEnabled = preferences[NOTIFICATION_ENABLED_KEY] ?: true

        Log.d("Rendang", "isReminderEnabled: $isReminderEnabled")

        if (isReminderEnabled && selisih in 8..10) {
            Log.d("Rendang", ">> Kondisi terpenuhi! Menampilkan notifikasi")
            showNotification()
        } else {
            Log.d("Rendang", ">> Kondisi TIDAK terpenuhi, notifikasi TIDAK ditampilkan")
        }


        return Result.success()
    }

    private fun showNotification() {
        Log.d("Rendang", ">> Notifikasi dikirim!")

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