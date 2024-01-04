package com.example.audire.shazam.models


import com.alexmercerind.audire.api.shazam.models.Track
import com.google.gson.annotations.SerializedName

data class ShazamResponse(
    @SerializedName("track")
    val track: Track?
)
