package com.example.domain.model.airlinesModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class AirlineProfileModel(val languageId: Int, val name: String) : Principal