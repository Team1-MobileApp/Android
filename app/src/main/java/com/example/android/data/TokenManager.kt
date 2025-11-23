package com.example.android.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("auth")

object TokenManager {
    val accessToken = stringPreferencesKey("access")
    val refreshToken = stringPreferencesKey("refresh")

    suspend fun saveTokens(context: Context, access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[accessToken] = access
            prefs[refreshToken] = refresh
        }
    }

    suspend fun getAccessToken(context: Context): String? =
        context.dataStore.data.first()[accessToken]
}
