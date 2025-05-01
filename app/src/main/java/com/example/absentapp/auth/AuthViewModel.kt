package com.example.absentapp.auth

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absentapp.data.model.AttendanceStamp
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

// ViewModel utama untuk autentikasi dan proses absensi
class AuthViewModel : ViewModel() {

    // Inisialisasi Firebase Auth untuk autentikasi user
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Inisialisasi Firestore untuk operasi database absensi & jadwal
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Menyimpan status autentikasi seperti Authenticated, Unauthenticated, Error
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // Menyimpan data absensi dalam bentuk StateFlow untuk pemantauan real-time
    private val _absenTime = MutableStateFlow<List<AttendanceStamp>>(emptyList())
    val absenTime = _absenTime.asStateFlow()

    // Penanda bahwa proses absensi sedang berlangsung
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    // Mengecek status login user saat ViewModel dibuat
    init {
        checkAuthState()
    }

    // Mengambil data absensi secara real-time dari Firestore
    fun getAbsentTime() {
        Firebase.firestore.collection("absensi")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                value?.let {
                    _absenTime.value = it.toObjects()
                }
            }
    }

    // Mengambil email dari user yang sedang login
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    // Mengecek status autentikasi user dan memperbarui LiveData
    fun checkAuthState() {
        _authState.value = if (auth.currentUser == null) {
            AuthState.Unauthenticated
        } else {
            getAbsentTime()
            AuthState.Authenticated
        }
    }

    // Fungsi untuk login dengan email dan password
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email atau password tidak boleh kosong")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                getAbsentTime()
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login gagal")
            }
    }

    // Fungsi untuk mendaftarkan akun baru
    fun signup(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email atau password tidak boleh kosong")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                getAbsentTime()
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Registrasi gagal")
            }
    }

    // Mendapatkan jadwal absen untuk hari ini berdasarkan hari dalam bahasa Indonesia
    fun getTodaySchedule(onResult: (Jadwal?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val Today = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("id"))?.lowercase()

        db.collection("jadwal")
            .whereEqualTo("hari", Today)
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

    // Mendapatkan jadwal absen untuk hari esok
    fun getTomorrowSchedule(onResult: (Jadwal?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_WEEK, 1)
        }
        val tomorrow = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("id"))?.lowercase()

        db.collection("jadwal")
            .whereEqualTo("hari", tomorrow)
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

    // Mengambil seluruh data jadwal dari koleksi 'jadwal'
    fun getAllJadwal(onResult: (List<Jadwal>) -> Unit) {
        firestore.collection("jadwal")
            .get()
            .addOnSuccessListener { result ->
                val jadwalList = result.toObjects(Jadwal::class.java)
                onResult(jadwalList)
            }
            .addOnFailureListener { e ->
//                Log.e("JadwalFetch", "Gagal mengambil semua jadwal", e)
                onResult(emptyList())
            }
    }

    // Fungsi utama untuk absen masuk/keluar dengan foto (selfie)
    fun absenWithPhoto(bitmap: Bitmap) {
        _isUpdating.value = true
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Error("User tidak ditemukan")
            getAbsentTime()
            return
        }

        _authState.value = AuthState.Loading

        // Encode bitmap ke Base64 untuk disimpan di Firestore
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

            val now = Calendar.getInstance()
            val totalNow = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val (jamMasukHour, jamMasukMinute) = sanitizeTimeInput(jadwal.jamMasuk)
                .split(":").map { it.toIntOrNull() ?: 0 }

            val (jamKeluarHour, jamKeluarMinute) = sanitizeTimeInput(jadwal.jamKeluar)
                .split(":").map { it.toIntOrNull() ?: 0 }

            val totalMasuk = jamMasukHour * 60 + jamMasukMinute
            val totalKeluar = jamKeluarHour * 60 + jamKeluarMinute

            // Tentukan jenis absen dan catatan waktu keterlambatan/ketepatan
            val (timeNote, type) = when {
                totalNow <= totalMasuk -> "+ ${totalMasuk - totalNow}" to "masuk"
                totalNow <= totalKeluar -> "- ${totalNow - totalMasuk}" to "masuk"
                else -> {
                    handleCheckout(user.uid, user.email ?: "", encodedImage)
                    return@getTodaySchedule
                }
            }

            val absenData = mapOf(
                "type" to type,
                "uid" to user.uid,
                "name" to user.email,
                "timestamp" to FieldValue.serverTimestamp(),
                "photoBase64" to encodedImage,
                "timeNote" to timeNote
            )

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

    // Menangani logika checkout: cek duplikat, hitung durasi, simpan foto dan timestamp
    private fun handleCheckout(uid: String, email: String, encodedImage: String) {
        val firestoreRef = firestore.collection("absensi")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = dateFormat.format(Date())

        firestoreRef.whereEqualTo("uid", uid)
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

                firestoreRef.whereEqualTo("uid", uid)
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

                        val absenData = mapOf(
                            "type" to "keluar",
                            "uid" to uid,
                            "name" to email,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "photoBase64" to encodedImage,
                            "timeNote" to durasiNote
                        )

                        firestoreRef.add(absenData)
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

    // Fungsi untuk logout dari akun
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    // Menghapus semua data absensi milik user dari Firestore
    fun deleteAllAbsenceHistory() {
//        Log.d("DONAT", "kepanggil")
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("absensi")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("absensi").document(document.id).delete()
                    }
//                    Log.d("DONAT", "Semua data absensi berhasil dihapus.")
                }
                .addOnFailureListener { e ->
//                    Log.e("DONAT", "Gagal menghapus data absensi", e)
                }
        }
    }
}
