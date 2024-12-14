package com.example.domain.model.transactionModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
@Serializable
data class TransactionModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val userId : String,
    val amount : Double,
    val blockToBookFees : Double,
    val chargerId : String,
    val checkoutId : String?=null,
    val topUpType : Int,
    val transactionType : Int,
    val createdDate : Long,
    val subscriptionType : Int ? =null,
): Principal
