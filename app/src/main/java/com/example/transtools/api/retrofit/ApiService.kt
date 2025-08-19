package com.example.transtools.api.retrofit

import DashboardExpiredModel
import com.example.transtools.api.model.BebasExpiredModel
import com.example.transtools.api.model.DashboardResponse
import com.example.transtools.api.model.GetGondolaModel
import com.example.transtools.api.model.ItemResponse
import com.example.transtools.api.model.LoginResponse
import com.example.transtools.api.model.SaveTokenResponse
import com.example.transtools.api.model.TarikBarangModel
import com.example.transtools.api.model.soldOutModel
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @POST("apiMobile/login")
    fun login(@Body body: JsonObject): Call<LoginResponse>


    @POST("/apiMobile/save-item")
    fun postData(@Body data: BebasExpiredModel?): Call<Void>

    @PUT("/apiMobile/sold-out-item")
    fun deleteData(@Body data: soldOutModel?): Call<Void>

    @GET("/apiMobile/dashboard-didata")
    fun getDashboardData(
        @Query("usp_user") uspUser: String,
        @Query("usp_dept") uspDept: String
    ): Call<DashboardResponse>

    @GET("/apiMobile/dashboard-expiringSoon")
    fun getListExpired(@Query("usp_user") uspUser: String): Call<List<DashboardExpiredModel>>

    @GET("/apiMobile/list-gondola")
    fun getListGondola(
        @Query("usp_user") uspUser: String
    ): Call<List<GetGondolaModel>>

    @POST("apiMobile/master-item")
    fun sendBarcode(@Body body: JsonObject): Call<ItemResponse>
    
    @POST("apiMobile/save-token")
    fun saveToken(@Body body: JsonObject): Call<SaveTokenResponse>

    @GET("/apiMobile/list-expired")
    fun getListExpired(
        @Query("usp_user") uspUser: String,
        @Query("usp_dept") uspDept: String
    ): Call<List<TarikBarangModel>>
    
//    @GET("stories")
//    fun getStoryLocation(
//        @Query("size") size: Int? = null,
//        @Query("location") location: Int? = 0,
//        @Header("Authorization") token: String,
//    ): Call<ResponseLocationStory>
//
//    @GET("stories")
//    suspend fun getPaging(
//        @Query("page") page: Int? = null,
//        @Query("size") size: Int? = null,
//        @Query("location") location: Int? = 0,
//        @Header("Authorization") token: String,
//    ): ResponsePagingStory
}