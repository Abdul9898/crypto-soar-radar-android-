package com.cryptosoar.ai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cryptosoar.ai.R
import com.cryptosoar.ai.databinding.ItemPotentialCardBinding
import java.util.*

data class PotentialItem(
    val rank: Int,
    val symbol: String,
    val name: String,
    val price: Double,
    val percent: Int
)

class PotentialAdapter(
    private var items: List<PotentialItem>
) : RecyclerView.Adapter<PotentialAdapter.PotentialViewHolder>() {

    fun updateList(newItems: List<PotentialItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PotentialViewHolder {
        val binding = ItemPotentialCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PotentialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PotentialViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PotentialViewHolder(private val binding: ItemPotentialCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PotentialItem) {
            val context = itemView.context
            binding.tvPotentialRank.text = "${item.rank}"
            binding.tvPotentialSymbol.text = item.symbol
            binding.tvPotentialName.text = item.name

            val priceStr = when {
                item.price >= 1000.0 -> String.format(Locale.US, "$%,.2f", item.price)
                item.price >= 1.0 -> String.format(Locale.US, "$%.2f", item.price)
                else -> String.format(Locale.US, "$%.4f", item.price)
            }
            binding.tvPotentialPrice.text = priceStr

            binding.pbPotential.progress = item.percent
            binding.tvPotentialPercent.text = "${item.percent}%"
        }
    }
}
