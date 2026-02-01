package com.example.orderly.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Fungsi Sign Up
    fun registerUser(
        fullName: String,
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {
                    // --- PERBAIKAN PENTING ---
                    // Karena ini akun kamu (Nurul), kita set role jadi "admin".
                    // Nanti kalau mau bikin fitur tambah pegawai, baru ubah logic-nya.
                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "role" to "admin", // <--- UBAH INI JADI ADMIN
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Simpan ke Firestore dengan ID yang sama dengan UID Auth
                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Jika gagal simpan data, hapus akun di Auth biar bersih
                            result.user?.delete()
                            onError("Gagal menyimpan data profil: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal mendaftar")
            }
    }

    // Fungsi Login (TETAP PERTAHANKAN INI)
    fun loginUser(
        email: String,
        pass: String,
        onSuccess: (String) -> Unit, // String = Role
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId != null) {
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Ambil role, kalau kosong anggap cashier
                                val role = document.getString("role") ?: "cashier"
                                onSuccess(role)
                            } else {
                                onError("Data user hilang di database. Hubungi Admin.")
                            }
                        }
                        .addOnFailureListener {
                            onError("Gagal koneksi ke database.")
                        }
                }
            }
            .addOnFailureListener {
                onError("Email atau Password salah.")
            }
    }

    // Fungsi Reset Password (TETAP PERTAHANKAN INI)
    fun resetPassword(email: String, onResult: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult("Link reset password telah dikirim ke email Anda") }
            .addOnFailureListener { onResult("Gagal: ${it.message}") }
    }
}