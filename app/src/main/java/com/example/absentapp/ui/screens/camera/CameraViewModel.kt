package com.example.absentapp.ui.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel untuk halaman kamera.
 * Menyimpan state bitmap hasil foto dan error yang terjadi.
 */
class CameraViewModel : ViewModel() {

    // List bitmap hasil pengambilan foto (bisa digunakan untuk histori, dsb.)
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps: StateFlow<List<Bitmap>> = _bitmaps.asStateFlow()

    // Error message yang muncul saat kamera gagal digunakan
    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError.asStateFlow()

    /**
     * Dipanggil saat berhasil ambil foto
     * Foto akan ditambahkan ke list bitmap
     */
    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }

    /**
     * Dipanggil saat terjadi kesalahan saat mengambil foto
     */
    fun setCameraError(message: String) {
        _cameraError.value = message
    }

    /**
     * Membersihkan pesan error setelah ditampilkan ke pengguna
     */
    fun clearError() {
        _cameraError.value = null
    }
}
