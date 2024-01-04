package com.example.audire.data

import com.example.audire.models.Music

interface IdentifyDataSource
{
    fun identify(data: ByteArray, duration: Int): Music?
}
