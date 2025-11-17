package com.varsitycollege.vc_eats.repository

import com.google.protobuf.LazyStringArrayList.emptyList
import com.varsitycollege.vc_eats.api.ApiService
import com.varsitycollege.vc_eats.models.*
import kotlin.collections.emptyList

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): LoginResponse? {
        val response = apiService.login(LoginRequest(email, password))
        if (response.isSuccessful) return response.body() as LoginResponse?
        else throw Exception(response.errorBody()?.string() ?: "Login failed")
    }

    suspend fun getMenuItems(token: String): List<MenuItem> {
        val response = apiService.getMenuItems("Bearer $token")
        if (response.isSuccessful) return (response.body() ?: emptyList()) as List<MenuItem>
        else throw Exception(response.errorBody()?.string() ?: "Failed to fetch menu items")
    }

    suspend fun getUserOrders(token: String): List<Order> {
        val response = apiService.getUserOrders("Bearer $token")
        if (response.isSuccessful) return (response.body() ?: emptyList()) as List<Order>
        else throw Exception(response.errorBody()?.string() ?: "Failed to fetch orders")
    }
}
