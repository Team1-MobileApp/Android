package com.example.android.ui.login

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.android.data.TokenManager
import com.example.android.network.ApiClient
import com.example.android.network.AuthService
import com.example.android.network.LoginRequest


class LoginViewModel(application :Application):AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext
    private val prefs : SharedPreferences = context.getSharedPreferences("UserPrefs",Context.MODE_PRIVATE)

    private val apiService : AuthService = ApiClient.getRetrofit(context).create(AuthService::class.java)

    var email = ""
    var password = ""

    fun checkAutoLogin(onSuccess : (String) ->Unit){
        val savedId = prefs.getString("userId",null)
        if (savedId != null){
            onSuccess(savedId)
        }
    }

    fun login(onSuccess : (String) ->Unit, onError : (String) ->Unit){
        if (email.isBlank() || password.isBlank()){
            onError("Please fill in all fields")
            return
        }

        viewModelScope.launch{
            try{
                val requestBody = LoginRequest(
                    email = email,
                    password = password
                )

                val response = apiService.login(requestBody)

                if (response.success){
                    val receivedId = response.Id ?: email
                    val receivedToken = response.token

                    if (receivedToken != null){
                        TokenManager.saveTokens(context, receivedToken, receivedToken)
                    }
                    saveLoginState(receivedId)
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    onSuccess(receivedId)
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError("Login Error: ${e.message}")
            }
        }
    }
    private fun saveLoginState(id: String) {
        prefs.edit().putString("userId", id).apply()
    }

    fun logout() {
        prefs.edit().remove("userId").apply()
    }
}