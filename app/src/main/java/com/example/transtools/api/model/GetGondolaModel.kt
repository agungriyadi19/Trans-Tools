package com.example.transtools.api.model

import com.google.gson.annotations.SerializedName


class GetGondolaModel(
    @SerializedName("id") var id: String?,
    @SerializedName("gondola") var gondola: String?
) {
    override fun toString(): String {
        return "GetGondolaModel(id=$id, gondola=$gondola)"
    }
}
