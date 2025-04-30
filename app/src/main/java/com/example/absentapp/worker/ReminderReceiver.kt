package com.example.absentapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.absentapp.MainActivity
import com.example.absentapp.R

/**
 * ReminderReceiver adalah BroadcastReceiver yang dipanggil oleh AlarmManager.
 * Tugasnya adalah menampilkan notifikasi pengingat absen kepada pengguna.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "reminder_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent untuk membuka MainActivity ketika notifikasi ditekan
        val notifIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notifIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE penting untuk Android 12+
        )

        // Buat Notification Channel jika belum ada (wajib Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = manager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Reminder Absen",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }

        // Bangun notifikasi
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.img_logo)
            .setContentTitle("Ingat Absen!")
            .setContentText("10 menit lagi batas absen, segera login dan absen.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Menutup notifikasi saat ditekan
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Tampilkan notifikasi dengan ID unik
        manager.notify(1002, notification)
    }
}
