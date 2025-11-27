package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

data class MailsResponse(
    val id: Int,
    val subject: String,
    val content: String,
    @SerializedName("write_date") val writeDate: String,
    @SerializedName("receive_date") val receiveDate: String,
    val email: String? // email 可以是 null
)
