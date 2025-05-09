package com.example.absentapp.location

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.absentapp.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Service yang berjalan di background untuk melakukan tracking lokasi
 * dan mengirimkan hasilnya ke ViewModel lewat LocationBridge.
 */
class LocationService : Service() {

    // CoroutineScope khusus untuk pekerjaan background
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Wrapper untuk FusedLocationProviderClient
    private lateinit var locationClient: LocationClient

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi LocationClient menggunakan DefaultLocationClient
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    /**
     * Menangani intent masuk.
     * Digunakan untuk memulai atau menghentikan tracking lokasi.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Memulai tracking lokasi dan menampilkan notifikasi foreground.
     */
    @SuppressLint("NotificationPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun start() {
        // Notifikasi yang menandakan service aktif
        val notification = NotificationCompat.Builder(this, "location")
            .setSmallIcon(R.drawable.img_logo)
            .setContentTitle("Pelacakan Lokasi Aktif")
            .setContentText("Sedang mencatat lokasi Anda...")
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tracking lokasi setiap 10 detik
        locationClient.getLocationUpdates(10000L)
            .catch { e ->
                Log.e("LocationService", "Error: ${e.message}")
            }
            .onEach { location ->
                // Kirim lokasi ke ViewModel melalui LocationBridge
                LocationBridge.viewModel?.updateLocation(location.latitude, location.longitude)

                // Ambil jarak dari titik absen (kalau tersedia)
                val distance = LocationBridge.viewModel?.currentDistance?.value ?: 100
                val formattedDistance = String.format("%.0f", distance)

                val latText = String.format("%.5f", location.latitude)
                val longText = String.format("%.5f", location.longitude)

                val newText = "Lokasi Anda berada di ($latText, $longText), berjarak ${formattedDistance}m dari titik absensi"

                notificationManager.notify(
                    1,
                    notification.setContentText(newText).build()
                )
            }
            .launchIn(serviceScope)

        // Menjalankan service sebagai foreground service agar tidak dihentikan oleh sistem
        startForeground(
            1,
            notification.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    /**
     * Mendapatkan lokasi saat ini satu kali (jika dibutuhkan).
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationSimple(onResult: (Double, Double) -> Unit) {
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    Log.e("LocationService", "Gagal mendapatkan lokasi saat ini")
                }
            }
    }

    /**
     * Menghentikan service dan notifikasi foreground.
     */
    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan semua coroutine saat service dihentikan
        serviceScope.cancel()
    }

    // Service ini tidak perlu di-bind, karena hanya berjalan mandiri
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
