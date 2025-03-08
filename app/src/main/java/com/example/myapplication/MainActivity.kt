package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.WalletScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting WalletApp")
        
        // 列出資源目錄中的檔案以確認
        try {
            val assetList = assets.list("json")
            if (assetList != null) {
                Log.d(TAG, "Found ${assetList.size} files in json directory:")
                assetList.forEach { Log.d(TAG, "- $it") }
            } else {
                Log.w(TAG, "No files found in json directory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing assets", e)
        }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WalletViewModel = viewModel()
                    WalletScreen(viewModel = viewModel)
                }
            }
        }
    }
}