package com.example.domain.model.transactionModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateTransaction(
    val userId: String? = null
): Principal