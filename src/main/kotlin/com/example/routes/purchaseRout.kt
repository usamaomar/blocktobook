package com.example.routes

import com.example.data.repository.purchaseDataSource.PurchaseDataSource
import com.example.domain.model.payment.CreatePaymentModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.CreateCustomerListModel
import com.example.domain.model.purchaseModel.CreateCustomerModel
import com.example.domain.model.transactionModel.CreateAction
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.receiveModel
import com.example.util.toSafeBoolean
import com.example.util.toSafeDouble
import com.example.util.toSafeInt
import com.example.util.toSafeLong
import com.example.util.toSafeString
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent


private const val errorCode: Int = 959

fun Route.purchaseRout() {

    val purchaseDataSource: PurchaseDataSource by KoinJavaComponent.inject(PurchaseDataSource::class.java)

    post(Api.Purchase.Checkout.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val purchaseData = purchaseDataSource.checkOut(
                userId = decodedPayload["userId"] ?: ""
            )
            call.respond(
                message = purchaseData
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
    post(Api.Purchase.CheckoutSubscription.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val request  = call.receiveModel<CreatePaymentModel>()
            val purchaseData = purchaseDataSource.checkOutSubscription(
                userId = decodedPayload["userId"] ?: "",request.id?:""
            )
            call.respond(
                message = purchaseData
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

    post(Api.Purchase.ApproveHotelReservation.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val requestReservationId = call.receiveModel<CreateAction>()
            val purchaseData = purchaseDataSource.approveHotelReservation(
                userId = decodedPayload["userId"] ?: "",
                purchasedId = requestReservationId.purchasedId ?: "",
                ticketNumber = requestReservationId.ticketNumber ?: "",
            )
            call.respond(
                message = purchaseData
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
    post(Api.Purchase.RejectHotelReservation.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val requestReservationId = call.receiveModel<CreateAction>()
            val purchaseData = purchaseDataSource.rejectHotelReservation(
                userId = decodedPayload["userId"] ?: "",
                purchasedId = requestReservationId.purchasedId ?: "",
                note = requestReservationId.note ?: "",
            )
            call.respond(
                message = purchaseData
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
    post(Api.Purchase.CancelHotelReservation.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val requestReservationId = call.receiveModel<CreateAction>()
            val purchaseData = purchaseDataSource.cancelHotelReservation(
                userId = decodedPayload["userId"] ?: "",
                purchasedId = requestReservationId.purchasedId ?: "",
                note = requestReservationId.note ?: "",
            )
            call.respond(
                message = purchaseData
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

    post(Api.Purchase.CreateOrUpdateCustomerInfo.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val request = call.receiveModel<CreateCustomerModel>()
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val purchaseData = purchaseDataSource.createOrUpdateCustomerInfo(
                userId = decodedPayload["userId"] ?: "", request
            )
            call.respond(
                message = purchaseData
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

    post(Api.Purchase.CreateOrUpdateCustomerInfoList.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val request = call.receiveModel<CreateCustomerListModel>()
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val purchaseData = purchaseDataSource.createOrUpdateCustomerInfoList(
                userId = decodedPayload["userId"] ?: "", request
            )
            call.respond(
                message = purchaseData
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

    get(Api.Purchase.GetAllHotelReservations.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = purchaseDataSource.getAll(
                userId = userId,
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                status = call.request.headers[paramNames.status]?.toSafeInt(),
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo = call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByVisibility = call.parameters[paramNames.FilterByVisibility]?.let { it.toSafeBoolean() },
                filterByPriceRangeFrom = call.parameters[paramNames.FilterByPriceRangeFrom]?.toSafeDouble(),
                filterByPriceRangeTo = call.parameters[paramNames.FilterByPriceRangeTo]?.toSafeDouble(),
                filterByHotelIds = call.parameters[paramNames.FilterByHotelIds].toSafeString()
                    ?.split(",")?.map { it },
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

    get(Api.Purchase.GetAllForAirlines.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = purchaseDataSource.getAllForAirlines(
                userId = userId,
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                status = call.request.headers[paramNames.status]?.toSafeInt(),
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo = call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByVisibility = call.parameters[paramNames.FilterByVisibility]?.let { it.toSafeBoolean() },
                filterByPriceRangeFrom = call.parameters[paramNames.FilterByPriceRangeFrom]?.toSafeDouble(),
                filterByPriceRangeTo = call.parameters[paramNames.FilterByPriceRangeTo]?.toSafeDouble(),
                filterByHotelIds = call.parameters[paramNames.FilterByHotelIds].toSafeString()
                    ?.split(",")?.map { it },
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }


    get(Api.Purchase.GetAllMerchantAirlineReservations.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = purchaseDataSource.getAllMerchantAirlineReservations(
                userId = userId,
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                status = call.request.headers[paramNames.status]?.toSafeInt(),
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo = call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByVisibility = call.parameters[paramNames.FilterByVisibility]?.let { it.toSafeBoolean() },
                filterByPriceRangeFrom = call.parameters[paramNames.FilterByPriceRangeFrom]?.toSafeDouble(),
                filterByPriceRangeTo = call.parameters[paramNames.FilterByPriceRangeTo]?.toSafeDouble(),
                filterByHotelIds = call.parameters[paramNames.FilterByHotelIds].toSafeString()
                    ?.split(",")?.map { it },
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

    get(Api.Purchase.GetAllMerchantHotelReservations.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = purchaseDataSource.getAllMerchantHotelReservations(
                userId = userId,
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                status = call.request.headers[paramNames.status]?.toSafeInt(),
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo = call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByVisibility = call.parameters[paramNames.FilterByVisibility]?.let { it.toSafeBoolean() },
                filterByPriceRangeFrom = call.parameters[paramNames.FilterByPriceRangeFrom]?.toSafeDouble(),
                filterByPriceRangeTo = call.parameters[paramNames.FilterByPriceRangeTo]?.toSafeDouble(),
                filterByHotelIds = call.parameters[paramNames.FilterByHotelIds].toSafeString()
                    ?.split(",")?.map { it },
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }

}
