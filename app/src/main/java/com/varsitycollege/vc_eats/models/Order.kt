package com.varsitycollege.vc_eats.models

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: String = "PENDING", // PENDING, PREPARING, READY, COMPLETED, CANCELLED
    val totalAmount: Double = 0.0,
    val orderTime: Long = System.currentTimeMillis(),
    val estimatedReadyTime: Long = 0L,
    val specialInstructions: String = ""
)

data class OrderItem(
    val menuItemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val specialInstructions: String = ""
)