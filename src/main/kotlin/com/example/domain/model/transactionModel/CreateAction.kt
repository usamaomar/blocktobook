package com.example.domain.model.transactionModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateAction(
    val purchasedId: String? = null,
    val ticketNumber: String? = null,
    val note: String? = null,
): Principal