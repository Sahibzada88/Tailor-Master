package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TailorRepository(private val tailorDao: TailorDao) {

    val allCustomers: Flow<List<Customer>> = tailorDao.getAllCustomers()
    val allOrdersWithCustomer: Flow<List<OrderWithCustomer>> = tailorDao.getAllOrdersWithCustomer()

    val customerCount: Flow<Int> = tailorDao.getCustomerCountFlow()
    val orderCount: Flow<Int> = tailorDao.getOrderCountFlow()
    val activeOrderCount: Flow<Int> = tailorDao.getActiveOrderCountFlow()
    
    val totalRevenue: Flow<Double> = tailorDao.getTotalRevenueFlow().map { it ?: 0.0 }
    val totalCollected: Flow<Double> = tailorDao.getTotalCollectedFlow().map { it ?: 0.0 }

    suspend fun getCustomerById(id: Long): Customer? {
        return tailorDao.getCustomerById(id)
    }

    suspend fun insertCustomer(customer: Customer): Long {
        return tailorDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        tailorDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        tailorDao.deleteCustomer(customer)
    }

    suspend fun getMeasurementsByCustomerId(customerId: Long): CustomerMeasurement? {
        return tailorDao.getMeasurementsByCustomerId(customerId)
    }

    suspend fun saveMeasurement(measurement: CustomerMeasurement) {
        tailorDao.insertMeasurement(measurement)
    }

    suspend fun getOrdersByCustomerId(customerId: Long): List<Order> {
        return tailorDao.getOrdersByCustomerId(customerId)
    }

    suspend fun getOrderById(id: Long): Order? {
        return tailorDao.getOrderById(id)
    }

    suspend fun insertOrder(order: Order): Long {
        return tailorDao.insertOrder(order)
    }

    suspend fun updateOrder(order: Order) {
        tailorDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: Order) {
        tailorDao.deleteOrder(order)
    }
}
