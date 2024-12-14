package com.example.domain.model.subscriptionTypesModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponsePostSubscriptionTypeModel(
    val id: String, val type: Int,  val profiles: List<SubscriptionTypeProfileModel>, val price: Double
) : Principal
