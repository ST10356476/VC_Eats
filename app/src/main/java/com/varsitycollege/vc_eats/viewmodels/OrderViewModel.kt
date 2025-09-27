package com.varsitycollege.vc_eats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAllOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            val orders = firebaseManager.getAllOrders()
            _orders.value = orders.sortedByDescending { it.orderTime }
            _isLoading.value = false
        }
    }

    fun loadUserOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                val orders = firebaseManager.getUserOrders(userId as String)
                _userOrders.value = orders.sortedByDescending { it.orderTime }
            }
            _isLoading.value = false
        }
    }

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            val success = firebaseManager.placeOrder(order)
            if (success) {
                loadUserOrders() // Refresh user orders
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val success = firebaseManager.updateOrderStatus(orderId, status)
            if (success) {
                loadAllOrders() // Refresh all orders
            }
        }
    }

    fun getOrdersByStatus(status: String): List<Order> {
        return _orders.value.filter { it.status == status }
    }
}