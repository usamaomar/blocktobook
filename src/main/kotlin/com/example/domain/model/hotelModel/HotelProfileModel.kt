package com.example.domain.model.hotelModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class HotelProfileModel(val languageId: Int, val name: String) :
    Principal