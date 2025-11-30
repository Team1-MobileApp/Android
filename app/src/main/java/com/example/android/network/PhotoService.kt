package com.example.android.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


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

    @DELETE("/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String): DeletePhotoResponse

    @DELETE("/photos/{photoId}/albums")
    suspend fun deletePhotoFromAlbum(
        @Path("photoId") photoId: String,
        @Query("album_id") albumId: String
    ): DeletePhotoFromAlbumResponse

    @PATCH("/photos/{photoId}/visibility")
    suspend fun changeVisibility(
        @Path("photoId") photoId: String,
        @Body request: ChangeVisibilityRequest
    ): ChangeVisibilityResponse

    @GET("/users/me/photos")
    suspend fun getMyUploadedPhotos(): List<UserPhotoItemResponse>

    // 좋아요 추가
    @POST("/likes")
    suspend fun likePhoto(@Body request: LikeRequest)

    // 좋아요 취소
    @DELETE("/likes")
    suspend fun unlikePhoto(
        @Query("target_type") targetType: String,
        @Query("target_id") targetId: String
    )

    @GET("/feed/public")
    suspend fun getPublicFeed(@Query("sort") sort: String,
        @Query("limit") limit : Int,
            @Query("cursor") cursor: String?)
    :FeedResponse

}


data class UserPhotoItemResponse(
    val id: String,
    @SerializedName("fileUrl") val url: String?,
    val isLiked : Boolean?=false,
    val likeCount: Int,
    val daysAgo: Int
)
data class GetPhotoDetailResponse(
    val photoId : String,
    val fileUrl : String,
    val visibility : String,
    val createdAt : String,
    val updatedAt : String
)

data class PhotoUploadResponse(
    @SerializedName("id") val photoId: String,
    val url: String
)

data class AddPhotoToAlbumRequest(
    val albumId: String,
    val visibility: String
)

data class DeletePhotoResponse(
    val success: String
)

data class DeletePhotoFromAlbumResponse(
    val success: String
)

data class ChangeVisibilityRequest(
    val visibility: String
)

data class ChangeVisibilityResponse(
    val visibility: String
)

data class LikeRequest(
    val targetType : String,
    val targetId : String
)

data class FeedRequest(
    val sort : String,
    val limit : Int = 20,
    val cursor : String?= null
)

data class FeedResponse(
    val items : List<UserPhotoItemResponse>,
    val nextCursor : String?
)

