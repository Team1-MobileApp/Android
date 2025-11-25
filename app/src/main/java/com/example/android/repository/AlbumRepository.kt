package com.example.android.repository

import com.example.android.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class Album(
    val id: String,
    val name: String
)

class AlbumRepository(
    private val albumService: AlbumService,
    private val photoService: PhotoService
) {

    suspend fun getAlbums(): List<Album> {
        val response = albumService.getAlbums()
        return response.items.map { Album(it.id, it.title ?: "제목 없음") }
    }

    suspend fun uploadPhotoFile(file: File): PhotoUploadResponse {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val visibilityPart = "private".toRequestBody("text/plain".toMediaTypeOrNull())

        return photoService.uploadPhotoFile(filePart, visibilityPart)
    }

    suspend fun addPhotoToAlbum(photoId: String, albumId: String) {
        photoService.addPhotoToAlbum(photoId, AddPhotoToAlbumRequest(albumId.toString()))
    }
}
