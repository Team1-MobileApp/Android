package com.example.android.repository

import com.example.android.network.PhotoService
import com.example.android.network.AddPhotoToAlbumRequest
import com.example.android.network.GetPhotoDetailResponse
import com.example.android.network.PhotoUploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PhotoRepository(
    private val api: PhotoService
) {

    suspend fun uploadPhotoFile(file: File): PhotoUploadResponse {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("image/*".toMediaType())
        )

        val visibilityPart = "private".toRequestBody("text/plain".toMediaType())

        // API 호출
        return api.uploadPhotoFile(filePart, visibilityPart)
    }

    suspend fun getPhotoDetail(photoId: String): GetPhotoDetailResponse {
        return api.getPhotoDetail(photoId)
    }

    suspend fun addPhotoToAlbum(photoId: String, albumId: String) {
        val req = AddPhotoToAlbumRequest(albumId)
        api.addPhotoToAlbum(photoId, req)
    }
}