package com.example.android.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlbumService {

    @GET("/albums")
    suspend fun getAlbums(): List<AlbumResponse>

    @POST("/albums")
    suspend fun createAlbum(@Body body: CreateAlbumRequest): CreateAlbumResponse

    @GET("/albums/{albumId}/photos")
    suspend fun getAlbumPhotos(
        @Path("albumId") albumId: String
        // 나중에 limit, cursor, sort를 @Query로 추가 가능
    ): List<PhotoResponse>
}

data class AlbumResponse(
    val id: Int,
    val name: String
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

data class PhotoResponse(
    val id: String,
    val albumId: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String
)