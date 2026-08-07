package com.example.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailorAppMain(
    viewModel: TailorViewModel,
    onSharePdf: (File) -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val generatedPdf by viewModel.generatedPdfFile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Automatically navigate to Activation screen if trial expired and app is locked
    LaunchedEffect(isAppUnlocked) {
        if (!isAppUnlocked && currentScreen != TailorViewModel.Screen.ACTIVATION) {
            viewModel.navigateTo(TailorViewModel.Screen.ACTIVATION)
        }
    }

    // Trigger toast notification
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    // Trigger native PDF sharing
    LaunchedEffect(generatedPdf) {
        generatedPdf?.let {
            onSharePdf(it)
            viewModel.clearPdfFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tailor Book",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (currentScreen != TailorViewModel.Screen.DASHBOARD &&
                        currentScreen != TailorViewModel.Screen.CUSTOMERS &&
                        currentScreen != TailorViewModel.Screen.ORDERS
                    ) {
                        IconButton(onClick = {
                            when (currentScreen) {
                                TailorViewModel.Screen.CUSTOMER_DETAIL -> viewModel.navigateTo(TailorViewModel.Screen.CUSTOMERS)
                                TailorViewModel.Screen.ADD_EDIT_CUSTOMER -> {
                                    viewModel.customerBeingEdited.value = null
                                    viewModel.navigateTo(TailorViewModel.Screen.CUSTOMERS)
                                }
                                TailorViewModel.Screen.ADD_EDIT_ORDER -> {
                                    viewModel.orderBeingEdited.value = null
                                    viewModel.navigateTo(TailorViewModel.Screen.ORDERS)
                                }
                                TailorViewModel.Screen.MEASUREMENTS -> viewModel.navigateTo(TailorViewModel.Screen.CUSTOMER_DETAIL)
                                else -> viewModel.navigateTo(if (isAppUnlocked) TailorViewModel.Screen.DASHBOARD else TailorViewModel.Screen.ACTIVATION)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(TailorViewModel.Screen.SETTINGS_BACKUP) }) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Backup Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.navigateTo(TailorViewModel.Screen.ACTIVATION) }) {
                        Icon(
                            imageVector = if (isActivated) Icons.Default.Verified else Icons.Default.VpnKey,
                            contentDescription = "Activation",
                            tint = if (isActivated) Color(0xFF00A859) else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        bottomBar = {
            // Standard Navigation Bar showing for core panels if app is unlocked
            if (isAppUnlocked && (
                currentScreen == TailorViewModel.Screen.DASHBOARD ||
                currentScreen == TailorViewModel.Screen.CUSTOMERS ||
                currentScreen == TailorViewModel.Screen.ORDERS
            )) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == TailorViewModel.Screen.DASHBOARD,
                        onClick = { viewModel.navigateTo(TailorViewModel.Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == TailorViewModel.Screen.CUSTOMERS,
                        onClick = { viewModel.navigateTo(TailorViewModel.Screen.CUSTOMERS) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Customers Register") },
                        label = { Text("Customers") },
                        modifier = Modifier.testTag("nav_customers")
                    )
                    NavigationBarItem(
                        selected = currentScreen == TailorViewModel.Screen.ORDERS,
                        onClick = { viewModel.navigateTo(TailorViewModel.Screen.ORDERS) },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Order Ledger") },
                        label = { Text("Orders") },
                        modifier = Modifier.testTag("nav_orders")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    TailorViewModel.Screen.DASHBOARD -> DashboardScreen(viewModel)
                    TailorViewModel.Screen.CUSTOMERS -> CustomersScreen(viewModel)
                    TailorViewModel.Screen.ORDERS -> OrdersScreen(viewModel)
                    TailorViewModel.Screen.CUSTOMER_DETAIL -> CustomerDetailScreen(viewModel)
                    TailorViewModel.Screen.ADD_EDIT_CUSTOMER -> AddEditCustomerScreen(viewModel)
                    TailorViewModel.Screen.ADD_EDIT_ORDER -> AddEditOrderScreen(viewModel)
                    TailorViewModel.Screen.MEASUREMENTS -> EditMeasurementsScreen(viewModel)
                    TailorViewModel.Screen.ACTIVATION -> ActivationScreen(viewModel)
                    TailorViewModel.Screen.SETTINGS_BACKUP -> SettingsBackupScreen(viewModel)
                }
            }
        }
    }
}

// 1. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: TailorViewModel) {
    val customerCount by viewModel.customerCount.collectAsStateWithLifecycle()
    val orderCount by viewModel.orderCount.collectAsStateWithLifecycle()
    val activeOrderCount by viewModel.activeOrderCount.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val totalCollected by viewModel.totalCollected.collectAsStateWithLifecycle()
    val totalOutstanding by viewModel.totalOutstanding.collectAsStateWithLifecycle()
    val stitchedOrderCount by viewModel.stitchedOrderCount.collectAsStateWithLifecycle()
    val pendingOrderCount by viewModel.pendingOrderCount.collectAsStateWithLifecycle()
    val inProgressOrderCount by viewModel.inProgressOrderCount.collectAsStateWithLifecycle()
    val deliveredOrderCount by viewModel.deliveredOrderCount.collectAsStateWithLifecycle()
    val orders by viewModel.ordersWithCustomer.collectAsStateWithLifecycle()
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
    val remainingTrialDays by viewModel.remainingTrialDays.collectAsStateWithLifecycle()
    var dashboardPaymentDialogOrder by remember { mutableStateOf<OrderWithCustomer?>(null) }

    val pressingOrders = remember(orders) {
        orders.filter { it.status != "DELIVERED" }
            .sortedBy { it.dueDate }
            .take(5)
    }

    if (dashboardPaymentDialogOrder != null) {
        val targetOrd = dashboardPaymentDialogOrder!!
        RecordPaymentDialog(
            order = targetOrd,
            onDismiss = { dashboardPaymentDialogOrder = null },
            onRecordPayment = { addAmt -> viewModel.recordPayment(targetOrd.id, addAmt) },
            onSetTotalPaid = { totAmt -> viewModel.updatePaidAmount(targetOrd.id, totAmt) }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isActivated) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(TailorViewModel.Screen.ACTIVATION) },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "1-Week Free Trial Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "$remainingTrialDays Days Remaining • Tap to Activate",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.navigateTo(TailorViewModel.Screen.ACTIVATION) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryContainer)
                        ) {
                            Text("Activate", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF1B807B) // Modern deep emerald transition
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Assalamu Alaikum,", // Arabic/Pakistani warm greeting
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFF2D1), // Luxury silk gold stitch accent color
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tailor Master Station",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Premium Stitching Ledger & Silhouette Vault",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Ledger Summary Row
        item {
            Text(
                text = "Ledger Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.2.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Due Balance",
                    value = "Rs. ${totalOutstanding.toInt()}",
                    icon = Icons.Default.MonetizationOn,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Revenue Cash",
                    value = "Rs. ${totalCollected.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Jobs",
                    value = "$activeOrderCount",
                    icon = Icons.Default.Loop,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Registered Clients",
                    value = "$customerCount",
                    icon = Icons.Default.Groups,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stitching Analytics & Suits Completion Section
        item {
            Text(
                text = "Stitching Analytics (سلائی اینالیٹکس)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.2.sp
            )
        }

        item {
            val progressRatio = if (orderCount > 0) stitchedOrderCount.toFloat() / orderCount.toFloat() else 0f
            val percentInt = (progressRatio * 100).toInt()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Suits Stitched",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "مکمل سلائی شدہ سوٹ کی تفصیلات",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$stitchedOrderCount / $orderCount Suits",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stitching Completion Rate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percentInt% Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // 4 Grid Status Analytics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stitched / Complete
                        MiniAnalyticsCard(
                            label = "Stitched",
                            urduLabel = "مکمل",
                            count = "$stitchedOrderCount",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        // Under Stitching
                        MiniAnalyticsCard(
                            label = "In Progress",
                            urduLabel = "زیرِ سلائی",
                            count = "$inProgressOrderCount",
                            icon = Icons.Default.ContentCut,
                            color = Color(0xFF0288D1),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pending
                        MiniAnalyticsCard(
                            label = "Pending",
                            urduLabel = "پینڈنگ",
                            count = "$pendingOrderCount",
                            icon = Icons.Default.HourglassEmpty,
                            color = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                        // Delivered
                        MiniAnalyticsCard(
                            label = "Delivered",
                            urduLabel = "ڈیلیور",
                            count = "$deliveredOrderCount",
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF6A1B9A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Backup & Restore Shortcut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(TailorViewModel.Screen.SETTINGS_BACKUP) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Settings & Data Backup (بیک اپ ڈیٹا)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Save or Load backup files to protect customer records",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Deadline alerts section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Deadlines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "See All",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(TailorViewModel.Screen.ORDERS) }
                )
            }
        }

        if (pressingOrders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "All current orders are fully delivered.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(pressingOrders) { order ->
                OrderCard(
                    order = order,
                    onClick = {
                        viewModel.selectOrder(order)
                        viewModel.navigateTo(TailorViewModel.Screen.ORDERS)
                    },
                    onMarkStatus = { viewModel.updateOrderStatus(order, it) },
                    onDownloadPdf = { },
                    showPdfAction = false,
                    onOpenPaymentDialog = { dashboardPaymentDialogOrder = order }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MiniAnalyticsCard(
    label: String,
    urduLabel: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = count,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = color
                )
                Text(
                    text = "$label ($urduLabel)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// 2. CUSTOMERS SCREEN
@Composable
fun CustomersScreen(viewModel: TailorViewModel) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.customerBeingEdited.value = null
                    viewModel.navigateTo(TailorViewModel.Screen.ADD_EDIT_CUSTOMER)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer", modifier = Modifier.size(28.dp))
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Customers Register",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.1.sp
            )

            // Optimized Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.customerSearchQuery.value = it },
                placeholder = { Text("Search by name or phone/WhatsApp...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.customerSearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No customer profiles found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap the plus button below to register a new client and save their master tailoring size specs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(customers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectCustomer(customer) }
                                .testTag("customer_card_${customer.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Circular Gender Avatar representation
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (customer.gender == "Male") MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.tertiaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = if (customer.gender == "Male") MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = customer.phone,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// 3. ORDERS SCREEN
@Composable
fun OrdersScreen(viewModel: TailorViewModel) {
    val orders by viewModel.ordersWithCustomer.collectAsStateWithLifecycle()
    val searchQuery by viewModel.orderSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.orderStatusFilter.collectAsStateWithLifecycle()
    val selectedOrdState by viewModel.selectedOrder.collectAsStateWithLifecycle()
    var paymentDialogOrder by remember { mutableStateOf<OrderWithCustomer?>(null) }
    val context = LocalContext.current

    val statuses = listOf("ALL", "PENDING", "IN_PROGRESS", "COMPLETED", "DELIVERED")

    if (paymentDialogOrder != null) {
        val targetOrd = paymentDialogOrder!!
        RecordPaymentDialog(
            order = targetOrd,
            onDismiss = { paymentDialogOrder = null },
            onRecordPayment = { additionalAmount ->
                viewModel.recordPayment(targetOrd.id, additionalAmount)
            },
            onSetTotalPaid = { newPaidTotal ->
                viewModel.updatePaidAmount(targetOrd.id, newPaidTotal)
            }
        )
    }

    // Order detail view dialog
    if (selectedOrdState != null) {
        val o = selectedOrdState!!
        val customerState by viewModel.selectedCustomer.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { viewModel.selectedOrder.value = null },
            confirmButton = {
                Button(
                    onClick = { viewModel.selectedOrder.value = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.generateOrderReceiptPdf(context, o.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Share Receipt")
                    }
                }
            },
            title = {
                Text(
                    text = "Receipt Details #OR-${o.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Client: ${customerState?.name ?: "Unknown"}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Phone: ${customerState?.phone ?: "N/A"}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Stitching Blueprints:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Upper (Kameez / Kurta):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Length: ${o.shirtLength}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Shoulder: ${o.shoulder}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Sleeve: ${o.sleeves}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Chest: ${o.chest}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Waist: ${o.waist}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Hip: ${o.hip}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Collar: ${o.collar}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Armhole: ${o.armhole}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Sleeve Mori: ${o.sleeveMori}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Lower (Shalwar/Trouser):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Length: ${o.trouserLength}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Bottom Mori: ${o.trouserBottom}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Aasan Thigh: ${o.trouserAsan}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (o.orderNotes.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Design Specs: ${o.orderNotes}",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stitching Amount:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Rs. ${o.totalAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid Advance:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Rs. ${o.paidAmount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        val due = o.totalAmount - o.paidAmount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Rs. ${due.toInt()}", color = if (due > 0) Color.Red else Color(0xFF12726E), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Stitching Orders Ledger",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.1.sp
        )

        // Optimized Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.orderSearchQuery.value = it },
            placeholder = { Text("Search by name, phone, suit or Tracking Tag (TRK-XXXX)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.orderSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("orders_search"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Modern flowing filter chips instead of heavy TabRow
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statuses) { statusName ->
                val isSelected = statusFilter == statusName
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.orderStatusFilter.value = statusName },
                    label = { Text(statusName.replace("_", " ")) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("status_tab_$statusName")
                )
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No order files found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "There are no stitching jobs recorded in this filter state.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        onClick = { viewModel.selectOrder(order) },
                        onMarkStatus = { viewModel.updateOrderStatus(order, it) },
                        onDownloadPdf = { viewModel.generateOrderReceiptPdf(context, order.id) },
                        showPdfAction = true,
                        onOpenPaymentDialog = { paymentDialogOrder = order }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderWithCustomer,
    onClick: () -> Unit,
    onMarkStatus: (String) -> Unit,
    onDownloadPdf: () -> Unit,
    showPdfAction: Boolean,
    onOpenPaymentDialog: (() -> Unit)? = null
) {
    val formatter = remember { SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()) }
    val isDueSoon = remember(order.dueDate) {
        order.dueDate - System.currentTimeMillis() < (48 * 60 * 60 * 1000) && order.status != "DELIVERED"
    }
    val trackingTag = remember(order.trackingId, order.id) {
        if (order.trackingId.isNotBlank()) order.trackingId else "TRK-${order.id}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDueSoon) Color(0xFFFFF6F6) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isDueSoon) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = order.itemType,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        // Physical Cloth Tag Badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🏷️ $trackingTag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Custom Urdu colored statuses label
                BadgeStatus(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Booked Details",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatter.format(Date(order.orderDate)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Delivery Deadline",
                        fontSize = 10.sp,
                        color = if (isDueSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatter.format(Date(order.dueDate)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDueSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium Billing & Status Actions Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val outstanding = order.totalAmount - order.paidAmount
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Stitching Charge", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text("Rs. ${order.totalAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("Remaining Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (outstanding > 0) "Rs. ${outstanding.toInt()}" else "Fully Paid",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (outstanding > 0) Color(0xFFCC2222) else Color(0xFF12726E)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Collect Payment action button if there is due balance
                    if (outstanding > 0 && onOpenPaymentDialog != null) {
                        TextButton(
                            onClick = { onOpenPaymentDialog() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("collect_payment_${order.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Collect Balance",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("وصول کریں", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (showPdfAction) {
                        IconButton(
                            onClick = { onDownloadPdf() },
                            modifier = Modifier.testTag("receipt_share_${order.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share PDF Receipt",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Cycle Status Menu button
                    IconButton(
                        onClick = {
                            val nextStatus = when (order.status) {
                                "PENDING" -> "IN_PROGRESS"
                                "IN_PROGRESS" -> "COMPLETED"
                                "COMPLETED" -> "DELIVERED"
                                else -> "PENDING"
                            }
                            onMarkStatus(nextStatus)
                        },
                        modifier = Modifier.testTag("cycle_status_${order.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cached,
                            contentDescription = "Cycle Status",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    order: OrderWithCustomer,
    onDismiss: () -> Unit,
    onRecordPayment: (Double) -> Unit,
    onSetTotalPaid: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var isCustomTotal by remember { mutableStateOf(false) }
    var customTotalInput by remember { mutableStateOf(order.paidAmount.toInt().toString()) }
    val remainingDue = order.totalAmount - order.paidAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (isCustomTotal) {
                        val newPaid = customTotalInput.toDoubleOrNull() ?: order.paidAmount
                        onSetTotalPaid(newPaid)
                    } else {
                        val addAmt = amountInput.toDoubleOrNull() ?: 0.0
                        if (addAmt > 0) {
                            onRecordPayment(addAmt)
                        }
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isCustomTotal) "پیمنٹ اپڈیٹ کریں" else "پیسے جمع کریں (Record)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("منسوخ (Cancel)")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "وصولی / Payment - ${order.customerName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("کل سلائی کا بل (Total):", fontSize = 12.sp)
                            Text("Rs. ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("پہلے وصول شدہ (Already Paid):", fontSize = 12.sp)
                            Text("Rs. ${order.paidAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("باقایا رقم (Remaining Balance):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (remainingDue > 0) "Rs. ${remainingDue.toInt()}" else "0 (کوئی بقایا نہیں)",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (remainingDue > 0) Color.Red else Color(0xFF2E7D32),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isCustomTotal,
                        onClick = { isCustomTotal = false },
                        label = { Text("بقایا وصولی (Add)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isCustomTotal,
                        onClick = { isCustomTotal = true },
                        label = { Text("ٹوٹل ایڈٹ کریں (Set)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isCustomTotal) {
                    Text("مزید وصول کی گئی رقم لکھیں:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("وصول شدہ رقم (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    // Quick payment chips
                    if (remainingDue > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val fullAmt = remainingDue.toInt()
                            AssistChip(
                                onClick = { amountInput = fullAmt.toString() },
                                label = { Text("پورا بقایا Rs. $fullAmt", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                            if (fullAmt >= 1000) {
                                AssistChip(
                                    onClick = { amountInput = "1000" },
                                    label = { Text("Rs. 1000", fontSize = 10.sp) }
                                )
                            }
                            if (fullAmt >= 500) {
                                AssistChip(
                                    onClick = { amountInput = "500" },
                                    label = { Text("Rs. 500", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                } else {
                    Text("کل ادا شدہ رقم کو براہ راست تبدیل کریں:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = customTotalInput,
                        onValueChange = { customTotalInput = it },
                        label = { Text("ٹوٹل ادا شدہ (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_total_paid_input")
                    )
                }
            }
        }
    )
}

@Composable
fun BadgeStatus(status: String) {
    val containerColor = when (status) {
        "PENDING" -> Color(0xFFFFF8E1) // Soft rich cream amber
        "IN_PROGRESS" -> Color(0xFFECEFF1) // High contrast clean ash-gray
        "COMPLETED" -> Color(0xFFE8F5E9) // Soft satin emerald
        "DELIVERED" -> Color(0xFFE1F5FE) // Premium sky blue
        else -> Color(0xFFF5F5F5)
    }

    val textColor = when (status) {
        "PENDING" -> Color(0xFFB78103)
        "IN_PROGRESS" -> Color(0xFF455A64)
        "COMPLETED" -> Color(0xFF2E7D32)
        "DELIVERED" -> Color(0xFF0277BD)
        else -> Color(0xFF616161)
    }

    val dotColor = when (status) {
        "PENDING" -> Color(0xFFFFB300)
        "IN_PROGRESS" -> Color(0xFF78909C)
        "COMPLETED" -> Color(0xFF4CAF50)
        "DELIVERED" -> Color(0xFF03A9F4)
        else -> Color(0xFF9E9E9E)
    }

    val urduStatus = when (status) {
        "PENDING" -> "باقی - PENDING"
        "IN_PROGRESS" -> "سلائی جاری - IN PROGRESS"
        "COMPLETED" -> "تیار - COMPLETED"
        "DELIVERED" -> "ڈیلیورڈ - DELIVERED"
        else -> status
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(dotColor)
            )
            Text(
                text = urduStatus,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}


// 4. CUSTOMER PROFILE DETAILS SCREEN
@Composable
fun CustomerDetailScreen(viewModel: TailorViewModel) {
    val customer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val measurement by viewModel.selectedCustomerMeasurements.collectAsStateWithLifecycle()
    val orders by viewModel.ordersWithCustomer.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val customerOrders = remember(customer, orders) {
        orders.filter { it.customerId == customer?.id }
    }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Client folder could not load.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer Basic Dossier Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer!!.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = customer!!.phone,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Text(
                                    text = customer!!.address.ifBlank { "No address registered" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.customerBeingEdited.value = customer
                                viewModel.navigateTo(TailorViewModel.Screen.ADD_EDIT_CUSTOMER)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("edit_customer_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit dossier info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.orderBeingEdited.value = null
                                viewModel.navigateTo(TailorViewModel.Screen.ADD_EDIT_ORDER)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("place_order_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Book Suit Stitch", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.generateCustomerSummaryReportPdf(context, customer!!.id)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("customer_pdf_report")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("PDF Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Master Measurements Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Silhouette Master Dimensions (لمبائی / سائز)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = { viewModel.navigateTo(TailorViewModel.Screen.MEASUREMENTS) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("edit_measurements_button")
                        ) {
                            Icon(Icons.Default.Straighten, contentDescription = "Edit measurements", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    if (measurement == null || (measurement!!.shirtLength == 0.0 && measurement!!.trouserLength == 0.0)) {
                        Text(
                            text = "No dimensions stored in this blueprint yet. Tap Ruler button above to fill Pakistani design stitching measurements.",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    } else {
                        val m = measurement!!
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Kameez / Kurta (لمبائی قمیض):",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MeasurementLabelValue("Length (لمبائی)", "${m.shirtLength}\"")
                                    MeasurementLabelValue("Shoulder (تیرا)", "${m.shoulder}\"")
                                    MeasurementLabelValue("Sleeves (آستین)", "${m.sleeves}\"")
                                    MeasurementLabelValue("Chest (سینہ)", "${m.chest}\"")
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MeasurementLabelValue("Waist (کمر)", "${m.waist}\"")
                                    MeasurementLabelValue("Hip / Ghera (گھیرا)", "${m.hip}\"")
                                    MeasurementLabelValue("Collar (Neck/ہالہ)", "${m.collar}\"")
                                    MeasurementLabelValue("Armhole (کندھا)", "${m.armhole}\"")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Shalwar / Trouser (شلوار لمبائی):",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MeasurementLabelValue("Length (لمبائی)", "${m.trouserLength}\"")
                                    MeasurementLabelValue("Bottom Mori (قند)", "${m.trouserBottom}\"")
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MeasurementLabelValue("Aasan Thigh (آسن)", "${m.trouserAsan}\"")
                                    MeasurementLabelValue("Sleeve Mori (موری)", "${m.sleeveMori}\"")
                                }
                            }

                            if (m.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "Designing Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = m.notes,
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Job order list history header
        item {
            Text(
                text = "Stitching Ledger History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.2.sp
            )
        }

        if (customerOrders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No individual stitched jobs recorded for this client yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(customerOrders) { order ->
                OrderCard(order = order, onClick = {
                    viewModel.selectOrder(order)
                }, onMarkStatus = {
                    viewModel.updateOrderStatus(order, it)
                }, onDownloadPdf = {
                    viewModel.generateOrderReceiptPdf(context, order.id)
                }, showPdfAction = true)
            }
        }
    }
}

@Composable
fun MeasurementLabelValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF4F8F8), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value.ifBlank { "0.0\"" }, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End
        )
    }
}


// 5. ADD / EDIT CUSTOMER BIO SCREEN
@Composable
fun AddEditCustomerScreen(viewModel: TailorViewModel) {
    val existing = viewModel.customerBeingEdited.value
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var gender by remember { mutableStateOf(existing?.gender ?: "Male") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (existing != null) "Update Client Dossier" else "Create New Client Folder",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.1.sp
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer full name (گاہک کا نام)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number / واٹس ایپ نمبر") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_phone_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Shop Home Address (پتہ)") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_address_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Gender toggling row
        Text("Gender / صنف", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("Male", "Female").forEach { option ->
                val isSelected = gender == option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { gender = option }
                        .testTag("gender_option_$option"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp), contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (option == "Male") Icons.Default.Male else Icons.Default.Female,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (option == "Male") "Male (مرد)" else "Female (عورت)",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveCustomer(name, phone, address, gender)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_customer_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Text(text = if (existing != null) "Apply Updated Info" else "Create Customer profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        if (existing != null) {
            OutlinedButton(
                onClick = {
                    viewModel.deleteCustomer(existing)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_customer_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Delete Customer dossier permanently", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// 6. EDIT MASTER SILHOUETTE MEASUREMENTS SCREEN
@Composable
fun EditMeasurementsScreen(viewModel: TailorViewModel) {
    val measurements = viewModel.selectedCustomerMeasurements.collectAsStateWithLifecycle().value
    val customer = viewModel.selectedCustomer.collectAsStateWithLifecycle().value

    if (customer == null || measurements == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading measurements configuration")
        }
        return
    }

    var mShirtLength by remember { mutableStateOf(measurements.shirtLength.toString().replace("0.0", "")) }
    var mShoulder by remember { mutableStateOf(measurements.shoulder.toString().replace("0.0", "")) }
    var mSleeves by remember { mutableStateOf(measurements.sleeves.toString().replace("0.0", "")) }
    var mChest by remember { mutableStateOf(measurements.chest.toString().replace("0.0", "")) }
    var mWaist by remember { mutableStateOf(measurements.waist.toString().replace("0.0", "")) }
    var mHip by remember { mutableStateOf(measurements.hip.toString().replace("0.0", "")) }
    var mCollar by remember { mutableStateOf(measurements.collar.toString().replace("0.0", "")) }
    var mArmhole by remember { mutableStateOf(measurements.armhole.toString().replace("0.0", "")) }
    var mSleeveMori by remember { mutableStateOf(measurements.sleeveMori.toString().replace("0.0", "")) }
    var mTrouserLength by remember { mutableStateOf(measurements.trouserLength.toString().replace("0.0", "")) }
    var mTrouserBottom by remember { mutableStateOf(measurements.trouserBottom.toString().replace("0.0", "")) }
    var mTrouserAsan by remember { mutableStateOf(measurements.trouserAsan.toString().replace("0.0", "")) }
    var mNotes by remember { mutableStateOf(measurements.notes) }
    var galaType by remember { mutableStateOf(measurements.galaType) }
    var collarSize by remember { mutableStateOf(measurements.collarSize) }
    var sleeveDesign by remember { mutableStateOf(measurements.sleeveDesign) }
    var frontPatti by remember { mutableStateOf(measurements.frontPatti) }
    var frontPocket by remember { mutableStateOf(measurements.frontPocket) }
    var sidePocket by remember { mutableStateOf(measurements.sidePocket) }
    var daman by remember { mutableStateOf(measurements.daman) }
    var shalwarWidth by remember { mutableStateOf(measurements.shalwarWidth) }
    var shalwarPocket by remember { mutableStateOf(measurements.shalwarPocket) }
    var bukramQuality by remember { mutableStateOf(measurements.bukramQuality) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Blueprint: ${customer.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.1.sp
        )

        Text(
            text = "Stitching master size chart. Standard measurements are in Inches (\")",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

        // 1. Pakistani Design Size Presets
        Text(
            text = "Pakistani Size Presets (پاکستانی سائز منتخب کریں):",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Small (S)", "Medium (M)", "Large (L)").forEach { size ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            when (size) {
                                "Small (S)" -> {
                                    mShirtLength = "38.0"
                                    mShoulder = "17.0"
                                    mSleeves = "23.0"
                                    mCollar = "14.5"
                                    mChest = "20.0"
                                    mWaist = "19.0"
                                    mHip = "21.0"
                                    mArmhole = "8.5"
                                    mSleeveMori = "5.5"
                                    mTrouserLength = "38.0"
                                    mTrouserBottom = "7.5"
                                    mTrouserAsan = "14.0"
                                }
                                "Medium (M)" -> {
                                    mShirtLength = "40.0"
                                    mShoulder = "18.0"
                                    mSleeves = "24.0"
                                    mCollar = "15.5"
                                    mChest = "22.0"
                                    mWaist = "21.0"
                                    mHip = "23.0"
                                    mArmhole = "9.5"
                                    mSleeveMori = "6.0"
                                    mTrouserLength = "40.0"
                                    mTrouserBottom = "8.0"
                                    mTrouserAsan = "15.0"
                                }
                                "Large (L)" -> {
                                    mShirtLength = "42.0"
                                    mShoulder = "19.5"
                                    mSleeves = "25.5"
                                    mCollar = "16.5"
                                    mChest = "24.5"
                                    mWaist = "24.0"
                                    mHip = "26.0"
                                    mArmhole = "10.5"
                                    mSleeveMori = "6.5"
                                    mTrouserLength = "42.0"
                                    mTrouserBottom = "8.5"
                                    mTrouserAsan = "16.0"
                                }
                            }
                        }
                        .testTag("preset_$size"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = size,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Offline Calculator & Unit Converter Widget
        var showConverter by remember { mutableStateOf(false) }
        var inputCm by remember { mutableStateOf("") }
        var inputYards by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConverter = !showConverter },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SquareFoot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Offline Unit Converter (کیلکولیٹر)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Convert CM to Inches or Gaz (Yards) to Meters/Inches",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showConverter) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle calculator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (showConverter) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // CM to Inches Section
                    Text(
                        text = "Centimeters to Inches (سینٹی میٹر سے انچ):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputCm,
                            onValueChange = { inputCm = it },
                            label = { Text("Centimeters (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        val inchesVal = remember(inputCm) {
                            val cm = inputCm.toDoubleOrNull() ?: 0.0
                            if (cm > 0) {
                                String.format(Locale.US, "%.2f", cm / 2.54)
                             } else {
                                "0.0"
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(0.8f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Inches", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                Text("$inchesVal\"", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gaz/Yards Section
                    Text(
                        text = "Yards (Gaz / گز) to Meters & Inches:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputYards,
                            onValueChange = { inputYards = it },
                            label = { Text("Yards / Gaz (گز)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        val outputMetersInches = remember(inputYards) {
                            val y = inputYards.toDoubleOrNull() ?: 0.0
                            if (y > 0) {
                                val meters = y * 0.9144
                                val inches = y * 36.0
                                String.format(Locale.US, "%.1f m / %.0f\"", meters, inches)
                            } else {
                                "0.0 m / 0\""
                            }
                        }
                        Card(
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Equivalent", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                Text(outputMetersInches, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Kameez Section
        Text(" قمیض - Kameez / Kurta (Upper Wear)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = mShirtLength,
                onValueChange = { mShirtLength = it },
                label = { Text("Length / لمبائی") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_shirt_length"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = mShoulder,
                onValueChange = { mShoulder = it },
                label = { Text("Shoulder / تیرا") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_shoulder"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = mSleeves,
                onValueChange = { mSleeves = it },
                label = { Text("Sleeve / آستین") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_sleeves"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = mCollar,
                onValueChange = { mCollar = it },
                label = { Text("Collar / گلا / کالر") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_collar"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = mChest,
                onValueChange = { mChest = it },
                label = { Text("Chest / سینہ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_chest"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = mWaist,
                onValueChange = { mWaist = it },
                label = { Text("Waist / کمر") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_waist"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = mHip,
                onValueChange = { mHip = it },
                label = { Text("Ghera / Hip (گھیرا)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_hip"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = mArmhole,
                onValueChange = { mArmhole = it },
                label = { Text("Armhole / کندھا") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_armhole"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        OutlinedTextField(
            value = mSleeveMori,
            onValueChange = { mSleeveMori = it },
            label = { Text("Sleeve Mori / موری") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("\"") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .testTag("in_sleeve_mori"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Shalwar Section
        Text("شلوار - Shalwar / Trouser / Pajama (Lower Wear)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = mTrouserLength,
                onValueChange = { mTrouserLength = it },
                label = { Text("Length / لمبائی") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_trouser_length"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = mTrouserBottom,
                onValueChange = { mTrouserBottom = it },
                label = { Text("Paincha / Mori") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_trouser_bottom"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        OutlinedTextField(
            value = mTrouserAsan,
            onValueChange = { mTrouserAsan = it },
            label = { Text("Aasan (Thigh size / آسن)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("\"") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .testTag("in_trouser_asan"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedTextField(
            value = mNotes,
            onValueChange = { mNotes = it },
            label = { Text("Cutting Style Special Notes (ڈیزائننگ نوٹ)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("in_measurements_notes"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        DesignAndStitchingSection(
            galaType = galaType, onGalaTypeChange = { galaType = it },
            collarSize = collarSize, onCollarSizeChange = { collarSize = it },
            sleeveDesign = sleeveDesign, onSleeveDesignChange = { sleeveDesign = it },
            frontPatti = frontPatti, onFrontPattiChange = { frontPatti = it },
            frontPocket = frontPocket, onFrontPocketChange = { frontPocket = it },
            sidePocket = sidePocket, onSidePocketChange = { sidePocket = it },
            daman = daman, onDamanChange = { daman = it },
            shalwarWidth = shalwarWidth, onShalwarWidthChange = { shalwarWidth = it },
            shalwarPocket = shalwarPocket, onShalwarPocketChange = { shalwarPocket = it },
            bukramQuality = bukramQuality, onBukramQualityChange = { bukramQuality = it }
        )

        Button(
            onClick = {
                val updatedVal = measurements.copy(
                    shirtLength = mShirtLength.toDoubleOrZero(),
                    shoulder = mShoulder.toDoubleOrZero(),
                    sleeves = mSleeves.toDoubleOrZero(),
                    chest = mChest.toDoubleOrZero(),
                    waist = mWaist.toDoubleOrZero(),
                    hip = mHip.toDoubleOrZero(),
                    collar = mCollar.toDoubleOrZero(),
                    armhole = mArmhole.toDoubleOrZero(),
                    sleeveMori = mSleeveMori.toDoubleOrZero(),
                    trouserLength = mTrouserLength.toDoubleOrZero(),
                    trouserBottom = mTrouserBottom.toDoubleOrZero(),
                    trouserAsan = mTrouserAsan.toDoubleOrZero(),
                    notes = mNotes,
                    lastUpdated = System.currentTimeMillis(),
                    galaType = galaType,
                    collarSize = collarSize,
                    sleeveDesign = sleeveDesign,
                    frontPatti = frontPatti,
                    frontPocket = frontPocket,
                    sidePocket = sidePocket,
                    daman = daman,
                    shalwarWidth = shalwarWidth,
                    shalwarPocket = shalwarPocket,
                    bukramQuality = bukramQuality
                )
                viewModel.saveMeasurements(updatedVal)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_measurements_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("Save master blueprint dimensions", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun String.toDoubleOrZero(): Double {
    return this.toDoubleOrNull() ?: 0.0
}


// 7. PLACE / EDIT SUIT STITCH ORDER SCREEN
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditOrderScreen(viewModel: TailorViewModel) {
    val existing = viewModel.orderBeingEdited.value
    val customer = viewModel.selectedCustomer.collectAsStateWithLifecycle().value
    val defaultMeasurements = viewModel.selectedCustomerMeasurements.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer selected. Choose a client file first!")
        }
        return
    }

    // Stitch state variables populated from existing order OR customer defaults
    var itemType by remember { mutableStateOf(existing?.itemType ?: "Kameez Shalwar") }
    var clothType by remember { mutableStateOf(existing?.clothType ?: "") }
    var trackingId by remember { mutableStateOf(existing?.trackingId ?: "") }
    var totalAmount by remember { mutableStateOf(existing?.totalAmount?.toString()?.replace("0.0", "") ?: "") }
    var paidAmount by remember { mutableStateOf(existing?.paidAmount?.toString()?.replace("0.0", "") ?: "0") }
    var status by remember { mutableStateOf(existing?.status ?: "PENDING") }
    var dueDate by remember { mutableStateOf(existing?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)) }
    var orderNotes by remember { mutableStateOf(existing?.orderNotes ?: "") }

    // Design & Stitching custom preferences state
    var galaType by remember { mutableStateOf(existing?.galaType ?: defaultMeasurements?.galaType ?: "کالر") }
    var collarSize by remember { mutableStateOf(existing?.collarSize ?: defaultMeasurements?.collarSize ?: "درمیانہ") }
    var sleeveDesign by remember { mutableStateOf(existing?.sleeveDesign ?: defaultMeasurements?.sleeveDesign ?: "آستین سادہ") }
    var frontPatti by remember { mutableStateOf(existing?.frontPatti ?: defaultMeasurements?.frontPatti ?: true) }
    var frontPocket by remember { mutableStateOf(existing?.frontPocket ?: defaultMeasurements?.frontPocket ?: true) }
    var sidePocket by remember { mutableStateOf(existing?.sidePocket ?: defaultMeasurements?.sidePocket ?: "2") }
    var daman by remember { mutableStateOf(existing?.daman ?: defaultMeasurements?.daman ?: "گول") }
    var shalwarWidth by remember { mutableStateOf(existing?.shalwarWidth ?: defaultMeasurements?.shalwarWidth ?: "نارمل") }
    var shalwarPocket by remember { mutableStateOf(existing?.shalwarPocket ?: defaultMeasurements?.shalwarPocket ?: false) }
    var bukramQuality by remember { mutableStateOf(existing?.bukramQuality ?: defaultMeasurements?.bukramQuality ?: "2 (درمیانی)") }

    // Measurements for this individual article
    var shirtLength by remember { mutableStateOf(existing?.shirtLength?.toString() ?: defaultMeasurements?.shirtLength?.toString() ?: "") }
    var shoulder by remember { mutableStateOf(existing?.shoulder?.toString() ?: defaultMeasurements?.shoulder?.toString() ?: "") }
    var sleeves by remember { mutableStateOf(existing?.sleeves?.toString() ?: defaultMeasurements?.sleeves?.toString() ?: "") }
    var chest by remember { mutableStateOf(existing?.chest?.toString() ?: defaultMeasurements?.chest?.toString() ?: "") }
    var waist by remember { mutableStateOf(existing?.waist?.toString() ?: defaultMeasurements?.waist?.toString() ?: "") }
    var hip by remember { mutableStateOf(existing?.hip?.toString() ?: defaultMeasurements?.hip?.toString() ?: "") }
    var collar by remember { mutableStateOf(existing?.collar?.toString() ?: defaultMeasurements?.collar?.toString() ?: "") }
    var armhole by remember { mutableStateOf(existing?.armhole?.toString() ?: defaultMeasurements?.armhole?.toString() ?: "") }
    var sleeveMori by remember { mutableStateOf(existing?.sleeveMori?.toString() ?: defaultMeasurements?.sleeveMori?.toString() ?: "") }
    var trouserLength by remember { mutableStateOf(existing?.trouserLength?.toString() ?: defaultMeasurements?.trouserLength?.toString() ?: "") }
    var trouserBottom by remember { mutableStateOf(existing?.trouserBottom?.toString() ?: defaultMeasurements?.trouserBottom?.toString() ?: "") }
    var trouserAsan by remember { mutableStateOf(existing?.trouserAsan?.toString() ?: defaultMeasurements?.trouserAsan?.toString() ?: "") }

    // Strip "0.0" values to clean inputs
    val cleanInputs = remember {
        mutableStateOf(true)
    }
    if (cleanInputs.value) {
        if (shirtLength == "0.0") shirtLength = ""
        if (shoulder == "0.0") shoulder = ""
        if (sleeves == "0.0") sleeves = ""
        if (chest == "0.0") chest = ""
        if (waist == "0.0") waist = ""
        if (hip == "0.0") hip = ""
        if (collar == "0.0") collar = ""
        if (armhole == "0.0") armhole = ""
        if (sleeveMori == "0.0") sleeveMori = ""
        if (trouserLength == "0.0") trouserLength = ""
        if (trouserBottom == "0.0") trouserBottom = ""
        if (trouserAsan == "0.0") trouserAsan = ""
        cleanInputs.value = false
    }

    val formatter = remember { SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (existing != null) "Update Stitch Order #${existing.id}" else "Place Stitching Order",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.1.sp
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text("Client Name", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WhatsApp File", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(customer.phone, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Apparel Type Input Selector Chips
        Text("Apparel Type / لباس کی قسم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        val apparelTypes = listOf("Kameez Shalwar", "Kurta Pajama", "Waistcoat", "Sherwani", "Pant Suit")
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = Int.MAX_VALUE,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            apparelTypes.forEach { type ->
                val isSelected = itemType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { itemType = type },
                    label = { Text(type, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("apparel_chip_$type")
                )
            }
        }

        OutlinedTextField(
            value = clothType,
            onValueChange = { clothType = it },
            label = { Text("Fabric/Cloth color & details (کپڑا / رنگ / برانڈ)") },
            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_cloth_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedTextField(
            value = trackingId,
            onValueChange = { trackingId = it },
            label = { Text("Physical Tag / Tracking ID (کپڑوں پر لگا ٹیگ - e.g. TRK-1084)") },
            placeholder = { Text("Auto-generated if empty (TRK-XXXXX)") },
            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_tracking_id_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Fabric specific adjustments checkbox / header
        Text(
            text = "Size measurements for this particular Article (Inches)",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = shirtLength,
                onValueChange = { shirtLength = it },
                label = { Text("Length / لمبائی") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = shoulder,
                onValueChange = { shoulder = it },
                label = { Text("Shoulder / تیرا") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = sleeves,
                onValueChange = { sleeves = it },
                label = { Text("Sleeve / آستین") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = collar,
                onValueChange = { collar = it },
                label = { Text("Collar / ہالہ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = chest,
                onValueChange = { chest = it },
                label = { Text("Chest / سینہ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = waist,
                onValueChange = { waist = it },
                label = { Text("Waist / کمر") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = hip,
                onValueChange = { hip = it },
                label = { Text("Ghera / Hip") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = armhole,
                onValueChange = { armhole = it },
                label = { Text("Armhole / کندھا") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = trouserLength,
                onValueChange = { trouserLength = it },
                label = { Text("Trouser L.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = trouserBottom,
                onValueChange = { trouserBottom = it },
                label = { Text("Bottom Mori / موری") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("\"") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Date selection
        var showDatePicker by remember { mutableStateOf(false) }
        if (showDatePicker) {
            val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }
                    dueDate = newCal.timeInMillis
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Stitched Delivery Date / واپسی کی تاریخ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text(formatter.format(Date(dueDate)), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Pick Delivery Date", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Payment Details Layout
        Text("Account Settlement / حساب کتاب", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                label = { Text("Stitching Charge (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_order_price"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = paidAmount,
                onValueChange = { paidAmount = it },
                label = { Text("Paid Advance (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("in_order_paid"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Live calculation display
        val totDouble = totalAmount.toDoubleOrZero()
        val pdDouble = paidAmount.toDoubleOrZero()
        val dueValue = totDouble - pdDouble

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (dueValue > 0) Color(0xFFFFF6F6) else Color(0xFFE8F5E9)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (dueValue > 0) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remaining Outstanding Balance:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
                Text(
                    text = if (dueValue > 0) "Rs. ${dueValue.toInt()}" else "Fully Cleared",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (dueValue > 0) Color.Red else Color(0xFF2E7D32)
                )
            }
        }

        OutlinedTextField(
            value = orderNotes,
            onValueChange = { orderNotes = it },
            label = { Text("Additional specifications (e.g., Ban Collar, Double cuff, pocket details...)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_notes_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        DesignAndStitchingSection(
            galaType = galaType, onGalaTypeChange = { galaType = it },
            collarSize = collarSize, onCollarSizeChange = { collarSize = it },
            sleeveDesign = sleeveDesign, onSleeveDesignChange = { sleeveDesign = it },
            frontPatti = frontPatti, onFrontPattiChange = { frontPatti = it },
            frontPocket = frontPocket, onFrontPocketChange = { frontPocket = it },
            sidePocket = sidePocket, onSidePocketChange = { sidePocket = it },
            daman = daman, onDamanChange = { daman = it },
            shalwarWidth = shalwarWidth, onShalwarWidthChange = { shalwarWidth = it },
            shalwarPocket = shalwarPocket, onShalwarPocketChange = { shalwarPocket = it },
            bukramQuality = bukramQuality, onBukramQualityChange = { bukramQuality = it }
        )

        Button(
            onClick = {
                viewModel.saveOrder(
                    itemType = itemType,
                    clothType = clothType,
                    trackingIdInput = trackingId,
                    totalAmount = totDouble,
                    paidAmount = pdDouble,
                    status = status,
                    dueDate = dueDate,
                    shirtLength = shirtLength.toDoubleOrZero(),
                    shoulder = shoulder.toDoubleOrZero(),
                    sleeves = sleeves.toDoubleOrZero(),
                    chest = chest.toDoubleOrZero(),
                    waist = waist.toDoubleOrZero(),
                    hip = hip.toDoubleOrZero(),
                    collar = collar.toDoubleOrZero(),
                    armhole = armhole.toDoubleOrZero(),
                    sleeveMori = sleeveMori.toDoubleOrZero(),
                    trouserLength = trouserLength.toDoubleOrZero(),
                    trouserBottom = trouserBottom.toDoubleOrZero(),
                    trouserAsan = trouserAsan.toDoubleOrZero(),
                    orderNotes = orderNotes,
                    galaType = galaType,
                    collarSize = collarSize,
                    sleeveDesign = sleeveDesign,
                    frontPatti = frontPatti,
                    frontPocket = frontPocket,
                    sidePocket = sidePocket,
                    daman = daman,
                    shalwarWidth = shalwarWidth,
                    shalwarPocket = shalwarPocket,
                    bukramQuality = bukramQuality
                )
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_order_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(text = if (existing != null) "Apply Order Changes" else "Authorize Stitching Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        if (existing != null) {
            OutlinedButton(
                onClick = {
                    viewModel.deleteOrder(existing)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_order_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Purge Stitching Order Permanently", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// FlowRow layout placeholder inside Screens since Experimental FlowRow is pre-baked in Compose foundation starting recent versions, but standard FlowRow allows chips layout beautifully.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        maxItemsInEachRow = maxItemsInEachRow,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

// 8. ACTIVATION & EASYPAISA SCREEN
@Composable
fun ActivationScreen(viewModel: TailorViewModel) {
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
    val isTrialActive by viewModel.isTrialActive.collectAsStateWithLifecycle()
    val daysLeft by viewModel.remainingTrialDays.collectAsStateWithLifecycle()
    val shopId by viewModel.shopId.collectAsStateWithLifecycle()
    val epNumber by viewModel.easyPaisaNumber.collectAsStateWithLifecycle()
    val epName by viewModel.easyPaisaName.collectAsStateWithLifecycle()

    var keyInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Brand Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (isActivated) Color(0xFF1B807B) else MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActivated) Icons.Default.Verified else Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Text(
            text = if (isActivated) "App Fully Activated!" else "Tailor Book License Activation",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        // Status Banner Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isActivated -> Color(0xFFE8F5E9)
                    isTrialActive -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when {
                        isActivated -> Icons.Default.Verified
                        isTrialActive -> Icons.Default.Timer
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when {
                        isActivated -> Color(0xFF2E7D32)
                        isTrialActive -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = when {
                            isActivated -> "Lifetime Premium Access Unlocked"
                            isTrialActive -> "1-Week Free Trial Active ($daysLeft days left)"
                            else -> "1-Week Free Trial Expired!"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = when {
                            isActivated -> Color(0xFF1B5E20)
                            isTrialActive -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isActivated -> "Enjoy unlimited tailoring records, customer measurements & PDF receipts."
                            isTrialActive -> "Trial will expire in $daysLeft days. Send payment via EasyPaisa to activate permanently."
                            else -> "Please send payment via EasyPaisa to unlock full app access."
                        },
                        fontSize = 12.sp,
                        color = when {
                            isActivated -> Color(0xFF2E7D32)
                            isTrialActive -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        }
                    )
                }
            }
        }

        if (!isActivated) {
            // EasyPaisa Payment Box
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00A859)), // EasyPaisa Brand Green
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Text(
                            text = "EasyPaisa Payment Details",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // EasyPaisa Number Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00A859).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EasyPaisa Mobile Number:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Text(epNumber, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A3E))
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(epNumber))
                            viewModel.showToast("EasyPaisa Number copied: $epNumber")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Number", tint = Color(0xFF00A859))
                        }
                    }

                    // Shop ID
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Your Unique Shop ID:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Text(shopId, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(shopId))
                            viewModel.showToast("Shop ID copied: $shopId")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Shop ID", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Easy steps list in Urdu/English
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Kaise Activate Karen? (Steps):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("1. Apne EasyPaisa app se uper diye gaye number ($epNumber) par fees bhejen.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("2. Payment screenshot/receipt WhatsApp par bhejen sath mein apna Shop ID ($shopId) likhen.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("3. Owner aapko Activation Key bhejega, usy neeche dakhil karke Activate button dabayein.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Enter Activation Key Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Enter Activation Key",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            if (keyInput.isBlank()) {
                                viewModel.showToast("Please enter activation key!")
                            } else {
                                viewModel.activateWithKey(keyInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Activate App Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        } else {
            // Unlocked State View
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00A859), modifier = Modifier.size(52.dp))
                    Text("Tailor Book Lifetime License Active", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Shop ID: $shopId", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("JazakAllah! Your app is fully activated with unlimited access.", textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Button(
                        onClick = { viewModel.navigateTo(TailorViewModel.Screen.DASHBOARD) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Go to Dashboard")
                    }
                }
            }
        }
    }
}

// 9. DESIGN & STITCHING SPECIFICATIONS COMPONENT
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignAndStitchingSection(
    galaType: String, onGalaTypeChange: (String) -> Unit,
    collarSize: String, onCollarSizeChange: (String) -> Unit,
    sleeveDesign: String, onSleeveDesignChange: (String) -> Unit,
    frontPatti: Boolean, onFrontPattiChange: (Boolean) -> Unit,
    frontPocket: Boolean, onFrontPocketChange: (Boolean) -> Unit,
    sidePocket: String, onSidePocketChange: (String) -> Unit,
    daman: String, onDamanChange: (String) -> Unit,
    shalwarWidth: String, onShalwarWidthChange: (String) -> Unit,
    shalwarPocket: Boolean, onShalwarPocketChange: (Boolean) -> Unit,
    bukramQuality: String, onBukramQualityChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "ڈیژائن اور سلائی (Design & Stitching Specifications)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 1. Gala Type / Neck Design
            Text("گلہ / کالر ٹائپ (Gala / Neck Design):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val galaOptions = listOf("کالر", "بین", "گول", "وی گلا", "ڈیزائن گلا")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                galaOptions.forEach { option ->
                    FilterChip(
                        selected = galaType == option,
                        onClick = { onGalaTypeChange(option) },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 2. Collar Size
            if (galaType == "کالر" || galaType == "بین") {
                Text("کالر / بین سائز (Collar Size):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                val collarSizeOptions = listOf("بڑا", "درمیانہ", "چھوٹا")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    collarSizeOptions.forEach { option ->
                        FilterChip(
                            selected = collarSize == option,
                            onClick = { onCollarSizeChange(option) },
                            label = { Text(option, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 3. Sleeve Design
            Text("آستین کا ڈیزائن (Sleeve Design):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val sleeveOptions = listOf("آستین سادہ", "کف", "پٹی کف", "ہاف آستین")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sleeveOptions.forEach { option ->
                    FilterChip(
                        selected = sleeveDesign == option,
                        onClick = { onSleeveDesignChange(option) },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 4. Front Patti & Front Pocket (Switches)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("سامنے پٹی (Front Patti):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Switch(
                    checked = frontPatti,
                    onCheckedChange = onFrontPattiChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("سامنے کی جیب (Front Pocket):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Switch(
                    checked = frontPocket,
                    onCheckedChange = onFrontPocketChange
                )
            }

            // 5. Side Pockets
            Text("سائیڈ جیب (Side Pockets):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val sidePocketOptions = listOf("0", "1", "2")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sidePocketOptions.forEach { option ->
                    FilterChip(
                        selected = sidePocket == option,
                        onClick = { onSidePocketChange(option) },
                        label = { Text("$option Pocket(s)", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 6. Daman (Daman shape)
            Text("دامن (Daman Cut):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val damanOptions = listOf("گول", "چورس")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                damanOptions.forEach { option ->
                    FilterChip(
                        selected = daman == option,
                        onClick = { onDamanChange(option) },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 7. Shalwar Width & Shalwar Pocket
            Text("شلوار چوڑائی (Shalwar Fitting/Width):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val shalwarWidthOptions = listOf("نارمل", "کھلی", "تنگ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shalwarWidthOptions.forEach { option ->
                    FilterChip(
                        selected = shalwarWidth == option,
                        onClick = { onShalwarWidthChange(option) },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("شلوار میں جیب (Shalwar Pocket):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Switch(
                    checked = shalwarPocket,
                    onCheckedChange = onShalwarPocketChange
                )
            }

            // 8. Bukram Quality
            Text("بکرم کوالٹی (Bukram Stiffness/Quality):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            val bukramOptions = listOf("1 (ہلکی)", "2 (درمیانی)", "3 (بھاری)")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bukramOptions.forEach { option ->
                    FilterChip(
                        selected = bukramQuality == option,
                        onClick = { onBukramQualityChange(option) },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

// 10. SETTINGS & DATA BACKUP SCREEN
@Composable
fun SettingsBackupScreen(viewModel: TailorViewModel) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val lastBackupPath by viewModel.lastBackupPath.collectAsStateWithLifecycle()

    // Launcher for selecting JSON backup file to restore
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonString = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!jsonString.isNullOrBlank()) {
                    viewModel.restoreBackupData(jsonString)
                } else {
                    viewModel.showToast("Selected backup file is empty.")
                }
            } catch (e: Exception) {
                viewModel.showToast("Failed to read backup file: ${e.localizedMessage}")
            }
        }
    }

    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteJsonText by remember { mutableStateOf("") }

    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text("Paste Backup JSON Data", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pasteJsonText,
                    onValueChange = { pasteJsonText = it },
                    label = { Text("Paste JSON code here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pasteJsonText.isNotBlank()) {
                            viewModel.restoreBackupData(pasteJsonText)
                            showPasteDialog = false
                            pasteJsonText = ""
                        }
                    }
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = "Settings & Backup",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Data Management (بیک اپ اور ری سٹور)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Info Banner Card (Urdu text)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "اپنے ڈیٹا کو محفوظ رکھنے کے لیے ہفتے میں ایک بار بیک اپ ضرور لیا کریں۔ بیک اپ فائل کو واٹس ایپ یا ای میل پر محفوظ کریں۔",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        // 1. Backup Data (Save) Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.exportBackupData(context) { backupFile ->
                        try {
                            val authority = "${context.packageName}.provider"
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, backupFile)
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Save or Share Backup File:"))
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Backup saved: ${backupFile.name}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Backup Data (Save)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sara record file main save karein (ڈیٹا سیو کریں)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Backup Save Location Info Box / Path Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Backup Save Location (فائل کی لوکیشن)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "📁 Internal Storage > Downloads > TailorBook_Backup_...json",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val activePath = lastBackupPath ?: "${android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)}/TailorBook_Backup_...json"

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activePath,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activePath))
                                viewModel.showToast("Backup path copied to clipboard!")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Path",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "جب آپ Backup Data پر کلک کرتے ہیں تو یہ فائل آپ کے موبائل کے Downloads فولڈر میں محفوظ ہو جاتی ہے۔ فائل مینیجر سے بھی کھولی جا سکتی ہے۔",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        // 2. Restore Data (Load) Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    restoreLauncher.launch("*/*")
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Restore Data (Load)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Backup file wapis upload karein (ڈیٹا لوڈ کریں)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Direct Paste JSON Backup Text Option
        OutlinedButton(
            onClick = { showPasteDialog = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Paste Backup JSON Text directly", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Red Warning Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFCDD2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "ایپ کو Delete کرنے سے پہلے Backup ضرور لیا کریں ورنہ آپ کا ڈیٹا ضائع ہو جائے گا جس کی ریکوری ناممکن ہے۔ شکریہ!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
