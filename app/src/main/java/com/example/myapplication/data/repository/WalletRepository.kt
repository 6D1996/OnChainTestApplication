package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class WalletRepository(private val context: Context) {
    private val TAG = "WalletRepository"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Get all supported currencies
    fun getCurrencies(): Flow<List<Currency>> = flow {
        try {
            val jsonString = loadJsonFromAsset("json/currencies.json")
            Log.d(TAG, "Currencies JSON loaded: ${jsonString.take(100)}...")
            val adapter = moshi.adapter(CurrencyResponse::class.java)
            val currencyResponse = adapter.fromJson(jsonString)
            
            currencyResponse?.let {
                Log.d(TAG, "Parsed ${it.currencies.size} currencies")
                emit(it.currencies)
            } ?: emit(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading currencies", e)
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Get all exchange rate information
    fun getRates(): Flow<List<RateTier>> = flow {
        try {
            val jsonString = loadJsonFromAsset("json/live-rates.json")
            Log.d(TAG, "Rates JSON loaded: ${jsonString.take(100)}...")
            val adapter = moshi.adapter(RatesResponse::class.java)
            val ratesResponse = adapter.fromJson(jsonString)
            
            ratesResponse?.let {
                Log.d(TAG, "Parsed ${it.tiers.size} rate tiers")
                emit(it.tiers)
            } ?: emit(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rates", e)
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Get wallet balances
    fun getWalletBalance(): Flow<List<WalletBalance>> = flow {
        try {
            val jsonString = loadJsonFromAsset("json/wallet-balance.json")
            Log.d(TAG, "Wallet JSON loaded: ${jsonString.take(100)}...")
            val adapter = moshi.adapter(WalletResponse::class.java)
            val walletResponse = adapter.fromJson(jsonString)
            
            walletResponse?.let {
                Log.d(TAG, "Parsed ${it.wallet.size} wallet balances")
                emit(it.wallet)
            } ?: emit(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading wallet balances", e)
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Integrate all information, generate wallet item list
    fun getWalletItems(): Flow<List<WalletItem>> = flow {
        val currencies = getCurrenciesList()
        val rates = getRatesList()
        val balances = getWalletBalanceList()

        val walletItems = mutableListOf<WalletItem>()
        
        balances.forEach { balance ->
            val currency = currencies.find { it.code == balance.currency }
            val rateTier = rates.find { it.from_currency == balance.currency && it.to_currency == "USD" }
            
            if (currency != null && rateTier != null && rateTier.rates.isNotEmpty()) {
                // Save original rate string to avoid precision loss
                val rateStr = rateTier.rates[0].rate 
                val rate = rateStr.toDoubleOrNull() ?: 0.0
                val usdValue = balance.amount * rate
                
                walletItems.add(
                    WalletItem(
                        currency = balance.currency,
                        name = currency.name,
                        symbol = currency.symbol,
                        amount = balance.amount,
                        usdRate = rate,
                        usdRateStr = rateStr, // Save original rate string
                        usdValue = usdValue,
                        imageUrl = currency.colorful_image_url
                    )
                )
            }
        }
        
        emit(walletItems)
    }.flowOn(Dispatchers.IO)

    // Helper function: load JSON file from assets
    private suspend fun loadJsonFromAsset(fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading asset from: $fileName")
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading asset: $fileName", e)
                ""
            }
        }
    }

    // Helper function: get currency list
    private suspend fun getCurrenciesList(): List<Currency> {
        var result = listOf<Currency>()
        try {
            val jsonString = loadJsonFromAsset("json/currencies.json")
            val adapter = moshi.adapter(CurrencyResponse::class.java)
            adapter.fromJson(jsonString)?.let {
                result = it.currencies
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // Helper function: get rates list
    private suspend fun getRatesList(): List<RateTier> {
        var result = listOf<RateTier>()
        try {
            val jsonString = loadJsonFromAsset("json/live-rates.json")
            val adapter = moshi.adapter(RatesResponse::class.java)
            adapter.fromJson(jsonString)?.let {
                result = it.tiers
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // Helper function: get wallet balance list
    private suspend fun getWalletBalanceList(): List<WalletBalance> {
        var result = listOf<WalletBalance>()
        try {
            val jsonString = loadJsonFromAsset("json/wallet-balance.json")
            val adapter = moshi.adapter(WalletResponse::class.java)
            adapter.fromJson(jsonString)?.let {
                result = it.wallet
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
