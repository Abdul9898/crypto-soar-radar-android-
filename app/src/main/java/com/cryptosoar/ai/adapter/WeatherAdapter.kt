package com.cryptosoar.ai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cryptosoar.ai.R
import com.cryptosoar.ai.databinding.ItemWeatherCoinBinding
import java.util.*

data class WeatherCoin(
    val name: String,
    val weatherResId: Int,
    val change24h: Double
)

class WeatherAdapter(
    private var items: List<WeatherCoin>
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    fun updateList(newItems: List<WeatherCoin>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = ItemWeatherCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WeatherViewHolder(private val binding: ItemWeatherCoinBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeatherCoin) {
            val context = itemView.context
            binding.tvWeatherCoinName.text = item.name
            binding.ivWeatherIcon.setImageResource(item.weatherResId)

            val prefix = if (item.change24h >= 0) "+" else ""
            binding.tvWeatherCoinChange.text = String.format(Locale.US, "%s%.2f%%", prefix, item.change24h)
            if (item.change24h >= 0) {
                binding.tvWeatherCoinChange.setTextColor(ContextCompat.getColor(context, R.color.surge_green))
            } else {
                binding.tvWeatherCoinChange.setTextColor(ContextCompat.getColor(context, R.color.surge_red))
            }
        }
    }
}
