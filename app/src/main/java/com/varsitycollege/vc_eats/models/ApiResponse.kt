package com.varsitycollege.vc_eats.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
