package com.example.audire.repository

import com.example.audire.data.IdentifyDataSource

class IdentifyRepository(private val source: IdentifyDataSource) {
     fun identify(data: ByteArray, duration: Int) = source.identify(data, duration)
}
