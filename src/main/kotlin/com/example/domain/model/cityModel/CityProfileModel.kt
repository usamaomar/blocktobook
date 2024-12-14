package com.example.domain.model.cityModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CityProfileModel(val languageId: Int, val name: String, val countryName: String) :
    Principal