package com.example.orderly.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orderly.R
// Pastikan import repository ini sesuai nama package kamu
import com.example.orderly.data.AuthRepository

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    // --- WARNA TEMA ---
    val PrimaryPurple = Color(0xFF6C5CE7)
    val TextGray = Color(0xFF888888)
    val ErrorRed = Color(0xFFFF4D4D)

    // State Input
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibility Password
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Checkbox Terms
    var isTermsAccepted by remember { mutableStateOf(false) }

    // Error Message & LOADING STATE (Fitur Baru)
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // <-- Penambahan Penting

    Row(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // ... (BAGIAN UI ATAS TETAP SAMA) ...

        // Langsung lompat ke bagian LOGIC di dalam Column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 64.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // ... (Kode Text Judul dll TETAP SAMA, tidak saya tulis ulang biar hemat tempat) ...

            Spacer(modifier = Modifier.height(32.dp))
            Text("Orderly LAWU Cafe", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Buat Akun Baru", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("Kelola Bisnis Dengan Lebih Mudah", fontSize = 14.sp, color = TextGray)
            Spacer(modifier = Modifier.height(24.dp))

            // INPUT FIELD (Tambahkan enabled = !isLoading supaya gak bisa diedit pas lagi loading)
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it },
                label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isLoading, // <-- Kunci UX
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = Color.LightGray),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = Color.LightGray),
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ... (Kode Password & Confirm Password SAMA, cuma tambah enabled = !isLoading) ...
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = Color.LightGray), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) { Icon(if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = Color.LightGray, errorBorderColor = ErrorRed), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it },
                    enabled = !isLoading, // <-- Disable pas loading
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                )
                Text("Saya setuju dengan Syarat dan Ketentuan", fontSize = 14.sp)
            }

            // Pesan Error
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === TOMBOL DAFTAR (LOGIC DIPERBAIKI) ===
            Button(
                onClick = {
                    errorMessage = "" // Reset error dulu

                    // Validasi Dasar
                    if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Semua kolom harus diisi!"
                    } else if (password != confirmPassword) {
                        errorMessage = "Password dan Konfirmasi tidak cocok!"
                    } else if (!isTermsAccepted) {
                        errorMessage = "Anda harus menyetujui Syarat & Ketentuan"
                    } else {
                        // Mulai Loading
                        isLoading = true

                        // Panggil Repository
                        AuthRepository.registerUser(
                            fullName = fullName,
                            email = email,
                            pass = password,
                            onSuccess = {
                                isLoading = false // Stop loading
                                onSignUpSuccess() // Pindah halaman
                            },
                            onError = { errorMsg ->
                                isLoading = false // Stop loading
                                errorMessage = errorMsg // Tampilkan error
                            }
                        )
                    }
                },
                // Disable tombol kalau lagi loading biar gak di-spam
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                // Ganti teks dengan Loading Spinner kalau lagi loading
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memproses...", fontSize = 16.sp)
                } else {
                    Text("Daftar Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer (Disable klik pas loading)
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Sudah punya akun? ", color = TextGray, fontSize = 14.sp)
                Text(
                    text = "Masuk",
                    color = if (isLoading) TextGray else PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToLogin() }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // BAGIAN KANAN: GAMBAR (TETAP SAMA)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_page), // Pastikan nama file benar
                contentDescription = "Login Illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}