package com.varsitycollege.vc_eats.api

import com.varsitycollege.vc_eats.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>

    // Menu
    @GET("menu")
    suspend fun getMenuItems(@Header("Authorization") token: String): Response<List<MenuItem>>

    @GET("menu/{id}")
    suspend fun getMenuItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<MenuItem>

    @GET("menu/categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<List<String>>

    // Orders
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: OrderRequest
    ): Response<Order>

    @GET("orders/my-orders")
    suspend fun getUserOrders(@Header("Authorization") token: String): Response<List<Order>>
}
