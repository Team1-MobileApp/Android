package com.example.android.repository

import com.example.android.network.AlbumResponse
import com.example.android.network.AlbumService
import com.example.android.network.CreateAlbumRequest
import com.example.android.network.CreateAlbumResponse
import com.example.android.network.PhotoResponse

class AlbumRepository(
    private val api: AlbumService
) {

    suspend fun getAlbums(): List<AlbumResponse> {
        return api.getAlbums()
    }

    suspend fun createAlbum(
        title: String,
        description: String,
        visibility: String
    ): CreateAlbumResponse {
        val request = CreateAlbumRequest(
            title = title,
            description = description,
            visibility = visibility
        )
        return api.createAlbum(request)
    }

    suspend fun getAlbumPhotos(albumId: String): List<PhotoResponse> {
        return api.getAlbumPhotos(albumId)
    }
}