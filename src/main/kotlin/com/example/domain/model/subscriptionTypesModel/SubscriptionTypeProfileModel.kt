package com.example.domain.model.subscriptionTypesModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionTypeProfileModel(
    val languageId: Int, val name: String, val description: String
) : Principal

