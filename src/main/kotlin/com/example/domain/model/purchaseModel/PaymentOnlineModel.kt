package com.example.domain.model.purchaseModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class PaymentOnlineModel(
    val id: String? = null
): Principal