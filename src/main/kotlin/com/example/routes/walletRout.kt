package com.example.routes

import com.example.data.repository.authDataSource.isTimestampExpired
import com.example.data.repository.cartDataSource.CartDataSource
import com.example.data.repository.sendGrid.SendGridDataSource
import com.example.data.repository.userDataSource.UserDataSource
import com.example.data.repository.walletDataSource.TransactionDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.transactionModel.CreateTransaction
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.TopUpType
import com.example.util.TransactionType
import com.example.util.paramNames
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent


private const val errorCode: Int = 10

fun Route.walletRoute() {
    val transactionDataSource: TransactionDataSource by KoinJavaComponent.inject(TransactionDataSource::class.java)
    val cartDataSource: CartDataSource by KoinJavaComponent.inject(CartDataSource::class.java)
    val userDataSource: UserDataSource by KoinJavaComponent.inject(UserDataSource::class.java)
    val sendGridDataSource: SendGridDataSource by KoinJavaComponent.inject(SendGridDataSource::class.java)

    post(Api.Wallet.TopUpAdminCart.path) {
        try {
            val request = call.receiveModel<CreateTransaction>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val chargerId = decodedPayload["userId"] ?: ""
            val user = userDataSource.getUserInfo(request.userId ?: "")


            if(user?.companyInfo == null){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("companyInfo is is not added"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }

            if(!user.companyInfo.isCompanyInfoVerified){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("companyInfo is is not Verified"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }

            if(user.subscription == null){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("subscription is not added"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }
            val blockToBookFees = user.companyInfo.blockToBookFees
            val pagingApiResponse = transactionDataSource.topUpWallet(
                userId = request.userId ?: "",
                amount = request.topUpAmount ?: 0.0,
                chargerId =chargerId ,
                blockToBookFees = blockToBookFees,
                transactionType = TransactionType.PLUS.ordinal,
                topUpType = TopUpType.CART.ordinal
            )
            call.respond(
                message = pagingApiResponse ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }


    post(Api.Wallet.TopDownAdminCart.path) {
        try {
            val request = call.receiveModel<CreateTransaction>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val chargerId = decodedPayload["userId"] ?: ""
            val user = userDataSource.getUserInfo(request.userId ?: "")


            if(user?.companyInfo == null){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("companyInfo is is not added"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }

            if(!user.companyInfo.isCompanyInfoVerified){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("companyInfo is is not Verified"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }

            if(user.subscription == null){
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("subscription is not added"),
                        data = null, errorCode = errorCode), status = HttpStatusCode.ExpectationFailed)
                return@post
            }
            val blockToBookFees = user.companyInfo.blockToBookFees
            val pagingApiResponse = transactionDataSource.topDownWallet(
                userId = request.userId ?: "",
                amount = request.topUpAmount ?: 0.0,
                chargerId =chargerId ,
                blockToBookFees = blockToBookFees,
                transactionType = TransactionType.MINUS.ordinal,
                topUpType = TopUpType.CART.ordinal
            )
            call.respond(
                message = pagingApiResponse ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

    post(Api.Wallet.TopUpRequestEmail.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = transactionDataSource.getWalletAmountByUserId(
                userId = userId
            )
            val wallet =  pagingApiResponse?.data ?: "0.0"
            val sendgrid = sendGridDataSource.sendEmailUsingSendGrid(userId,wallet)
            call.respond(
                message = sendgrid
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

    get(Api.Wallet.GetAllWalletsByUserId.path) {
        try {
            val pagingApiResponse = transactionDataSource.getAllWalletsByUserId(
                userId = call.parameters[paramNames.Id] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0
            )
            call.respond(
                message = pagingApiResponse ?: PagingApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    get(Api.Wallet.GetWalletAmountByUserId.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = transactionDataSource.getWalletAmountByUserId(
                userId = userId
            )
            call.respond(
                message = pagingApiResponse ?:  ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    get(Api.Wallet.GetWalletAmountByAdmin.path) {
        try {
            val pagingApiResponse = transactionDataSource.getWalletAmountByUserId(
                userId = call.parameters[paramNames.Id] ?: ""
            )
            call.respond(
                message = pagingApiResponse ?:  ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }


}