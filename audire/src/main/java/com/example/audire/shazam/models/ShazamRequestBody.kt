package com.example.audire.shazam.models


import com.alexmercerind.audire.api.shazam.models.Geolocation
import com.alexmercerind.audire.api.shazam.models.Signature
import com.google.gson.annotations.SerializedName

data class ShazamRequestBody(
    @SerializedName("geolocation")
    val geolocation: Geolocation,
    @SerializedName("signature")
    val signature: Signature,
    @SerializedName("timestamp")
    val timestamp: Int,
    @SerializedName("timezone")
    val timezone: String
)
