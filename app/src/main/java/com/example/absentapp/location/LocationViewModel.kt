package com.example.absentapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.absentapp.utils.LocationUtils
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel untuk mengelola dan memantau lokasi user secara real-time.
 * Data dikelola dalam bentuk StateFlow agar bisa di-observe oleh UI secara reaktif.
 */
class LocationViewModel : ViewModel() {

    // Menyimpan teks status lokasi yang ditampilkan ke UI
    private val _location = MutableStateFlow("Sedang mengambil lokasi Anda saat ini...")
    val location: StateFlow<String> get() = _location

    // Menyimpan jarak saat ini dari lokasi referensi
    private val _currentDistance = MutableStateFlow(0f)
    val currentDistance: StateFlow<Float> get() = _currentDistance

    // Status apakah sedang mengambil lokasi atau tidak
    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> get() = _isFetchingLocation

    // Batas maksimal jarak absensi (default: 20 meter)
    private val _distanceLimit = MutableStateFlow(20)
    val distanceLimit: StateFlow<Int> get() = _distanceLimit

    /**
     * Mengambil lokasi referensi dan batas jaraknya dari Firebase Realtime Database.
     * Dipanggil dari updateLocation untuk menghitung jarak terhadap lokasi user.
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
     * Mengupdate lokasi user saat ini, menghitung jarak ke lokasi referensi,
     * lalu memperbarui nilai state.
     */
    fun updateLocation(lat: Double, long: Double) {
        _isFetchingLocation.value = true

        fetchReferenceLocation { refLat, refLng, limit ->
            val distance = LocationUtils(lat, long, refLat, refLng)
            val formattedDistance = "%.0f".format(distance)

            Log.d("KACANGTANAH", "lat $lat ; long: $long ; refLat: $refLat ; refLng: $refLng")
            Log.e("KACANGTANAH", "Error: $distance")

            _location.value = "Lokasi Anda berada di (%.5f, %.5f), berjarakk ${formattedDistance}m dari titik absensi".format(lat, long)
            _currentDistance.value = distance
            _distanceLimit.value = limit
            _isFetchingLocation.value = false
        }
    }

    /**
     * Memperbarui batas jarak maksimal (misalnya 50m) untuk absensi,
     * dan menyimpannya ke Firebase.
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

    /**
     * Menyimpan lokasi user saat ini sebagai lokasi referensi baru di Firebase.
     */
    fun updateReferenceLocation(context: Context) {
        getCurrentLocationSimple(context) { lat, long ->
            val ref = FirebaseDatabase.getInstance().getReference("reference_location")

            Log.d("ROTIPANGGANG", "Updating reference location to lat=$lat, long=$long")

            val updates = mapOf(
                "latitude" to lat,
                "longitude" to long
            )

            ref.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("LocationViewModel", "Reference location updated to lat=$lat, long=$long")
                }
                .addOnFailureListener {
                    Log.e("LocationViewModel", "Failed to update reference location: ${it.message}")
                }
        }
    }

    /**
     * Mengambil lokasi terakhir user dari FusedLocationProviderClient secara langsung.
     * Biasanya dipakai untuk update lokasi referensi.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocationSimple(context: Context, onResult: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    Log.e("LocationViewModel", "Gagal mendapatkan lokasi saat ini")
                }
            }
    }
}
