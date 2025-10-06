package com.varsitycollege.vc_eats.firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.models.Order
import com.varsitycollege.vc_eats.models.User
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Authentication
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(
                id = result.user?.uid ?: "",
                name = name,
                email = email,
                role = "CUSTOMER"
            )
            saveUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signOut() = auth.signOut()

    fun getCurrentUserId() = auth.currentUser?.uid

    // Users
    suspend fun saveUser(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Menu Items
    suspend fun getAllMenuItems(): List<MenuItem> {
        return try {
            val snapshot = firestore.collection("menuItems").get().await()
            snapshot.toObjects(MenuItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMenuItem(menuItem: MenuItem): Boolean {
        return try {
            firestore.collection("menuItems").add(menuItem).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateMenuItem(itemId: String, updates: Map<String, Any>): Boolean {
        return try {
            firestore.collection("menuItems").document(itemId).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteMenuItem(itemId: String): Boolean {
        return try {
            firestore.collection("menuItems").document(itemId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Orders
    suspend fun placeOrder(order: Order): Boolean {
        return try {
            firestore.collection("orders").add(order).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllOrders(): List<Order> {
        return try {
            val snapshot = firestore.collection("orders").get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserOrders(userId: String): List<Order> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("customerId", userId)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            firestore.collection("orders").document(orderId)
                .update("status", status).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager().also { INSTANCE = it }
            }
        }
    }
}