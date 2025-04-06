package com.example.absentapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.location.LocationBridge
import com.example.absentapp.location.LocationService
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.navigation.MyAppNavigation
import com.example.absentapp.ui.screens.camera.CameraViewModel
import com.example.absentapp.ui.theme.AbsentAppTheme
import com.example.absentapp.worker.ReminderWorker
import java.util.concurrent.TimeUnit

/**
 * MainActivity adalah entry point dari aplikasi.
 * Di sini dilakukan:
 * - Inisialisasi ViewModel
 * - Setup permission & notification channel
 * - Menjalankan location service
 * - Menampilkan UI menggunakan Jetpack Compose
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Mengaktifkan edge-to-edge layout agar UI bisa penuh ke seluruh layar

        // Inisialisasi ViewModel yang digunakan di seluruh aplikasi
        val authViewModel = AuthViewModel()         // Untuk login & autentikasi
        val locationViewModel = LocationViewModel() // Untuk pelacakan lokasi
        val cameraViewModel = CameraViewModel()     // Untuk kamera & selfie

        // Menyambungkan ViewModel lokasi ke bridge agar bisa digunakan di luar Compose (di service)
        LocationBridge.viewModel = locationViewModel

        // Membuat notification channel untuk notifikasi tracking lokasi (wajib Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location", // ID harus sama dengan yang dipakai di LocationService
                "Location Tracking", // Nama channel yang tampil di pengaturan
                NotificationManager.IMPORTANCE_LOW // Level pentingnya rendah (tidak bunyi)
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Meminta izin akses lokasi, kamera, dan notifikasi (tergantung versi Android)
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
            )
        }

        // Meminta izin tersebut ke pengguna
        ActivityCompat.requestPermissions(this, permissions, 0)

        // Menjalankan background service untuk pelacakan lokasi
        startLocationService(this)

        // Menjadwalkan Worker notifikasi reminder
        scheduleReminderWorker(this)

        // Menampilkan UI aplikasi dengan Jetpack Compose
        setContent {
            AbsentAppTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    // Navigasi utama aplikasi, dengan membawa semua ViewModel yang dibutuhkan
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        locationViewModel = locationViewModel,
                        cameraViewModel = cameraViewModel
                    )
                }
            }
        }
    }

    // Lifecycle method yang terpanggil saat activity dihancurkan
    override fun onDestroy() {
        super.onDestroy()
        // Menghentikan location service saat aplikasi ditutup
        stopLocationService(this)
    }

    // Fungsi untuk memulai LocationService
    private fun startLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START // Memberi tahu service untuk mulai tracking
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8+ butuh startForegroundService untuk background service
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // Fungsi untuk menghentikan LocationService
    private fun stopLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP // Memberi tahu service untuk berhenti
        }
        context.stopService(intent)
    }

    private fun scheduleReminderWorker(context: Context) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS) // Worker jalan setelah 10 detik
            .build()
        //        val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
//            .setConstraints(
//                Constraints.Builder()
//                    .setRequiresBatteryNotLow(true)
//                    .build()
//            )
//            .build()
        WorkManager.getInstance(context).enqueue(request)

//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            "reminderWorker",
//            ExistingPeriodicWorkPolicy.KEEP,
//            request
//        )
    }

}