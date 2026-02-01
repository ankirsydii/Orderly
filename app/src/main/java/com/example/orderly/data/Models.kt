package com.example.orderly.data

import androidx.compose.ui.graphics.Color

// Model Menu Makanan (Dipakai Admin & Kasir)
// Kita simpan warna sebagai Angka (Long) agar bisa disimpan di Firebase
data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val colorHex: Long = 0xFF6C5CE7,
    val isAvailable: Boolean = true,

    // TAMBAHAN BARU:
    val imageUrl: String = "" // Menyimpan link gambar
) {
    constructor() : this("", "", 0.0, "", 0xFF6C5CE7, true, "")

    fun getColor(): Color {
        return Color(colorHex)
    }
}

data class CartItem(
    val productName: String = "",

    // --- PERBAIKAN: Ganti 'productPrice' jadi 'price' ---
    // PENTING: Harus ditaruh di dalam kurung (...) ini supaya dibaca Firebase
    val price: Double = 0.0,
    // ----------------------------------------------------

    val quantity: Int = 0
) {
    // Constructor kosong wajib buat Firebase
    constructor() : this("", 0.0, 0)
}

// Model Transaksi / Order (Disimpan Kasir -> Dibaca Admin)
data class Order(
    val id: String = "",
    val orderNumber: Int = 0,
    val date: String = "",
    val totalAmount: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    val cashierName: String = "Kasir"
)