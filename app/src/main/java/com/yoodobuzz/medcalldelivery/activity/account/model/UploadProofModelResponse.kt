package com.yoodobuzz.medcalldelivery.activity.account.model

import com.google.gson.annotations.SerializedName

data class UploadProofModelResponse(
    @SerializedName("status"  ) var status  : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("profile" ) var profile : String? = null
)
