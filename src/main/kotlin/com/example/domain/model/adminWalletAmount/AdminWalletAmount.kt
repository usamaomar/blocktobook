package com.example.domain.model.adminWalletAmount

import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
@Serializable
data class AdminWalletAmount(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val merchantId : String,
    val amount : Double,
    val checkoutId : String,
    val createdDate : Long,
): Principal