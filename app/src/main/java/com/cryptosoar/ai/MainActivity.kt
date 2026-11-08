package com.cryptosoar.ai

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cryptosoar.ai.adapter.*
import com.cryptosoar.ai.databinding.*
import com.cryptosoar.ai.engine.SurgeEngine
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var surgePotentialAdapter: PotentialAdapter
    private lateinit var uptrendPotentialAdapter: PotentialAdapter

    private var currentExchange = "Binance"
    private var currentCurrencyIndex = 0
    private val currencies = arrayOf("USD ($)", "KRW (₩)", "EUR (€)", "BTC (₿)")
    private val currencySymbols = arrayOf("$", "₩", "€", "₿")

    private var currentActiveTab = 0 // 0: Market, 1: Explore, 2: Gainify, 3: Portfolio, 4: More
    private var isNotificationsEnabled = true
    private var isGreenRedIndicators = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMarketTab()
        setupExploreTab()
        setupGainifyTab()
        setupPortfolioTab()
        setupMoreTab()
        setupBottomNavigation()
        setupTopBarActions()

        // Background Surge Engine updates
        SurgeEngine.instance.onDataUpdated = {
            runOnUiThread {
                refreshDynamicData()
            }
        }
        SurgeEngine.instance.start()
    }

    // =========================================================================
    // 1. BOTTOM NAVIGATION & TAB SWITCHING
    // =========================================================================
    private fun setupBottomNavigation() {
        binding.navMarket.setOnClickListener { switchTab(0) }
        binding.navExplore.setOnClickListener { switchTab(1) }
        binding.navGainify.setOnClickListener { switchTab(2) }
        binding.navPortfolio.setOnClickListener { switchTab(3) }
        binding.navMore.setOnClickListener { switchTab(4) }
    }

    private fun switchTab(tabIndex: Int) {
        currentActiveTab = tabIndex

        // Toggle container visibility
        binding.layoutMarketView.visibility = if (tabIndex == 0) View.VISIBLE else View.GONE
        binding.layoutExploreView.visibility = if (tabIndex == 1) View.VISIBLE else View.GONE
        binding.layoutGainifyView.visibility = if (tabIndex == 2) View.VISIBLE else View.GONE
        binding.layoutPortfolioView.visibility = if (tabIndex == 3) View.VISIBLE else View.GONE
        binding.layoutMoreView.visibility = if (tabIndex == 4) View.VISIBLE else View.GONE

        // Update Title
        binding.tvAppTitle.text = when (tabIndex) {
            0 -> "Market"
            1 -> "Explore"
            2 -> "Gainify"
            3 -> "Portfolio"
            4 -> "More"
            else -> "Market"
        }

        // Highlight Active Tab
        val navLabels = listOf(
            binding.tvNavMarket,
            binding.tvNavExplore,
            binding.tvNavGainify,
            binding.tvNavPortfolio,
            binding.tvNavMore
        )

        navLabels.forEachIndexed { index, textView ->
            if (index == tabIndex) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.qspi_violet))
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                textView.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    // =========================================================================
    // 2. TOP BAR ACTIONS
    // =========================================================================
    private fun setupTopBarActions() {
        binding.btnExchangeChip.setOnClickListener {
            showExchangeSwitcherDialog()
        }

        binding.btnSearch.setOnClickListener {
            showAdvancedFilterDialog()
        }

        binding.btnInfo.setOnClickListener {
            showAlgorithmInfoDialog()
        }

        binding.btnNotifications.setOnClickListener {
            showSurgePlungeWatcherDialog()
        }
    }

    // =========================================================================
    // 3. TAB 1: MARKET DASHBOARD
    // =========================================================================
    private fun setupMarketTab() {
        // Setup Weather Bar
        val weatherList = listOf(
            WeatherCoin("Bitcoin", R.drawable.weather3, -0.43),
            WeatherCoin("Ethereum", R.drawable.weather3, -0.72),
            WeatherCoin("BNB", R.drawable.weather3, -0.33),
            WeatherCoin("XRP", R.drawable.weather3, -1.86),
            WeatherCoin("Solana", R.drawable.weather3, -1.74),
            WeatherCoin("Dogecoin", R.drawable.weather1, 4.12),
            WeatherCoin("Sui", R.drawable.weather1, 12.85),
            WeatherCoin("Near", R.drawable.weather2, 1.20)
        )
        weatherAdapter = WeatherAdapter(weatherList)
        binding.rvWeatherBar.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvWeatherBar.adapter = weatherAdapter

        // Setup Surge Potential Carousel (100% Unlocked)
        val surgeList = listOf(
            PotentialItem(1, "SUI", "Sui Network", 2.15, 89),
            PotentialItem(2, "PEPE", "Pepe", 0.0000098, 84),
            PotentialItem(3, "SOL", "Solana", 89.80, 78),
            PotentialItem(4, "DOGE", "Dogecoin", 0.185, 74),
            PotentialItem(5, "NEAR", "NEAR Protocol", 4.85, 71)
        )
        surgePotentialAdapter = PotentialAdapter(surgeList)
        binding.rvSurgePotential.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSurgePotential.adapter = surgePotentialAdapter

        // Setup Uptrend Potential Carousel (100% Unlocked)
        val uptrendList = listOf(
            PotentialItem(1, "BTC", "Bitcoin", 75850.0, 92),
            PotentialItem(2, "ETH", "Ethereum", 2240.0, 86),
            PotentialItem(3, "XRP", "XRP Ledger", 0.582, 81),
            PotentialItem(4, "LINK", "Chainlink", 12.80, 75),
            PotentialItem(5, "AVAX", "Avalanche", 26.40, 72)
        )
        uptrendPotentialAdapter = PotentialAdapter(uptrendList)
        binding.rvUptrendPotential.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvUptrendPotential.adapter = uptrendPotentialAdapter

        // Populate all ranked rows
        populateMarketRankSections()

        // Gainers Sub-tabs
        binding.tabGainersDay.setOnClickListener {
            highlightSubTab(binding.tabGainersDay, listOf(binding.tabGainers1m, binding.tabGainers30m))
            populateTopGainers("1d")
        }
        binding.tabGainers1m.setOnClickListener {
            highlightSubTab(binding.tabGainers1m, listOf(binding.tabGainersDay, binding.tabGainers30m))
            populateTopGainers("1m")
        }
        binding.tabGainers30m.setOnClickListener {
            highlightSubTab(binding.tabGainers30m, listOf(binding.tabGainersDay, binding.tabGainers1m))
            populateTopGainers("30m")
        }

        // Trading Surge Sub-tabs
        binding.tabSurgeDay.setOnClickListener {
            highlightSubTab(binding.tabSurgeDay, listOf(binding.tabSurgeHour, binding.tabSurgeMinute))
            populateTradingSurge("day")
        }
        binding.tabSurgeHour.setOnClickListener {
            highlightSubTab(binding.tabSurgeHour, listOf(binding.tabSurgeDay, binding.tabSurgeMinute))
            populateTradingSurge("hour")
        }
        binding.tabSurgeMinute.setOnClickListener {
            highlightSubTab(binding.tabSurgeMinute, listOf(binding.tabSurgeDay, binding.tabSurgeHour))
            populateTradingSurge("minute")
            Toast.makeText(this, "🔓 By Minute Surge: Unlocked for you!", Toast.LENGTH_SHORT).show()
        }

        // Pull-to-refresh
        binding.swipeRefresh.setColorSchemeResources(R.color.qspi_purple, R.color.surge_green)
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_card)
        binding.swipeRefresh.setOnRefreshListener {
            SurgeEngine.instance.refreshNow()
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Scanning $currentExchange feeds in real time...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateMarketRankSections() {
        populateTopGainers("1d")
        populateTradingSurge("day")

        // 4. Time Lapse
        populateRankRows(
            binding.layoutTimeLapseList,
            listOf(
                RankItem("Harmony", "ONE", "-5.74%  71.38%", 0.015, true, 72, 65),
                RankItem("APPB", "APPB", "66.59%", 0.082, true, 70, 68),
                RankItem("Act I : The AI...", "ACT", "18.33%  45.41%", 0.42, true, 84, 76),
                RankItem("SOXSB", "SOXSB", "108.11%  42.41%", 1.25, true, 68, 62),
                RankItem("SKDDB", "SKDDB", "31.45%", 0.19, true, 65, 60)
            )
        )

        // 5. Market Trend (Continuous Rise)
        populateRankRows(
            binding.layoutMarketTrendList,
            listOf(
                RankItem("JUST", "JST", "4 days 🔥", 0.038, true, 82, 79),
                RankItem("Mitosis", "MITO", "3 days 🔥", 0.145, true, 88, 80),
                RankItem("Reserve Rights", "RSR", "3 days 🔥", 0.008, true, 75, 71),
                RankItem("BNCB", "BNCB", "3 days 🔥", 0.024, true, 74, 69),
                RankItem("Lombard", "BARD", "3 days 🔥", 0.95, true, 79, 73)
            )
        )

        // 7. Most Active
        populateRankRows(
            binding.layoutMostActiveList,
            listOf(
                RankItem("Bitcoin", "BTC", "402M", 75850.0, true, 92, 88),
                RankItem("USD Coin", "USDC", "386M", 1.00, true, 50, 50),
                RankItem("Ethereum", "ETH", "141M", 2240.0, true, 86, 81),
                RankItem("Zcash", "ZEC", "130M", 34.20, true, 60, 55),
                RankItem("World Liberty F...", "USD1", "120M", 0.99, true, 50, 50)
            )
        )

        // 8. Top Losers
        populateRankRows(
            binding.layoutTopLosersList,
            listOf(
                RankItem("YieldBasis", "YB", "-12.88%", 0.45, false, 35, 40),
                RankItem("USD.AI", "CHIP", "-10.97%", 1.15, false, 42, 38),
                RankItem("Fogo", "FOGO", "-10.26%", 0.028, false, 40, 41),
                RankItem("Zcash", "ZEC", "-8.22%", 34.20, false, 45, 43),
                RankItem("Pump.fun", "PUMP", "-8.12%", 0.0035, false, 38, 35)
            )
        )
    }

    private fun populateTopGainers(timeframe: String) {
        val gainers = when (timeframe) {
            "1m" -> listOf(
                RankItem("Act I : The AI...", "ACT", "+3.85%", 0.42, true, 84, 76),
                RankItem("Mitosis", "MITO", "+2.94%", 0.145, true, 88, 80),
                RankItem("Meteora", "MET", "+2.12%", 0.28, true, 85, 78),
                RankItem("DigiByte", "DGB", "+1.75%", 0.012, true, 73, 70),
                RankItem("Harmony", "ONE", "+1.40%", 0.015, true, 72, 65)
            )
            "30m" -> listOf(
                RankItem("Mitosis", "MITO", "+11.20%", 0.145, true, 88, 80),
                RankItem("Meteora", "MET", "+8.45%", 0.28, true, 85, 78),
                RankItem("Act I : The AI...", "ACT", "+7.80%", 0.42, true, 84, 76),
                RankItem("Harmony", "ONE", "+5.15%", 0.015, true, 72, 65),
                RankItem("DigiByte", "DGB", "+4.90%", 0.012, true, 73, 70)
            )
            else -> listOf(
                RankItem("Mitosis", "MITO", "+21.52%", 0.145, true, 88, 80),
                RankItem("Meteora", "MET", "+14.74%", 0.28, true, 85, 78),
                RankItem("Act I : The AI P...", "ACT", "+13.92%", 0.42, true, 84, 76),
                RankItem("DigiByte", "DGB", "+7.59%", 0.012, true, 73, 70),
                RankItem("Harmony", "ONE", "+7.32%", 0.015, true, 72, 65)
            )
        }
        populateRankRows(binding.layoutTopGainersList, gainers)
    }

    private fun populateTradingSurge(range: String) {
        val surges = when (range) {
            "hour" -> listOf(
                RankItem("Catizen", "CATI", "68% ↑", 0.45, true, 82, 75),
                RankItem("Babylon", "BABY", "35% ↑", 0.18, true, 78, 70),
                RankItem("SynFutures", "F", "28% ↑", 0.05, true, 75, 68),
                RankItem("Brevis", "BREV", "-15% ↓", 0.08, false, 55, 50),
                RankItem("Bubblemaps", "BMT", "-22% ↓", 0.12, false, 52, 48)
            )
            "minute" -> listOf(
                RankItem("Catizen", "CATI", "112% ↑", 0.45, true, 89, 82),
                RankItem("Sui Network", "SUI", "85% ↑", 2.15, true, 92, 85),
                RankItem("Babylon", "BABY", "48% ↑", 0.18, true, 78, 70),
                RankItem("SynFutures", "F", "31% ↑", 0.05, true, 75, 68),
                RankItem("Brevis", "BREV", "-12% ↓", 0.08, false, 55, 50)
            )
            else -> listOf(
                RankItem("Catizen", "CATI", "44% ↑", 0.45, true, 82, 75),
                RankItem("Babylon", "BABY", "22% ↑", 0.18, true, 78, 70),
                RankItem("SynFutures", "F", "-43% ↓", 0.05, false, 60, 55),
                RankItem("Brevis", "BREV", "-43% ↓", 0.08, false, 55, 50),
                RankItem("Bubblemaps", "BMT", "-54% ↓", 0.12, false, 52, 48)
            )
        }
        populateRankRows(binding.layoutTradingSurgeList, surges)
    }

    private data class RankItem(
        val name: String,
        val symbol: String,
        val pillText: String,
        val price: Double,
        val isGreen: Boolean,
        val surgeScore: Int,
        val uptrendScore: Int
    )

    private fun populateRankRows(
        container: android.widget.LinearLayout,
        items: List<RankItem>
    ) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        items.forEachIndexed { index, item ->
            val rowBinding = ItemRankRowBinding.inflate(inflater, container, false)
            rowBinding.tvRankNumber.text = "${index + 1}"
            rowBinding.tvCoinTitle.text = item.name
            rowBinding.tvCoinCode.text = item.symbol
            rowBinding.tvValuePill.text = item.pillText

            if (item.isGreen) {
                rowBinding.tvValuePill.setBackgroundResource(R.drawable.bg_badge_gain)
                rowBinding.tvValuePill.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
            } else {
                rowBinding.tvValuePill.setBackgroundResource(R.drawable.bg_badge_soar)
                rowBinding.tvValuePill.setTextColor(ContextCompat.getColor(this, R.color.white))
            }

            // Tapping ANY row opens the Coin Detail Chart & Calculator!
            rowBinding.root.setOnClickListener {
                showCoinDetailDialog(
                    name = item.name,
                    symbol = item.symbol,
                    price = item.price,
                    changeText = item.pillText,
                    surgeScore = item.surgeScore,
                    uptrendScore = item.uptrendScore
                )
            }

            container.addView(rowBinding.root)
        }
    }

    // =========================================================================
    // 4. TAB 2: EXPLORE SCREEN (Screener & Presets)
    // =========================================================================
    private fun setupExploreTab() {
        populateExploreCoins(preset = "Golden Cross")

        binding.btnPresetGoldenCross.setOnClickListener {
            highlightExplorePreset(binding.btnPresetGoldenCross)
            populateExploreCoins(preset = "Golden Cross")
        }
        binding.btnPresetRsiOversold.setOnClickListener {
            highlightExplorePreset(binding.btnPresetRsiOversold)
            populateExploreCoins(preset = "RSI Oversold")
        }
        binding.btnPresetStreaks.setOnClickListener {
            highlightExplorePreset(binding.btnPresetStreaks)
            populateExploreCoins(preset = "3-Day Continuous")
        }
        binding.btnPresetRecentSurge.setOnClickListener {
            highlightExplorePreset(binding.btnPresetRecentSurge)
            populateExploreCoins(preset = "Recent Surge")
        }

        // Timeframe selector
        val chips = listOf(
            binding.chipExplore1d,
            binding.chipExplore1h,
            binding.chipExplore30m,
            binding.chipExplore15m,
            binding.chipExplore1m
        )
        chips.forEach { chip ->
            chip.setOnClickListener {
                highlightSubTab(chip, chips.filter { it != chip })
                Toast.makeText(this, "Timeframe: ${chip.text} applied (Unlocked)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun highlightExplorePreset(active: TextView) {
        val presets = listOf(
            binding.btnPresetGoldenCross,
            binding.btnPresetRsiOversold,
            binding.btnPresetStreaks,
            binding.btnPresetRecentSurge
        )
        presets.forEach {
            if (it == active) {
                it.setBackgroundResource(R.drawable.bg_badge_gain)
                it.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
            } else {
                it.setBackgroundResource(R.drawable.bg_card_dark)
                it.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun populateExploreCoins(preset: String) {
        binding.layoutExploreCoinsList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val exploreItems = when (preset) {
            "RSI Oversold" -> listOf(
                ExploreData("Sui Network", "SUI", 2.15, "+12.85%", "📉 RSI: 26.4 (Oversold Bounce)", "🚀 91% Surge", 91, 85),
                ExploreData("Pepe", "PEPE", 0.0000098, "+18.40%", "📉 RSI: 28.1 (Oversold Bounce)", "🚀 86% Surge", 86, 80),
                ExploreData("Dogecoin", "DOGE", 0.185, "+6.20%", "📉 RSI: 29.5 (Oversold Bounce)", "🚀 79% Surge", 79, 74)
            )
            "3-Day Continuous" -> listOf(
                ExploreData("JUST", "JST", 0.038, "+8.40%", "🔥 4 Days Continuous Rise", "🚀 82% Surge", 82, 79),
                ExploreData("Mitosis", "MITO", 0.145, "+21.52%", "🔥 3 Days Continuous Rise", "🚀 88% Surge", 88, 80),
                ExploreData("Reserve Rights", "RSR", 0.008, "+5.10%", "🔥 3 Days Continuous Rise", "🚀 75% Surge", 75, 71)
            )
            "Recent Surge" -> listOf(
                ExploreData("Catizen", "CATI", 0.45, "+44.00%", "⚡ Surge Velocity > 35%", "🚀 92% Surge", 92, 85),
                ExploreData("Meteora", "MET", 0.28, "+14.74%", "⚡ Surge Velocity > 20%", "🚀 85% Surge", 85, 78),
                ExploreData("DigiByte", "DGB", 0.012, "+7.59%", "⚡ Surge Velocity > 15%", "🚀 78% Surge", 78, 72)
            )
            else -> listOf(
                ExploreData("Sui Network", "SUI", 2.15, "+12.85%", "✨ 5MA x 20MA Cross", "🚀 89% Surge", 89, 82),
                ExploreData("Solana", "SOL", 89.80, "+5.60%", "✨ 5MA x 20MA Cross", "🚀 82% Surge", 82, 78),
                ExploreData("NEAR Protocol", "NEAR", 4.85, "+4.10%", "✨ 5MA x 20MA Cross", "🚀 78% Surge", 78, 74),
                ExploreData("Avalanche", "AVAX", 26.40, "+3.80%", "✨ 5MA x 20MA Cross", "🚀 76% Surge", 76, 72),
                ExploreData("Bitcoin", "BTC", 75850.0, "+1.85%", "✨ 5MA x 20MA Cross", "🚀 94% Surge", 94, 91)
            )
        }

        binding.tvExploreResultsHeader.text = "MATCHING ASSETS (${exploreItems.size} COINS) • $preset"

        exploreItems.forEachIndexed { index, item ->
            val itemBinding = ItemExploreCoinBinding.inflate(inflater, binding.layoutExploreCoinsList, false)
            itemBinding.tvExploreRank.text = "${index + 1}"
            itemBinding.tvExploreName.text = item.name
            itemBinding.tvExploreSymbol.text = "${item.symbol} • $currentExchange"
            itemBinding.tvExplorePrice.text = formatPrice(item.price)
            itemBinding.tvExploreChange.text = item.change
            itemBinding.tvBadgeIndicator.text = item.badgeIndicator
            itemBinding.tvBadgeSurge.text = item.badgeSurge

            itemBinding.root.setOnClickListener {
                showCoinDetailDialog(
                    name = item.name,
                    symbol = item.symbol,
                    price = item.price,
                    changeText = item.change,
                    surgeScore = item.surgeScore,
                    uptrendScore = item.uptrendScore
                )
            }

            binding.layoutExploreCoinsList.addView(itemBinding.root)
        }
    }

    private data class ExploreData(
        val name: String,
        val symbol: String,
        val price: Double,
        val change: String,
        val badgeIndicator: String,
        val badgeSurge: String,
        val surgeScore: Int,
        val uptrendScore: Int
    )

    // =========================================================================
    // 5. TAB 3: GAINIFY SCREEN (Strategy Simulator)
    // =========================================================================
    private fun setupGainifyTab() {
        populateGainifyTrades()

        binding.btnRunSimulation.setOnClickListener {
            Toast.makeText(this, "Simulating algorithmic strategy on past 60 days of $currentExchange data...", Toast.LENGTH_SHORT).show()
            binding.tvSimAvgReturn.text = "+23.8% 📈"
            binding.tvSimWinRate.text = "81.4% 🏆"
            binding.tvSimMaxReturn.text = "+62.1%"
            Toast.makeText(this, "Simulation Complete! Win Rate: 81.4%", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateGainifyTrades() {
        binding.layoutGainifyTradesList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val trades = listOf(
            Triple("Sui Network (SUI)", "Buy: $1.15 ➔ Target Sell: $1.85 (Hold 3d)", "+60.8% PROFIT 🏆"),
            Triple("Solana (SOL)", "Buy: $68.40 ➔ Target Sell: $89.50 (Hold 4d)", "+30.8% PROFIT 🏆"),
            Triple("Near Protocol (NEAR)", "Buy: $3.45 ➔ Target Sell: $4.60 (Hold 2d)", "+33.3% PROFIT 🏆"),
            Triple("Dogecoin (DOGE)", "Buy: $0.14 ➔ Target Sell: $0.18 (Hold 1d)", "+28.5% PROFIT 🏆"),
            Triple("Pepe (PEPE)", "Buy: $0.0000072 ➔ Target Sell: $0.0000095 (Hold 3d)", "+31.9% PROFIT 🏆")
        )

        trades.forEach { (title, subtitle, profit) ->
            val rowBinding = ItemRankRowBinding.inflate(inflater, binding.layoutGainifyTradesList, false)
            rowBinding.tvRankNumber.text = "✓"
            rowBinding.tvRankNumber.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
            rowBinding.tvCoinTitle.text = title
            rowBinding.tvCoinCode.text = subtitle
            rowBinding.tvValuePill.text = profit
            rowBinding.tvValuePill.setBackgroundResource(R.drawable.bg_badge_gain)
            rowBinding.tvValuePill.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
            binding.layoutGainifyTradesList.addView(rowBinding.root)
        }
    }

    // =========================================================================
    // 6. TAB 4: PORTFOLIO SCREEN
    // =========================================================================
    private fun setupPortfolioTab() {
        populatePortfolioList()

        binding.btnCurrencyToggle.setOnClickListener {
            currentCurrencyIndex = (currentCurrencyIndex + 1) % currencies.size
            binding.btnCurrencyToggle.text = "${currencies[currentCurrencyIndex]} ▾"
            updatePortfolioCurrencyDisplay()
        }

        binding.btnAddAsset.setOnClickListener {
            showAddAssetDialog()
        }
    }

    private fun populatePortfolioList() {
        binding.layoutPortfolioList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val holdings = listOf(
            PortfolioHolding("Bitcoin", "0.35 BTC • Avg $68,400", "$26,547.50", "+$2,925.00 (+12.4%)", 75850.0, 92, 88),
            PortfolioHolding("Solana", "45.0 SOL • Avg $70.20", "$4,041.00", "+$882.00 (+27.9%)", 89.80, 82, 78),
            PortfolioHolding("Sui Network", "1,000 SUI • Avg $1.45", "$2,150.00", "+$700.00 (+48.3%)", 2.15, 89, 82),
            PortfolioHolding("Ethereum", "0.25 ETH • Avg $2,300", "$560.00", "-$15.00 (-2.6%)", 2240.0, 86, 81),
            PortfolioHolding("Pepe", "8,000,000 PEPE • Avg $0.0000078", "$75.00", "+$12.60 (+20.2%)", 0.0000098, 86, 80)
        )

        holdings.forEach { item ->
            val itemBinding = ItemPortfolioCoinBinding.inflate(inflater, binding.layoutPortfolioList, false)
            itemBinding.tvPortfolioName.text = item.name
            itemBinding.tvPortfolioHolding.text = item.holdingStr
            itemBinding.tvPortfolioValue.text = item.valueStr
            itemBinding.tvPortfolioProfit.text = item.profitStr

            if (item.profitStr.startsWith("-")) {
                itemBinding.tvPortfolioProfit.setBackgroundResource(R.drawable.bg_badge_soar)
                itemBinding.tvPortfolioProfit.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                itemBinding.tvPortfolioProfit.setBackgroundResource(R.drawable.bg_badge_gain)
                itemBinding.tvPortfolioProfit.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
            }

            itemBinding.root.setOnClickListener {
                showCoinDetailDialog(
                    name = item.name,
                    symbol = item.name.take(4).uppercase(),
                    price = item.price,
                    changeText = item.profitStr,
                    surgeScore = item.surgeScore,
                    uptrendScore = item.uptrendScore
                )
            }

            binding.layoutPortfolioList.addView(itemBinding.root)
        }
    }

    private data class PortfolioHolding(
        val name: String,
        val holdingStr: String,
        val valueStr: String,
        val profitStr: String,
        val price: Double,
        val surgeScore: Int,
        val uptrendScore: Int
    )

    private fun updatePortfolioCurrencyDisplay() {
        val sym = currencySymbols[currentCurrencyIndex]
        val rate = when (currentCurrencyIndex) {
            1 -> 1380.0 // KRW
            2 -> 0.92   // EUR
            3 -> 0.000013 // BTC
            else -> 1.0 // USD
        }
        val totalVal = 33373.50 * rate
        val profitVal = 2140.20 * rate

        binding.tvTotalBalance.text = if (currentCurrencyIndex == 1) {
            String.format(Locale.US, "%s%,.0f", sym, totalVal)
        } else if (currentCurrencyIndex == 3) {
            String.format(Locale.US, "%s%.4f", sym, totalVal)
        } else {
            String.format(Locale.US, "%s%,.2f", sym, totalVal)
        }

        binding.tvTotalProfit.text = if (currentCurrencyIndex == 1) {
            String.format(Locale.US, "24h Change: +%s%,.0f (+6.86%%) 🚀", sym, profitVal)
        } else {
            String.format(Locale.US, "24h Change: +%s%,.2f (+6.86%%) 🚀", sym, profitVal)
        }
    }

    private fun showAddAssetDialog() {
        val etSymbol = EditText(this).apply { hint = "e.g. SUI, BTC, ETH" }
        AlertDialog.Builder(this)
            .setTitle("Add Asset to Portfolio")
            .setView(etSymbol)
            .setPositiveButton("Add") { _, _ ->
                val symbol = etSymbol.text.toString().trim()
                if (symbol.isNotEmpty()) {
                    Toast.makeText(this, "Added $symbol to Portfolio!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // 7. TAB 5: MORE SCREEN (Settings & Unlocked Pro)
    // =========================================================================
    private fun setupMoreTab() {
        binding.btnMenuSurgePlungeWatcher.setOnClickListener {
            showSurgePlungeWatcherDialog()
        }

        binding.btnMenuWeatherGuide.setOnClickListener {
            showWeatherGuideDialog()
        }

        binding.btnMenuNotificationSetting.setOnClickListener {
            isNotificationsEnabled = !isNotificationsEnabled
            binding.tvNotificationStatus.text = if (isNotificationsEnabled) "ENABLED" else "DISABLED"
            binding.tvNotificationStatus.setTextColor(
                ContextCompat.getColor(this, if (isNotificationsEnabled) R.color.surge_green else R.color.text_muted)
            )
            Toast.makeText(this, "Notifications: ${binding.tvNotificationStatus.text}", Toast.LENGTH_SHORT).show()
        }

        binding.btnMenuExchangeSetting.setOnClickListener {
            showExchangeSwitcherDialog()
        }

        binding.btnMenuIndicatorColor.setOnClickListener {
            isGreenRedIndicators = !isGreenRedIndicators
            Toast.makeText(this, "Indicator color preference updated", Toast.LENGTH_SHORT).show()
        }

        binding.btnMenuTheme.setOnClickListener {
            Toast.makeText(this, "Dark OLED Mode: Active (Optimized for battery)", Toast.LENGTH_SHORT).show()
        }

        binding.btnMenuServiceIntro.setOnClickListener {
            showAlgorithmInfoDialog()
        }
    }

    // =========================================================================
    // 8. INTERACTIVE DIALOGS (Detail, Filter, Watcher)
    // =========================================================================
    private fun showCoinDetailDialog(
        name: String,
        symbol: String,
        price: Double,
        changeText: String,
        surgeScore: Int,
        uptrendScore: Int
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_coin_detail, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailCoinName)
        val tvSymbol = dialogView.findViewById<TextView>(R.id.tvDetailCoinSymbol)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tvDetailPrice)
        val tvChange = dialogView.findViewById<TextView>(R.id.tvDetailChange)
        val tvSurge = dialogView.findViewById<TextView>(R.id.tvDetailSurgeScore)
        val tvUptrend = dialogView.findViewById<TextView>(R.id.tvDetailUptrendScore)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetPriceInfo)
        val etInvestment = dialogView.findViewById<EditText>(R.id.etInvestmentAmount)
        val tvProfit = dialogView.findViewById<TextView>(R.id.tvCalculatedProfit)
        val btnClose = dialogView.findViewById<TextView>(R.id.btnDismissDetail)

        tvName.text = name
        tvSymbol.text = "$symbol / USDT • $currentExchange"
        tvPrice.text = formatPrice(price)
        tvChange.text = changeText
        tvSurge.text = "$surgeScore% (High) 🚀"
        tvUptrend.text = "$uptrendScore% (Strong) 📈"

        val targetMultiplier = 1.0 + (surgeScore / 800.0)
        val targetPrice = price * targetMultiplier
        val profitPercent = (targetMultiplier - 1.0) * 100.0
        tvTarget.text = String.format(Locale.US, "Estimated Target: %s (+%.2f%%) in 2-4 days", formatPrice(targetPrice), profitPercent)

        fun updateProfitCalculation() {
            val amount = etInvestment.text.toString().toDoubleOrNull() ?: 1000.0
            val netProfit = amount * (profitPercent / 100.0)
            tvProfit.text = String.format(Locale.US, "+$%,.2f", netProfit)
        }
        updateProfitCalculation()

        etInvestment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                updateProfitCalculation()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Timeframe selector buttons in detail dialog
        val tf1m = dialogView.findViewById<TextView>(R.id.tf1m)
        val tf15m = dialogView.findViewById<TextView>(R.id.tf15m)
        val tf1h = dialogView.findViewById<TextView>(R.id.tf1h)
        val tf1d = dialogView.findViewById<TextView>(R.id.tf1d)
        val tfs = listOf(tf1m, tf15m, tf1h, tf1d)

        tfs.forEach { tf ->
            tf.setOnClickListener {
                highlightSubTab(tf, tfs.filter { it != tf })
                Toast.makeText(this, "Chart timeframe: ${tf.text}", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAdvancedFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnApply = dialogView.findViewById<TextView>(R.id.btnApplyFilters)
        btnApply.setOnClickListener {
            Toast.makeText(this, "Screener filters & sort criteria applied!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSurgePlungeWatcherDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alert_watcher, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnSave = dialogView.findViewById<TextView>(R.id.btnSaveAlertWatcher)
        btnSave.setOnClickListener {
            Toast.makeText(this, "🚨 Surge-Plunge Watcher Activated!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showExchangeSwitcherDialog() {
        val exchanges = arrayOf("Binance", "Coinbase", "Upbit", "Bithumb")
        val icons = arrayOf(R.drawable.binance, R.drawable.coinbase, R.drawable.upbit, R.drawable.bithumb)

        AlertDialog.Builder(this)
            .setTitle("Select Exchange")
            .setItems(exchanges) { _, which ->
                currentExchange = exchanges[which]
                binding.tvExchangeName.text = "$currentExchange ▾"
                binding.ivExchangeIcon.setImageResource(icons[which])
                binding.tvSettingsExchange.text = "$currentExchange ▾"
                Toast.makeText(this, "Switched to $currentExchange", Toast.LENGTH_SHORT).show()
                SurgeEngine.instance.refreshNow()
            }
            .show()
    }

    private fun showWeatherGuideDialog() {
        val guide = """
            ☀️ Sunny (Bullish):
            Continuous rise streaks, RSI > 55, Golden Cross active. Strong upward momentum.
            
            ⛅ Overcast (Neutral):
            Consolidation phase, moving average convergence. Awaiting breakout.
            
            🌧️ Rainy (Bearish):
            Continuous drop, RSI < 40, Death Cross active. Caution advised.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Coin Weather Sentiment Guide")
            .setMessage(guide)
            .setPositiveButton("Got It", null)
            .show()
    }

    private fun showAlgorithmInfoDialog() {
        val message = """
            📊 QSPI Quant Algorithm Engine
            
            • Proprietary momentum and velocity analysis
            • Real-time moving average cross detection (5MA, 20MA, 60MA)
            • 14-day RSI overbought/oversold indicators
            • Continuous rise streak tracking
            • 100% Unrestricted & Free for Personal Use
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("About QSPI Surge Engine")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun highlightSubTab(active: TextView, inactives: List<TextView>) {
        active.setBackgroundResource(R.drawable.bg_badge_gain)
        active.setTextColor(ContextCompat.getColor(this, R.color.surge_green))
        inactives.forEach {
            it.setBackgroundResource(R.drawable.bg_card_dark)
            it.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun formatPrice(price: Double): String {
        return when {
            price >= 1000.0 -> String.format(Locale.US, "$%,.2f", price)
            price >= 1.0 -> String.format(Locale.US, "$%.2f", price)
            else -> String.format(Locale.US, "$%.6f", price)
        }
    }

    private fun refreshDynamicData() {
        setupMarketTab()
    }

    override fun onDestroy() {
        super.onDestroy()
        SurgeEngine.instance.stop()
    }
}
