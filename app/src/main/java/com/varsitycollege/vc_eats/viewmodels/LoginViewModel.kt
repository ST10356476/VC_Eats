package com.varsitycollege.vc_eats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.repository.ApiRepository
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = ApiRepository(RetrofitClient.apiService)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.login(email, password)
                response?.data?.token?.let { TokenManager.saveToken(it) }
                _loginState.value = LoginState.Success(response?.data?.user?.role ?: "STUDENT")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
            _isLoading.value = false
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    data class Success(val userRole: String) : LoginState()
    data class Error(val message: String) : LoginState()
}
