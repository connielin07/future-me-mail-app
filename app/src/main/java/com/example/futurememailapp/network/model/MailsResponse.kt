package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

data class MailsResponse(
    val id: String,
    val subject: String,
    val content: String,
    val writeDate: String,
    val receiveDate: String,
    val email: String? = null,
    @SerializedName("deviceToken") val deviceToken: String? = null,
    @SerializedName("delivered") val delivered: Int? = null,
    @SerializedName("deliveredAt") val deliveredAt: String? = null,
    @SerializedName("createdAt") val createdAt: String
)
