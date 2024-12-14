package com.example.domain.model.subscriptionTypesModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionTypeModel(
    val type: Int,
    val profiles: List<SubscriptionTypeProfileModel>,
    val price: Double
) : Principal


fun CreateSubscriptionTypeModel.toSubscriptionTypeModel(): SubscriptionTypeModel {
    return SubscriptionTypeModel(
        type = this.type,
        profiles = this.profiles,
        price = this.price,
    )
}