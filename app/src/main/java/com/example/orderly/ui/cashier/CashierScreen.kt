package com.example.orderly.ui.cashier

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
// IMPORT PENTING
import com.example.orderly.data.FirestoreRepository
import com.example.orderly.data.Product
import com.example.orderly.data.Order
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Wrapper Lokal untuk Keranjang UI
data class LocalCartItem(
    val product: Product,
    var quantity: Int
)

@Composable
fun CashierPosScreen(onLogout: () -> Unit) {
    val PrimaryPurple = Color(0xFF6C5CE7)
    val BgColor = Color(0xFFF5F6FA)
    val context = LocalContext.current

    // --- STATE DATA REALTIME ---
    val allProducts = remember { mutableStateListOf<Product>() }
    val historyList = remember { mutableStateListOf<Order>() }

    // Hitung nomor order berikutnya
    val nextOrderNumber = historyList.size + 1

    // AMBIL DATA DARI FIREBASE (DIPERBAIKI: Tambah onError)
    LaunchedEffect(Unit) {
        // 1. Ambil Produk
        FirestoreRepository.getProductsRealtime(
            onData = { products ->
                allProducts.clear()
                allProducts.addAll(products)
            },
            onError = { errorMsg ->
                Toast.makeText(context, "Gagal memuat menu: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )

        // 2. Ambil History Order
        FirestoreRepository.getOrdersRealtime(
            onData = { orders ->
                historyList.clear()
                historyList.addAll(orders)
            },
            onError = { errorMsg ->
                Toast.makeText(context, "Gagal memuat history: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // STATE UI
    var currentScreen by remember { mutableStateOf("Menu") }
    val cartItems = remember { mutableStateListOf<LocalCartItem>() }

    // STATE POP-UP PEMBAYARAN
    var showPaymentDialog by remember { mutableStateOf(false) }
    var cashInput by remember { mutableStateOf("") }

    // Filter Kategori
    var selectedCategory by remember { mutableStateOf("All") }
    val displayedProducts = if (selectedCategory == "All") allProducts else allProducts.filter { it.category == selectedCategory }

    // Hitung Total
    val total = cartItems.sumOf { it.product.price * it.quantity }
    val cashAmount = cashInput.toDoubleOrNull() ?: 0.0
    val changeAmount = cashAmount - total

    // --- LOGIKA KERANJANG ---
    fun addToCart(product: Product) {
        val existing = cartItems.find { it.product.id == product.id }
        if (existing != null) {
            val idx = cartItems.indexOf(existing)
            cartItems[idx] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(LocalCartItem(product, 1))
        }
    }

    fun removeFromCart(item: LocalCartItem) {
        if (item.quantity > 1) {
            val idx = cartItems.indexOf(item)
            cartItems[idx] = item.copy(quantity = item.quantity - 1)
        } else {
            cartItems.remove(item)
        }
    }

    // --- LOGIKA CHECKOUT KE FIREBASE (DIPERBAIKI) ---
    fun processCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang Kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi LocalCartItem ke CartItem Database
        val dbCartItems = cartItems.map {
            com.example.orderly.data.CartItem(it.product.name, it.product.price, it.quantity)
        }

        val newOrder = Order(
            id = System.currentTimeMillis().toString(),
            orderNumber = nextOrderNumber,
            date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
            totalAmount = total,
            items = dbCartItems,
            cashierName = "Kasir" // Bisa diganti nama dinamis nanti
        )

        // Kirim ke Firebase (Tambah parameter onSuccess & onError)
        FirestoreRepository.addOrder(
            order = newOrder,
            onSuccess = {
                cartItems.clear()
                showPaymentDialog = false
                cashInput = ""
                Toast.makeText(context, "Transaksi Sukses & Tersimpan!", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMsg ->
                Toast.makeText(context, "Gagal Transaksi: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Fungsi Buka Dialog Pembayaran
    fun openPaymentDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
        } else {
            cashInput = ""
            showPaymentDialog = true
        }
    }

    // --- POP-UP DIALOG PEMBAYARAN ---
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Pembayaran", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Total Tagihan:", fontSize = 14.sp, color = Color.Gray)
                    Text("Rp ${total.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { cashInput = it },
                        label = { Text("Nominal Bayar") },
                        placeholder = { Text("Masukkan uang diterima") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("Rp ") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (changeAmount >= 0) "Rp ${changeAmount.toInt()}" else "Kurang Rp ${Math.abs(changeAmount.toInt())}",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (changeAmount >= 0) Color(0xFF00B894) else Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { processCheckout() },
                    enabled = cashAmount >= total,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bayar & Cetak Struk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Batal", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    // --- TAMPILAN UTAMA ---
    Row(Modifier.fillMaxSize().background(BgColor)) {
        // 1. Sidebar
        NavigationSidebar(Modifier.weight(0.15f), currentScreen, { currentScreen = it }, onLogout, PrimaryPurple)

        // 2. Tengah (Menu / History)
        Box(Modifier.weight(0.55f)) {
            if (currentScreen == "Menu") {
                MenuSection(displayedProducts, selectedCategory, { selectedCategory = it }, { addToCart(it) }, PrimaryPurple)
            } else {
                HistorySection(historyList, PrimaryPurple)
            }
        }

        // 3. Kanan (Keranjang)
        CartSection(Modifier.weight(0.3f), cartItems, total, nextOrderNumber, { addToCart(it.product) }, { removeFromCart(it) }, { openPaymentDialog() }, PrimaryPurple)
    }
}

// =========================================
// KOMPONEN UI TAMBAHAN
// =========================================

@Composable
fun NavigationSidebar(modifier: Modifier, currentScreen: String, onMenuSelected: (String) -> Unit, onLogout: () -> Unit, activeColor: Color) {
    Column(modifier.fillMaxHeight().background(Color.White).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.RestaurantMenu, "Logo", tint = activeColor, modifier = Modifier.size(40.dp))
        Text("Lawu POS", fontWeight = FontWeight.Bold, color = activeColor, fontSize = 16.sp)
        Spacer(Modifier.height(40.dp))
        NavItem(Icons.Default.GridView, "Menu", currentScreen == "Menu", activeColor) { onMenuSelected("Menu") }
        NavItem(Icons.Default.History, "History", currentScreen == "History", activeColor) { onMenuSelected("History") }
        Spacer(Modifier.weight(1f))
        // Ganti Icon ExitToApp ke AutoMirrored biar gak warning
        NavItem(Icons.AutoMirrored.Filled.ExitToApp, "Logout", false, Color.Red, onClick = onLogout)
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 12.dp).size(80.dp).clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }.background(if (isSelected) activeColor.copy(0.1f) else Color.Transparent).padding(8.dp)) {
        Icon(icon, label, tint = if (isSelected) activeColor else Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = if (isSelected) activeColor else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun MenuSection(
    products: List<Product>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    activeColor: Color
) {
    Column(Modifier.padding(16.dp)) {
        // --- HEADER ---
        Text("Orderly LAWU", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Pilih menu terbaik hari ini", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(16.dp))

        // --- FILTER CHIP (KATEGORI) ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Minuman", "Boba", "Nasi", "Snack").forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(if (category == "All") "See All" else category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeColor,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- GRID MENU ---
        if (products.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada menu tersedia.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp), // Lebar minimal kartu diperbesar dikit biar lega
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(products) { product ->
                    // --- KARTU PRODUK (DIPERBAIKI) ---
                    Card(
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(4.dp), // Bayangan lebih tegas
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(260.dp) // PENTING: Tinggi kartu dikunci (Fixed Height) biar sejajar semua
                            .fillMaxWidth()
                            .clickable { onAddToCart(product) } // Bisa diklik se-kartu-kartunya
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween // PENTING: Membagi ruang atas dan bawah
                        ) {

                            // BAGIAN ATAS: Gambar & Teks
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Gambar/Icon Placeholder
                                ProductImage(
                                    imageUrl = product.imageUrl, // Ambil link dari data produk
                                    color = product.getColor(),  // Warna cadangan kalau gambar error/kosong
                                    size = 100.dp                // Ukuran gambar
                                )

                                Spacer(Modifier.height(12.dp))

                                // Nama Produk
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 2, // Batasi 2 baris
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                // Harga
                                Text(
                                    text = "Rp ${product.price.toInt()}",
                                    color = activeColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            // BAGIAN BAWAH: Tombol Add
                            // Tombol akan selalu terdorong ke paling bawah kartu karena 'Arrangement.SpaceBetween'
                            Button(
                                onClick = { onAddToCart(product) },
                                colors = ButtonDefaults.buttonColors(activeColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth() // Tombol lebar penuh
                                    .height(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySection(historyList: List<Order>, activeColor: Color) {
    Column(Modifier.padding(24.dp)) {
        Text("Riwayat Pesanan", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Daftar transaksi yang sudah dibayar", color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        if (historyList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat transaksi.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historyList) { order ->
                    Card(colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
                                Text(order.date, fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("Rp ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = activeColor)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, null, tint = Color.Green)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartSection(
    modifier: Modifier,
    cartItems: List<LocalCartItem>,
    total: Double,
    orderNumber: Int,
    onIncrease: (LocalCartItem) -> Unit,
    onDecrease: (LocalCartItem) -> Unit,
    onCheckout: () -> Unit,
    activeColor: Color
) {
    val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH))

    Column(modifier.fillMaxHeight().background(Color.White).padding(24.dp)) {
        Text("Order Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Order #$orderNumber", color = activeColor, fontWeight = FontWeight.Bold)
        Text(currentDate, color = Color.Gray, fontSize = 12.sp)
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(0.3f))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(cartItems) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // --- PERUBAHAN DISINI: PAKAI GAMBAR ASLI ---
                    ProductImage(
                        imageUrl = item.product.imageUrl,
                        color = item.product.getColor(),
                        size = 50.dp
                    )
                    // -------------------------------------------

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(item.product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("Rp ${item.product.price.toInt()}", color = Color.Gray, fontSize = 12.sp)
                    }

                    // Tombol Tambah Kurang
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ onDecrease(item) }, Modifier.size(45.dp).background(Color(0xFFF0F0F0), CircleShape)) {
                            Icon(Icons.Default.Remove, "-", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Text(item.quantity.toString(), Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton({ onIncrease(item) }, Modifier.size(45.dp).background(activeColor, CircleShape)) {
                            Icon(Icons.Default.Add, "+", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(0.3f))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Rp ${total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = activeColor)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(activeColor), shape = RoundedCornerShape(12.dp)) {
            Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ProductImage(imageUrl: String, color: Color, size: androidx.compose.ui.unit.Dp) {
    if (imageUrl.isNotEmpty()) {
        // Kalau ada link gambar, tampilkan gambarnya
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
    } else {
        // Kalau link kosong, tampilkan ICON bawaan (Fallback)
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}