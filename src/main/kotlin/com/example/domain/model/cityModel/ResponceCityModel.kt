package com.example.domain.model.cityModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCityModel(
    val id: String,
    val name: String,
    val countryName: String,
    val twoDigitCountryCode: String,
    val threeDigitCountryCode: String,
    val profiles : List<CityProfileModel>? = null,
) : Principal
