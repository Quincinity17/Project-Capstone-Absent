package com.example.absentapp.location

/**
 * Singleton bridge untuk menghubungkan antara background Service dan ViewModel (yang ada di UI).
 * Digunakan agar LocationService bisa mengirim data lokasi ke UI (melalui ViewModel),
 * bahkan ketika UI sedang tidak aktif secara langsung.
 */
object LocationBridge {
    // Referensi ke ViewModel yang aktif, bisa di-update dari service
    var viewModel: LocationViewModel? = null
}
