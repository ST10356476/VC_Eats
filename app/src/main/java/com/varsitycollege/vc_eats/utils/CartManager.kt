package com.varsitycollege.vc_eats.utils

import com.varsitycollege.vc_eats.viewmodels.CartItem
import com.varsitycollege.vc_eats.models.MenuItem

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addItem(menuItem: MenuItem) {
        val existingItem = cartItems.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(menuItem, 1))
        }
    }

    fun removeItem(menuItem: MenuItem) {
        cartItems.removeAll { it.menuItem.id == menuItem.id }
    }

    fun updateQuantity(menuItem: MenuItem, quantity: Int) {
        val item = cartItems.find { it.menuItem.id == menuItem.id }
        if (item != null) {
            if (quantity > 0) {
                item.quantity = quantity
            } else {
                removeItem(menuItem)
            }
        }
    }

    fun getItems(): List<CartItem> = cartItems.toList()

    fun getItemCount(): Int = cartItems.sumOf { it.quantity }

    fun getSubtotal(): Double = cartItems.sumOf { it.getTotalPrice() }

    fun getServiceFee(): Double = getSubtotal() * 0.05 // 5% service fee

    fun getTotal(): Double = getSubtotal() + getServiceFee()

    fun clear() {
        cartItems.clear()
    }

    fun isEmpty(): Boolean = cartItems.isEmpty()
}