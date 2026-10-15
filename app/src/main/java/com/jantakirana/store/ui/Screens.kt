package com.jantakirana.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jantakirana.store.data.Product
import com.jantakirana.store.viewmodel.StoreViewModel

@Composable
fun DashboardScreen(viewModel: StoreViewModel, onNavigateToScanner: () -> Unit) {
    val totalProducts by viewModel.allProducts.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val totalValue by viewModel.totalInventoryValue.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text("Janta Kirana Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Total Products", "${totalProducts.size}", Modifier.weight(1f))
            StatCard("Low Stock", "${lowStock.size}", Modifier.weight(1f), color = WarningYellow)
        }
        Spacer(modifier = Modifier.height(8.dp))
        StatCard("Inventory Value", "₹${"%.2f".format(totalValue)}", Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Text("Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ActionButton(Icons.Default.QrCodeScanner, "Scan Barcode", onNavigateToScanner)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color = TextWhite) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextGrey, fontSize = 14.sp)
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = TextWhite, modifier = Modifier.size(30.dp))
        }
        Text(label, color = TextWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(viewModel: StoreViewModel, scannedBarcode: String = "", prefillName: String = "", onBack: () -> Unit) {
    var name by remember { mutableStateOf(prefillName) }
    var barcode by remember { mutableStateOf(scannedBarcode) }
    var sellingPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text("Add Product", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel.saveProduct(Product(name = name.ifEmpty { "Unknown" }, barcode = barcode, purchasePrice = 0.0, sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0, mrp = 0.0, quantity = quantity.toIntOrNull() ?: 0, category = "General"))
            onBack()
        }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
            Text("Save Product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StockScreen(viewModel: StoreViewModel) {
    val products by viewModel.allProducts.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Current Stock", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(products) { product ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Qty: ${product.quantity} | Price: ₹${product.sellingPrice}", color = TextGrey)
                    }
                }
            }
        }
    }
}
