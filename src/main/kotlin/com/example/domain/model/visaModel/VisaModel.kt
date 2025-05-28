package com.example.domain.model.visaModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class VisaModel(
    val userId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val middleName: String? = null,
) : Principal