package com.yoodobuzz.medcalldelivery.network

import com.google.gson.annotations.SerializedName

data class ModelResponse(
    @SerializedName("status"  ) var status  : String? = null,
    @SerializedName("message" ) var message : String? = null
)
