package com.example.android.repository

import com.example.android.network.PhotoService
import com.example.android.network.AddPhotoToAlbumRequest
import com.example.android.network.ChangeVisibilityRequest
import com.example.android.network.ChangeVisibilityResponse
import com.example.android.network.DeletePhotoFromAlbumResponse
import com.example.android.network.GetPhotoDetailResponse
import com.example.android.network.PhotoUploadResponse
import com.example.android.network.UserPhotoItemResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

open class PhotoRepository(
    private val api: PhotoService
) {

    suspend fun uploadPhotoFile(file: File): PhotoUploadResponse {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("image/*".toMediaType())
        )

        val visibilityPart = "PRIVATE".toRequestBody("text/plain".toMediaType())

        // API 호출
        return api.uploadPhotoFile(filePart, visibilityPart)
    }

    suspend fun getPhotoDetail(photoId: String): GetPhotoDetailResponse {
        return api.getPhotoDetail(photoId)
    }

    suspend fun addPhotoToAlbum(photoId: String, albumId: String, visibility: String = "PRIVATE") {
        val req = AddPhotoToAlbumRequest(albumId, visibility)
        api.addPhotoToAlbum(photoId, req)
    }

    suspend fun deletePhoto(photoId: String): Boolean {
        val response = api.deletePhoto(photoId)
        return response.success == "true"
    }

    suspend fun deletePhotoFromAlbum(photoId: String, albumId: String): Boolean {
        val response = api.deletePhotoFromAlbum(photoId, albumId)
        return response.success == "true"
    }

    suspend fun changeVisibility(photoId: String, newVisibility: String): ChangeVisibilityResponse {
        return api.changeVisibility(photoId, ChangeVisibilityRequest(newVisibility))
    }
    suspend fun getMyUploadedPhotos(): Result<List<UserPhotoItemResponse>> = try {
        val response = api.getMyUploadedPhotos()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

}