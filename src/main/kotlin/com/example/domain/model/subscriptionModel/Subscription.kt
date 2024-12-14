package com.example.domain.model.subscriptionModel

import com.example.util.SubscriptionType
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val type: Int,
    val subscriptionDate: Long? =0 ,
    val expirationDate: Long? =0 ,
    val createdAt: Long? =0
) : Principal