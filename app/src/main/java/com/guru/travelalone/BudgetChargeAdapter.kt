package com.guru.travelalone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetChargeAdapter(private val charges: List<BudgetCharge>) : RecyclerView.Adapter<BudgetChargeAdapter.ChargeViewHolder>() {

    class ChargeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val greenAmount: TextView = view.findViewById(R.id.greenAmount)
        val greenDate: TextView = view.findViewById(R.id.greenDate)
        val greenDetail: TextView = view.findViewById(R.id.greenDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_green, parent, false)
        return ChargeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChargeViewHolder, position: Int) {
        val charge = charges[position]
        holder.greenAmount.text = charge.charge_amount
        holder.greenDate.text = formatDate(charge.charge_date)
        holder.greenDetail.text = charge.charge_memo
    }

    override fun getItemCount(): Int {
        return charges.size
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault())
        return sdf.format(date)
    }
}
