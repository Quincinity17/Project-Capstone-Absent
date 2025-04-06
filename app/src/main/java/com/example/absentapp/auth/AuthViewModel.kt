package com.example.absentapp.auth

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absentapp.data.model.AbsentStamp
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Base64
import com.example.absentapp.data.model.Jadwal
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Locale


class AuthViewModel : ViewModel() {

    // Instance Firebase Auth & Firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData untuk memantau status autentikasi (digunakan oleh UI)
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // StateFlow untuk menyimpan daftar waktu absen (realtime)
    private val _absenTime = MutableStateFlow<List<AbsentStamp>>(emptyList())
    val absenTime = _absenTime.asStateFlow()

    init {
        checkAuthState() // Cek status login saat ViewModel dibuat
    }

    /**
     * Ambil data absensi secara realtime dari Firestore
     */
    fun getAbsentTime() {
        Firebase.firestore.collection("absensi")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                value?.let {
                    _absenTime.value = it.toObjects()
                }
            }
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Cek apakah user sedang login atau tidak
     */
    fun checkAuthState() {
        _authState.value = if (auth.currentUser == null) {
            AuthState.Unauthenticated
        } else {
            AuthState.Authenticated
        }
    }

    /**
     * Login dengan email & password
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email or pass can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _authState.value = if (task.isSuccessful) {
                    AuthState.Authenticated
                } else {
                    AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Register akun baru
     */
    fun signup(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email or pass can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _authState.value = if (task.isSuccessful) {
                    AuthState.Authenticated
                } else {
                    AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    /**
     * Menambahkan data absen ke Firestore
     */
    fun absen() {
        auth.currentUser?.let { user ->
            val data = hashMapOf(
                "uid" to user.uid,
                "name" to user.email,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("absensi")
                .add(data)
                .addOnSuccessListener {
                    _authState.value = AuthState.Success("Absen berhasil!")
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Error("Gagal absen: ${it.message}")
                }
        } ?: run {
            _authState.value = AuthState.Error("User tidak ditemukan")
        }
    }

    fun getTodaySchedule(onResult: (Jadwal?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val hariIni = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("id"))?.lowercase()

        db.collection("jadwal")
            .whereEqualTo("hari", hariIni)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                val jadwal = doc?.toObject(Jadwal::class.java)
                onResult(jadwal)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    fun absenWithPhoto(bitmap: Bitmap, context: Context) {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Error("User tidak ditemukan")
            return
        }

        _authState.value = AuthState.Loading

        // Kompres dan konversi ke Base64
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos) // 50 = kompres sedang
        val imageBytes = baos.toByteArray()
        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        Log.d("Klepon", "✅ Gambar berhasil dikonversi ke Base64 (${encodedImage.length} karakter)")

        // Buat data absen
        val absenData = hashMapOf(
            "uid" to user.uid,
            "name" to user.email,
            "timestamp" to FieldValue.serverTimestamp(),
            "photoBase64" to encodedImage
        )

        firestore.collection("absensi")
            .add(absenData)
            .addOnSuccessListener {
                Log.d("Klepon", "✅ Data absen dengan gambar berhasil disimpan ke Firestore")
                _authState.value = AuthState.Success("Absen dengan foto berhasil!")
            }
            .addOnFailureListener {
                Log.e("Klepon", "❌ Gagal simpan absen: ${it.message}")
                _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
            }
    }




    /**
     * Logout user
     */
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Absen sambil menyertakan lokasi
     */
    fun absenWithLocation(context: Context) {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Error("User tidak ditemukan")
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            _authState.value = AuthState.Error("Permission lokasi belum diizinkan!")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    _authState.value = AuthState.Error("Lokasi tidak ditemukan")
                    return@addOnSuccessListener
                }

                val data = hashMapOf(
                    "uid" to user.uid,
                    "name" to user.email,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                )

                firestore.collection("absensi")
                    .add(data)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success("Absen + lokasi berhasil!")
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error("Gagal absen: ${it.message}")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error ambil lokasi: ${it.message}")
            }
    }
}
