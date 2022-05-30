package com.example.airpollutionpublicapi.stationData


import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("response")
    val response: Response?
)