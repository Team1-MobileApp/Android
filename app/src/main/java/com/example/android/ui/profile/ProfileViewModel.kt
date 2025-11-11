package com.example.android.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.android.R

data class UserProfile(
    val name: String = "Text your name",
    val description: String = "Hi, how are you :)",
    val profileImageUri: String? = null
)

class ProfileViewModel(private val context: Context) : ViewModel() {


    private val PREFS_NAME = "ProfilePrefs"
    private val PROFILE_KEY = "user_profile"
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _userProfile: MutableState<UserProfile> = mutableStateOf(UserProfile())
    val userProfile: State<UserProfile> = _userProfile


    private val _albumPhotos: MutableState<List<Int>> = mutableStateOf(emptyList())
    val albumPhotos: State<List<Int>> = _albumPhotos

    init {
        loadUserProfile()
        loadStaticAlbumPhotos()
    }

    private fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = prefs.getString(PROFILE_KEY, null)
            if (json != null) {
                val profile = gson.fromJson(json, UserProfile::class.java)
                withContext(Dispatchers.Main) {
                    _userProfile.value = profile
                }
            }
        }
    }

    fun updateProfile(
        name: String = _userProfile.value.name,
        description: String = _userProfile.value.description,
        profileImageUri: Uri? = _userProfile.value.profileImageUri?.let { Uri.parse(it) }
    ) {
        val newProfile = _userProfile.value.copy(
            name = name,
            description = description,
            profileImageUri = profileImageUri?.toString()
        )
        _userProfile.value = newProfile
        saveUserProfile(newProfile)
    }

    private fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = gson.toJson(profile)
            prefs.edit().putString(PROFILE_KEY, json).apply()
        }
    }

    fun loadStaticAlbumPhotos() {
        val resourceIds = listOf(
            R.drawable.photo0, R.drawable.photo1, R.drawable.photo2, R.drawable.photo3,
            R.drawable.photo4, R.drawable.photo5, R.drawable.photo6, R.drawable.photo7,
        )
        _albumPhotos.value = resourceIds
    }
}