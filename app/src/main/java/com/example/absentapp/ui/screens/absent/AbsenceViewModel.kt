package com.example.absentapp.ui.screens.absent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absentapp.data.model.Absence
import com.example.absentapp.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel untuk menangani data perizinan (Absence) dan komentar (Comment)
 * Mengelola fetch, posting, penghapusan data, dan penyimpanan lokal sementara dengan StateFlow.
 */
class AbsenceViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    // Semua data absensi
    private val _allAbsences = MutableStateFlow<List<Absence>>(emptyList())
    val allAbsences: StateFlow<List<Absence>> get() = _allAbsences

    // Pemetaan komentar berdasarkan ID absensi
    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<Comment>>> get() = _commentsMap

    /**
     * Mengambil objek Absence berdasarkan ID.
     */
    fun getAbsenceById(id: String): Absence {
        return _allAbsences.value.first { it.id == id }
    }

    /**
     * Menghapus semua data absensi dan komentar dari Firestore.
     */
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
                        _allAbsences.value = emptyList()
                        _commentsMap.value = emptyMap()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Gagal menghapus semua data perizinan.")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal mengambil data.")
            }
    }

    /**
     * Menambahkan data perizinan ke Firestore.
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
            photoUrl = "",
            reason = reason
        )

        firestore.collection("Absence")
            .document(id)
            .set(absence)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menyimpan data")
            }
    }

    /**
     * Menambahkan komentar pada absensi tertentu.
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
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menambahkan komentar")
            }
    }

    /**
     * Mengambil komentar dari sebuah absensi dan menyimpannya di local map.
     */
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

    /**
     * Mengambil semua data absensi dari Firestore.
     */
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
