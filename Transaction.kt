package com.smartspender.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val amount: Float,
    val category: String,
    val type: String, // "Income" or "Expense"
    val time: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)