package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName

data class ClosestItem(
    @SerializedName("remaining_days") val remainingDays: Int,
    @SerializedName("ie_id") val ieId: String,
    @SerializedName("ie_store_code") val ieStoreCode: String,
    @SerializedName("ie_gondola_no") val ieGondolaNo: String,
    @SerializedName("ie_barcode") val ieBarcode: String,
    @SerializedName("ie_item_code") val ieItemCode: String,
    @SerializedName("ie_item_name") val ieItemName: String,
    @SerializedName("ie_item_status") val ieItemStatus: String,
    @SerializedName("ie_expired_date") val ieExpiredDate: String,
    @SerializedName("ie_qty") val ieQty: Int,
    @SerializedName("ie_action") val ieAction: String,
    @SerializedName("ie_insert_user") val ieInsertUser: String,
    @SerializedName("ie_insert_date") val ieInsertDate: String,
    @SerializedName("ie_update_user") val ieUpdateUser: String,
    @SerializedName("ie_update_date") val ieUpdateDate: String,
    @SerializedName("ie_image_path") val ieImagePath: String?,
    @SerializedName("ie_qty_pull") val ieQtyPull: Int?,
    @SerializedName("closest_item_expired_date") val closestItemExpiredDate: String
)
