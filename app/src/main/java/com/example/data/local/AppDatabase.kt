package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converters.Converters
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.BillDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.IncomeDao
import com.example.data.local.dao.PaymentMethodDao
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        IncomeEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        BudgetEntity::class,
        BillEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun budgetDao(): BudgetDao
    abstract fun billDao(): BillDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories & payment methods in background
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedDefaultData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }

    suspend fun seedDefaultData() {
        if (categoryDao().getCategoriesCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Groceries", iconName = "ShoppingCart", colorHex = 0xFF4CAF50, subCategories = listOf("Supermarket", "Vegetables", "Fruits", "Dairy"), isDefault = true),
                CategoryEntity(name = "Food", iconName = "Restaurant", colorHex = 0xFFFF9800, subCategories = listOf("Dining Out", "Coffee", "Snacks", "Delivery"), isDefault = true),
                CategoryEntity(name = "Petrol", iconName = "LocalGasStation", colorHex = 0xFFF44336, subCategories = listOf("Fuel", "Service", "Toll"), isDefault = true),
                CategoryEntity(name = "Transportation", iconName = "DirectionsBus", colorHex = 0xFF00BCD4, subCategories = listOf("Metro", "Cab", "Train", "Flight"), isDefault = true),
                CategoryEntity(name = "Electricity", iconName = "Bolt", colorHex = 0xFFFFEB3B, subCategories = listOf("Power Bill"), isDefault = true),
                CategoryEntity(name = "Water", iconName = "WaterDrop", colorHex = 0xFF2196F3, subCategories = listOf("Water Tanker", "Utility Bill"), isDefault = true),
                CategoryEntity(name = "Mobile", iconName = "PhoneAndroid", colorHex = 0xFF9C27B0, subCategories = listOf("Prepaid Recharge", "Postpaid Bill"), isDefault = true),
                CategoryEntity(name = "Internet", iconName = "Wifi", colorHex = 0xFF673AB7, subCategories = listOf("Broadband", "Fiber"), isDefault = true),
                CategoryEntity(name = "Rent", iconName = "Home", colorHex = 0xFF795548, subCategories = listOf("House Rent", "Maintenance"), isDefault = true),
                CategoryEntity(name = "Medical", iconName = "LocalHospital", colorHex = 0xFFE91E63, subCategories = listOf("Medicines", "Doctor Consultation", "Lab Tests"), isDefault = true),
                CategoryEntity(name = "Education", iconName = "School", colorHex = 0xFF3F51B5, subCategories = listOf("Tuition Fees", "Books", "Courses"), isDefault = true),
                CategoryEntity(name = "Shopping", iconName = "ShoppingBag", colorHex = 0xFFFF4081, subCategories = listOf("Clothing", "Electronics", "Footwear"), isDefault = true),
                CategoryEntity(name = "Entertainment", iconName = "Movie", colorHex = 0xFF9C27B0, subCategories = listOf("Cinema", "Concerts", "Events"), isDefault = true),
                CategoryEntity(name = "Fun", iconName = "SportsEsports", colorHex = 0xFF00E676, subCategories = listOf("Games", "Outings", "Hobbies"), isDefault = true),
                CategoryEntity(name = "Travel", iconName = "Flight", colorHex = 0xFF00B0FF, subCategories = listOf("Hotel", "Tickets", "Sightseeing"), isDefault = true),
                CategoryEntity(name = "Subscriptions", iconName = "Subscriptions", colorHex = 0xFFFF5722, subCategories = listOf("OTT Streaming", "Cloud Storage", "Software"), isDefault = true),
                CategoryEntity(name = "EMI/Loans", iconName = "AccountBalance", colorHex = 0xFF607D8B, subCategories = listOf("Home Loan", "Car Loan", "Personal Loan"), isDefault = true),
                CategoryEntity(name = "Insurance", iconName = "Security", colorHex = 0xFF009688, subCategories = listOf("Health Insurance", "Term Life", "Vehicle"), isDefault = true),
                CategoryEntity(name = "Gifts", iconName = "CardGiftcard", colorHex = 0xFFFF80AB, subCategories = listOf("Birthdays", "Festivals", "Donations"), isDefault = true),
                CategoryEntity(name = "Investments", iconName = "TrendingUp", colorHex = 0xFF4CAF50, subCategories = listOf("Mutual Funds", "Stocks", "Fixed Deposit", "Gold"), isDefault = true),
                CategoryEntity(name = "Miscellaneous", iconName = "Category", colorHex = 0xFF9E9E9E, subCategories = listOf("General"), isDefault = true)
            )
            categoryDao().insertCategories(defaultCategories)
        }

        if (paymentMethodDao().getPaymentMethodsCount() == 0) {
            val defaultMethods = listOf(
                PaymentMethodEntity(name = "Cash", iconName = "Payments", isDefault = true),
                PaymentMethodEntity(name = "UPI", iconName = "QrCode", isDefault = true),
                PaymentMethodEntity(name = "Debit Card", iconName = "CreditCard", isDefault = true),
                PaymentMethodEntity(name = "Credit Card", iconName = "CreditCard", isDefault = true),
                PaymentMethodEntity(name = "Bank Transfer", iconName = "AccountBalance", isDefault = true),
                PaymentMethodEntity(name = "Wallet", iconName = "AccountBalanceWallet", isDefault = true),
                PaymentMethodEntity(name = "Other", iconName = "MoreHoriz", isDefault = true)
            )
            paymentMethodDao().insertPaymentMethods(defaultMethods)
        }

        if (appSettingsDao().getSettingsSync() == null) {
            appSettingsDao().insertOrUpdate(AppSettingsEntity())
        }
    }
}
