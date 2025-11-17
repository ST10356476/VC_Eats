package com.varsitycollege.vc_eats.models

data class LoginRequest(val email: String, val password: String)


data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class OrderRequest(
    val items: List<OrderItem>,
    val specialInstructions: String = ""
)

data class OrdersResponse(
    val success: Boolean,
    val message: String,
    val data: List<Order>
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val token: String
)

data class MenuResponse(
    val success: Boolean,
    val count: Int,
    val data: MenuData
)

data class MenuData(
    val menuItems: List<MenuItem>
)
