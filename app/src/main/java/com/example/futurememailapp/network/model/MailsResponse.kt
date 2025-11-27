package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

data class MailsResponse(
    val id: String,
    val subject: String,
    val content: String,
    val writeDate: String,
    val receiveDate: String,
    val email: String? // email 可以是 null
)
