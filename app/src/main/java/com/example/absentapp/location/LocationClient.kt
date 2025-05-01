package com.example.absentapp.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface lokasi yang mendefinisikan bagaimana cara mendapatkan data lokasi dalam bentuk Flow.
 * Digunakan agar implementasi seperti DefaultLocationClient tetap fleksibel dan bisa diuji.
 */
interface LocationClient {

    /**
     * Mengembalikan aliran (Flow) data lokasi secara periodik berdasarkan interval waktu.
     * @param interval Waktu antar update lokasi (dalam milidetik).
     */
    fun getLocationUpdates(interval: Long): Flow<Location>

    /**
     * Exception khusus untuk menangani error saat pengambilan lokasi.
     */
    class LocationException(message: String) : Exception(message)
}
