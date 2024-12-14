package com.example.domain.model.walletAmountModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class WalletAmountModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val userId : String,
    val amount : Double
): Principal