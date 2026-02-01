package com.example.orderly.ui.common

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orderly.R
import com.example.orderly.data.AuthRepository // Import ini biar kodingan lebih bersih

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    // --- WARNA TEMA ---
    val PrimaryPurple = Color(0xFF6C5CE7)
    val TextGray = Color(0xFF888888)
    val ErrorRed = Color(0xFFFF4D4D)

    val context = LocalContext.current

    // State Input Login
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    // State UI: Error & Loading
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // <-- FITUR BARU: LOADING

    // State Dialog Forgot Password
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // ==========================================
    // LOGIKA POP-UP DIALOG (FORGOT PASSWORD)
    // ==========================================
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Penjelasan yang benar sesuai cara kerja Firebase
                    Text(
                        "Masukkan email Anda. Kami akan mengirimkan link untuk membuat password baru.",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // CUKUP MINTA EMAIL SAJA
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Akun") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isBlank()) {
                            Toast.makeText(context, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Panggil Repository
                            AuthRepository.resetPassword(resetEmail) { pesan ->
                                Toast.makeText(context, pesan, Toast.LENGTH_LONG).show()
                                showForgotPasswordDialog = false // Tutup dialog apapun hasilnya
                                resetEmail = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Kirim Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Batal", color = TextGray)
                }
            },
            containerColor = Color.White
        )
    }

    // ==========================================
    // TAMPILAN UTAMA
    // ==========================================
    Row(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // --- BAGIAN KIRI ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 64.dp), // Padding kiri-kanan
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Orderly LAWU Cafe", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Holla,\nSelamat Datang",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                lineHeight = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Welcome back to your special place", fontSize = 14.sp, color = TextGray)

            Spacer(modifier = Modifier.height(32.dp))

            // INPUT EMAIL (Disable saat loading)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading, // <-- PENTING
                isError = errorMessage.isNotEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor = ErrorRed
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // INPUT PASSWORD (Disable saat loading)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading, // <-- PENTING
                isError = errorMessage.isNotEmpty(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = TextGray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.LightGray,
                    errorBorderColor = ErrorRed
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            // Tampilkan Pesan Error Login
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Me & Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = !isLoading,
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                    )
                    Text("Remember me", fontSize = 14.sp, color = Color.Black)
                }

                Text(
                    text = "Lupa Password?",
                    fontSize = 14.sp,
                    color = if (isLoading) Color.LightGray else Color.Gray,
                    modifier = Modifier.clickable(enabled = !isLoading) { showForgotPasswordDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL LOGIN (DENGAN LOADING STATE)
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email dan Password tidak boleh kosong!"
                        return@Button
                    }

                    isLoading = true // Mulai Loading
                    errorMessage = "" // Bersihkan error lama

                    AuthRepository.loginUser(
                        email = email,
                        pass = password,
                        onSuccess = { role ->
                            isLoading = false // Stop Loading
                            onLoginSuccess(role) // Pindah Halaman
                        },
                        onError = { errorMsg ->
                            isLoading = false // Stop Loading
                            errorMessage = errorMsg // Munculkan Error Merah
                        }
                    )
                },
                enabled = !isLoading, // Tombol mati saat loading
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memproses...", fontSize = 16.sp)
                } else {
                    Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Belum punya akun? ", color = TextGray, fontSize = 14.sp)
                Text(
                    text = "Daftar",
                    color = if (isLoading) TextGray else PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToSignUp() }
                )
            }
        }

        // --- BAGIAN KANAN: GAMBAR (TETAP SAMA) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_page),
                contentDescription = "Login Illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}