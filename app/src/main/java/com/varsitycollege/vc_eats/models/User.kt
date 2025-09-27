package com.varsitycollege.vc_eats.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CUSTOMER", // CUSTOMER, STAFF, ADMIN
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)