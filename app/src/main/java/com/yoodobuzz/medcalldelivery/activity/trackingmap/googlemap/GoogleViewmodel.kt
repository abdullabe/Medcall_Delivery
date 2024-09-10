package com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.ActivityAssignedModelResponse
import com.yoodobuzz.medcalldelivery.network.ApiRequest
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleViewmodel{
    //map distance speed calculation
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val directionsApi = retrofit.create(ApiRequest.DirectionsApiService::class.java)

    suspend fun fetchTravelTime(origin: LatLng, destination: LatLng): String? {
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"

        try {
            val response = directionsApi.getDirections(originStr, destinationStr,RetrofitInstance.API_KEY)
            if (response.isSuccessful) {
                val directions = response.body()
                if (directions != null && directions.routes.isNotEmpty()) {
                    val duration = directions.routes[0].legs[0].duration
                    return duration.text // e.g., "10 mins"
                }
            } else {
                Log.e("DirectionsAPI", "Failed to get directions: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("DirectionsAPI", "Error: ${e.message}")
        }
        return null
    }



}