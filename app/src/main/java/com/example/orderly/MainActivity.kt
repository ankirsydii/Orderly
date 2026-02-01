package com.example.orderly

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
// Import Halaman UI
import com.example.orderly.ui.admin.AdminDashboardScreen
import com.example.orderly.ui.cashier.CashierPosScreen
import com.example.orderly.ui.common.LoginScreen
import com.example.orderly.ui.common.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrderlyAppNavigation()
                }
            }
        }
    }
}

@Composable
fun OrderlyAppNavigation() {
    val navController = rememberNavController()

    // UBAH startDestination ke "splash_screen"
    // Supaya aplikasi nge-cek dulu sebelum nampilin halaman apapun
    NavHost(
        navController = navController,
        startDestination = "splash_screen"
    ) {

        // 0. SPLASH SCREEN (Gerbang Pengecekan)
        composable("splash_screen") {
            SplashScreen(navController)
        }

        // 1. HALAMAN LOGIN
        composable("login_screen") {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "admin") {
                        navController.navigate("admin_dashboard") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    } else {
                        navController.navigate("cashier_pos") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup_screen")
                }
            )
        }

        // 2. HALAMAN SIGN UP
        composable("signup_screen") {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { navController.popBackStack() }
            )
        }

        // 3. HALAMAN ADMIN
        composable("admin_dashboard") {
            AdminDashboardScreen(
                onLogout = {
                    // Saat Logout, Hapus Sesi Firebase
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login_screen") {
                        popUpTo(0)
                    }
                }
            )
        }

        // 4. HALAMAN KASIR
        composable("cashier_pos") {
            CashierPosScreen(
                onLogout = {
                    // Saat Logout, Hapus Sesi Firebase
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login_screen") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
// === KOMPONEN BARU: LAYAR PENGECEKAN ===
@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // --- KODE ANIMASI LOTTIE ---

        // 1. Ambil file json dari folder raw
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_food))

        // 2. Tampilkan Animasinya
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Gerak terus tanpa henti
            modifier = Modifier.size(250.dp) // Sesuaikan ukuran animasinya
        )

        // Opsional: Tambah teks di bawahnya
        Text(
            text = "Menyiapkan Dapur...",
            modifier = Modifier.padding(top = 200.dp), // Geser ke bawah animasi
            color = Color(0xFF6C5CE7),
            fontWeight = FontWeight.Bold
        )
    }

    // LOGIKA PENGECEKAN OTOMATIS
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // A. USER SUDAH LOGIN -> Cek Role di Database
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role") ?: "cashier"

                    // Pindah Halaman Sesuai Role
                    if (role == "admin") {
                        navController.navigate("admin_dashboard") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    } else {
                        navController.navigate("cashier_pos") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener {
                    // Kalau gagal baca database (misal internet mati), lempar ke login aja biar aman
                    navController.navigate("login_screen") {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
        } else {
            // B. BELUM LOGIN -> Masuk ke Login Screen
            navController.navigate("login_screen") {
                popUpTo("splash_screen") { inclusive = true }
            }
        }
    }
}