package com.example.orderly.ui.admin

import android.content.Intent // PENTING: Untuk fitur Export CSV
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// IMPORT DATA
import com.example.orderly.data.FirestoreRepository
import com.example.orderly.data.Product
import com.example.orderly.data.Order

@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val PrimaryPurple = Color(0xFF6C5CE7)
    val BgColor = Color(0xFFF5F6FA)
    var currentTab by remember { mutableStateOf("Dashboard") }

    // Context untuk Toast Notifikasi
    val context = LocalContext.current

    // --- STATE DATA REALTIME ---
    val menuList = remember { mutableStateListOf<Product>() }
    val orderList = remember { mutableStateListOf<Order>() }

    // Ambil data saat layar dibuka
    LaunchedEffect(Unit) {
        // 1. Ambil Produk
        FirestoreRepository.getProductsRealtime(
            onData = { products ->
                menuList.clear()
                menuList.addAll(products)
            },
            onError = { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )

        // 2. Ambil Order
        FirestoreRepository.getOrdersRealtime(
            onData = { orders ->
                orderList.clear()
                orderList.addAll(orders)
            },
            onError = { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // 1. Top Bar
        AdminTopBar(currentTab, { currentTab = it }, onLogout, PrimaryPurple)

        // 2. Konten
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            when (currentTab) {
                "Dashboard" -> DashboardTab(orderList, menuList.size, PrimaryPurple)
                "Order" -> OrderTab(orderList)
                // UPDATE: Kirim data orderList ke ReportTab agar bisa direkap
                "Report" -> ReportTab(orderList)
                "Inventory" -> ManageMenuTab(menuList, PrimaryPurple)
            }
        }
    }
}

// =========================================
// 1. TOP BAR
// =========================================
@Composable
fun AdminTopBar(currentTab: String, onTabSelected: (String) -> Unit, onLogout: () -> Unit, activeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = activeColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Orderly Admin", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Row(modifier = Modifier.background(Color(0xFFF5F6FA), RoundedCornerShape(50)).padding(4.dp)) {
            listOf("Dashboard", "Order", "Report", "Inventory").forEach { tab ->
                val isSelected = currentTab == tab
                Box(modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onTabSelected(tab) }
                    .background(if (isSelected) activeColor else Color.Transparent).padding(horizontal = 24.dp, vertical = 10.dp)) {
                    Text(tab, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red) }
    }
}

// =========================================
// 2. DASHBOARD TAB
// =========================================
@Composable
fun DashboardTab(orders: List<Order>, totalMenu: Int, activeColor: Color) {
    val totalSales = orders.sumOf { it.totalAmount }
    val totalTx = orders.size

    val topSelling = remember(orders) {
        orders.flatMap { it.items }
            .groupBy { it.productName }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }

    val recentOrders = orders.sortedByDescending { it.orderNumber }.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Dashboard Overview", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Pantau performa cafe hari ini", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Total Sales", "Rp ${totalSales.toInt()}", Icons.Default.MonetizationOn, Color(0xFF00B894), Modifier.weight(1f))
            StatCard("Total Orders", "$totalTx Tx", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF0984E3), Modifier.weight(1f))
            StatCard("Active Menu", "$totalMenu Item", Icons.Default.Fastfood, activeColor, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // KARTU MENU TERLARIS (KIRI)
            Card(
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107))
                        Spacer(Modifier.width(8.dp))
                        Text("Menu Terfavorit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    if (topSelling.isEmpty()) {
                        Text("Belum ada data penjualan.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        topSelling.forEachIndexed { index, (name, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(activeColor.copy(0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", fontWeight = FontWeight.Bold, color = activeColor, fontSize = 12.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(name, fontWeight = FontWeight.Medium)
                                }
                                Text("$qty Terjual", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                            }
                            HorizontalDivider(color = Color.LightGray.copy(0.2f))
                        }
                    }
                }
            }

            // KARTU TRANSAKSI TERAKHIR (KANAN)
            Card(
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Transaksi Terakhir", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    if (recentOrders.isEmpty()) {
                        Text("Belum ada transaksi masuk.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        recentOrders.forEach { order ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(order.date, color = Color.LightGray, fontSize = 10.sp)
                                }
                                Text("Rp ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF00B894))
                            }
                            HorizontalDivider(color = Color.LightGray.copy(0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(title, color = Color.Gray)
        }
    }
}

// =========================================
// 3. ORDER TAB
// =========================================
@Composable
fun OrderTab(orders: List<Order>) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Riwayat Pesanan Masuk", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(orders) { order ->
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
                                Text(order.date, fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("Rp ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF6C5CE7))
                            Spacer(Modifier.width(16.dp))

                            Column(Modifier.weight(1f)) {
                                order.items.forEach { item ->
                                    Text("${item.quantity}x ${item.productName}", fontSize = 12.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(0.2f))
                    }
                }
            }
        }
    }
}

// =========================================
// 4. REPORT TAB (SUDAH JADI CSV EXPORT)
// =========================================

@Composable
fun ReportTab(orders: List<Order>) {
    val context = LocalContext.current

    // --- 1. DATA UNTUK TAMPILAN UI (REKAP HARIAN SIMPEL) ---
    // Hasilnya: [("27 Jan", 5 Transaksi, Rp 100.000), ...]
    val uiReport = remember(orders) {
        orders.groupBy { it.date.split(",")[0] }
            .map { (date, orderList) ->
                val totalOmzet = orderList.sumOf { it.totalAmount }
                val totalTx = orderList.size
                Triple(date, totalTx, totalOmzet)
            }
            .sortedByDescending { it.first }
    }

    // --- 2. DATA UNTUK EXPORT CSV (RINCIAN MENDALAM) ---
    // Hasilnya: Tanggal -> [("Menu A", 2, Rp 10.000), ("Menu B", ...)]
    val csvDetailedReport = remember(orders) {
        orders.groupBy { it.date.split(",")[0] }
            .map { (date, ordersForDay) ->
                val allItems = ordersForDay.flatMap { it.items }
                val menuSummaries = allItems.groupBy { it.productName }
                    .map { (name, specificItems) ->
                        val totalQty = specificItems.sumOf { it.quantity }
                        // Safety Check Harga
                        val price = specificItems.firstOrNull()?.price ?: 0.0
                        val totalIncome = totalQty * price
                        Triple(name, totalQty, totalIncome)
                    }
                    .sortedByDescending { it.second }
                date to menuSummaries
            }
            .sortedByDescending { it.first }
    }

    // --- FUNGSI EXPORT (MENGGUNAKAN DATA NO. 2) ---
    fun exportToCsv() {
        var csvContent = "Tanggal,Nama Menu,Jumlah Terjual,Total Pendapatan\n"

        // Loop Data Detail untuk ditulis ke File
        csvDetailedReport.forEach { (date, items) ->
            items.forEach { (menuName, qty, income) ->
                // Format CSV: Tanggal,Menu,Qty,Total
                csvContent += "$date,$menuName,$qty,Rp ${income.toInt()}\n"
            }
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, csvContent)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Detail Orderly")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Simpan Laporan Ke...")
        context.startActivity(shareIntent)
    }

    // --- TAMPILAN UI (MENGGUNAKAN DATA NO. 1) ---
    Column(modifier = Modifier.fillMaxSize()) {
        // Header & Tombol
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Laporan Harian", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("Rekap pendapatan per hari", color = Color.Gray, fontSize = 14.sp)
            }

            Button(
                onClick = { exportToCsv() }, // Saat diklik, dia download yang DETAIL
                colors = ButtonDefaults.buttonColors(Color(0xFF00B894)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export Detail")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TABEL TAMPILAN (Sama persis kayak gambar requestmu)
        if (uiReport.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada data penjualan.", color = Color.Gray)
            }
        } else {
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(12.dp)) {
                Column {
                    // Header Tabel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F6FA))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Transaksi", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Omzet", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }

                    // Isi Tabel (Pakai uiReport yang simpel)
                    LazyColumn {
                        items(uiReport) { (date, count, total) ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(date, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    Text("$count Tx", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                                    Text("Rp ${total.toInt()}", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End, color = Color(0xFF00B894), fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(color = Color.LightGray.copy(0.1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
// =========================================
// 5. INVENTORY TAB
// =========================================
@Composable
fun ManageMenuTab(menuList: MutableList<Product>, activeColor: Color) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var imageUrl by remember { mutableStateOf("") }

    var editingProduct by remember { mutableStateOf<Product?>(null) }
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {

        // --- FORM INPUT ---
        Card(colors = CardDefaults.cardColors(Color.White), modifier = Modifier.weight(0.35f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (editingProduct == null) "Tambah Menu Baru" else "Edit Menu: ${editingProduct?.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (editingProduct == null) Color.Black else activeColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Link Gambar (URL)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Minuman", "Boba", "Nasi", "Snack").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (editingProduct != null) {
                    Button(
                        onClick = {
                            editingProduct = null
                            name = ""; price = ""; imageUrl = ""; category = "Makanan"
                        },
                        colors = ButtonDefaults.buttonColors(Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Batal Edit") }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.isNotEmpty()) {
                            val priceDouble = price.toDoubleOrNull() ?: 0.0

                            if (editingProduct == null) {
                                val newProduct = Product(
                                    id = System.currentTimeMillis().toString(),
                                    name = name,
                                    price = priceDouble,
                                    category = category,
                                    colorHex = activeColor.value.toLong(),
                                    isAvailable = true,
                                    imageUrl = imageUrl
                                )
                                FirestoreRepository.addProduct(newProduct,
                                    onSuccess = {
                                        Toast.makeText(context, "Menu Berhasil Ditambah!", Toast.LENGTH_SHORT).show()
                                        name = ""; price = ""; imageUrl = ""
                                    },
                                    onError = { Toast.makeText(context, "Gagal: $it", Toast.LENGTH_SHORT).show() }
                                )
                            } else {
                                val updatedProduct = editingProduct!!.copy(
                                    name = name,
                                    price = priceDouble,
                                    category = category,
                                    imageUrl = imageUrl
                                )

                                FirestoreRepository.updateProduct(updatedProduct,
                                    onSuccess = {
                                        Toast.makeText(context, "Menu Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                                        val index = menuList.indexOfFirst { it.id == updatedProduct.id }
                                        if (index != -1) menuList[index] = updatedProduct
                                        editingProduct = null
                                        name = ""; price = ""; imageUrl = ""; category = "Makanan"
                                    },
                                    onError = { Toast.makeText(context, "Gagal Update: $it", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(activeColor)
                ) {
                    Text(if (editingProduct == null) "Simpan Menu Baru" else "Update Perubahan")
                }
            }
        }

        // --- LIST MENU ---
        Card(colors = CardDefaults.cardColors(Color.White), modifier = Modifier.weight(0.65f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Daftar Menu & Stok", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menuList) { item ->
                        val cardColor = if (item.isAvailable) Color(0xFFF5F6FA) else Color.LightGray.copy(0.3f)

                        Card(colors = CardDefaults.cardColors(cardColor)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("Rp ${item.price.toInt()}", color = if (item.isAvailable) activeColor else Color.Gray)

                                Spacer(Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = {
                                            editingProduct = item
                                            name = item.name
                                            price = item.price.toInt().toString()
                                            category = item.category
                                            imageUrl = item.imageUrl
                                        },
                                        colors = ButtonDefaults.buttonColors(Color(0xFF0984E3)),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.weight(1f).height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val updatedItem = item.copy(isAvailable = !item.isAvailable)
                                            val index = menuList.indexOf(item)
                                            if (index != -1) menuList[index] = updatedItem
                                            FirestoreRepository.updateProduct(updatedItem, {}, { if (index != -1) menuList[index] = item })
                                        },
                                        colors = ButtonDefaults.buttonColors(if (item.isAvailable) Color(0xFF00B894) else Color.Gray),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.width(40.dp).height(30.dp)
                                    ) {
                                        Icon(if (item.isAvailable) Icons.Default.Check else Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = { FirestoreRepository.deleteProduct(item.id, {}) },
                                        colors = ButtonDefaults.buttonColors(Color.Red.copy(0.1f)),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.width(30.dp).height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}