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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        getTodaySchedule { jadwal ->
            if (jadwal == null) {
                _authState.value = AuthState.Error("Jadwal hari ini tidak ditemukan")
                return@getTodaySchedule
            }

            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val totalNow = currentHour * 60 + currentMinute

            val (jamMasukHour, jamMasukMinute) = jadwal.jamMasuk.split(":").map { it.toInt() }
            val (jamKeluarHour, jamKeluarMinute) = jadwal.jamKeluar.split(":").map { it.toInt() }
            val totalMasuk = jamMasukHour * 60 + jamMasukMinute
            val totalKeluar = jamKeluarHour * 60 + jamKeluarMinute

            val (timeNote, type) = when {
                totalNow <= totalMasuk -> "+ ${totalMasuk - totalNow}" to "masuk"
                totalNow <= totalKeluar -> "- ${totalNow - totalMasuk}" to "masuk"
                else -> {
                    val type = "keluar"
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayKey = dateFormat.format(Date())

                    firestore.collection("absensi")
                        .whereEqualTo("uid", user.uid)
                        .whereEqualTo("type", "masuk")
                        .get()
                        .addOnSuccessListener { masukDocs ->
                            val masukHariIni = masukDocs.documents
                                .mapNotNull { it.getTimestamp("timestamp")?.toDate() }
                                .filter { dateFormat.format(it) == todayKey }
                                .minByOrNull { it.time }

                            val durasiNote = if (masukHariIni != null) {
                                val now = Date()
                                val diffMillis = now.time - masukHariIni.time
                                val totalMinutes = diffMillis / (1000 * 60)
                                val hours = totalMinutes / 60
                                val minutes = totalMinutes % 60
                                "Hadir selama ${hours} jam ${minutes} menit"
                            } else {
                                "Hadir hari ini"
                            }

                            // Lanjut: hapus checkout sebelumnya
                            firestore.collection("absensi")
                                .whereEqualTo("uid", user.uid)
                                .whereEqualTo("type", "keluar")
                                .get()
                                .addOnSuccessListener { checkoutDocs ->
                                    val toDelete = checkoutDocs.documents.filter { doc ->
                                        val ts = doc.getTimestamp("timestamp")?.toDate()
                                        ts != null && dateFormat.format(ts) == todayKey
                                    }

                                    toDelete.forEach { doc ->
                                        firestore.collection("absensi").document(doc.id).delete()
                                    }

                                    // Tambahkan absen keluar terbaru
                                    val absenData = hashMapOf(
                                        "type" to type,
                                        "uid" to user.uid,
                                        "name" to user.email,
                                        "timestamp" to FieldValue.serverTimestamp(),
                                        "photoBase64" to encodedImage,
                                        "timeNote" to durasiNote
                                    )

                                    firestore.collection("absensi")
                                        .add(absenData)
                                        .addOnSuccessListener {
                                            _authState.value = AuthState.Success("Checkout berhasil!")
                                        }
                                        .addOnFailureListener {
                                            _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
                                        }
                                }
                        }

                    return@getTodaySchedule // keluar dari fungsi di sini
                }
            }

            val absenData = hashMapOf(
                "type" to type,
                "uid" to user.uid,
                "name" to user.email,
                "timestamp" to FieldValue.serverTimestamp(),
                "photoBase64" to encodedImage,
                "timeNote" to timeNote
            )

            if (type == "keluar") {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayKey = dateFormat.format(Date())

                firestore.collection("absensi")
                    .whereEqualTo("uid", user.uid)
                    .whereEqualTo("type", "keluar")
                    .get()
                    .addOnSuccessListener { documents ->
                        val toDelete = documents.documents.filter { doc ->
                            val ts = doc.getTimestamp("timestamp")?.toDate()
                            ts != null && dateFormat.format(ts) == todayKey
                        }

                        toDelete.forEach { doc ->
                            firestore.collection("absensi").document(doc.id).delete()
                        }

                        // Tambah data absen checkout terbaru
                        firestore.collection("absensi")
                            .add(absenData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success("Checkout berhasil!")
                            }
                            .addOnFailureListener {
                                _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error("Gagal cek absen keluar sebelumnya")
                    }
            } else {
                // Tambah absen masuk langsung
                firestore.collection("absensi")
                    .add(absenData)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success("Checkin berhasil!")
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
                    }
            }
        }
    }


//    fun absenWithPhoto(bitmap: Bitmap, context: Context) {
//        val user = auth.currentUser
//        if (user == null) {
//            _authState.value = AuthState.Error("User tidak ditemukan")
//            return
//        }
//
//        _authState.value = AuthState.Loading
//
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//        val imageBytes = baos.toByteArray()
//        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
//
//        getTodaySchedule { jadwal ->
//            if (jadwal == null) {
//                _authState.value = AuthState.Error("Jadwal hari ini tidak ditemukan")
//                return@getTodaySchedule
//            }
//
//            val now = Calendar.getInstance()
//            val currentHour = now.get(Calendar.HOUR_OF_DAY)
//            val currentMinute = now.get(Calendar.MINUTE)
//            val totalNow = currentHour * 60 + currentMinute
//
//            val (jamMasukHour, jamMasukMinute) = jadwal.jamMasuk.split(":").map { it.toInt() }
//            val (jamKeluarHour, jamKeluarMinute) = jadwal.jamKeluar.split(":").map { it.toInt() }
//            val totalMasuk = jamMasukHour * 60 + jamMasukMinute
//            val totalKeluar = jamKeluarHour * 60 + jamKeluarMinute
//
//            val (timeNote, type) = when {
//                totalNow <= totalMasuk -> "+ ${totalMasuk - totalNow}" to "masuk"
//                totalNow <= totalKeluar -> "- ${totalNow - totalMasuk}" to "masuk"
//                else -> "Absen setelah jam keluar" to "keluar"
//            }
//
//            val absenData = hashMapOf(
//                "type" to type,
//                "uid" to user.uid,
//                "name" to user.email,
//                "timestamp" to FieldValue.serverTimestamp(),
//                "photoBase64" to encodedImage,
//                "timeNote" to timeNote,
//
//            )
//
//            firestore.collection("absensi")
//                .add(absenData)
//                .addOnSuccessListener {
//                    _authState.value = AuthState.Success("Absen $type berhasil!")
//                }
//                .addOnFailureListener {
//                    _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
//                }
//        }
//    }



//    fun absenWithPhoto(bitmap: Bitmap, context: Context) {
//        val user = auth.currentUser
//        if (user == null) {
//            _authState.value = AuthState.Error("User tidak ditemukan")
//            return
//        }
//
//        _authState.value = AuthState.Loading
//
//        // Kompres dan konversi ke Base64
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos) // 50 = kompres sedang
//        val imageBytes = baos.toByteArray()
//        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
//
//        // Buat data absen
//        val absenData = hashMapOf(
//            "uid" to user.uid,
//            "name" to user.email,
//            "timestamp" to FieldValue.serverTimestamp(),
//            "photoBase64" to encodedImage
//        )
//
//        firestore.collection("absensi")
//            .add(absenData)
//            .addOnSuccessListener {
//                _authState.value = AuthState.Success("Absen dengan foto berhasil!")
//            }
//            .addOnFailureListener {
//                _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
//            }
//    }




    /**
     * Logout user
     */
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun deleteAllAbsenceHistory() {
        Log.d("DeleteAbsensi", "kepanggil")

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("absensi")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("absensi").document(document.id).delete()
                    }
                    Log.d("DeleteAbsensi", "Semua data absensi berhasil dihapus.")
                }
                .addOnFailureListener { e ->
                    Log.e("DeleteAbsensi", "Gagal menghapus data absensi", e)
                }
        }
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
