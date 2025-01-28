package com.example.domain.model.purchaseModel

import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class ResponsePurchasedHotelTicketModel(
    val id: String? = null,
    val hotelTicketModel: ResponseHotelTicketModel ? =null,
    val airLineModel: ResponseAirlineTicketModel? = null,
    val returnAirLineModel: ResponseAirlineTicketModel? =null,
    val userId: String,
    val checkoutId: String,
    val status: Int?=null,
    val checkInDate: Long? = null,
    val checkOutDate: Long? = null,
    val note: String? = null,
    val customerModel: CustomerModel? = null,
    val airLineCustomerModels: List<CustomerModel>? = null,
    val createdAt: Long? =0,
    val numberOfRooms: Int,
    val numberOfInfants: Int ? = null
): Principal

