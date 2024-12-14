package com.example.domain.model.hotelModel


import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseHotelModel(
    val id: String,
    val name: String,
    val profiles : List<HotelProfileModel>? = null,
    val latitude : Double,
    val longitude : Double,
    val stars : Double,
    val city : ResponseCityModel? = null,
    val logo: String,
) : Principal
