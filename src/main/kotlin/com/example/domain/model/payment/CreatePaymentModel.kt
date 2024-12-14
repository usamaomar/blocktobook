package com.example.domain.model.payment

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreatePaymentModel(
    val resourcePath: String,
    val id: String,
    val includeAmount: Boolean? = null
) : Principal
