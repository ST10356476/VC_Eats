package com.varsitycollege.vc_eats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager.getInstance()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _selectedCategory = MutableStateFlow("BREAKFAST")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadMenuItems()
    }

    fun loadMenuItems() {
        viewModelScope.launch {
            _isLoading.value = true
            val items = firebaseManager.getAllMenuItems()
            _menuItems.value = items
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getFilteredItems(): List<MenuItem> {
        return _menuItems.value.filter {
            it.category == _selectedCategory.value && it.isAvailable
        }
    }

    fun addMenuItem(menuItem: MenuItem) {
        viewModelScope.launch {
            val success = firebaseManager.addMenuItem(menuItem)
            if (success) {
                loadMenuItems() // Refresh list
            }
        }
    }

    fun updateMenuItem(itemId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            val success = firebaseManager.updateMenuItem(itemId, updates)
            if (success) {
                loadMenuItems() // Refresh list
            }
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch {
            val success = firebaseManager.deleteMenuItem(itemId)
            if (success) {
                loadMenuItems() // Refresh list
            }
        }
    }
}