package com.varsitycollege.vc_eats

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.vc_eats.adapters.MenuItemAdapter
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityCustomerMenuBinding
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMenuBinding
    private var menuItems: List<MenuItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMenuItems()
    }

    private fun loadMenuItems() {
        val token = TokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMenuItems("Bearer $token")
                if (response.isSuccessful) {
                    menuItems = response.body() ?: emptyList()
                    if (menuItems.isEmpty()) {
                        binding.recyclerMenuItems.visibility = View.GONE
                        binding.layoutEmptyMenu.visibility = View.VISIBLE
                    } else displayMenuItems(menuItems)
                } else {
                    Toast.makeText(this@CustomerMenuActivity, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMenuActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayMenuItems(items: List<MenuItem>) {
        val adapter = MenuItemAdapter(
            items,
            onViewClick = { Toast.makeText(this, "Viewing ${it.name}", Toast.LENGTH_SHORT).show() },
            onEditClick = { Toast.makeText(this, "Editing ${it.name}", Toast.LENGTH_SHORT).show() },
            onDeleteClick = { Toast.makeText(this, "Deleting ${it.name}", Toast.LENGTH_SHORT).show() }
        )
        binding.recyclerMenuItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerMenuItems.adapter = adapter

        binding.recyclerMenuItems.visibility = View.VISIBLE
        binding.layoutEmptyMenu.visibility = View.GONE
    }
}
