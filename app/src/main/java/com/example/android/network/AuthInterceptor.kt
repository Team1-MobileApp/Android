package com.example.android.network

import android.content.Context
import com.example.android.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            TokenManager.getAccessToken(context)
        }

        Log.d("AuthInterceptor", "Extracted Token: $token")

        val req = chain.request().newBuilder().apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
                Log.d("AuthInterceptor", "Authorization Header Added.")
            }
        }.build()

        return chain.proceed(req)
    }
}