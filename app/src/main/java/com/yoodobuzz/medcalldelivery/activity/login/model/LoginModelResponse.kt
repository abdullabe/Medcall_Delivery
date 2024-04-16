package com.yoodobuzz.medcalldelivery.activity.login.model

import com.google.gson.annotations.SerializedName

data class LoginModelResponse(
    @SerializedName("status"    ) var status    : String? = null,
    @SerializedName("user_id"   ) var userId    : String? = null,
    @SerializedName("agentname" ) var agentname : String? = null,
    @SerializedName("email"     ) var email     : String? = null,
    @SerializedName("message"   ) var message   : String? = null
)
