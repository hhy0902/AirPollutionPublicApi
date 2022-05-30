package com.example.airpollutionpublicapi.stationData


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("addr")
    val addr: String?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("tm")
    val tm: Double?
)