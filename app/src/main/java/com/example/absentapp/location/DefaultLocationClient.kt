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
 * Implementasi dari LocationClient.
 * Menggunakan FusedLocationProvider untuk mengirim data lokasi secara periodik dalam bentuk Flow.
 */
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        // Cek apakah permission lokasi sudah diberikan
        if (!context.hasLocationPermission()) {
            throw LocationClient.LocationException("Missing location permission")
        }

        // Cek apakah GPS atau Network Provider aktif
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            throw LocationClient.LocationException("GPS or Network Provider is disabled")
        }

        // Setup interval untuk update lokasi
        val request = LocationRequest.create()
            .setInterval(interval)
            .setFastestInterval(interval)

        // Callback saat lokasi diperbarui
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    launch { send(location) }
                }
            }
        }

        // Mulai request lokasi
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        // Bersihkan callback saat flow selesai
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}