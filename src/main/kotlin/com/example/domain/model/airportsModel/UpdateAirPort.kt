package com.example.domain.model.airportsModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAirPort(
    val id: String,
    val profiles : List<AirPortProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String,
) : Principal


