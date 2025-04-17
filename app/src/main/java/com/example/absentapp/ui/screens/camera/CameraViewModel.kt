package com.example.absentapp.ui.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    // Daftar bitmap hasil foto
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps: StateFlow<List<Bitmap>> = _bitmaps.asStateFlow()

    // Error message untuk ditampilkan di dialog
    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError.asStateFlow()

    /**
     * Dipanggil saat berhasil ambil foto
     */
    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }

    /**
     * Set error saat terjadi kegagalan pengambilan gambar
     */
    fun setCameraError(message: String) {
        _cameraError.value = message
    }

    /**
     * Reset error setelah ditampilkan ke UI
     */
    fun clearError() {
        _cameraError.value = null
    }
}
