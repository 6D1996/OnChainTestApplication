package com.example.myapplication.data.model

// Currency information models
data class CurrencyResponse(
    val currencies: List<Currency>,
    val total: Int,
    val ok: Boolean
)

data class Currency(
    val coin_id: String,
    val name: String,
    val symbol: String,
    val token_decimal: Int,
    val colorful_image_url: String,
    val gray_image_url: String,
    val blockchain_symbol: String,
    val trading_symbol: String,
    val code: String,
    val is_erc20: Boolean,
    val display_decimal: Int
)

// Exchange rate information models
data class RatesResponse(
    val ok: Boolean,
    val warning: String,
    val tiers: List<RateTier>
)

data class RateTier(
    val from_currency: String,
    val to_currency: String,
    val rates: List<Rate>,
    val time_stamp: Long
)

data class Rate(
    val amount: String,
    val rate: String
)

// Wallet balance models
data class WalletResponse(
    val ok: Boolean,
    val warning: String,
    val wallet: List<WalletBalance>
)

data class WalletBalance(
    val currency: String,
    val amount: Double
)

// UI display models
data class WalletItem(
    val currency: String,
    val name: String,
    val symbol: String,
    val amount: Double,
    val usdRate: Double,
    val usdRateStr: String, // Added original rate string to maintain full precision
    val usdValue: Double,
    val imageUrl: String
)
