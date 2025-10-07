package com.varsitycollege.vc_eats.models

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double {
        return menuItem.price * quantity
    }
}