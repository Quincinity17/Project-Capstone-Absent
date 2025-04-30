package com.example.absentapp.utils

import android.location.Location

/**
 * Fungsi LocationUtils menghitung jarak (dalam meter) antara dua titik koordinat.
 *
 * @param userLat Latitude pengguna saat ini
 * @param userLng Longitude pengguna saat ini
 * @param targetLat Latitude tujuan
 * @param targetLng Longitude tujuan
 * @return Jarak dalam meter antara pengguna dan tujuan
 */
fun LocationUtils(
    userLat: Double,
    userLng: Double,
    targetLat: Double,
    targetLng: Double
): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        userLat, userLng,
        targetLat, targetLng,
        result
    )
    return result[0]
}
