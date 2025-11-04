package com.example.android.ui.theme.login

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// 임시
interface ApiService{
    suspend fun login(userId:String, password:String):LoginResponse
}

// 임시
data class LoginResponse(
    val success :Boolean,
    val message : String,
    val token : String?,
    val Id : String?
)

// 임시
class DummyApiService : ApiService{
    override suspend fun login(userId: String, password: String): LoginResponse {
        return if (userId == "test" && password == "1234") {
            LoginResponse(true, "Login Successful", "dummy_token", userId)
        } else {
            LoginResponse(false, "Incorrect ID or Password", null, null)
        }
    }
}

class LoginViewModel(application :Application):AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext
    private val prefs : SharedPreferences = context.getSharedPreferences("UserPrefs",Context.MODE_PRIVATE)

    private val apiService : ApiService = DummyApiService()

    var userId = ""
    var password = ""

    fun checkAutoLogin(onSuccess : (String) ->Unit){
        val savedId = prefs.getString("userId",null)
        if (savedId != null){
            onSuccess(savedId)
        }
    }

    fun login(onSuccess : (String) ->Unit, onError : (String) ->Unit){
        if (userId.isBlank() || password.isBlank()){
            onError("Please fill in all fields")
            return
        }

        viewModelScope.launch{
            try{
                val response = apiService.login(userId,password)
                if (response.success){
                    val receivedId = response.Id ?: userId
                    saveLoginState(receivedId)
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    onSuccess(receivedId)
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError("Login Error")
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