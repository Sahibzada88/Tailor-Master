package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class TailorViewModel(application: Application, private val repository: TailorRepository) : AndroidViewModel(application) {

    // Visual Screen Navigation State (lightweight & extremely high performant)
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    enum class Screen {
        DASHBOARD,
        CUSTOMERS,
        ORDERS,
        CUSTOMER_DETAIL,
        ADD_EDIT_CUSTOMER,
        ADD_EDIT_ORDER,
        MEASUREMENTS
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
        orderNotes: String
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
                    orderNotes = orderNotes
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
                    orderNotes = orderNotes
                )
                repository.insertOrder(newOrder)
                showToast("Order registered with Tracking ID: $finalTrackingId")
            }
            orderBeingEdited.value = null
            navigateTo(Screen.ORDERS)
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
