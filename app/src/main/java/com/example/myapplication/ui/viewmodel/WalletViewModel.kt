package com.example.myapplication.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.WalletItem
import com.example.myapplication.data.repository.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WalletViewModel"

    private val repository = WalletRepository(application)

    private val _walletItems = MutableStateFlow<List<WalletItem>>(emptyList())
    val walletItems: StateFlow<List<WalletItem>> = _walletItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _totalUsdBalance = MutableStateFlow(0.0)
    val totalUsdBalance: StateFlow<Double> = _totalUsdBalance

    init {
        loadWalletData()
    }

    // Improved refresh function with visible refresh animation
    fun refreshWalletData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            try {
                // Add slight delay to ensure refresh animation is visible
                delay(1000)
                
                // Load integrated data directly
                repository.getWalletItems()
                    .catch { e ->
                        Log.e(TAG, "Error refreshing wallet items", e)
                        _error.value = "Refresh failed: ${e.message}"
                    }
                    .collect { items ->
                        Log.d(TAG, "Successfully refreshed ${items.size} wallet items")
                        val filteredItems = items.filter {
                            it.currency.isNotBlank()
                        }
                        _walletItems.value = filteredItems
                        _totalUsdBalance.value = filteredItems.sumOf { it.usdValue }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in refreshWalletData", e)
                _error.value = "Error during refresh: ${e.message}"
            } finally {
                // Delay ending refresh state to ensure animation is visible
                delay(500)
                _isRefreshing.value = false
            }
        }
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // First diagnose if JSON files can be read
                Log.d(TAG, "Testing JSON files availability")
                repository.getCurrencies().collect { currencies ->
                    Log.d(TAG, "Found ${currencies.size} currencies")
                    if (currencies.isEmpty()) {
                        _error.value = "Could not load currencies data. Check assets path."
                    }
                }
                repository.getRates().collect { rates ->
                    Log.d(TAG, "Found ${rates.size} rate tiers")
                    if (rates.isEmpty()) {
                        _error.value = "Could not load rates data. Check assets path."
                    }
                }
                repository.getWalletBalance().collect { balances ->
                    Log.d(TAG, "Found ${balances.size} wallet balances")
                    if (balances.isEmpty()) {
                        _error.value = "Could not load wallet data. Check assets path."
                    }
                }

                // Continue loading integrated data
                repository.getWalletItems()
                    .catch { e ->
                        Log.e(TAG, "Error loading wallet items", e)
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                    .collect { items ->
                        Log.d(TAG, "Loaded ${items.size} wallet items")
                        val filteredItems = items.filter {
                            // Filter out needed currencies only: BTC, ETH, CRO
                            // it.currency == "BTC" || it.currency == "ETH" || it.currency == "CRO"
                            it.currency.isNotBlank()
                        }
                        Log.d(TAG, "Filtered to ${filteredItems.size} wallet items")
                        _walletItems.value = filteredItems
                        _totalUsdBalance.value = filteredItems.sumOf { it.usdValue }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadWalletData", e)
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Format currency amount, preserve original precision
    fun formatCurrencyAmount(amount: Double, symbol: String): String {
        val format = DecimalFormat("#,##0.########")  // Keep up to 8 decimal places
        return "${format.format(amount)} $symbol"
    }
    
    // Format USD value using currency format
    fun formatUsdValue(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }

    // Display original rate in format like $X.XXXXXX/SYMBOL
    fun formatOriginalRate(rateStr: String, symbol: String): String {
        // Use original string to ensure precision is not lost
        return "$$rateStr/$symbol"
    }
}
