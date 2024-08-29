package com.yoodobuzz.medcalldelivery.activity.deliveries.model

import com.google.gson.annotations.SerializedName

data class ActivityAssignedModelResponse(
    @SerializedName("status"    ) var status    : String?              = null,
    @SerializedName("cartItems" ) var cartItems : ArrayList<CartItems> = arrayListOf(),
    @SerializedName("message"   ) var message   : String?              = null
)
data class CartItems (

    @SerializedName("firstname"       ) var firstname      : String?         = null,
    @SerializedName("lastname"        ) var lastname       : String?         = null,
    @SerializedName("phone_number"    ) var phoneNumber    : Long?            = null,
    @SerializedName("user_add"        ) var userAdd        : UserAdd?        = UserAdd(),
    @SerializedName("store_name"      ) var storeName      : String?         = null,
    @SerializedName("agent_name"      ) var agentName      : String?         = null,
    @SerializedName("order_id"        ) var orderId        : String?         = null,
    @SerializedName("status"          ) var status         : String?         = null,
    @SerializedName("date"            ) var date           : String?         = null,
    @SerializedName("product_image"      ) var prod_image      : String?         = null,
    @SerializedName("qty"                ) var qty : Int?         = null,
    @SerializedName("product_Details" ) var productDetails : ProductDetails? = ProductDetails(),
    @SerializedName("tot_amount"      ) var totAmount      : String?         = null,
    @SerializedName("products"        ) var products       : ArrayList<Products> = arrayListOf()

)
data class UserAdd (

    @SerializedName("_id"      ) var Id       : String? = null,
    @SerializedName("user_id"  ) var userId   : String? = null,
    @SerializedName("active"   ) var active   : String? = null,
    @SerializedName("address_detail"   ) var address_detail   : String? = null,
    @SerializedName("lat"   ) var lat   : String? = null,
    @SerializedName("__v"      ) var _v       : Int?    = null,

)

data class ProductDetails (

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
data class Products (

    @SerializedName("product_name" ) var productName : String? = null,
    @SerializedName("quantity"     ) var quantity    : Int?    = null,
    @SerializedName("price"        ) var price       : String?    = null

)
