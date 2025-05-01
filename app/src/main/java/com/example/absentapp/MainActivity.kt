package com.example.absentapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
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
 * MainActivity.kt
 * Entry point utama aplikasi ATTEND.
 *
 * Fitur:
 * - Setup tema aplikasi dan scaffold utama untuk menampilkan NavigationHost
 * - Mengatur permission yang dibutuhkan: lokasi, kamera, notifikasi
 * - Menjalankan LocationService (foreground service untuk tracking lokasi)
 * - Sinkronisasi jadwal dari Firestore ke DataStore lokal
 * - Menyiapkan alarm mingguan untuk reminder presensi
 * - Membuat notification channel untuk layanan lokasi
 */

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModel: LocationViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup AlarmManager untuk Android S+
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        }

        // Sinkronisasi jadwal dan penjadwalan notifikasi mingguan
        lifecycleScope.launch {
            syncJadwalFromFirestore(applicationContext)

            val notificationPref = NotificationPreference(this@MainActivity)
            val isEnabled = notificationPref.isNotificationEnabled.first()
            if (isEnabled) {
                scheduleAllWeekAbsenAlarms(applicationContext)
            }
        }

        // Inisialisasi ViewModel
        val authViewModel = AuthViewModel()
        locationViewModel = LocationViewModel()
        val cameraViewModel = CameraViewModel()
        val absenceViewModel = AbsenceViewModel()
        LocationBridge.viewModel = locationViewModel

        // Setup NotificationChannel untuk lokasi
        val channel = NotificationChannel(
            "location", "Location Tracking", NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Minta permission yang diperlukan
        requestAllPermissions()

        // Tampilkan UI menggunakan Jetpack Compose
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

    /**
     * Hentikan LocationService saat Activity dihancurkan.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopLocationService(this)
    }

    /**
     * Minta semua permission penting:
     * - Kamera
     * - Lokasi (coarse, fine, foreground service)
     * - Notifikasi (Android 13+)
     */
    private fun requestAllPermissions() {
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
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 0)
        } else {
            startLocationService(this)
        }
    }

    /**
     * Callback untuk hasil permission request.
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startLocationService(this)
        } else {
            Log.e("LocationService", "Permission ditolak, tidak menjalankan service")
        }
    }

    /**
     * Menjalankan LocationService sebagai foreground service.
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
     * Menghentikan LocationService jika sedang aktif.
     */
    private fun stopLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        context.stopService(intent)
    }

    /**
     * Sinkronisasi jadwal presensi dari Firestore dan simpan ke DataStore lokal.
     */
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
     * Fungsi opsional untuk mengecek selisih waktu sekarang dengan jadwal masuk.
     * (Digunakan untuk debugging, tidak dipakai di UI saat ini)
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
