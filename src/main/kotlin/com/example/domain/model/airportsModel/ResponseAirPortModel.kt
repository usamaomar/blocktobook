package com.example.domain.model.airportsModel

import com.example.domain.model.airlinesModel.AirlineProfileModel
import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAirPortModel(
    val id: String,
    val name: String,
    val profiles : List<AirPortProfileModel>? = null,
    val referenceName: String? = null,
    val cityId: String? = null,
    val city: ResponseCityModel? = null,
    val code: String?
) : Principal


