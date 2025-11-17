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

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val token: String
)
