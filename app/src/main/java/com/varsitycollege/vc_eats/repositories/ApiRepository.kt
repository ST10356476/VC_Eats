package com.varsitycollege.vc_eats.repository

import com.varsitycollege.vc_eats.api.ApiService
import com.varsitycollege.vc_eats.models.*

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): LoginResponse? {
        val response = apiService.login(LoginRequest(email, password))
        if (response.isSuccessful) return response.body()
        else throw Exception(response.errorBody()?.string() ?: "Login failed")
    }

    suspend fun getMenuItems(token: String): List<MenuItem> {
        val response = apiService.getMenuItems("Bearer $token")
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception(response.errorBody()?.string() ?: "Failed to fetch menu items")
    }

    suspend fun getUserOrders(token: String): List<Order> {
        val response = apiService.getUserOrders("Bearer $token")
        if (response.isSuccessful) return response.body() ?: emptyList()
        else throw Exception(response.errorBody()?.string() ?: "Failed to fetch orders")
    }
}
