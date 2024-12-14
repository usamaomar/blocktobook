package com.example.domain.model.purchaseModel

import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class PurchaseModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val hotelTicketModel: ResponseHotelTicketModel?,
    val airLineModel: ResponseAirlineTicketModel?,
    val returnAirLineModel: ResponseAirlineTicketModel?,
    val userId: String,
    val checkoutId: String,
    val note: String?=null,
    val status: Int?=null,
    val checkInDate: Long? = null,
    val checkOutDate: Long? = null,
    val customerModel: CustomerModel? = null,
    val airLineCustomerModels: List<CustomerModel>? = null,
    val createdAt: Long? =0,
    val numberOfRooms: Int
): Principal
