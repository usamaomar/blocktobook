package com.example.routes

import com.example.data.repository.SubscriptionTypesDataSource.SubscriptionTypesDataSource
import com.example.data.repository.cartDataSource.CartDataSource
import com.example.data.repository.cartDataSource.formatAmount
import com.example.data.repository.paymentDataSource.PaymentDataSource
import com.example.data.repository.purchaseDataSource.PurchaseDataSource
import com.example.data.repository.userDataSource.UserDataSource
import com.example.data.repository.walletDataSource.TransactionDataSource
import com.example.domain.model.payment.CreatePaymentIncludeAmount
import com.example.domain.model.payment.CreatePaymentModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.transactionModel.TransactionModel
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.TopUpType
import com.example.util.TransactionType
import com.example.util.receiveModel
import com.example.util.toSafeDouble
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.mongodb.client.model.Filters
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.json
import org.litote.kmongo.util.idValue
import kotlin.reflect.full.memberProperties


private const val errorCode: Int = 64143


val client = HttpClient(CIO) // Or another engine

@OptIn(InternalAPI::class)
fun Route.paymentRout() {
    val paymentDataSource: PaymentDataSource by KoinJavaComponent.inject(PaymentDataSource::class.java)
    val transactionDataSource: TransactionDataSource by KoinJavaComponent.inject(
        TransactionDataSource::class.java
    )
    val subscriptionTypeModel: SubscriptionTypesDataSource by KoinJavaComponent.inject(
        SubscriptionTypesDataSource::class.java
    )
    val cartDataSource: CartDataSource by KoinJavaComponent.inject(CartDataSource::class.java)
    val userDataSource: UserDataSource by KoinJavaComponent.inject(UserDataSource::class.java)
    val purchaseDataSource: PurchaseDataSource by KoinJavaComponent.inject(PurchaseDataSource::class.java)
    post(Api.Payment.CreateCartCheckout.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val request = call.receiveModel<CreatePaymentIncludeAmount>()
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val paymentUrl = "https://eu-test.oppwa.com/v1/checkouts"
            val authorizationBearer =
                "Bearer OGE4Mjk0MTc0ZDA1OTViYjAxNGQwNWQ4MjllNzAxZDF8OVRuSlBjMm45aA=="
            val result = withContext(Dispatchers.IO) {
                val amounts: String = if (request.includeAmount) {
                    cartDataSource.getAmountWithCurrentWalletAmountWithBlockFees(
                        userId,
                        transactionDataSource.getWalletAmountByUserId(userId)?.data.toSafeDouble()
                            ?: 0.0
                    )
                } else {
                    cartDataSource.getAmountWithBlockFees(userId)
                }
                val parameters = mutableListOf(
                    "entityId" to "8a8294174d0595bb014d05d829cb01cd",
                    "currency" to "USD",
                    "paymentType" to "DB",
                    "integrity" to "true",
                    "amount" to amounts,
                )
                paymentUrl.httpPost(parameters)
                    .header("Authorization" to authorizationBearer)
                    .header("Content-Type" to "application/x-www-form-urlencoded")
                    .responseString()
            }
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    message = arrayListOf("paymentRedirectUrl"),
                    data = result.third.get(), errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }


    post(Api.Payment.CreateSubscriptionCheckout.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val request = call.receiveModel<CreatePaymentIncludeAmount>()
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val paymentUrl = "https://eu-test.oppwa.com/v1/checkouts"
            val authorizationBearer =
                "Bearer OGE4Mjk0MTc0ZDA1OTViYjAxNGQwNWQ4MjllNzAxZDF8OVRuSlBjMm45aA=="
            val amounts: String
            val result = withContext(Dispatchers.IO) {
                amounts = if (request.includeAmount) {
                  cartDataSource.getAmountWithCurrentWalletAmountWithSubscription(
                        userId,
                        transactionDataSource.getWalletAmountByUserId(userId)?.data.toSafeDouble()
                            ?: 0.0, request.subscriptionTypeId ?: ""
                    )
                } else {
                     cartDataSource.getAmountWithSubscriptionFees(userId,request.subscriptionTypeId ?: "")
                }
                val parameters = mutableListOf(
                    "entityId" to "8a8294174d0595bb014d05d829cb01cd",
                    "currency" to "USD",
                    "paymentType" to "DB",
                    "integrity" to "true",
                    "amount" to amounts,
                )
                paymentUrl.httpPost(parameters)
                    .header("Authorization" to authorizationBearer)
                    .header("Content-Type" to "application/x-www-form-urlencoded")
                    .responseString()
            }
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    message = arrayListOf("paymentRedirectUrl"),
                    data = result.third.get(), errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

    post(Api.Payment.GetPaymentStatus.path) {
        try {
            val request = call.receiveModel<CreatePaymentModel>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val paymentUrl = "https://eu-test.oppwa.com${request.resourcePath}"
            val authorizationBearer =
                "Bearer OGE4Mjk0MTc0ZDA1OTViYjAxNGQwNWQ4MjllNzAxZDF8OVRuSlBjMm45aA=="
            val result = withContext(Dispatchers.IO) {
                val parameters = mutableListOf(
                    "entityId" to "8a8294174d0595bb014d05d829cb01cd"
                )
                paymentUrl.httpGet(parameters)
                    .header("Authorization" to authorizationBearer)
                    .header("Content-Type" to "application/x-www-form-urlencoded")
                    .responseString()
            }
//            val statsCode = extractCode(result.third.get())
//            if (statsCode == "800.100.156") {
                val amounts: String = if (request.includeAmount == true) {
                    cartDataSource.getAmountWithCurrentWalletAmountWithBlockFees(
                        userId,
                        transactionDataSource.getWalletAmountByUserId(userId)?.data.toSafeDouble()
                            ?: 0.0
                    )
                } else {
                    cartDataSource.getAmountWithBlockFees(userId)
                }
                val blockToBookFees =
                    userDataSource.getUserInfo(userId)?.companyInfo?.blockToBookFees
                val respond = paymentDataSource.createCheckout(
                    userId,
                    amounts.toSafeDouble() ?: 0.0,
                    blockToBookFees ?: 0.0
                )
                if (respond?.data == true) {
                    val finalResponse = purchaseDataSource.checkOut(userId)
                    if (finalResponse.data != "Error") {
                        call.respond(
                            message = ApiResponse(
                                succeeded = true,
                                message = arrayListOf("paymentRedirectUrl"),
                                data = "result.third.get()", errorCode = errorCode
                            )
                        )
                    } else {
                        call.respond(
                            message = ApiResponse(
                                succeeded = false,
                                message = arrayListOf("paymentRedirectUrl"),
                                data = "result.third.get()", errorCode = errorCode
                            )
                        )
                    }


//                } else {
//                    call.respond(
//                        message = ApiResponse(
//                            succeeded = false,
//                            message = arrayListOf("paymentRedirectUrl"),
//                            data = result.third.get(), errorCode = errorCode
//                        )
//                    )
//                }
            } else {
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("paymentRedirectUrl"),
                        data = result.third.get(), errorCode = errorCode
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }


    post(Api.Payment.GetSubscriptionPaymentStatus.path) {
        try {
            val request = call.receiveModel<CreatePaymentModel>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val paymentUrl = "https://eu-test.oppwa.com${request.resourcePath}"
            val authorizationBearer =
                "Bearer OGE4Mjk0MTc0ZDA1OTViYjAxNGQwNWQ4MjllNzAxZDF8OVRuSlBjMm45aA=="
//            val result = withContext(Dispatchers.IO) {
//                val parameters = mutableListOf(
//                    "entityId" to "8a8294174d0595bb014d05d829cb01cd"
//                )
//                paymentUrl.httpGet(parameters)
//                    .header("Authorization" to authorizationBearer)
//                    .header("Content-Type" to "application/x-www-form-urlencoded")
//                    .responseString()
//            }
//            val statsCode = extractCode(result.third.get()) //todo update this
//            if (statsCode == "800.100.156") {
               val amounts = if (request.includeAmount == true) {
                    cartDataSource.getAmountWithCurrentWalletAmountWithSubscription(
                        userId,
                        transactionDataSource.getWalletAmountByUserId(userId)?.data.toSafeDouble()
                            ?: 0.0, request.id ?: ""
                    )
                } else {
                    cartDataSource.getAmountWithSubscriptionFees(userId,request.id ?: "")
                }
                paymentDataSource.createCheckout(
                    userId,
                    amounts.toSafeDouble() ?: 0.0,
                      0.0
                )
                val finalResponse = purchaseDataSource.checkOutSubscription(userId,request.id)
                if (finalResponse.data != "Error") {
                    call.respond(
                        message = ApiResponse(
                            succeeded = true,
                            message = arrayListOf("paymentRedirectUrl"),
                            data = "result.third.get()", errorCode = errorCode
                        )
                    )
                } else {
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf("paymentRedirectUrl"),
                            data = "result.third.get()", errorCode = errorCode
                        )
                    )
                }
//            } else {
//                call.respond(
//                    message = ApiResponse(
//                        succeeded = false,
//                        message = arrayListOf("paymentRedirectUrl"),
//                        data = result.third.get(), errorCode = errorCode
//                    )
//                )
//            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
}


fun extractCode(jsonString: String): String? {
    // Parse the JSON string into the PaymentResponse object
    val jsonElement = Json.parseToJsonElement(jsonString)

    // Extract the "code" field from the "result" object
    val code = jsonElement.jsonObject["result"]
        ?.jsonObject?.get("code")
        ?.jsonPrimitive?.content

    return code
}

fun stringToMap(jsonString: String): Map<String, Any?> {
    val jsonElement = Json.parseToJsonElement(jsonString)

    // Parse it to a JSON object and convert to map
    return jsonElement.jsonObject.toMap()
}

fun JsonObject.toMap(): Map<String, Any?> {
    return this.mapValues { entry ->
        when (val value = entry.value) {
            is JsonObject -> value.toMap()  // Recursively convert JSON objects
            else -> value.toString()        // Convert primitive types to string
        }
    }
}

