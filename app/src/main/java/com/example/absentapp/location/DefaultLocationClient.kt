package com.example.absentapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Mengimplementasikan LocationClient.
 * Menggunakan FusedLocationProviderClient untuk mengirim update lokasi secara periodik dalam bentuk Flow.
 */
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    /**
     * Mengembalikan Flow<Location> yang mengirim lokasi secara berkala.
     * @param interval waktu antar update dalam milidetik.
     */
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        // Periksa apakah permission lokasi sudah diberikan
        if (!context.hasLocationPermission()) {
            throw LocationClient.LocationException("Missing location permission")
        }

        // Periksa apakah GPS atau Network Provider aktif
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            throw LocationClient.LocationException("GPS or Network Provider is disabled")
        }

        // Konfigurasi permintaan lokasi
        val request = LocationRequest.create()
            .setInterval(interval)
            .setFastestInterval(interval)

        // Callback ketika lokasi diperbarui
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    launch { send(location) }
                }
            }
        }

        // Mulai mendengarkan update lokasi
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        // Hentikan saat flow selesai
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}
