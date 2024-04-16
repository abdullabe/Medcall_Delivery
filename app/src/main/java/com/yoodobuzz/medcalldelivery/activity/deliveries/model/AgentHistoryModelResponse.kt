package com.yoodobuzz.medcalldelivery.activity.deliveries.model

import com.google.gson.annotations.SerializedName

data class AgentHistoryModelResponse(
    @SerializedName("status"    ) var status    : String?              = null,
    @SerializedName("cartItems" ) var cartItems : ArrayList<CartItemsHistory> = arrayListOf(),
    @SerializedName("message"   ) var message   : String?              = null
)
data class UserAddHistory (

    @SerializedName("_id"      ) var Id       : String? = null,
    @SerializedName("user_id"  ) var userId   : String? = null,
    @SerializedName("building" ) var building : String? = null,
    @SerializedName("area"     ) var area     : String? = null,
    @SerializedName("landmark" ) var landmark : String? = null,
    @SerializedName("pincode"  ) var pincode  : String? = null,
    @SerializedName("type"     ) var type     : String? = null,
    @SerializedName("country"  ) var country  : String? = null,
    @SerializedName("state"    ) var state    : String? = null,
    @SerializedName("district" ) var district : String? = null,
    @SerializedName("active"   ) var active   : String? = null,
    @SerializedName("__v"      ) var _v       : Int?    = null

)
data class ProductDetailsHistory (

    @SerializedName("_id"           ) var Id           : String? = null,
    @SerializedName("product_name"  ) var productName  : String? = null,
    @SerializedName("price"         ) var price        : String? = null,
    @SerializedName("product_image" ) var productImage : String? = null,
    @SerializedName("sub_category"  ) var subCategory  : String? = null,
    @SerializedName("quantity"      ) var quantity     : Int?    = null,
    @SerializedName("prescription"  ) var prescription : String? = null,
    @SerializedName("category"      ) var category     : String? = null,
    @SerializedName("brand_name"    ) var brandName    : String? = null,
    @SerializedName("__v"           ) var _v           : Int?    = null

)
data class CartItemsHistory (

    @SerializedName("firstname"       ) var firstname      : String?         = null,
    @SerializedName("lastname"        ) var lastname       : String?         = null,
    @SerializedName("phone_number"    ) var phoneNumber    : Int?            = null,
    @SerializedName("user_add"        ) var userAdd        : UserAddHistory?        = UserAddHistory(),
    @SerializedName("store_name"      ) var storeName      : String?         = null,
    @SerializedName("agent_name"      ) var agentName      : String?         = null,
    @SerializedName("order_id"        ) var orderId        : String?         = null,
    @SerializedName("status"          ) var status         : String?         = null,
    @SerializedName("date"            ) var date           : String?         = null,
    @SerializedName("product_Details" ) var productDetails : ProductDetailsHistory? = ProductDetailsHistory(),
    @SerializedName("tot_amount"      ) var totAmount      : String?         = null

)
