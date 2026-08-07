package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.LicenseManager
import com.example.util.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class TailorViewModel(application: Application, private val repository: TailorRepository) : AndroidViewModel(application) {

    // License & Free Trial Manager
    val licenseManager = LicenseManager(application)
    val isActivated = MutableStateFlow(licenseManager.isActivated())
    val remainingTrialDays = MutableStateFlow(licenseManager.getRemainingTrialDays())
    val isTrialActive = MutableStateFlow(licenseManager.isTrialActive())
    val isAppUnlocked = MutableStateFlow(licenseManager.isAppUnlocked())
    val shopId = MutableStateFlow(licenseManager.getShopId())
    val easyPaisaNumber = MutableStateFlow(licenseManager.getEasyPaisaNumber())
    val easyPaisaName = MutableStateFlow(licenseManager.getEasyPaisaName())

    // Visual Screen Navigation State (lightweight & extremely high performant)
    private val _currentScreen = MutableStateFlow(
        if (licenseManager.isAppUnlocked()) Screen.DASHBOARD else Screen.ACTIVATION
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    enum class Screen {
        DASHBOARD,
        CUSTOMERS,
        ORDERS,
        CUSTOMER_DETAIL,
        ADD_EDIT_CUSTOMER,
        ADD_EDIT_ORDER,
        MEASUREMENTS,
        ACTIVATION,
        SETTINGS_BACKUP
    }

    // Active Selections
    val selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomerMeasurements = MutableStateFlow<CustomerMeasurement?>(null)
    val selectedOrder = MutableStateFlow<Order?>(null)

    // Editing State Flags
    val customerBeingEdited = MutableStateFlow<Customer?>(null)
    val orderBeingEdited = MutableStateFlow<Order?>(null)

    // Filtering & Searches
    val customerSearchQuery = MutableStateFlow("")
    val orderSearchQuery = MutableStateFlow("")
    val orderStatusFilter = MutableStateFlow("ALL") // ALL, PENDING, IN_PROGRESS, COMPLETED, DELIVERED

    // Toast/Feedback state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Generated PDF file tracking (to share or open)
    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    // Reactive Data Sources
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .combine(customerSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordersWithCustomer: StateFlow<List<OrderWithCustomer>> = repository.allOrdersWithCustomer
        .combine(orderSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter {
                it.customerName.contains(query, ignoreCase = true) || 
                it.customerPhone.contains(query) || 
                it.itemType.contains(query, ignoreCase = true) ||
                it.trackingId.contains(query, ignoreCase = true) ||
                it.id.toString().contains(query)
            }
        }
        .combine(orderStatusFilter) { list, status ->
            if (status == "ALL") list
            else list.filter { it.status == status }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shop Statistics Card Data
    val customerCount: StateFlow<Int> = repository.customerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val orderCount: StateFlow<Int> = repository.orderCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeOrderCount: StateFlow<Int> = repository.activeOrderCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stitchedOrderCount: StateFlow<Int> = ordersWithCustomer
        .map { list -> list.count { it.status == "COMPLETED" || it.status == "DELIVERED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingOrderCount: StateFlow<Int> = ordersWithCustomer
        .map { list -> list.count { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressOrderCount: StateFlow<Int> = ordersWithCustomer
        .map { list -> list.count { it.status == "IN_PROGRESS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val deliveredOrderCount: StateFlow<Int> = ordersWithCustomer
        .map { list -> list.count { it.status == "DELIVERED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCollected: StateFlow<Double> = repository.totalCollected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Outstanding balance calculate reactively
    val totalOutstanding: StateFlow<Double> = totalRevenue
        .combine(totalCollected) { revenue, collected ->
            val due = revenue - collected
            if (due < 0) 0.0 else due
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearPdfFile() {
        _generatedPdfFile.value = null
    }

    // Customer Actions
    fun selectCustomer(customer: Customer) {
        selectedCustomer.value = customer
        viewModelScope.launch {
            val measurements = repository.getMeasurementsByCustomerId(customer.id)
            selectedCustomerMeasurements.value = measurements ?: CustomerMeasurement(customerId = customer.id)
            navigateTo(Screen.CUSTOMER_DETAIL)
        }
    }

    fun saveCustomer(name: String, phone: String, address: String, gender: String) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank()) {
                showToast("Name and Phone are mandatory!")
                return@launch
            }
            val existing = customerBeingEdited.value
            if (existing != null) {
                val updated = existing.copy(name = name, phone = phone, address = address, gender = gender)
                repository.updateCustomer(updated)
                showToast("Customer info updated successfully")
                if (selectedCustomer.value?.id == existing.id) {
                    selectedCustomer.value = updated
                }
            } else {
                val newCustomer = Customer(name = name, phone = phone, address = address, gender = gender)
                val newId = repository.insertCustomer(newCustomer)
                // Initialize default measurements for new customer
                repository.saveMeasurement(CustomerMeasurement(customerId = newId))
            }
            customerBeingEdited.value = null
            navigateTo(Screen.CUSTOMERS)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showToast("Customer deleted successfully")
            if (selectedCustomer.value?.id == customer.id) {
                selectedCustomer.value = null
                navigateTo(Screen.CUSTOMERS)
            }
        }
    }

    // Measurement Actions
    fun saveMeasurements(measurements: CustomerMeasurement) {
        viewModelScope.launch {
            repository.saveMeasurement(measurements)
            selectedCustomerMeasurements.value = measurements
            showToast("Default measurements updated successfully")
            navigateTo(Screen.CUSTOMER_DETAIL)
        }
    }

    // Order Actions
    fun selectOrder(order: OrderWithCustomer) {
        viewModelScope.launch {
            val fullOrder = repository.getOrderById(order.id)
            if (fullOrder != null) {
                selectedOrder.value = fullOrder
                val cust = repository.getCustomerById(order.customerId)
                if (cust != null) {
                    selectedCustomer.value = cust
                }
                // Pre-fetch measurements associated
                val measurements = repository.getMeasurementsByCustomerId(order.customerId)
                selectedCustomerMeasurements.value = measurements ?: CustomerMeasurement(customerId = order.customerId)
            }
        }
    }

    fun saveOrder(
        trackingIdInput: String = "",
        itemType: String,
        clothType: String,
        totalAmount: Double,
        paidAmount: Double,
        status: String,
        dueDate: Long,
        shirtLength: Double,
        shoulder: Double,
        sleeves: Double,
        chest: Double,
        waist: Double,
        hip: Double,
        collar: Double,
        armhole: Double,
        sleeveMori: Double,
        trouserLength: Double,
        trouserBottom: Double,
        trouserAsan: Double,
        orderNotes: String,
        galaType: String = "کالر",
        collarSize: String = "درمیانہ",
        sleeveDesign: String = "آستین سادہ",
        frontPatti: Boolean = true,
        frontPocket: Boolean = true,
        sidePocket: String = "2",
        daman: String = "گول",
        shalwarWidth: String = "نارمل",
        shalwarPocket: Boolean = false,
        bukramQuality: String = "2 (درمیانی)"
    ) {
        viewModelScope.launch {
            val customerId = selectedCustomer.value?.id ?: 0L
            if (customerId == 0L) {
                showToast("Please select a customer first!")
                return@launch
            }
            if (itemType.isBlank()) {
                showToast("Please specify the attire type!")
                return@launch
            }

            val finalTrackingId = if (trackingIdInput.isNotBlank()) {
                trackingIdInput.trim().uppercase()
            } else {
                "TRK-${(10000..99999).random()}"
            }

            val existing = orderBeingEdited.value
            if (existing != null) {
                val updated = existing.copy(
                    trackingId = if (trackingIdInput.isNotBlank()) finalTrackingId else (existing.trackingId.ifBlank { finalTrackingId }),
                    itemType = itemType,
                    clothType = clothType,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    status = status,
                    dueDate = dueDate,
                    shirtLength = shirtLength,
                    shoulder = shoulder,
                    sleeves = sleeves,
                    chest = chest,
                    waist = waist,
                    hip = hip,
                    collar = collar,
                    armhole = armhole,
                    sleeveMori = sleeveMori,
                    trouserLength = trouserLength,
                    trouserBottom = trouserBottom,
                    trouserAsan = trouserAsan,
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
                repository.updateOrder(updated)
                showToast("Order #${updated.id} (${updated.trackingId}) updated")
            } else {
                val newOrder = Order(
                    customerId = customerId,
                    trackingId = finalTrackingId,
                    itemType = itemType,
                    clothType = clothType,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    status = status,
                    dueDate = dueDate,
                    shirtLength = shirtLength,
                    shoulder = shoulder,
                    sleeves = sleeves,
                    chest = chest,
                    waist = waist,
                    hip = hip,
                    collar = collar,
                    armhole = armhole,
                    sleeveMori = sleeveMori,
                    trouserLength = trouserLength,
                    trouserBottom = trouserBottom,
                    trouserAsan = trouserAsan,
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
                repository.insertOrder(newOrder)
                showToast("Order registered with Tracking ID: $finalTrackingId")
            }
            orderBeingEdited.value = null
            navigateTo(Screen.ORDERS)
        }
    }

    val lastBackupPath = MutableStateFlow<String?>(null)

    fun exportBackupData(context: Context, onShareBackup: (java.io.File) -> Unit) {
        viewModelScope.launch {
            try {
                val customersList = repository.getAllCustomersList()
                val measurementsList = repository.getAllMeasurementsList()
                val ordersList = repository.getAllOrdersList()

                val rootJson = org.json.JSONObject()
                rootJson.put("appName", "TailorBook")
                rootJson.put("version", 1)
                rootJson.put("exportTime", System.currentTimeMillis())

                val customersArray = org.json.JSONArray()
                for (c in customersList) {
                    val cObj = org.json.JSONObject()
                    cObj.put("id", c.id)
                    cObj.put("name", c.name)
                    cObj.put("phone", c.phone)
                    cObj.put("address", c.address)
                    cObj.put("gender", c.gender)
                    cObj.put("dateAdded", c.dateAdded)
                    customersArray.put(cObj)
                }
                rootJson.put("customers", customersArray)

                val measurementsArray = org.json.JSONArray()
                for (m in measurementsList) {
                    val mObj = org.json.JSONObject()
                    mObj.put("customerId", m.customerId)
                    mObj.put("shirtLength", m.shirtLength)
                    mObj.put("shoulder", m.shoulder)
                    mObj.put("sleeves", m.sleeves)
                    mObj.put("chest", m.chest)
                    mObj.put("waist", m.waist)
                    mObj.put("hip", m.hip)
                    mObj.put("collar", m.collar)
                    mObj.put("armhole", m.armhole)
                    mObj.put("sleeveMori", m.sleeveMori)
                    mObj.put("trouserLength", m.trouserLength)
                    mObj.put("trouserBottom", m.trouserBottom)
                    mObj.put("trouserAsan", m.trouserAsan)
                    mObj.put("notes", m.notes)
                    mObj.put("lastUpdated", m.lastUpdated)
                    mObj.put("galaType", m.galaType)
                    mObj.put("collarSize", m.collarSize)
                    mObj.put("sleeveDesign", m.sleeveDesign)
                    mObj.put("frontPatti", m.frontPatti)
                    mObj.put("frontPocket", m.frontPocket)
                    mObj.put("sidePocket", m.sidePocket)
                    mObj.put("daman", m.daman)
                    mObj.put("shalwarWidth", m.shalwarWidth)
                    mObj.put("shalwarPocket", m.shalwarPocket)
                    mObj.put("bukramQuality", m.bukramQuality)
                    measurementsArray.put(mObj)
                }
                rootJson.put("measurements", measurementsArray)

                val ordersArray = org.json.JSONArray()
                for (o in ordersList) {
                    val oObj = org.json.JSONObject()
                    oObj.put("id", o.id)
                    oObj.put("customerId", o.customerId)
                    oObj.put("trackingId", o.trackingId)
                    oObj.put("itemType", o.itemType)
                    oObj.put("clothType", o.clothType)
                    oObj.put("totalAmount", o.totalAmount)
                    oObj.put("paidAmount", o.paidAmount)
                    oObj.put("status", o.status)
                    oObj.put("orderDate", o.orderDate)
                    oObj.put("dueDate", o.dueDate)
                    oObj.put("shirtLength", o.shirtLength)
                    oObj.put("shoulder", o.shoulder)
                    oObj.put("sleeves", o.sleeves)
                    oObj.put("chest", o.chest)
                    oObj.put("waist", o.waist)
                    oObj.put("hip", o.hip)
                    oObj.put("collar", o.collar)
                    oObj.put("armhole", o.armhole)
                    oObj.put("sleeveMori", o.sleeveMori)
                    oObj.put("trouserLength", o.trouserLength)
                    oObj.put("trouserBottom", o.trouserBottom)
                    oObj.put("trouserAsan", o.trouserAsan)
                    oObj.put("orderNotes", o.orderNotes)
                    oObj.put("galaType", o.galaType)
                    oObj.put("collarSize", o.collarSize)
                    oObj.put("sleeveDesign", o.sleeveDesign)
                    oObj.put("frontPatti", o.frontPatti)
                    oObj.put("frontPocket", o.frontPocket)
                    oObj.put("sidePocket", o.sidePocket)
                    oObj.put("daman", o.daman)
                    oObj.put("shalwarWidth", o.shalwarWidth)
                    oObj.put("shalwarPocket", o.shalwarPocket)
                    oObj.put("bukramQuality", o.bukramQuality)
                    ordersArray.put(oObj)
                }
                rootJson.put("orders", ordersArray)

                val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val fileName = "TailorBook_Backup_$dateStr.json"
                val backupFile = java.io.File(context.cacheDir, fileName)
                backupFile.writeText(rootJson.toString(2))

                var displayPath = backupFile.absolutePath

                // Also try saving to public Downloads folder for direct user file access
                try {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    if (downloadsDir != null && (downloadsDir.exists() || downloadsDir.mkdirs())) {
                        val externalBackup = java.io.File(downloadsDir, fileName)
                        externalBackup.writeText(rootJson.toString(2))
                        displayPath = externalBackup.absolutePath
                    }
                } catch (_: Exception) {}

                lastBackupPath.value = displayPath

                onShareBackup(backupFile)
                showToast("Backup saved to: $displayPath")
            } catch (e: Exception) {
                showToast("Backup error: ${e.localizedMessage}")
            }
        }
    }

    fun restoreBackupData(jsonString: String) {
        viewModelScope.launch {
            try {
                val rootJson = org.json.JSONObject(jsonString)

                val customersList = mutableListOf<Customer>()
                val customersArray = rootJson.optJSONArray("customers") ?: org.json.JSONArray()
                for (i in 0 until customersArray.length()) {
                    val cObj = customersArray.getJSONObject(i)
                    customersList.add(
                        Customer(
                            id = cObj.optLong("id", 0L),
                            name = cObj.optString("name", ""),
                            phone = cObj.optString("phone", ""),
                            address = cObj.optString("address", ""),
                            gender = cObj.optString("gender", "Male"),
                            dateAdded = cObj.optLong("dateAdded", System.currentTimeMillis())
                        )
                    )
                }

                val measurementsList = mutableListOf<CustomerMeasurement>()
                val measurementsArray = rootJson.optJSONArray("measurements") ?: org.json.JSONArray()
                for (i in 0 until measurementsArray.length()) {
                    val mObj = measurementsArray.getJSONObject(i)
                    measurementsList.add(
                        CustomerMeasurement(
                            customerId = mObj.optLong("customerId", 0L),
                            shirtLength = mObj.optDouble("shirtLength", 0.0),
                            shoulder = mObj.optDouble("shoulder", 0.0),
                            sleeves = mObj.optDouble("sleeves", 0.0),
                            chest = mObj.optDouble("chest", 0.0),
                            waist = mObj.optDouble("waist", 0.0),
                            hip = mObj.optDouble("hip", 0.0),
                            collar = mObj.optDouble("collar", 0.0),
                            armhole = mObj.optDouble("armhole", 0.0),
                            sleeveMori = mObj.optDouble("sleeveMori", 0.0),
                            trouserLength = mObj.optDouble("trouserLength", 0.0),
                            trouserBottom = mObj.optDouble("trouserBottom", 0.0),
                            trouserAsan = mObj.optDouble("trouserAsan", 0.0),
                            notes = mObj.optString("notes", ""),
                            lastUpdated = mObj.optLong("lastUpdated", System.currentTimeMillis()),
                            galaType = mObj.optString("galaType", "کالر"),
                            collarSize = mObj.optString("collarSize", "درمیانہ"),
                            sleeveDesign = mObj.optString("sleeveDesign", "آستین سادہ"),
                            frontPatti = mObj.optBoolean("frontPatti", true),
                            frontPocket = mObj.optBoolean("frontPocket", true),
                            sidePocket = mObj.optString("sidePocket", "2"),
                            daman = mObj.optString("daman", "گول"),
                            shalwarWidth = mObj.optString("shalwarWidth", "نارمل"),
                            shalwarPocket = mObj.optBoolean("shalwarPocket", false),
                            bukramQuality = mObj.optString("bukramQuality", "2 (درمیانی)")
                        )
                    )
                }

                val ordersList = mutableListOf<Order>()
                val ordersArray = rootJson.optJSONArray("orders") ?: org.json.JSONArray()
                for (i in 0 until ordersArray.length()) {
                    val oObj = ordersArray.getJSONObject(i)
                    ordersList.add(
                        Order(
                            id = oObj.optLong("id", 0L),
                            customerId = oObj.optLong("customerId", 0L),
                            trackingId = oObj.optString("trackingId", ""),
                            itemType = oObj.optString("itemType", "Kameez Shalwar"),
                            clothType = oObj.optString("clothType", ""),
                            totalAmount = oObj.optDouble("totalAmount", 0.0),
                            paidAmount = oObj.optDouble("paidAmount", 0.0),
                            status = oObj.optString("status", "PENDING"),
                            orderDate = oObj.optLong("orderDate", System.currentTimeMillis()),
                            dueDate = oObj.optLong("dueDate", System.currentTimeMillis()),
                            shirtLength = oObj.optDouble("shirtLength", 0.0),
                            shoulder = oObj.optDouble("shoulder", 0.0),
                            sleeves = oObj.optDouble("sleeves", 0.0),
                            chest = oObj.optDouble("chest", 0.0),
                            waist = oObj.optDouble("waist", 0.0),
                            hip = oObj.optDouble("hip", 0.0),
                            collar = oObj.optDouble("collar", 0.0),
                            armhole = oObj.optDouble("armhole", 0.0),
                            sleeveMori = oObj.optDouble("sleeveMori", 0.0),
                            trouserLength = oObj.optDouble("trouserLength", 0.0),
                            trouserBottom = oObj.optDouble("trouserBottom", 0.0),
                            trouserAsan = oObj.optDouble("trouserAsan", 0.0),
                            orderNotes = oObj.optString("orderNotes", ""),
                            galaType = oObj.optString("galaType", "کالر"),
                            collarSize = oObj.optString("collarSize", "درمیانہ"),
                            sleeveDesign = oObj.optString("sleeveDesign", "آستین سادہ"),
                            frontPatti = oObj.optBoolean("frontPatti", true),
                            frontPocket = oObj.optBoolean("frontPocket", true),
                            sidePocket = oObj.optString("sidePocket", "2"),
                            daman = oObj.optString("daman", "گول"),
                            shalwarWidth = oObj.optString("shalwarWidth", "نارمل"),
                            shalwarPocket = oObj.optBoolean("shalwarPocket", false),
                            bukramQuality = oObj.optString("bukramQuality", "2 (درمیانی)")
                        )
                    )
                }

                repository.restoreData(customersList, measurementsList, ordersList)
                showToast("Data restored successfully! (${customersList.size} customers, ${ordersList.size} orders)")
            } catch (e: Exception) {
                showToast("Restore failed: ${e.localizedMessage}")
            }
        }
    }

    fun recordPayment(orderId: Long, additionalAmount: Double) {
        viewModelScope.launch {
            if (additionalAmount <= 0) {
                showToast("Please enter a valid amount")
                return@launch
            }
            val rawOrder = repository.getOrderById(orderId)
            if (rawOrder != null) {
                val updatedPaid = rawOrder.paidAmount + additionalAmount
                val updated = rawOrder.copy(paidAmount = updatedPaid)
                repository.updateOrder(updated)
                showToast("Payment of Rs. ${additionalAmount.toInt()} recorded!")
            }
        }
    }

    fun updatePaidAmount(orderId: Long, newTotalPaid: Double) {
        viewModelScope.launch {
            val rawOrder = repository.getOrderById(orderId)
            if (rawOrder != null) {
                val updated = rawOrder.copy(paidAmount = newTotalPaid)
                repository.updateOrder(updated)
                showToast("Updated total paid: Rs. ${newTotalPaid.toInt()}")
            }
        }
    }

    fun updateOrderStatus(order: OrderWithCustomer, newStatus: String) {
        viewModelScope.launch {
            val rawOrder = repository.getOrderById(order.id)
            if (rawOrder != null) {
                val updated = rawOrder.copy(status = newStatus)
                repository.updateOrder(updated)
                showToast("Order marked as $newStatus")
            }
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            showToast("Order removed from ledger")
            navigateTo(Screen.ORDERS)
        }
    }

    // PDF and Receipts triggers
    fun generateOrderReceiptPdf(context: Context, orderId: Long) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId)
            if (order == null) {
                showToast("Error: Order not found")
                return@launch
            }
            val customer = repository.getCustomerById(order.customerId)
            if (customer == null) {
                showToast("Error: Customer not found")
                return@launch
            }

            try {
                val pdfFile = PdfGenerator.generateReceiptPdf(context, customer, order)
                _generatedPdfFile.value = pdfFile
                showToast("PDF Receipt generated: \n${pdfFile.name}")
            } catch (e: Exception) {
                showToast("Error generating receipt PDF: ${e.localizedMessage}")
            }
        }
    }

    fun generateCustomerSummaryReportPdf(context: Context, customerId: Long) {
        viewModelScope.launch {
            val customer = repository.getCustomerById(customerId)
            if (customer == null) {
                showToast("Error: Customer not found")
                return@launch
            }
            val measurements = repository.getMeasurementsByCustomerId(customerId)
            val orders = repository.getOrdersByCustomerId(customerId)

            try {
                val pdfFile = PdfGenerator.generateCustomerReportPdf(context, customer, measurements, orders)
                _generatedPdfFile.value = pdfFile
                showToast("Customer dossier report ready:\n${pdfFile.name}")
            } catch (e: Exception) {
                showToast("Error compiling dossier report PDF: ${e.localizedMessage}")
            }
        }
    }

    // License & Activation Actions
    fun refreshLicenseState() {
        isActivated.value = licenseManager.isActivated()
        remainingTrialDays.value = licenseManager.getRemainingTrialDays()
        isTrialActive.value = licenseManager.isTrialActive()
        isAppUnlocked.value = licenseManager.isAppUnlocked()
        shopId.value = licenseManager.getShopId()
        easyPaisaNumber.value = licenseManager.getEasyPaisaNumber()
        easyPaisaName.value = licenseManager.getEasyPaisaName()
    }

    fun activateWithKey(key: String): Boolean {
        val success = licenseManager.verifyAndActivate(key)
        refreshLicenseState()
        if (success) {
            showToast("Success! Tailor Book Activated Permanently.")
            navigateTo(Screen.DASHBOARD)
        } else {
            showToast("Invalid Key! Please verify key or contact support on WhatsApp.")
        }
        return success
    }

    fun updateEasyPaisaAccount(number: String, name: String) {
        licenseManager.setEasyPaisaDetails(number, name)
        refreshLicenseState()
        showToast("EasyPaisa account updated")
    }

    fun getExpectedKeyForCurrentShop(): String {
        return licenseManager.generateExpectedKey(licenseManager.getShopId())
    }

    // Simple viewmodel factory helper
    class Factory(private val application: Application, private val repository: TailorRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TailorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TailorViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
