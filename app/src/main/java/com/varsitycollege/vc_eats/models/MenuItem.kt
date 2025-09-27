package com.varsitycollege.vc_eats.models

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "BREAKFAST", // BREAKFAST, LUNCH, BEVERAGES, SNACKS
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isSpecial: Boolean = false,
    val allergens: List<String> = emptyList(),
    val calories: Int = 0,
    val preparationTime: Int = 15 // minutes
)