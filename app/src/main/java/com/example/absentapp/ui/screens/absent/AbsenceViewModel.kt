package com.example.absentapp.ui.screens.absent

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absentapp.data.model.Absence
import com.example.absentapp.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class AbsenceViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private val _allAbsences = MutableStateFlow<List<Absence>>(emptyList())
    val allAbsences: StateFlow<List<Absence>> get() = _allAbsences

    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<Comment>>> get() = _commentsMap


    fun getAbsenceById(id: String): Absence {
        return _allAbsences.value.first { it.id == id }

    }

    fun deleteAllAbsences(onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        firestore.collection("Absence")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("AbsenceViewModel", "Semua data perizinan berhasil dihapus.")
                        _allAbsences.value = emptyList()
                        _commentsMap.value = emptyMap()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AbsenceViewModel", "Gagal hapus perizinan: ${e.message}")
                        onFailure(e.message ?: "Gagal menghapus semua data perizinan.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AbsenceViewModel", "Gagal ambil data sebelum hapus: ${e.message}")
                onFailure(e.message ?: "Gagal mengambil data.")
            }
    }



    /**
     * Upload foto ke Firebase Storage dan simpan data absensi ke Firestore
     */
    fun postAbsence(
        userEmail: String,
        reason: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val id = UUID.randomUUID().toString()

        val absence = Absence(
            id = id,
            userEmail = userEmail,
            timestamp = System.currentTimeMillis(),
            photoUrl = "", // kosongkan
            reason = reason // tambahkan ini ke data class jika belum
        )

        firestore.collection("Absence")
            .document(id)
            .set(absence)
            .addOnSuccessListener {
                Log.d("PERIZINAN", "Data perizinan berhasil disimpan")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("PERIZINAN", "Gagal simpan izin: ${e.message}")
                onFailure(e.message ?: "Gagal menyimpan data")
            }
    }



    /**
     * Tambahkan komentar ke sebuah post absensi
     */
    fun addCommentToAbsence(
        absenceId: String,
        commenterId: String,
        commenterEmail: String,
        commentText: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val comment = Comment(
            commenterID = commenterId,
            commenterEmail = commenterEmail,
            commentText = commentText,
            commentedAt = System.currentTimeMillis()
        )

        val commentsRef = firestore.collection("Absence")
            .document(absenceId)
            .collection("comments")

        commentsRef.add(comment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Gagal menambahkan komentar") }
    }

    fun loadCommentsForAbsence(absenceId: String) {
        firestore.collection("Absence")
            .document(absenceId)
            .collection("comments")
            .get()
            .addOnSuccessListener { snapshot ->
                val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
                _commentsMap.value = _commentsMap.value.toMutableMap().apply {
                    put(absenceId, comments)
                }
            }
            .addOnFailureListener {
                Log.e("COMMENT", "Gagal ambil komentar: ${it.message}")
            }
    }



    fun getAllAbsences() {
        viewModelScope.launch {
            firestore.collection("Absence")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { it.toObject(Absence::class.java) }
                    _allAbsences.value = list
                }
                .addOnFailureListener { e ->
                    Log.e("AbsenceViewModel", "Gagal ambil data absensi: ${e.message}")
                }
        }
    }
}