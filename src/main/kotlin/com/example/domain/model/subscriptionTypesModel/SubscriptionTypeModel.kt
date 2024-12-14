package com.example.domain.model.subscriptionTypesModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class SubscriptionTypeModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val type: Int,
    val profiles: List<SubscriptionTypeProfileModel>,
    val price: Double
) : Principal



fun SubscriptionTypeModel.toResponseSubscriptionTypeModel(_id: String?,xAppLanguageId : Int): ResponseSubscriptionTypeModel {
    return ResponseSubscriptionTypeModel(
        id = _id,
        type = type,
        name = this.profiles.find { it.languageId == xAppLanguageId }?.name,
        description = this.profiles.find { it.languageId == xAppLanguageId }?.description,
        price = this.price
    )
}


fun SubscriptionTypeModel.toResponsePostSubscriptionTypeModel(_id: String): ResponsePostSubscriptionTypeModel {
    return ResponsePostSubscriptionTypeModel(
        id = _id,
        type = type,
        profiles = this.profiles,
        price = this.price
    )
}