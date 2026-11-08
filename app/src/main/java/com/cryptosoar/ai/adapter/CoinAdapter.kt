package com.cryptosoar.ai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cryptosoar.ai.R
import com.cryptosoar.ai.databinding.ItemCoinBinding
import com.cryptosoar.ai.model.CoinItem
import java.util.*

class CoinAdapter(
    private var items: List<CoinItem>
) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    fun updateList(newItems: List<CoinItem>) {
        items = newItems.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CoinViewHolder(private val binding: ItemCoinBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coin: CoinItem) {
            val context = itemView.context
            binding.tvRank.text = "#${coin.rank}"
            binding.tvSymbol.text = coin.symbol
            binding.tvName.text = coin.name

            // Format price
            val priceStr = when {
                coin.price >= 1000.0 -> String.format(Locale.US, "$%,.2f", coin.price)
                coin.price >= 1.0 -> String.format(Locale.US, "$%.2f", coin.price)
                coin.price >= 0.001 -> String.format(Locale.US, "$%.4f", coin.price)
                else -> String.format(Locale.US, "$%.8f", coin.price)
            }
            binding.tvPrice.text = priceStr

            // 24h change
            val prefix = if (coin.change24h >= 0) "+" else ""
            binding.tvChange24h.text = String.format(Locale.US, "%s%.2f%% (24h)", prefix, coin.change24h)
            if (coin.change24h >= 0) {
                binding.tvChange24h.setTextColor(ContextCompat.getColor(context, R.color.surge_green))
            } else {
                binding.tvChange24h.setTextColor(ContextCompat.getColor(context, R.color.surge_red))
            }

            // Soar %
            binding.tvSoarPercent.text = "${coin.soarPercent}%"
            binding.tvTargetPeriod.text = coin.targetPeriod
            binding.tvSignalDetail.text = coin.signalDetail
        }
    }
}
