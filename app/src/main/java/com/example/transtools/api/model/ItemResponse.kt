package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName

//data class ItemResponse(
//    val data: String
//)
data class ItemResponse(
    val barcode: String,
    val item_name: String,
    val item_code: String,
    val dep: String,
    val storecode: String,
    val storename: String,
    val returnable: String
)
//data class MasterItemResponse(val data: String)
