package com.yoodobuzz.medcalldelivery.network

import com.yoodobuzz.medcalldelivery.activity.account.model.GetProfileModelResponse
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.ActivityAssignedModelResponse
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.AgentHistoryModelResponse
import com.yoodobuzz.medcalldelivery.activity.login.model.LoginModelResponse
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.DirectionsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiRequest {

    @FormUrlEncoded
    @POST("signin_delivery")
    fun login(@FieldMap map: HashMap<String, String>): Call<LoginModelResponse>

    @FormUrlEncoded
    @POST("agentsorderstatus")
    fun agentsorderstatus(@FieldMap map: HashMap<String, String>): Call<ModelResponse>

    @FormUrlEncoded
    @POST("agentsorderstatus_cancel")
    fun agentsorderstatus_cancel(@FieldMap map: HashMap<String, String>): Call<ModelResponse>

    @FormUrlEncoded
    @POST("signup_del")
    fun signup(@FieldMap map: HashMap<String, String>): Call<ModelResponse>

    @Headers("Content-Type: application/json")
    @GET("getProfile_del/{id}")
    fun getProfile(
        @Path("id") str: String,
    ): Call<GetProfileModelResponse>

    @Headers("Content-Type: application/json")
    @GET("getorderassign_del/{id}")
    fun getorderassign_del(
        @Path("id") str: String,
    ): Call<ActivityAssignedModelResponse>

    @Headers("Content-Type: application/json")
    @GET("getagentorderhistory/{id}")
    fun getagentorderhistory(
        @Path("id") str: String,
    ): Call<AgentHistoryModelResponse>

    interface DirectionsApiService {
        @GET("maps/api/directions/json")
        suspend fun getDirections(
            @Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("key") apiKey: String
        ): Response<DirectionsResponse>
    }
}