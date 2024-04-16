package com.yoodobuzz.medcalldelivery.activity.account.model

import com.google.gson.annotations.SerializedName

data class GetProfileModelResponse(

    @SerializedName("status"  ) var status  : String?  = null,
    @SerializedName("profile" ) var profile : Profile? = Profile(),
    @SerializedName("message" ) var message : String?  = null
)
data class Profile (

    @SerializedName("_id"        ) var Id        : String? = null,
    @SerializedName("agent_name" ) var agentName : String? = null,
    @SerializedName("address"    ) var address   : String? = null,
    @SerializedName("phoneno"    ) var phoneno   : Long?    = null,
    @SerializedName("email"      ) var email     : String? = null,
    @SerializedName("pincode"    ) var pincode   : String? = null,
    @SerializedName("active"     ) var active    : String? = null,
    @SerializedName("busy"       ) var busy      : String? = null,
    @SerializedName("__v"        ) var _v        : Int?    = null

)
