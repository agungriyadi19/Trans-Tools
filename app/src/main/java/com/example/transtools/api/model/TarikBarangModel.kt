package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName


class TarikBarangModel(
    @SerializedName("remaining_days") var remainingDays: Int?,
    @SerializedName("ie_id") var ieId: String?,
    @SerializedName("ie_store_code") var ieStoreCode: String?,
    @SerializedName("ie_gondola_no") var ieGondolaNo: String?,
    @SerializedName("ie_barcode") var ieBarcode: String?,
    @SerializedName("ie_item_code") var ieItemCode: String?,
    @SerializedName("ie_item_name") var ieItemName: String?,
    @SerializedName("ie_item_status") var ieItemStatus: String?,
    @SerializedName("ie_expired_date") var ieExpiredDate: String?,
    @SerializedName("ie_qty") var ieQty: Int?,
    @SerializedName("ie_action") var ieAction: String?,
    @SerializedName("ie_insert_user") var ieInsertUser: String?,
    @SerializedName("ie_insert_date") var ieInsertDate: String?,
    @SerializedName("ie_update_user") var ieUpdateUser: String?,
    @SerializedName("ie_update_date") var ieUpdateDate: String?
) {
    override fun toString(): String {
        return "TarikBarangModel(remainingDays=$remainingDays ieId=$ieId, ieStoreCode=$ieStoreCode, ieGondolaNo=$ieGondolaNo, ieBarcode=$ieBarcode, ieItemCode=$ieItemCode, ieItemName=$ieItemName, ieItemStatus=$ieItemStatus, ieExpiredDate=$ieExpiredDate, ieQty=$ieQty, ieAction=$ieAction, ieInsertUser=$ieInsertUser, ieInsertDate=$ieInsertDate, ieUpdateUser=$ieUpdateUser, ieUpdateDate=$ieUpdateDate)"
    }
}
