package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val gender: String = "Male", // "Male", "Female", "Other"
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "measurements")
data class CustomerMeasurement(
    @PrimaryKey val customerId: Long,
    val shirtLength: Double = 0.0,
    val shoulder: Double = 0.0,
    val sleeves: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hip: Double = 0.0,
    val collar: Double = 0.0,
    val armhole: Double = 0.0,
    val sleeveMori: Double = 0.0,
    val trouserLength: Double = 0.0,
    val trouserBottom: Double = 0.0,
    val trouserAsan: Double = 0.0,
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val galaType: String = "کالر",
    val collarSize: String = "درمیانہ",
    val sleeveDesign: String = "آستین سادہ",
    val frontPatti: Boolean = true,
    val frontPocket: Boolean = true,
    val sidePocket: String = "2",
    val daman: String = "گول",
    val shalwarWidth: String = "نارمل",
    val shalwarPocket: Boolean = false,
    val bukramQuality: String = "2 (درمیانی)"
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val trackingId: String = "", // Unique tracking tag for cloth physical tag and search e.g. TRK-8492
    val itemType: String, // e.g., "Kameez Shalwar", "Kurta", "Waistcoat", "Sherwani", "Trouser Suit"
    val clothType: String = "", // e.g., "Cotton", "Wash'n'Wear", "Latha", "Karandi"
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, DELIVERED
    val orderDate: Long = System.currentTimeMillis(),
    val dueDate: Long,
    // Custom measurements snapshot specifically for this order
    val shirtLength: Double = 0.0,
    val shoulder: Double = 0.0,
    val sleeves: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hip: Double = 0.0,
    val collar: Double = 0.0,
    val armhole: Double = 0.0,
    val sleeveMori: Double = 0.0,
    val trouserLength: Double = 0.0,
    val trouserBottom: Double = 0.0,
    val trouserAsan: Double = 0.0,
    val orderNotes: String = "",
    val galaType: String = "کالر",
    val collarSize: String = "درمیانہ",
    val sleeveDesign: String = "آستین سادہ",
    val frontPatti: Boolean = true,
    val frontPocket: Boolean = true,
    val sidePocket: String = "2",
    val daman: String = "گول",
    val shalwarWidth: String = "نارمل",
    val shalwarPocket: Boolean = false,
    val bukramQuality: String = "2 (درمیانی)"
)

data class OrderWithCustomer(
    val id: Long,
    val customerId: Long,
    val trackingId: String = "",
    val customerName: String,
    val customerPhone: String,
    val itemType: String,
    val clothType: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val status: String,
    val orderDate: Long,
    val dueDate: Long,
    val shirtLength: Double,
    val shoulder: Double,
    val sleeves: Double,
    val chest: Double,
    val waist: Double,
    val hip: Double,
    val collar: Double,
    val armhole: Double,
    val sleeveMori: Double,
    val trouserLength: Double,
    val trouserBottom: Double,
    val trouserAsan: Double,
    val orderNotes: String,
    val galaType: String = "کالر",
    val collarSize: String = "درمیانہ",
    val sleeveDesign: String = "آستین سادہ",
    val frontPatti: Boolean = true,
    val frontPocket: Boolean = true,
    val sidePocket: String = "2",
    val daman: String = "گول",
    val shalwarWidth: String = "نارمل",
    val shalwarPocket: Boolean = false,
    val bukramQuality: String = "2 (درمیانی)"
)

@Dao
interface TailorDao {

    // Customer Queries
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Measurement Queries
    @Query("SELECT * FROM measurements WHERE customerId = :customerId")
    suspend fun getMeasurementsByCustomerId(customerId: Long): CustomerMeasurement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: CustomerMeasurement)

    // Order Queries
    @Query("""
        SELECT o.*, c.name AS customerName, c.phone AS customerPhone 
        FROM orders o 
        INNER JOIN customers c ON o.customerId = c.id 
        ORDER BY o.dueDate ASC
    """)
    fun getAllOrdersWithCustomer(): Flow<List<OrderWithCustomer>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    suspend fun getOrdersByCustomerId(customerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<Customer>

    @Query("SELECT * FROM measurements")
    suspend fun getAllMeasurementsList(): List<CustomerMeasurement>

    @Query("SELECT * FROM orders")
    suspend fun getAllOrdersList(): List<Order>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<CustomerMeasurement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    // Statistics and quick counts for summary reports
    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrderCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status != 'DELIVERED'")
    fun getActiveOrderCountFlow(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders")
    fun getTotalRevenueFlow(): Flow<Double?>

    @Query("SELECT SUM(paidAmount) FROM orders")
    fun getTotalCollectedFlow(): Flow<Double?>
}

@Database(entities = [Customer::class, CustomerMeasurement::class, Order::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tailorDao(): TailorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tailor_book_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
