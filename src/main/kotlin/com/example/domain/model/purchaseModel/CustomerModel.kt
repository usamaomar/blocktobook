package com.example.domain.model.purchaseModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CustomerModel(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val passportUrl: String? = null,
    val visaUrl: String? = null,
    val note: String? = null,
    val isInfant: Boolean? = false,
) : Principal