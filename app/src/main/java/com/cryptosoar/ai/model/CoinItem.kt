package com.cryptosoar.ai.model

data class CoinItem(
    var rank: Int,
    val symbol: String,
    val name: String,
    var price: Double,
    var change24h: Double,
    var soarPercent: Double,
    var targetPeriod: String,
    var signalDetail: String,
    var volume24h: Double
)
