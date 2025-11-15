package com.varsitycollege.vc_eats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = firebaseManager.signIn(email, password)
                if (success) {
                    val userId = firebaseManager.getCurrentUserId()
                    val user = userId?.let { firebaseManager.getUser(it) }
                    val role = user?.role ?: "CUSTOMER"
                    _loginState.value = LoginState.Success(role)
                } else {
                    _loginState.value = LoginState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = firebaseManager.signUp(email, password, name)
                if (success) {
                    _loginState.value = LoginState.Success("CUSTOMER")
                } else {
                    _loginState.value = LoginState.Error("Sign up failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Sign up failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // extra helper for auto-login on app start
    fun checkExistingLogin() {
        viewModelScope.launch {
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                val user = firebaseManager.getUser(userId)
                val role = user?.role ?: "CUSTOMER"
                _loginState.value = LoginState.Success(role)
            }
        }
    }

    fun clearState() {
        _loginState.value = LoginState.Initial
    }
}

sealed class LoginState {
    object Initial : LoginState()
    data class Success(val userRole: String) : LoginState()
    data class Error(val message: String) : LoginState()
}
