package com.example.android.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlbumService {

    @GET("/albums")
    suspend fun getAlbums(): GetAlbumsResponse

    @POST("/albums")
    suspend fun createAlbum(@Body body: CreateAlbumRequest): CreateAlbumResponse

    @DELETE("/albums/{albumId}")
    suspend fun deleteAlbum(@Path("albumId") albumId: String): DeleteAlbumResponse

    @GET("/albums/{albumId}/photos")
    suspend fun getAlbumPhotos(
        @Path("albumId") albumId: String
    ): AlbumPhotosResponse


}

data class AlbumResponse(
    val id: String,
    val ownerId: String,
    val title: String?,
    val description: String?,
    val visibility: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class GetAlbumsResponse(
    val items: List<AlbumResponse>,
    val nextCursor: String?
)

data class CreateAlbumRequest(
    val title : String,
    val description : String,
    val visibility : String
)

data class CreateAlbumResponse(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val visibility: String,
    val createdAt: String,
    val updatedAt: String
)

data class DeleteAlbumResponse(
    val success : String
)

data class AlbumPhotosResponse(
    val album: AlbumResponse,
    val photos: List<PhotoResponse>,
    val nextCursor: String?
)

data class PhotoResponse(
    val id: String,
    val albumId: String,
    val fileUrl: String,
    val createdAt: String,
    val updatedAt: String
)