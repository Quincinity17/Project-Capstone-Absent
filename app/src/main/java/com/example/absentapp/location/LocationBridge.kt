package com.example.absentapp.location

/**
 * Singleton bridge untuk menghubungkan antara background Service dan ViewModel (yang ada di UI).
 * Dipakai supaya LocationService bisa update data ke UI meskipun UI tidak aktif secara langsung.
 */
object LocationBridge {
    var viewModel: LocationViewModel? = null
}
