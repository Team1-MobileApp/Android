package com.example.android.ui.login

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.android.data.TokenManager
import com.example.android.network.ApiClient
import com.example.android.network.AuthService
import com.example.android.network.LoginRequest
import retrofit2.HttpException
import java.io.IOException


class LoginViewModel(application :Application):AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext
    private val prefs : SharedPreferences = context.getSharedPreferences("UserPrefs",Context.MODE_PRIVATE)

    private val apiService : AuthService = ApiClient.getRetrofit(context).create(AuthService::class.java)

    var email = ""
    var password = ""

    fun checkAutoLogin(onSuccess : (String) ->Unit){
        viewModelScope.launch {
            val savedId = prefs.getString("userId",null)

            val accessToken = TokenManager.getAccessToken(context)

            if (savedId != null && accessToken != null){
                onSuccess(savedId)
            }
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

                // 액세스 토큰 저장
                val receivedToken = response.accessToken
                TokenManager.saveTokens(context, receivedToken, receivedToken)
                val receivedId = response.user.id
                saveLoginState(receivedId)

                Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
                onSuccess(receivedId)

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("LoginViewModel", "HTTP Error: $errorBody")
                onError("Login Error : check your email or password")

            } catch (e: IOException) {
                Log.e("LoginViewModel", "Network Error: ${e.message}")
                onError("network error : check your network")

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login Error: ${e.message}")
                onError("Login error")
            }
        }
    }

    private fun saveLoginState(id: String) {
        prefs.edit().putString("userId", id).apply()
    }

}