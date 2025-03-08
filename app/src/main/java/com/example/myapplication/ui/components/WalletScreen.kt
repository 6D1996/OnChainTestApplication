package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.model.WalletItem
import com.example.myapplication.ui.viewmodel.WalletViewModel
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WalletScreen(viewModel: WalletViewModel) {
    val walletItems by viewModel.walletItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalUsdBalance by viewModel.totalUsdBalance.collectAsState()
    
    // Control bottom sheet visibility
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Control Asset Overview visibility
    var showAssetOverview by remember { mutableStateOf(true) }
    
    // Pull-to-refresh state
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // Create pull-to-refresh state controller - ensure indicator is visible and works properly
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { 
            viewModel.refreshWalletData() 
        }
    )
    
    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    // Get screen height to calculate maximum bottom sheet height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Scroll state for content scrolling
    val scrollState = rememberScrollState()

    // Use Scaffold to handle system insets
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        // Use Box + pullRefresh modifier to add pull-to-refresh functionality
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state = pullRefreshState)
        ) {
            // Main content area - using scrolling list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState) // Make the entire content scrollable for pull-to-refresh
                    .padding(horizontal = 16.dp)
            ) {
                // Title
                Text(
                    text = "Crypto.com Wallet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Total assets display card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Balance",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            // Expand/collapse icon - click to control Asset Overview visibility
                            Icon(
                                imageVector = if (showAssetOverview) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showAssetOverview) "Hide Details" else "Show Details",
                                modifier = Modifier.clickable { showAssetOverview = !showAssetOverview },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Total amount - click to open bottom sheet
                        Text(
                            text = viewModel.formatUsdValue(totalUsdBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showBottomSheet = true }
                        )
                        
                        Text(
                            text = "Tap to see all assets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Show error message
                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Show loading indicator
                if (isLoading && !isRefreshing) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Asset overview area - controlled by showAssetOverview
                if (showAssetOverview) {
                    // Display asset overview
                    AssetOverviewSection(
                        walletItems = walletItems,
                        viewModel = viewModel,
                        onItemClick = { showBottomSheet = true }
                    )
                }
                
                // Add bottom space to ensure content can be fully scrolled
                Spacer(modifier = Modifier.height(30.dp))
            }
            
            // Pull-to-refresh indicator - placed at the top layer to ensure visibility
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding()), // Ensure not covered by status bar
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                scale = true // Make the indicator more noticeable
            )
        }
    }
    
    // Bottom sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDragHandle() },
            // Set maximum height of bottom sheet to 2/3 of screen height
            windowInsets = WindowInsets.ime
        ) {
            // Use Box to set maximum height, ensuring content doesn't exceed 2/3 of screen height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 2/3)
            ) {
                WalletDetailBottomSheet(
                    walletItems = walletItems,
                    viewModel = viewModel,
                    totalUsdBalance = totalUsdBalance
                )
            }
        }
    }
}

@Composable
fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun AssetOverviewSection(
    walletItems: List<WalletItem>,
    viewModel: WalletViewModel,
    onItemClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Main asset overview title
        Text(
            text = "Asset Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display first 2 assets as overview
        walletItems.take(2).forEach { item ->
            SummaryCryptoItem(
                item = item,
                viewModel = viewModel,
                onItemClick = onItemClick
            )
        }
        
        // If there are more assets, show "View More" button
        if (walletItems.size > 2) {
            Button(
                onClick = onItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("View All Assets (${walletItems.size})")
            }
        }
    }
}

@Composable
fun SummaryCryptoItem(
    item: WalletItem, 
    viewModel: WalletViewModel,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use colored icon
            CryptoCurrencyIcon(
                imageUrl = item.imageUrl,
                symbol = item.symbol,
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.formatCurrencyAmount(item.amount, item.symbol),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.formatUsdValue(item.usdValue),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                // Format using original rate string
                Text(
                    text = viewModel.formatOriginalRate(item.usdRateStr, item.symbol),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun WalletDetailBottomSheet(
    walletItems: List<WalletItem>,
    viewModel: WalletViewModel,
    totalUsdBalance: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Fixed top area: title and total amount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Bottom sheet title and total amount
            Text(
                text = "Your Assets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Total: ${viewModel.formatUsdValue(totalUsdBalance)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // Asset list - using LazyColumn to ensure scrollability
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(walletItems) { item ->
                CryptoWalletItem(item = item, viewModel = viewModel)
                Spacer(modifier = Modifier.height(4.dp))
            }
            // Add bottom padding to ensure the last item isn't covered by system navigation bar
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CryptoWalletItem(item: WalletItem, viewModel: WalletViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use colored icon
            CryptoCurrencyIcon(
                imageUrl = item.imageUrl,
                symbol = item.symbol,
                size = 48.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.formatCurrencyAmount(item.amount, item.symbol),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.formatUsdValue(item.usdValue),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                // Format using original rate string
                Text(
                    text = viewModel.formatOriginalRate(item.usdRateStr, item.symbol),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CryptoCurrencyIcon(
    imageUrl: String,
    symbol: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "$symbol icon",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        loading = {
            // Show placeholder while loading
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(getColorForCurrency(symbol)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value / 2.5).sp
                )
            }
        },
        error = {
            // Show fallback icon when loading fails
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(getColorForCurrency(symbol)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value / 2.5).sp
                )
            }
        }
    )
}

// Simple color selection based on currency (as fallback)
fun getColorForCurrency(currency: String): Color {
    return when (currency) {
        "BTC" -> Color(0xFFFF9900)
        "ETH" -> Color(0xFF627EEA)
        "CRO" -> Color(0xFF0D3694)
        "USDT" -> Color(0xFF26A17B)
        "DAI" -> Color(0xFFF5AC37)
        else -> Color.Gray
    }
}