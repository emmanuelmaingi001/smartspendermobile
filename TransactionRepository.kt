package com.smartspender.app

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    // Room returns a Flow, so the UI will update automatically
    // when a new M-PESA SMS is saved!
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // This feeds the data directly to your pie charts
    val categoryTotals: Flow<List<CategorySummary>> = transactionDao.getSpendingByCategory()

    // We use 'suspend' so the database work happens on a background thread
    // This keeps the "Blue" UI smooth and lag-free
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}