package com.example.android.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    fun getRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            //.baseUrl("https://fourtogenic-server-production.up.railway.app/api/")
            .baseUrl("https://fourtogenic-server-production.up.railway.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
