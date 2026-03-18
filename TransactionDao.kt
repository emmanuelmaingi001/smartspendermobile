package com.smartspender.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // 1. Save a new M-PESA transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    // 2. Get all transactions (latest first) for your main list
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // 3. Delete a specific transaction (if the user made a mistake)
    @Delete
    suspend fun delete(transaction: Transaction)

    // 4. Data for your Pie Chart: Sums up amounts by category
    // This is perfect for your "Total Spent on Food vs Transport" view
    @Query("SELECT category, SUM(amount) as total FROM transactions GROUP BY category")
    fun getSpendingByCategory(): Flow<List<CategorySummary>>
}

// A simple helper class to hold the pie chart data
data class CategorySummary(
    val category: String,
    val total: Float
)