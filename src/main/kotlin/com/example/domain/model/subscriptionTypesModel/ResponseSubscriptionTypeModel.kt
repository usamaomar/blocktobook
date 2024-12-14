package com.example.domain.model.subscriptionTypesModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseSubscriptionTypeModel(
    val id: String?, val type: Int, val name: String?, val description: String?, val price: Double
) : Principal
