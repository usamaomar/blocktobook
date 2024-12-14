package com.example.domain.model.airlinesModel

import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAirLineModel(
    val id: String,
    val name: String,
    val logo: String,
    val price: Double? = null,
    val profiles : List<AirlineProfileModel>? = null,
    val referenceName: String? = null,
    val cityId: String? = null,
    val city: ResponseCityModel? = null,
    val code: String,
) : Principal


