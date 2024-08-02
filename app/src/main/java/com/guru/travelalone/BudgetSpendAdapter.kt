package com.guru.travelalone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetSpendAdapter(private val spends: List<BudgetSpend>) : RecyclerView.Adapter<BudgetSpendAdapter.SpendViewHolder>() {

    class SpendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val redAmount: TextView = view.findViewById(R.id.spendMoney)
        val redDate: TextView = view.findViewById(R.id.redDate)
        val redStore: TextView = view.findViewById(R.id.spendStore)
        val redDetail: TextView = view.findViewById(R.id.spendMemo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_red, parent, false)
        return SpendViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpendViewHolder, position: Int) {
        val spend = spends[position]
        holder.redAmount.text = spend.spend_amount
        holder.redDate.text = formatDate(spend.spend_date)
        holder.redStore.text = spend.spend_store
        holder.redDetail.text = spend.spend_memo
    }

    override fun getItemCount(): Int {
        return spends.size
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm", Locale.getDefault())
        return sdf.format(date)
    }
}
