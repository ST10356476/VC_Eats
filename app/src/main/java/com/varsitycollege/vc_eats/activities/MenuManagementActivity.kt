package com.varsitycollege.vc_eats

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.varsitycollege.vc_eats.models.MenuItem
import java.util.*

class MenuManagementActivity : AppCompatActivity() {

    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var menuRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnAddItem: MaterialButton
    private lateinit var btnAddFirstItem: MaterialButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnCategoryFilter: MaterialButton
    private lateinit var categoryDropdown: MaterialCardView
    private lateinit var recyclerMenuItems: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    // Stats TextViews
    private lateinit var tvTotalItems: TextView
    private lateinit var tvAvailableItems: TextView
    private lateinit var tvSoldOutItems: TextView
    private lateinit var tvSpecialItems: TextView

    // Add Item Modal Views
    private lateinit var overlayAddItem: FrameLayout
    private lateinit var etItemName: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerCategoryModal: Spinner
    private lateinit var btnUploadImage: MaterialButton
    private lateinit var switchAvailable: SwitchMaterial
    private lateinit var switchSpecialItem: SwitchMaterial
    private lateinit var btnCloseModal: ImageButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnAddItemConfirm: MaterialButton

    // Edit Item Modal Views
    private lateinit var overlayEditItem: FrameLayout
    private lateinit var etEditItemName: TextInputEditText
    private lateinit var etEditPrice: TextInputEditText
    private lateinit var etEditDescription: TextInputEditText
    private lateinit var spinnerEditCategory: Spinner
    private lateinit var btnEditUploadImage: MaterialButton
    private lateinit var switchEditAvailable: SwitchMaterial
    private lateinit var switchEditSpecialItem: SwitchMaterial
    private lateinit var btnCloseEditModal: ImageButton
    private lateinit var btnCancelEdit: MaterialButton
    private lateinit var btnUpdateItem: MaterialButton

    // Allergen chips
    private val allergenChips = mutableMapOf<String, Chip>()
    private val editAllergenChips = mutableMapOf<String, Chip>()
    private val selectedAllergens = mutableSetOf<String>()
    private val editSelectedAllergens = mutableSetOf<String>()

    // Data
    private val menuItems = mutableListOf<MenuItem>()
    private val filteredItems = mutableListOf<MenuItem>()
    private lateinit var menuAdapter: MenuAdapter
    private var selectedCategory = "All Categories"
    private var selectedImageUri: Uri? = null
    private var editingItemId: String? = null
    private var isUploading = false

    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            btnUploadImage.text = "Image Selected ✓"
            btnEditUploadImage.text = "Image Selected ✓"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_management)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        menuRef = database.getReference("menu_items")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("menu_images")

        initializeViews()
        setupRecyclerView()
        setupListeners()
        setupCategorySpinners()
        setupAllergenChips()
        loadMenuItemsFromFirebase()
    }

    private fun initializeViews() {
        // Header
        btnBack = findViewById(R.id.btnBack)
        btnAddItem = findViewById(R.id.btnAddItem)
        btnAddFirstItem = findViewById(R.id.btnAddFirstItem)

        // Search and filter
        etSearch = findViewById(R.id.etSearch)
        btnCategoryFilter = findViewById(R.id.btnCategoryFilter)
        categoryDropdown = findViewById(R.id.categoryDropdown)

        // RecyclerView and empty state
        recyclerMenuItems = findViewById(R.id.recyclerMenuItems)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        // Stats
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvAvailableItems = findViewById(R.id.tvAvailableItems)
        tvSoldOutItems = findViewById(R.id.tvSoldOutItems)
        tvSpecialItems = findViewById(R.id.tvSpecialItems)

        // Add Item Modal
        overlayAddItem = findViewById(R.id.overlayAddItem)
        etItemName = findViewById(R.id.etItemName)
        etPrice = findViewById(R.id.etPrice)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategoryModal = findViewById(R.id.spinnerCategoryModal)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        switchAvailable = findViewById(R.id.switchAvailable)
        switchSpecialItem = findViewById(R.id.switchSpecialItem)
        btnCloseModal = findViewById(R.id.btnCloseModal)
        btnCancel = findViewById(R.id.btnCancel)
        btnAddItemConfirm = findViewById(R.id.btnAddItemConfirm)

        // Edit Item Modal
        overlayEditItem = findViewById(R.id.overlayEditItem)
        etEditItemName = findViewById(R.id.etEditItemName)
        etEditPrice = findViewById(R.id.etEditPrice)
        etEditDescription = findViewById(R.id.etEditDescription)
        spinnerEditCategory = findViewById(R.id.spinnerEditCategory)
        btnEditUploadImage = findViewById(R.id.btnEditUploadImage)
        switchEditAvailable = findViewById(R.id.switchEditAvailable)
        switchEditSpecialItem = findViewById(R.id.switchEditSpecialItem)
        btnCloseEditModal = findViewById(R.id.btnCloseEditModal)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        btnUpdateItem = findViewById(R.id.btnUpdateItem)
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter(
            filteredItems,
            onEditClick = { position -> showEditModal(position) },
            onDeleteClick = { position -> confirmDeleteItem(position) },
            onToggleAvailability = { position -> toggleAvailability(position) }
        )
        recyclerMenuItems.apply {
            layoutManager = LinearLayoutManager(this@MenuManagementActivity)
            adapter = menuAdapter
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnAddItem.setOnClickListener { showAddModal() }
        btnAddFirstItem.setOnClickListener { showAddModal() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnCategoryFilter.setOnClickListener {
            categoryDropdown.visibility = if (categoryDropdown.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        setupCategoryDropdownListeners()

        btnCloseModal.setOnClickListener { hideAddModal() }
        btnCancel.setOnClickListener { hideAddModal() }
        btnAddItemConfirm.setOnClickListener { addItem() }
        btnUploadImage.setOnClickListener { openGallery() }

        btnCloseEditModal.setOnClickListener { hideEditModal() }
        btnCancelEdit.setOnClickListener { hideEditModal() }
        btnUpdateItem.setOnClickListener { updateItem() }
        btnEditUploadImage.setOnClickListener { openGallery() }
    }

    private fun setupCategoryDropdownListeners() {
        findViewById<TextView>(R.id.tvAllCategories).setOnClickListener {
            selectCategory("All Categories")
        }
        findViewById<LinearLayout>(R.id.layoutBreakfast).setOnClickListener {
            selectCategory("Breakfast")
        }
        findViewById<LinearLayout>(R.id.layoutLunch).setOnClickListener {
            selectCategory("Lunch")
        }
        findViewById<LinearLayout>(R.id.layoutBeverages).setOnClickListener {
            selectCategory("Beverages")
        }
        findViewById<LinearLayout>(R.id.layoutSnacks).setOnClickListener {
            selectCategory("Snacks")
        }
    }

    private fun setupCategorySpinners() {
        val categories = arrayOf("Breakfast", "Lunch", "Beverages", "Snacks")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryModal.adapter = adapter
        spinnerEditCategory.adapter = adapter
    }

    private fun setupAllergenChips() {
        allergenChips["Gluten"] = findViewById(R.id.chipGluten)
        allergenChips["Dairy"] = findViewById(R.id.chipDairy)
        allergenChips["Eggs"] = findViewById(R.id.chipEggs)
        allergenChips["Nuts"] = findViewById(R.id.chipNuts)
        allergenChips["Soy"] = findViewById(R.id.chipSoy)
        allergenChips["Fish"] = findViewById(R.id.chipFish)
        allergenChips["Shellfish"] = findViewById(R.id.chipShellfish)

        allergenChips.forEach { (name, chip) ->
            chip.setOnClickListener {
                if (selectedAllergens.contains(name)) {
                    selectedAllergens.remove(name)
                } else {
                    selectedAllergens.add(name)
                }
                updateAllergenChipState(chip, selectedAllergens.contains(name))
            }
        }

        editAllergenChips["Gluten"] = findViewById(R.id.chipEditGluten)
        editAllergenChips["Dairy"] = findViewById(R.id.chipEditDairy)
        editAllergenChips["Eggs"] = findViewById(R.id.chipEditEggs)
        editAllergenChips["Nuts"] = findViewById(R.id.chipEditNuts)
        editAllergenChips["Soy"] = findViewById(R.id.chipEditSoy)
        editAllergenChips["Fish"] = findViewById(R.id.chipEditFish)
        editAllergenChips["Shellfish"] = findViewById(R.id.chipEditShellfish)

        editAllergenChips.forEach { (name, chip) ->
            chip.setOnClickListener {
                if (editSelectedAllergens.contains(name)) {
                    editSelectedAllergens.remove(name)
                } else {
                    editSelectedAllergens.add(name)
                }
                updateAllergenChipState(chip, editSelectedAllergens.contains(name))
            }
        }
    }

    private fun updateAllergenChipState(chip: Chip, selected: Boolean) {
        if (selected) {
            chip.setChipBackgroundColorResource(android.R.color.holo_red_light)
            chip.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            chip.setChipBackgroundColorResource(android.R.color.transparent)
            chip.setTextColor(resources.getColor(android.R.color.black, null))
        }
    }

    // Firebase Operations
    private fun loadMenuItemsFromFirebase() {
        menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(MenuItem::class.java)
                    item?.let { menuItems.add(it) }
                }
                filterItems(etSearch.text.toString())
                updateStats()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MenuManagementActivity,
                    "Error loading items: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val fileName = "menu_${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                onFailure()
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addItem() {
        if (isUploading) return

        val name = etItemName.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        isUploading = true
        btnAddItemConfirm.isEnabled = false
        btnAddItemConfirm.text = "Uploading..."

        if (selectedImageUri != null) {
            uploadImageToFirebase(
                selectedImageUri!!,
                onSuccess = { imageUrl ->
                    saveItemToFirebase(name, description, price, imageUrl)
                },
                onFailure = {
                    isUploading = false
                    btnAddItemConfirm.isEnabled = true
                    btnAddItemConfirm.text = "Add Item"
                }
            )
        } else {
            saveItemToFirebase(name, description, price, "")
        }
    }

    private fun saveItemToFirebase(name: String, description: String, price: Double, imageUrl: String) {
        val itemId = menuRef.push().key ?: return
        val item = MenuItem(
            id = itemId,
            name = name,
            description = description,
            price = price,
            category = spinnerCategoryModal.selectedItem.toString(),
            isAvailable = switchAvailable.isChecked,
            isSpecial = switchSpecialItem.isChecked,
            allergens = selectedAllergens.toList(),
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        menuRef.child(itemId).setValue(item)
            .addOnSuccessListener {
                isUploading = false
                btnAddItemConfirm.isEnabled = true
                btnAddItemConfirm.text = "Add Item"
                hideAddModal()
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isUploading = false
                btnAddItemConfirm.isEnabled = true
                btnAddItemConfirm.text = "Add Item"
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateItem() {
        if (isUploading || editingItemId == null) return

        val name = etEditItemName.text.toString().trim()
        val priceStr = etEditPrice.text.toString().trim()
        val description = etEditDescription.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        isUploading = true
        btnUpdateItem.isEnabled = false
        btnUpdateItem.text = "Updating..."

        val currentItem = menuItems.find { it.id == editingItemId }

        if (selectedImageUri != null) {
            uploadImageToFirebase(
                selectedImageUri!!,
                onSuccess = { imageUrl ->
                    updateItemInFirebase(name, description, price, imageUrl)
                },
                onFailure = {
                    isUploading = false
                    btnUpdateItem.isEnabled = true
                    btnUpdateItem.text = "Update Item"
                }
            )
        } else {
            updateItemInFirebase(name, description, price, currentItem?.imageUrl ?: "")
        }
    }

    private fun updateItemInFirebase(name: String, description: String, price: Double, imageUrl: String) {
        val updates = mapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to spinnerEditCategory.selectedItem.toString(),
            "isAvailable" to switchEditAvailable.isChecked,
            "isSpecial" to switchEditSpecialItem.isChecked,
            "allergens" to editSelectedAllergens.toList(),
            "imageUrl" to imageUrl,
            "updatedAt" to System.currentTimeMillis()
        )

        menuRef.child(editingItemId!!).updateChildren(updates)
            .addOnSuccessListener {
                isUploading = false
                btnUpdateItem.isEnabled = true
                btnUpdateItem.text = "Update Item"
                hideEditModal()
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isUploading = false
                btnUpdateItem.isEnabled = true
                btnUpdateItem.text = "Update Item"
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeleteItem(position: Int) {
        val item = filteredItems[position]
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete ${item.name}?")
            .setPositiveButton("Delete") { _, _ -> deleteItem(item.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(itemId: String) {
        menuRef.child(itemId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleAvailability(position: Int) {
        val item = filteredItems[position]
        menuRef.child(item.id).child("isAvailable").setValue(!item.isAvailable)
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        btnCategoryFilter.text = category
        categoryDropdown.visibility = View.GONE
        filterItems(etSearch.text.toString())
    }

    private fun filterItems(query: String) {
        filteredItems.clear()
        val searchQuery = query.lowercase()

        filteredItems.addAll(menuItems.filter { item ->
            val matchesSearch = item.name.lowercase().contains(searchQuery) ||
                    item.description.lowercase().contains(searchQuery)
            val matchesCategory = selectedCategory == "All Categories" ||
                    item.category == selectedCategory
            matchesSearch && matchesCategory
        })

        menuAdapter.notifyDataSetChanged()
        updateUI()
    }

    private fun showAddModal() {
        clearAddModalFields()
        overlayAddItem.visibility = View.VISIBLE
    }

    private fun hideAddModal() {
        overlayAddItem.visibility = View.GONE
        selectedImageUri = null
    }

    private fun showEditModal(position: Int) {
        val item = filteredItems[position]
        editingItemId = item.id

        etEditItemName.setText(item.name)
        etEditPrice.setText(item.price.toString())
        etEditDescription.setText(item.description)
        spinnerEditCategory.setSelection(getCategoryPosition(item.category))
        switchEditAvailable.isChecked = item.isAvailable
        switchEditSpecialItem.isChecked = item.isSpecial

        editSelectedAllergens.clear()
        editSelectedAllergens.addAll(item.allergens)
        updateEditAllergenChips()

        selectedImageUri = null
        btnEditUploadImage.text = "Change Image"

        overlayEditItem.visibility = View.VISIBLE
    }

    private fun hideEditModal() {
        overlayEditItem.visibility = View.GONE
        selectedImageUri = null
        editingItemId = null
    }

    private fun clearAddModalFields() {
        etItemName.text?.clear()
        etPrice.text?.clear()
        etDescription.text?.clear()
        spinnerCategoryModal.setSelection(0)
        switchAvailable.isChecked = true
        switchSpecialItem.isChecked = false
        selectedAllergens.clear()
        selectedImageUri = null
        btnUploadImage.text = "Choose File"
        updateAddAllergenChips()
    }

    private fun updateAddAllergenChips() {
        allergenChips.forEach { (name, chip) ->
            updateAllergenChipState(chip, selectedAllergens.contains(name))
        }
    }

    private fun updateEditAllergenChips() {
        editAllergenChips.forEach { (name, chip) ->
            updateAllergenChipState(chip, editSelectedAllergens.contains(name))
        }
    }

    private fun updateStats() {
        tvTotalItems.text = menuItems.size.toString()
        tvAvailableItems.text = menuItems.count { it.isAvailable }.toString()
        tvSoldOutItems.text = menuItems.count { !it.isAvailable }.toString()
        tvSpecialItems.text = menuItems.count { it.isSpecial }.toString()
    }

    private fun updateUI() {
        if (filteredItems.isEmpty()) {
            recyclerMenuItems.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            recyclerMenuItems.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun getCategoryPosition(category: String): Int {
        return when (category) {
            "Breakfast" -> 0
            "Lunch" -> 1
            "Beverages" -> 2
            "Snacks" -> 3
            else -> 0
        }
    }
}