package com.example.orderly.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- BAGIAN PRODUK (MENU) ---

    // 1. Ambil Semua Menu (Realtime) + Error Handling
    fun getProductsRealtime(
        onData: (List<Product>) -> Unit,
        onError: (String) -> Unit // Tambahan: Biar UI tau kalau error
    ) {
        db.collection("products")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil data menu")
                    return@addSnapshotListener
                }
                // Mapping data
                val list = value?.toObjects(Product::class.java) ?: emptyList()
                onData(list)
            }
    }

    // 2. Tambah Menu Baru (Dari Admin)
    fun addProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Tips: Gunakan .set() jika ID sudah ditentukan dari UI/ViewModel
        db.collection("products").document(product.id).set(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal tambah produk") }
    }

    // 3. Update Menu (Fitur Baru: PENTING untuk Admin)
    fun updateProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Konsepnya sama kayak add, dia bakal menimpa data lama berdasarkan ID
        db.collection("products").document(product.id).set(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal update produk") }
    }

    // 4. Hapus Menu
    fun deleteProduct(productId: String, onSuccess: () -> Unit) {
        db.collection("products").document(productId).delete()
            .addOnSuccessListener { onSuccess() }
    }

    // --- BAGIAN ORDER (TRANSAKSI) ---

    // 1. Simpan Order Baru
    fun addOrder(order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("orders").document(order.id).set(order)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal membuat order") }
    }

    // 2. Ambil Riwayat Order
    fun getOrdersRealtime(
        onData: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("orders")
            // Tips: Pastikan field "date" atau "orderNumber" ada di data Order kamu
            .orderBy("orderNumber", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil data order")
                    return@addSnapshotListener
                }
                val list = value?.toObjects(Order::class.java) ?: emptyList()
                onData(list)
            }
    }
}