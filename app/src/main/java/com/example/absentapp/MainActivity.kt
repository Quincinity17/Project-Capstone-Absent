package com.example.absentapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.example.absentapp.data.dataStore.NotificationPreference
import com.example.absentapp.data.dataStore.helper.jadwalDataStore
import com.example.absentapp.location.LocationBridge
import com.example.absentapp.location.LocationService
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.navigation.NavigationHost
import com.example.absentapp.ui.screens.absent.AbsenceViewModel
import com.example.absentapp.ui.screens.camera.CameraViewModel
import com.example.absentapp.ui.theme.AbsentAppTheme
import com.example.absentapp.utils.scheduleAllWeekAbsenAlarms
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * MainActivity adalah entry point dari aplikasi AbsentApp.
 *
 * Fungsinya mencakup:
 * - Inisialisasi ViewModel (Auth, Lokasi, Kamera)
 * - Setup permission & notifikasi
 * - Menyinkronkan jadwal dari Firestore ke DataStore
 * - Menjadwalkan notifikasi absen mingguan (jika diaktifkan)
 * - Menjalankan location service
 * - Menampilkan UI berbasis Jetpack Compose
 */
class MainActivity : ComponentActivity() {

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup AlarmManager agar bisa menjadwalkan alarm tepat waktu (khusus Android 12+)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Log.w("RENDANGAYAM", "Device tidak support buka pengaturan exact alarm")
            }
        }

        // Sinkronisasi jadwal dari Firestore ke DataStore + jadwalkan alarm absen mingguan
        lifecycleScope.launch {
            syncJadwalFromFirestore(applicationContext)

            val notificationPref = NotificationPreference(this@MainActivity)
            val isEnabled = notificationPref.isNotificationEnabled.first()
            Log.e("RENDANGAYAM", "Manggil schedule")
            Log.e("RENDANGAYAM", "kondisi cache $isEnabled")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isEnabled) {
                scheduleAllWeekAbsenAlarms(applicationContext)
            }
        }

        // Inisialisasi semua ViewModel utama
        val authViewModel = AuthViewModel()
        val locationViewModel = LocationViewModel()
        val cameraViewModel = CameraViewModel()
        val absenceViewModel = AbsenceViewModel()

        LocationBridge.viewModel = locationViewModel

        // Buat notification channel untuk pelacakan lokasi
        val channel = NotificationChannel(
            "location",
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Meminta semua permission yang dibutuhkan
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
        ActivityCompat.requestPermissions(this, permissions, 0)

        // Jalankan background location service
        startLocationService(this)

        // Render UI dengan Jetpack Compose
        setContent {
            AbsentAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    NavigationHost(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        locationViewModel = locationViewModel,
                        cameraViewModel = cameraViewModel,
                        absenceViewModel = absenceViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationService(this)
    }

    /**
     * Fungsi untuk memulai LocationService sebagai foreground service
     */
    private fun startLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Fungsi untuk menghentikan LocationService
     */
    private fun stopLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        context.stopService(intent)
    }

<<<<<<< HEAD
    private fun scheduleReminderWorker(context: Context) {


        val request = OneTimeWorkRequestBuilder<AbsentReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS) // Worker jalan setelah 10 detik
            .build()

        Log.d("NASIPADANG", "kepanggil di Main membuat $request.")

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
=======
    /**
     * Sinkronisasi jadwal absen dari Firestore ke DataStore
     */
>>>>>>> 6517416 (Finishing Iterasi 1)
    fun syncJadwalFromFirestore(context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val dataStore = context.jadwalDataStore

        firestore.collection("jadwal").get().addOnSuccessListener { snapshot ->
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.edit { prefs ->
                    snapshot.documents.forEach { doc ->
                        val hari = doc.getString("hari")?.lowercase() ?: return@forEach
                        val jamMasuk = doc.getString("jamMasuk") ?: "07:00"
                        prefs[stringPreferencesKey("jam_masuk_$hari")] = jamMasuk
                    }
                }
            }
        }
    }

    /**
     * Mengecek selisih waktu antara sekarang dan waktu masuk
     * Digunakan untuk keperluan debug atau logika notifikasi
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun cekSelisihWaktuMasuk() {
        val jadwalPref = JadwalCachePreference(this)
        val waktuMasukString = jadwalPref.getJamMasukForToday()

        if (waktuMasukString.isBlank() || waktuMasukString == "null") {
            Log.e("klepon", "waktuMasukString kosong atau null dari DataStore")
            return
        }

        try {
            val waktuMasuk = LocalTime.parse(waktuMasukString)
            val now = LocalTime.now()
            val selisih = ChronoUnit.MINUTES.between(now, waktuMasuk)
            val hari = android.text.format.DateFormat.format("EEEE", Date()).toString()

            Log.d("klepon", "Hari ini: $hari | Jam masuk: $waktuMasuk | Sekarang: $now | Selisih: $selisih menit")
        } catch (e: Exception) {
            Log.e("klepon", "Gagal parse waktuMasukString: $waktuMasukString", e)
        }
    }
}
