package com.cryptosoar.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cryptosoar.ai.adapter.CoinAdapter
import com.cryptosoar.ai.databinding.ActivityMainBinding
import com.cryptosoar.ai.engine.SurgeEngine

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CoinAdapter
    private var currentTab = TabMode.SOAR

    enum class TabMode {
        SOAR,
        GAINERS,
        VOLUME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        setupListeners()

        // Start engine
        SurgeEngine.instance.onDataUpdated = { items ->
            runOnUiThread {
                adapter.updateList(items)
            }
        }
        SurgeEngine.instance.start()
    }

    private fun setupRecyclerView() {
        adapter = CoinAdapter(SurgeEngine.instance.coins)
        binding.rvCoins.layoutManager = LinearLayoutManager(this)
        binding.rvCoins.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabSoar.setOnClickListener {
            selectTab(TabMode.SOAR)
        }
        binding.tabGainers.setOnClickListener {
            selectTab(TabMode.GAINERS)
        }
        binding.tabVolume.setOnClickListener {
            selectTab(TabMode.VOLUME)
        }
    }

    private fun selectTab(tab: TabMode) {
        currentTab = tab

        // Reset all tabs to inactive background
        binding.tabSoar.setBackgroundResource(R.drawable.bg_card_dark)
        binding.tabSoar.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

        binding.tabGainers.setBackgroundResource(R.drawable.bg_card_dark)
        binding.tabGainers.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

        binding.tabVolume.setBackgroundResource(R.drawable.bg_card_dark)
        binding.tabVolume.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

        when (tab) {
            TabMode.SOAR -> {
                binding.tabSoar.setBackgroundResource(R.drawable.bg_badge_soar)
                binding.tabSoar.setTextColor(ContextCompat.getColor(this, R.color.white))
                SurgeEngine.instance.sortCoinsBySoar()
            }
            TabMode.GAINERS -> {
                binding.tabGainers.setBackgroundResource(R.drawable.bg_badge_soar)
                binding.tabGainers.setTextColor(ContextCompat.getColor(this, R.color.white))
                SurgeEngine.instance.sortCoinsByGainers()
            }
            TabMode.VOLUME -> {
                binding.tabVolume.setBackgroundResource(R.drawable.bg_badge_soar)
                binding.tabVolume.setTextColor(ContextCompat.getColor(this, R.color.white))
                SurgeEngine.instance.sortCoinsByVolume()
            }
        }
        adapter.updateList(SurgeEngine.instance.coins)
    }

    private fun setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.qspi_purple, R.color.surge_green)
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_card)
        binding.swipeRefresh.setOnRefreshListener {
            SurgeEngine.instance.refreshNow()
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Scanning exchange feeds...", Toast.LENGTH_SHORT).show()
        }

        binding.btnRefresh.setOnClickListener {
            SurgeEngine.instance.refreshNow()
            Toast.makeText(this, "⚡ Live surge scan complete", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SurgeEngine.instance.stop()
    }
}
