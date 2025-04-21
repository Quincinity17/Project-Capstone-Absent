package com.example.absentapp.auth

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absentapp.data.model.AbsentStamp
import com.example.absentapp.data.model.Jadwal
import com.example.absentapp.utils.sanitizeTimeInput
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()


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

    /**
     * Ambil data email pengguna yang login saat ini
     */
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Cek apakah user sedang login atau tidak
     */
    fun checkAuthState() {
        _authState.value = if (auth.currentUser == null) {
            AuthState.Unauthenticated
        } else {
            getAbsentTime()
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

    /**
     * Melakukan proses absensi dengan menyertakan foto dalam bentuk bitmap.
     * Proses ini mencakup: encoding gambar, pengecekan jadwal, perhitungan waktu,
     * serta menyimpan data ke Firestore.
     */
    fun absenWithPhoto(bitmap: Bitmap) {
        _isUpdating.value = true
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Error("User tidak ditemukan")
            getAbsentTime()

            return
        }

        _authState.value = AuthState.Loading

        // Encode bitmap menjadi Base64 string
        val encodedImage = ByteArrayOutputStream().use { baos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        }

        // Ambil jadwal hari ini
        getTodaySchedule { jadwal ->
            if (jadwal == null) {
                _authState.value = AuthState.Error("Jadwal hari ini tidak ditemukan")
                return@getTodaySchedule
            }

            // Hitung waktu sekarang dalam menit
            val now = Calendar.getInstance()
            val totalNow = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            // Hitung waktu masuk dan keluar dari jadwal
            val (jamMasukHour, jamMasukMinute) = sanitizeTimeInput(jadwal.jamMasuk)
                .split(":")
                .map { it.toIntOrNull() ?: 0 }

            val (jamKeluarHour, jamKeluarMinute) = sanitizeTimeInput(jadwal.jamKeluar)
                .split(":")
                .map { it.toIntOrNull() ?: 0 }

            val totalMasuk = jamMasukHour * 60 + jamMasukMinute
            val totalKeluar = jamKeluarHour * 60 + jamKeluarMinute

            // Tentukan jenis absen dan catatan waktunya
            val (timeNote, type) = when {
                totalNow <= totalMasuk -> "+ ${totalMasuk - totalNow}" to "masuk"
                totalNow <= totalKeluar -> "- ${totalNow - totalMasuk}" to "masuk"
                else -> {
                    // Jika sudah lewat jam keluar, lakukan proses checkout
                    handleCheckout(user.uid, user.email ?: "", encodedImage)
                    return@getTodaySchedule
                }
            }

            // Data absen untuk check-in
            val absenData = mapOf(
                "type" to type,
                "uid" to user.uid,
                "name" to user.email,
                "timestamp" to FieldValue.serverTimestamp(),
                "photoBase64" to encodedImage,
                "timeNote" to timeNote
            )

            // Simpan data absen
            firestore.collection("absensi")
                .add(absenData)
                .addOnSuccessListener {
                    _authState.value = AuthState.Success("Checkin berhasil!")
                    _isUpdating.value = false
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
                    _isUpdating.value = false
                }
        }
    }

    /**
     * Proses absensi "keluar" (checkout).
     * Mengecek apakah user sudah absen keluar sebelumnya dan hapus data yang duplikat.
     * Juga menghitung durasi kehadiran dari absen masuk.
     */
    private fun handleCheckout(uid: String, email: String, encodedImage: String) {
        val firestoreRef = firestore.collection("absensi")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = dateFormat.format(Date())

        firestoreRef
            .whereEqualTo("uid", uid)
            .whereEqualTo("type", "masuk")
            .get()
            .addOnSuccessListener { masukDocs ->
                val masukHariIni = masukDocs.documents
                    .mapNotNull { it.getTimestamp("timestamp")?.toDate() }
                    .firstOrNull { dateFormat.format(it) == todayKey }

                val durasiNote = masukHariIni?.let {
                    val diffMillis = Date().time - it.time
                    val minutes = diffMillis / 60000
                    val hours = minutes / 60
                    "Hadir selama $hours jam ${minutes % 60} menit"
                } ?: "Tidak absen masuk"

                firestoreRef
                    .whereEqualTo("uid", uid)
                    .whereEqualTo("type", "keluar")
                    .get()
                    .addOnSuccessListener { checkoutDocs ->
                        checkoutDocs.documents.filter { doc ->
                            doc.getTimestamp("timestamp")?.let { ts ->
                                dateFormat.format(ts.toDate()) == todayKey
                            } ?: false
                        }.forEach { doc ->
                            firestoreRef.document(doc.id).delete()
                        }

                        // Data checkout baru
                        val absenData = mapOf(
                            "type" to "keluar",
                            "uid" to uid,
                            "name" to email,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "photoBase64" to encodedImage,
                            "timeNote" to durasiNote
                        )

                        firestoreRef
                            .add(absenData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success("Checkout berhasil!")
                                getAbsentTime()
                                _isUpdating.value = false

                            }
                            .addOnFailureListener {
                                _authState.value = AuthState.Error("Gagal simpan absen: ${it.message}")
                                _isUpdating.value = false
                            }
                    }
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
     * Menghapus semua data absensi dari pengguna
     */
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
}
