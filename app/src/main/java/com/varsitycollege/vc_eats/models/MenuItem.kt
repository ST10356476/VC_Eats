package com.varsitycollege.vc_eats.models

import android.R

data class MenuItem(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var isAvailable: Boolean = true,
    var isSpecial: Boolean = false,
    var allergens: List<String> = emptyList(),
    var imageUrl: String = "",
    var createdAt: String = "",
    var updatedAt: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", 0.0, "", true, false, emptyList(), ""  )
}