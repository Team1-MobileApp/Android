package com.example.android.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoService {

    @Multipart
    @POST("/photos")
    suspend fun uploadPhotoFile(
        @Part file: MultipartBody.Part,
        @Part("visibility") visibility: RequestBody
    ): PhotoUploadResponse

    @GET("/photos/{photoId}")
    suspend fun getPhotoDetail(
        @Path("photoId") photoId: String,
    ): GetPhotoDetailResponse

    @POST("/photos/{photoId}/albums")
    suspend fun addPhotoToAlbum(
        @Path("photoId") photoId: String,
        @Body request: AddPhotoToAlbumRequest
    )

}

data class GetPhotoDetailResponse(
    val photoId : String,
    val fileurl : String,
    val visibility : String,
    val createdAt : String,
    val updatedAt : String
)

data class PhotoUploadResponse(
    @SerializedName("id") val photoId: String,
    val url: String
)

data class AddPhotoToAlbumRequest(
    val albumId: String
)
