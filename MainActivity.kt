package com.smartspender.app

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val transactionList = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private lateinit var pieChart: PieChart

    // Database and Repository setup
    private lateinit var repository: TransactionRepository

    // SmartSpender Signature Branding
    private val smartSpenderBlue = Color.parseColor("#1976D2")

    private val smsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val amount = intent?.getFloatExtra("amount", 0f) ?: 0f
            val category = intent?.getStringExtra("category") ?: "Other"
            val isIncome = intent?.getBooleanExtra("isIncome", false) ?: false
            val type = if (isIncome) "Income" else "Expense"

            // Save to Database and then refresh UI
            lifecycleScope.launch {
                repository.insert(Transaction(amount = amount, category = category, type = type))
                refreshData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Repository via MainApplication
        repository = (application as MainApplication).repository

        pieChart = findViewById(R.id.pieChart)
        setupPieChartStyle()

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactionList)
        rv.adapter = adapter

        checkPermissions()
        refreshData()
    }

    private fun refreshData() {
        // Load all transactions from the database
        lifecycleScope.launch {
            repository.allTransactions.collect { transactions ->
                transactionList.clear()
                transactionList.addAll(transactions)

                // Add sample data if database is empty
                if (transactionList.isEmpty()) {
                    repository.insert(Transaction(amount = 10000f, category = "Initial Deposit", type = "Income"))
                    repository.insert(Transaction(amount = 2500f, category = "Groceries", type = "Expense"))
                }

                updateUI()
            }
        }
    }

    private fun setupPieChartStyle() {
        pieChart.apply {
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 60f
            setDrawCenterText(true)
            setCenterTextColor(smartSpenderBlue)
            setCenterTextSize(18f)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1200)
        }
    }

    private fun updateUI() {
        // Fixed the Type Mismatch: sumOf requires Double or Long
        val income = transactionList.filter { it.type == "Income" }.sumOf { it.amount.toDouble() }
        val expense = transactionList.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }

        findViewById<TextView>(R.id.tvDailyIncome).text = "Income: KES ${String.format("%,.2f", income)}"
        findViewById<TextView>(R.id.tvDailyExpense).text = "Spent: KES ${String.format("%,.2f", expense)}"

        val expenseEntries = transactionList.filter { it.type == "Expense" }
            .map { PieEntry(it.amount, it.category) }

        if (expenseEntries.isNotEmpty()) {
            val dataSet = PieDataSet(expenseEntries, "")
            dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            dataSet.valueTextSize = 12f
            dataSet.sliceSpace = 3f
            pieChart.data = PieData(dataSet)
            pieChart.centerText = "SmartSpender\nTotal: KES ${String.format("%,.0f", expense)}"
        } else {
            pieChart.centerText = "SmartSpender\nReady"
        }

        pieChart.invalidate()
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.smartspender.UPDATE_CHART")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsUpdateReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsUpdateReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(smsUpdateReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    private fun checkPermissions() {
        val p = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (p.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, p, 101)
        }
    }
}