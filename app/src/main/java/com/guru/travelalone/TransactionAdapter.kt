package com.guru.travelalone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guru.travelalone.databinding.ItemTransactionGreenBinding
import com.guru.travelalone.databinding.ItemTransactionRedBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private val transactionList: List<Transaction>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CHARGE = 0
        private const val TYPE_SPEND = 1

        private fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            return sdf.format(date)
        }

        private fun formatAmount(amount: String): String {
            val numberFormat = NumberFormat.getInstance(Locale.getDefault())
            return numberFormat.format(amount.replace(",", "").toIntOrNull() ?: 0) + "원"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (transactionList[position].type) {
            TransactionType.CHARGE -> TYPE_CHARGE
            TransactionType.SPEND -> TYPE_SPEND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CHARGE -> {
                val binding = ItemTransactionGreenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChargeViewHolder(binding)
            }
            TYPE_SPEND -> {
                val binding = ItemTransactionRedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SpendViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transaction = transactionList[position]
        when (holder) {
            is ChargeViewHolder -> holder.bind(transaction)
            is SpendViewHolder -> holder.bind(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    class ChargeViewHolder(private val binding: ItemTransactionGreenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.greenAmount.text = formatAmount(transaction.amount)
            binding.greenDetail.text = transaction.memo
            binding.greenDate.text = formatDate(transaction.date)
        }
    }

    class SpendViewHolder(private val binding: ItemTransactionRedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.redAmount.text = formatAmount(transaction.amount)
            binding.redDetail.text = transaction.memo
            binding.redDate.text = formatDate(transaction.date)
            binding.redstore.text = transaction.store
            binding.redCategory.setImageResource(getCategoryIcon(transaction.category))
        }

        private fun getCategoryIcon(category: String?): Int {
            return when (category) {
                "SHOPPING" -> R.drawable.budget_shopping
                "FOOD" -> R.drawable.budget_food
                "ETC" -> R.drawable.budget_etc
                else -> R.drawable.budget_etc
            }
        }
    }
}