package com.example.absentapp.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface lokasi yang mendefinisikan bagaimana cara mendapatkan data lokasi dalam bentuk Flow.
 */
interface LocationClient {

    fun getLocationUpdates(interval: Long): Flow<Location>

    // Exception custom untuk penanganan error lokasi
    class LocationException(message: String) : Exception(message)
}
