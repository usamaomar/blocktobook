package com.example.domain.model.transactionModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
@Serializable
data class ResponseTransactionModel(
    val id : String,
    val userId : String,
    val amount : Double,
    val chargerId : String,
    val chargerName : String,
    val createdDate : Long,
    val transactionType : Int,
    val checkoutId : String?=null,
    val blockToBookFees : Double,
    val topUpType : Int,
): Principal


