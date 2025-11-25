package com.example.android.ui.register

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.network.ApiClient
import com.example.android.network.AuthService
import com.example.android.network.RegisterRequest
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val authService: AuthService = ApiClient.getRetrofit(context).create(AuthService::class.java)
    var email by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordConfirm by mutableStateOf("")
    var displayName by mutableStateOf("")

    fun register(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank() || username.isBlank() || displayName.isBlank()) {
            onError("Please fill in all fields.")
            return
        }
        if (password != passwordConfirm) {
            onError("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            try {
                val requestBody = RegisterRequest(
                    email = email,
                    password = password,
                    username = username,
                    displayName = displayName
                )

                val response = authService.register(requestBody)

                if (response.success) {
                    Toast.makeText(context, "Register Success", Toast.LENGTH_SHORT).show()
                    onSuccess(response.email ?: email)
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val statusCode = e.code()

                    Log.e("RegisterVM", "HTTP Error $statusCode: $errorBody")

                    onError("Register Error (Code $statusCode): Check Logcat for details.")
                }else{
                    onError("Register Error: ${e.message}")
                }

            }
        }
    }
}