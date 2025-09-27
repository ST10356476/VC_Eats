package com.varsitycollege.vc_eats

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.viewmodels.MenuViewModel
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {
    private lateinit var menuViewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_menu)

        menuViewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        // Observe menu items
        lifecycleScope.launch {
            menuViewModel.menuItems.collect { items ->
                // Update your RecyclerView adapter here
                updateMenuDisplay(items)
            }
        }

        // Category buttons
        findViewById<CardView>(R.id.cardBreakfast).setOnClickListener {
            menuViewModel.selectCategory("BREAKFAST")
        }
        findViewById<CardView>(R.id.cardLunch).setOnClickListener {
            menuViewModel.selectCategory("LUNCH")
        }
        // Add other category buttons...
    }

    private fun updateMenuDisplay(items: List<MenuItem>) {
        // Filter by selected category and update UI
        val filteredItems = menuViewModel.getFilteredItems()
        // Update your menu display
    }
}