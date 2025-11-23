package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

data class FutureMailRequest(
    @SerializedName("writeDate") val writeDate: String,
    @SerializedName("receiveDate") val receiveDate: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("content") val content: String
)

data class FutureMailResponse(
    val id: String? = null,
    val status: String? = null,
    val message: String? = null
)
