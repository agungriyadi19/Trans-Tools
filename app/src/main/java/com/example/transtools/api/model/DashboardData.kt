package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("item_listed_today") val itemListedToday: String,
    @SerializedName("total_items_listed") val totalItemsListed: String,
    @SerializedName("item_withdrawn_today") val itemWithdrawnToday: String,
    @SerializedName("total_items_withdrawn") val totalItemsWithdrawn: String
)
