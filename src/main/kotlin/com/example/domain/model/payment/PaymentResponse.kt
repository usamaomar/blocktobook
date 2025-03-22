package com.example.domain.model.payment

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val clientSecret: String,
    val id: String
) : Principal
