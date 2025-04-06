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

    // Mutable State yang hanya bisa diubah di dalam ViewModel
    private val _location = MutableStateFlow("Sedang mengambil lokasi Anda saat ini...")

    // State yang bisa di-observe dari luar (UI)
    val location: StateFlow<String> = _location

    private val _distance = MutableStateFlow(0f)
    val currentDistance: StateFlow<Float> = _distance

    // Ambil lokasi referensi dari Firebase Realtime Database
    private fun fetchReferenceLocation(onResult: (Double, Double) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("reference_location")
        ref.get().addOnSuccessListener { snapshot ->
            val lat = snapshot.child("latitude").getValue(Double::class.java)
            val long = snapshot.child("longitude").getValue(Double::class.java)

            Log.d("rawon", "Fetched from DB: lat=$lat, long=$long")


            if (lat != null && long != null) {
                onResult(lat, long)
            } else {
                Log.e("rawon", "Null latitude or longitude from DB")
            }
        }.addOnFailureListener {
            Log.e("rawon", "Failed to get reference location: ${it.message}")
        }
    }
    fun updateLocation(lat: Double, long: Double) {
        fetchReferenceLocation { refLat, refLng ->
            val distance = calculateDistanceInMeters(lat, long, refLat, refLng)

            val formattedDistance = String.format("%.0f", distance)
            _location.value = "Lokasi Anda berada di (%.5f, %.5f), berjarak ${formattedDistance}m dari titik absensi".format(lat, long)

            _distance.value = distance.toFloat()
        }
    }

}
