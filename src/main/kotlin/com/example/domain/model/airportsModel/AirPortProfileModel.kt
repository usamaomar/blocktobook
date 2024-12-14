package com.example.domain.model.airportsModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class AirPortProfileModel(val languageId: Int, val name: String) : Principal