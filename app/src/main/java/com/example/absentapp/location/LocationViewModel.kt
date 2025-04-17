package com.example.absentapp.location

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.absentapp.utils.LocationUtils
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel untuk mengelola dan memantau lokasi user secara real-time.
 * Data dikelola dalam bentuk StateFlow agar bisa di-observe oleh UI secara reaktif.
 */
class LocationViewModel : ViewModel() {

    private val _location = MutableStateFlow("Sedang mengambil lokasi Anda saat ini...")
    val location: StateFlow<String> get() = _location

    private val _currentDistance = MutableStateFlow(0f)
    val currentDistance: StateFlow<Float> get() = _currentDistance

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> get() = _isFetchingLocation

    private val _distanceLimit = MutableStateFlow(20) // default 20 meter
    val distanceLimit: StateFlow<Int> get() = _distanceLimit

    /**
     * Mengambil referensi lokasi dan batas jarak dari Firebase Realtime Database.
     * @param onResult callback yang mengembalikan latitude, longitude, dan limit.
     */
    private fun fetchReferenceLocation(onResult: (Double, Double, Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("reference_location")
        ref.get()
            .addOnSuccessListener { snapshot ->
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val long = snapshot.child("longitude").getValue(Double::class.java)
                val limit = snapshot.child("Limit").getValue(Int::class.java) ?: 20

                if (lat != null && long != null) {
                    Log.d("LocationViewModel", "Fetched lat=$lat, long=$long, limit=$limit")
                    onResult(lat, long, limit)
                } else {
                    Log.e("LocationViewModel", "Null latitude or longitude from DB")
                }
            }
            .addOnFailureListener {
                Log.e("LocationViewModel", "Failed to get reference location: ${it.message}")
            }
    }

    /**
     * Menghitung jarak antara lokasi user dan lokasi referensi, lalu memperbarui state.
     * @param lat latitude dari lokasi user
     * @param long longitude dari lokasi user
     */
    fun updateLocation(lat: Double, long: Double) {
        _isFetchingLocation.value = true

        fetchReferenceLocation { refLat, refLng, limit ->
            val distance = LocationUtils(lat, long, refLat, refLng)
            val formattedDistance = "%.0f".format(distance)

            _location.value = "Lokasi Anda berada di (%.5f, %.5f), berjarak ${formattedDistance}m dari titik absensi".format(lat, long)
            _currentDistance.value = distance
            _distanceLimit.value = limit
            _isFetchingLocation.value = false
        }
    }

    /**
     * Memperbarui batas maksimal jarak absensi di Firebase dan local state.
     * @param newLimit nilai jarak maksimal baru (dalam meter)
     */
    fun updateDistanceLimit(newLimit: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("reference_location/Limit")
        ref.setValue(newLimit)
            .addOnSuccessListener {
                Log.d("LocationViewModel", "Limit updated to $newLimit")
                _distanceLimit.value = newLimit
            }
            .addOnFailureListener {
                Log.e("LocationViewModel", "Failed to update limit: ${it.message}")
            }
    }
}
