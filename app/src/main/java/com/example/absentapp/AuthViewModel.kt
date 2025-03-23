package com.example.absentapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AuthViewModel : ViewModel(){
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    val absenTime = MutableLiveData<String?>()

//    private val firestore = FirebaseFirestore.getInstance()



    init {
        checkAuthState()
    }

    fun checkAuthState(){
        if (auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login (email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or pass cant be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun signup (email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or pass cant be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun absen() {

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        absenTime.value = time


        val user = auth.currentUser
        if (user != null) {
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
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error("Gagal absen: ${e.message}")
                }
        } else {
            _authState.value = AuthState.Error("User tidak ditemukan")
        }
    }



    fun signout (){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated

    }


}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()


}