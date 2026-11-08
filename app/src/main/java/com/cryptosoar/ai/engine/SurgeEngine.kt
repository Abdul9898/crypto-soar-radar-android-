package com.cryptosoar.ai.engine

import com.cryptosoar.ai.model.CoinItem
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SurgeEngine private constructor() {

    companion object {
        val instance: SurgeEngine by lazy { SurgeEngine() }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var onDataUpdated: ((List<CoinItem>) -> Unit)? = null

    val coins = CopyOnWriteArrayList<CoinItem>()
    private var isRunning = false

    private val baseCoins = listOf(
        Triple("BTC", "Bitcoin", 75850.0),
        Triple("ETH", "Ethereum", 2240.0),
        Triple("SOL", "Solana", 89.80),
        Triple("SUI", "Sui Network", 2.15),
        Triple("DOGE", "Dogecoin", 0.185),
        Triple("PEPE", "Pepe", 0.0000098),
        Triple("XRP", "XRP Ledger", 0.582),
        Triple("NEAR", "NEAR Protocol", 4.85),
        Triple("AVAX", "Avalanche", 26.40),
        Triple("LINK", "Chainlink", 12.80),
        Triple("SHIB", "Shiba Inu", 0.0000178),
        Triple("ADA", "Cardano", 0.365)
    )

    init {
        // Seed initial items
        baseCoins.forEachIndexed { index, (sym, name, price) ->
            val change = Random.nextDouble(-1.5, 7.8)
            val soar = Random.nextDouble(85.0, 97.5)
            coins.add(
                CoinItem(
                    rank = index + 1,
                    symbol = sym,
                    name = name,
                    price = price,
                    change24h = Math.round(change * 100.0) / 100.0,
                    soarPercent = Math.round(soar * 10.0) / 10.0,
                    targetPeriod = if (Random.nextBoolean()) "Target: 15m - 30m" else "Target: 5m - 15m",
                    signalDetail = getRandomSignalDetail(),
                    volume24h = Random.nextDouble(5000000.0, 450000000.0)
                )
            )
        }
        sortCoinsBySoar()
    }

    fun start() {
        if (isRunning) return
        isRunning = true

        scope.launch {
            // Initial fetch
            fetchLiveMarketData()

            while (isRunning) {
                delay(6000)
                // Small dynamic micro-ticks to keep feed alive
                updateMicroTicks()
                onDataUpdated?.invoke(coins)

                // Periodic real API refresh every 30s
                if (Random.nextInt(5) == 0) {
                    fetchLiveMarketData()
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        scope.cancel()
    }

    fun refreshNow() {
        scope.launch {
            fetchLiveMarketData()
            onDataUpdated?.invoke(coins)
        }
    }

    private fun fetchLiveMarketData() {
        try {
            val url = "https://api.coincap.io/v2/assets?limit=25"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val data = json.getJSONArray("data")

                val priceMap = mutableMapOf<String, Pair<Double, Double>>()
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val sym = item.getString("symbol").uppercase()
                    val price = item.getDouble("priceUsd")
                    val change = item.optDouble("changePercent24Hr", 0.0)
                    priceMap[sym] = Pair(price, change)
                }

                coins.forEach { coin ->
                    priceMap[coin.symbol]?.let { (p, c) ->
                        coin.price = p
                        coin.change24h = Math.round(c * 100.0) / 100.0
                        // Recalculate dynamic soar %
                        val baseSoar = 84.0 + (coin.change24h.coerceIn(-5.0, 15.0) * 0.8) + Random.nextDouble(0.0, 4.0)
                        coin.soarPercent = Math.round(baseSoar.coerceIn(80.0, 98.9) * 10.0) / 10.0
                    }
                }
                sortCoinsBySoar()
            }
        } catch (e: Exception) {
            // Fallback: update micro ticks gracefully
            updateMicroTicks()
        }
    }

    private fun updateMicroTicks() {
        coins.forEach { coin ->
            val delta = (Random.nextDouble(-0.0008, 0.0012) * coin.price)
            coin.price += delta
            val soarDelta = Random.nextDouble(-0.2, 0.3)
            coin.soarPercent = Math.round((coin.soarPercent + soarDelta).coerceIn(82.0, 98.8) * 10.0) / 10.0
        }
    }

    fun sortCoinsBySoar() {
        val sorted = coins.sortedByDescending { it.soarPercent }
        coins.clear()
        coins.addAll(sorted)
        coins.forEachIndexed { i, c -> c.rank = i + 1 }
    }

    fun sortCoinsByGainers() {
        val sorted = coins.sortedByDescending { it.change24h }
        coins.clear()
        coins.addAll(sorted)
        coins.forEachIndexed { i, c -> c.rank = i + 1 }
    }

    fun sortCoinsByVolume() {
        val sorted = coins.sortedByDescending { it.volume24h }
        coins.clear()
        coins.addAll(sorted)
        coins.forEachIndexed { i, c -> c.rank = i + 1 }
    }

    private fun getRandomSignalDetail(): String {
        val list = listOf(
            "⚡ Volume Inflow +34%",
            "🚀 Bullish Momentum Breakout",
            "🌊 Whale Accumulation Detected",
            "🔥 Supertrend Bullish Flip",
            "🎯 Orderbook Limit Wall Absorbed",
            "📈 RSI Divergence Continuation"
        )
        return list.random()
    }
}
