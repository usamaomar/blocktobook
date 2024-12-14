package com.example.domain.model.cityModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCityModel(
    val id: String,
    val profiles : List<CityProfileModel>,
    val twoDigitCountryCode: String,
    val threeDigitCountryCode: String
) : Principal
