package com.varsitycollege.vc_eats

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.vc_eats.models.Order
class OrderDashboardActivity : AppCompatActivity() {

    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var ordersRef: DatabaseReference

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnRefresh: MaterialButton
    private lateinit var tvPendingCount: TextView
    private lateinit var tvPreparingCount: TextView
    private lateinit var tvReadyCount: TextView
    private lateinit var tvCompletedCount: TextView

    // Filter views
    private lateinit var btnOrderFilter: MaterialButton
    private lateinit var orderFilterDropdown: MaterialCardView
    private lateinit var tvOrderCount: TextView

    // Status cards
    private lateinit var cardPending: MaterialCardView
    private lateinit var cardPreparing: MaterialCardView
    private lateinit var cardReady: MaterialCardView
    private lateinit var cardCompleted: MaterialCardView

    // RecyclerView
    private lateinit var recyclerOrders: RecyclerView
    private lateinit var layoutEmptyOrders: LinearLayout
    private lateinit var orderAdapter: OrderAdapter

    // Data
    private val allOrders = mutableListOf<Order>()
    private val filteredOrders = mutableListOf<Order>()
    private var currentFilter = "All Orders"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_dashboard)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        ordersRef = database.getReference("orders")

        initializeViews()
        setupRecyclerView()
        setupListeners()
        loadOrdersFromFirebase()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)

        // Status counts
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvPreparingCount = findViewById(R.id.tvPreparingCount)
        tvReadyCount = findViewById(R.id.tvReadyCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)

        // Filter
        btnOrderFilter = findViewById(R.id.btnOrderFilter)
        orderFilterDropdown = findViewById(R.id.orderFilterDropdown)
        tvOrderCount = findViewById(R.id.tvOrderCount)

        // Status cards
        cardPending = findViewById(R.id.cardPending)
        cardPreparing = findViewById(R.id.cardPreparing)
        cardReady = findViewById(R.id.cardReady)
        cardCompleted = findViewById(R.id.cardCompleted)

        // RecyclerView and empty state
        recyclerOrders = findViewById(R.id.recyclerOrders)
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            filteredOrders,
            onStatusChange = { orderId, newStatus -> updateOrderStatus(orderId, newStatus) },
            onViewDetails = { order -> showOrderDetails(order) }
        )

        recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderDashboardActivity)
            adapter = orderAdapter
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadOrdersFromFirebase() }

        // Filter button
        btnOrderFilter.setOnClickListener {
            orderFilterDropdown.visibility = if (orderFilterDropdown.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Filter options
        findViewById<TextView>(R.id.filterAllOrders).setOnClickListener {
            applyFilter("All Orders")
        }
        findViewById<TextView>(R.id.filterPending).setOnClickListener {
            applyFilter("PENDING")
        }
        findViewById<TextView>(R.id.filterPreparing).setOnClickListener {
            applyFilter("PREPARING")
        }
        findViewById<TextView>(R.id.filterReady).setOnClickListener {
            applyFilter("READY")
        }
        findViewById<TextView>(R.id.filterCompleted).setOnClickListener {
            applyFilter("COMPLETED")
        }

        // Status cards click listeners
        cardPending.setOnClickListener { applyFilter("PENDING") }
        cardPreparing.setOnClickListener { applyFilter("PREPARING") }
        cardReady.setOnClickListener { applyFilter("READY") }
        cardCompleted.setOnClickListener { applyFilter("COMPLETED") }
    }

    private fun loadOrdersFromFirebase() {
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrders.clear()

                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    order?.let { allOrders.add(it) }
                }

                // Sort by order time (newest first)
                allOrders.sortByDescending { it.orderTime }

                updateStatusCounts()
                applyFilter(currentFilter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@OrderDashboardActivity,
                    "Error loading orders: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateStatusCounts() {
        tvPendingCount.text = allOrders.count { it.status == "PENDING" }.toString()
        tvPreparingCount.text = allOrders.count { it.status == "PREPARING" }.toString()
        tvReadyCount.text = allOrders.count { it.status == "READY" }.toString()
        tvCompletedCount.text = allOrders.count { it.status == "COMPLETED" }.toString()
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        orderFilterDropdown.visibility = View.GONE

        // Update button text
        btnOrderFilter.text = when (filter) {
            "All Orders" -> "All Orders"
            "PENDING" -> "Pending"
            "PREPARING" -> "Preparing"
            "READY" -> "Ready"
            "COMPLETED" -> "Completed"
            else -> "All Orders"
        }

        // Filter orders
        filteredOrders.clear()
        if (filter == "All Orders") {
            filteredOrders.addAll(allOrders)
        } else {
            filteredOrders.addAll(allOrders.filter { it.status == filter })
        }

        // Update UI
        orderAdapter.notifyDataSetChanged()
        updateOrderCountText()
        updateEmptyState()
    }

    private fun updateOrderCountText() {
        tvOrderCount.text = "Showing ${filteredOrders.size} of ${allOrders.size} orders"
    }

    private fun updateEmptyState() {
        if (filteredOrders.isEmpty()) {
            recyclerOrders.visibility = View.GONE
            layoutEmptyOrders.visibility = View.VISIBLE
        } else {
            recyclerOrders.visibility = View.VISIBLE
            layoutEmptyOrders.visibility = View.GONE
        }
    }

    private fun updateOrderStatus(orderId: String, newStatus: String) {
        val updates = hashMapOf<String, Any>(
            "status" to newStatus
        )

        // Add estimated ready time when moving to PREPARING
        if (newStatus == "PREPARING") {
            val estimatedTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
            updates["estimatedReadyTime"] = estimatedTime
        }

        ordersRef.child(orderId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showOrderDetails(order: Order) {
        // TODO: Implement order details dialog or navigate to details screen
        Toast.makeText(this, "Order #${order.id.take(8)}", Toast.LENGTH_SHORT).show()
    }
}