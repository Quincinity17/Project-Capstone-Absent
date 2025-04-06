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

    // CoroutineScope khusus untuk pekerjaan background (IO-bound)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Client untuk akses lokasi dari FusedLocationProviderClient
    private lateinit var locationClient: LocationClient

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi DefaultLocationClient (wrapper lokasi)
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    /**
     * Dipanggil saat Service menerima perintah via Intent.
     * Bisa ACTION_START (mulai tracking) atau ACTION_STOP (hentikan service).
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("NotificationPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun start() {

        // Notifikasi supaya service bisa jalan sebagai ForegroundService
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Pelacakan Lokasi Aktif")
            .setContentText("Sedang mencatat lokasi Anda...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true) // nggak bisa dihapus user

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Mulai tracking lokasi setiap 10 detik
        locationClient.getLocationUpdates(10000L)
            .catch { e ->
                Log.e("LocationService", "Error: ${e.message}")
            }
            .onEach { location ->

                LocationBridge.viewModel?.updateLocation(location.latitude, location.longitude)


                val distance = LocationBridge.viewModel?.currentDistance?.value ?: 0f
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

        // Wajib: nyalakan service sebagai Foreground supaya nggak ke-kill di background
        startForeground(
            1,
            notification.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }



    /**
     * Stop service dan bersihkan semua resource
     */
    private fun stop() {
        stopForeground(true) // hilangkan notifikasi
        stopSelf()           // hentikan servicenya
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // cancel semua job coroutine
    }

    // Tidak mendukung bound service (nggak perlu binding ke komponen lain)
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
