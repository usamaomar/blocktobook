package com.example.domain.model.hotelModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateHotelModel(
    val id: String,
    val profiles : List<HotelProfileModel>,
    val latitude : Double,
    val longitude : Double,
    val stars : Double,
    val cityId : String,
    val logo: String,
) : Principal
