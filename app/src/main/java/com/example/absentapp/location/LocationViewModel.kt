package com.example.absentapp.location


import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.absentapp.utils.calculateDistanceInMeters
import com.google.firebase.database.FirebaseDatabase


/**
 * ViewModel khusus untuk menyimpan lokasi terkini.
 * Lokasi ini di-observe oleh UI (Jetpack Compose) secara reactive via StateFlow.
 */
class LocationViewModel : ViewModel() {

    private val _location = MutableStateFlow("Sedang mengambil lokasi Anda saat ini...")
    val location: StateFlow<String> = _location

    private val _distance = MutableStateFlow(0f)
    val currentDistance: StateFlow<Float> = _distance

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation

    private val _distanceLimit = MutableStateFlow(20) // default 20 meter
    val distanceLimit: StateFlow<Int> = _distanceLimit

    // 🔽 Ambil lat, long, dan limit dari Firebase
    private fun fetchReferenceLocation(onResult: (Double, Double, Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("reference_location")
        ref.get().addOnSuccessListener { snapshot ->
            val lat = snapshot.child("latitude").getValue(Double::class.java)
            val long = snapshot.child("longitude").getValue(Double::class.java)
            val limit = snapshot.child("Limit").getValue(Int::class.java) ?: 20

            Log.d("rawon", "Fetched lat=$lat, long=$long, limit=$limit")

            if (lat != null && long != null) {
                onResult(lat, long, limit)
            } else {
                Log.e("rawon", "Null latitude or longitude from DB")
            }
        }.addOnFailureListener {
            Log.e("rawon", "Failed to get reference location: ${it.message}")
        }
    }

    // 🔄 Update distance & limit
    fun updateLocation(lat: Double, long: Double) {
        _isFetchingLocation.value = true

        fetchReferenceLocation { refLat, refLng, limit ->
            val distance = calculateDistanceInMeters(lat, long, refLat, refLng)
            val formattedDistance = String.format("%.0f", distance)

            _location.value = "Lokasi Anda berada di (%.5f, %.5f), berjarak ${formattedDistance}m dari titik absensi".format(lat, long)
            _distance.value = distance.toFloat()
            _distanceLimit.value = limit

            _isFetchingLocation.value = false
        }
    }

    // ✍️ Update limit dari UI ke Firebase
    fun updateDistanceLimit(newLimit: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("reference_location/Limit")
        ref.setValue(newLimit)
            .addOnSuccessListener {
                Log.d("rawon", "Limit updated to $newLimit")
                _distanceLimit.value = newLimit
            }
            .addOnFailureListener {
                Log.e("rawon", "Failed to update limit: ${it.message}")
            }
    }
}
