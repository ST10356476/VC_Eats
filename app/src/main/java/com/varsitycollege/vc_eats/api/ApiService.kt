package com.varsitycollege.vc_eats.api

import com.varsitycollege.vc_eats.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<ApiResponse<User>>

    // Menu
    @GET("menu")
    suspend fun getMenuItems(@Header("Authorization") token: String): Response<MenuResponse>

    @GET("menu/{id}")
    suspend fun getMenuItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<ApiResponse<MenuItem>>

    @GET("menu/categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<ApiResponse<List<String>>>

    // Orders
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: OrderRequest
    ): Response<ApiResponse<Order>>

    @GET("orders/my-orders")
    suspend fun getUserOrders(
        @Header("Authorization") token: String
    ): Response<OrdersResponse>

}
