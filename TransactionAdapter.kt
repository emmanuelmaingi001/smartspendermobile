package com.smartspender.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.tvCategory)
        val type: TextView = view.findViewById(R.id.tvType)
        val amount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transactions[position]
        holder.category.text = item.category
        holder.type.text = "${item.type} • ${item.time}"
        holder.amount.text = "KES ${String.format("%,.2f", item.amount)}"

        // Fintech colors: Dark Green for Income, Bold Red for Expense
        holder.amount.setTextColor(if (item.type == "Income") Color.parseColor("#2E7D32") else Color.parseColor("#D32F2F"))
    }

    override fun getItemCount() = transactions.size
}