package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName

class BebasExpiredModel {
    @SerializedName("ie_store_code")
    var ieStoreCode: String? = null

    @SerializedName("ie_gondola_no")
    var ieGondolaNo: String? = null

    @SerializedName("ie_barcode")
    var ieBarcode: String? = null

    @SerializedName("ie_item_code")
    var ieItemCode: String? = null

    @SerializedName("ie_item_name")
    var ieItemName: String? = null

    @SerializedName("ie_item_status")
    var ieItemStatus: String? = null

    @SerializedName("ie_expired_date")
    var ieExpiredDate: String? = null

    @SerializedName("ie_qty")
    var ieQty: Int? = null

    @SerializedName("ie_action")
    var ieAction: String? = null

    @SerializedName("ie_insert_user")
    var ieInsertUser: String? = null

    @SerializedName("ie_update_user")
    var ieUpdateUser: String? = null
}

class soldOutModel {
    @SerializedName("ie_gondola_no")
    var ieGondolaNo: String? = null

    @SerializedName("ie_id")
    var ieId: String? = null

    @SerializedName("ie_update_user")
    var ieUpdateUser: String? = null
}
