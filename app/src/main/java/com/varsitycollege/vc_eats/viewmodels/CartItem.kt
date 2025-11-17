package com.varsitycollege.vc_eats.viewmodels

import com.varsitycollege.vc_eats.models.MenuItem

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double {
        return menuItem.price * quantity
    }
}