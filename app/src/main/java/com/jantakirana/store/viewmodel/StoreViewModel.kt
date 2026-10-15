package com.jantakirana.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jantakirana.store.data.AppDatabase
import com.jantakirana.store.data.NetworkClient
import com.jantakirana.store.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(private val db: AppDatabase) : ViewModel() {
    val allProducts: StateFlow<List<Product>> = db.productDao().getAllProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val lowStockProducts: StateFlow<List<Product>> = db.productDao().getLowStockProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val totalInventoryValue = MutableStateFlow(0.0)
    
    val scannedBarcodeResult = MutableStateFlow<String?>(null)
    val onlineProductLookup = MutableStateFlow<String?>(null)
    val isFetchingOnline = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            allProducts.collect { products -> totalInventoryValue.value = products.sumOf { it.sellingPrice * it.quantity } }
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0) db.productDao().insertProduct(product) else db.productDao().updateProduct(product)
        }
    }

    fun processScannedBarcode(barcode: String) {
        viewModelScope.launch {
            scannedBarcodeResult.value = barcode
            val localProduct = db.productDao().getProductByBarcode(barcode)
            if (localProduct == null) {
                isFetchingOnline.value = true
                try {
                    val response = NetworkClient.api.getProductInfo(barcode)
                    if (response.status == 1 && response.product?.product_name != null) onlineProductLookup.value = response.product.product_name else onlineProductLookup.value = ""
                } catch (e: Exception) { onlineProductLookup.value = "" } finally { isFetchingOnline.value = false }
            } else { onlineProductLookup.value = localProduct.name }
        }
    }

    fun clearScannerState() {
        scannedBarcodeResult.value = null
        onlineProductLookup.value = null
    }
}
