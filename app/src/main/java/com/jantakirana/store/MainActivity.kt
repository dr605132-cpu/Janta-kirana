package com.jantakirana.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.jantakirana.store.data.AppDatabase
import com.jantakirana.store.ui.AddProductScreen
import com.jantakirana.store.ui.DashboardScreen
import com.jantakirana.store.ui.StockScreen
import com.jantakirana.store.ui.JantaKiranaTheme
import com.jantakirana.store.viewmodel.StoreViewModel

class StoreViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StoreViewModel(db) as T
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val viewModel = ViewModelProvider(this, StoreViewModelFactory(db))[StoreViewModel::class.java]
        val scannerOptions = GmsBarcodeScannerOptions.Builder().enableAutoZoom().build()
        val scanner = GmsBarcodeScanning.getClient(this, scannerOptions)

        setContent {
            JantaKiranaTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(icon = { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") }, selected = currentRoute == "dashboard", onClick = { navController.navigate("dashboard") })
                            NavigationBarItem(icon = { Icon(Icons.Default.Inventory, "Stock") }, label = { Text("Stock") }, selected = currentRoute == "stock", onClick = { navController.navigate("stock") })
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = "dashboard", Modifier.padding(innerPadding)) {
                        composable("dashboard") {
                            DashboardScreen(viewModel) {
                                scanner.startScan().addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { raw ->
                                        viewModel.processScannedBarcode(raw)
                                        navController.navigate("addProduct")
                                    }
                                }.addOnFailureListener {
                                    viewModel.clearScannerState()
                                    navController.navigate("addProduct")
                                }
                            }
                        }
                        composable("stock") { StockScreen(viewModel) }
                        composable("addProduct") {
                            val scannedCode by viewModel.scannedBarcodeResult.collectAsState()
                            val prefillName by viewModel.onlineProductLookup.collectAsState()
                            val isFetching by viewModel.isFetchingOnline.collectAsState()
                            if (isFetching) { CircularProgressIndicator() } else {
                                AddProductScreen(viewModel, scannedCode ?: "", prefillName ?: "") { navController.popBackStack() }
                            }
                        }
                    }
                }
            }
        }
    }
}
