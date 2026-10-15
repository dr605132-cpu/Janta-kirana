package com.jantakirana.store.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val barcode: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val mrp: Double,
    val quantity: Int,
    val category: String,
    val lowStockThreshold: Int = 5,
    val brand: String = "",
    val expiryDate: String = ""
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE quantity <= lowStockThreshold")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "janta_kirana_db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}

data class OffResponse(val status: Int, val product: OffProduct?)
data class OffProduct(val product_name: String?, val brands: String?)

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductInfo(@Path("barcode") barcode: String): OffResponse
}

object NetworkClient {
    val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder().baseUrl("https://world.openfoodfacts.org/").addConverterFactory(GsonConverterFactory.create()).build().create(OpenFoodFactsApi::class.java)
    }
}
