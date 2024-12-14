package com.example.domain.model.payment

import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreatePaymentIncludeAmount(
    val includeAmount: Boolean,
    val subscriptionTypeId: String? = null,
) : Principal