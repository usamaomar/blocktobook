package com.example.domain.model.cartModel

import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.purchaseModel.PurchaseModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCartModel(
    val id: String? = null,
    val hotelTicketModel: ResponseHotelTicketModel? = null,
    val airLineModel: ResponseAirlineTicketModel? = null,
    val returnAirLineTicketModel: ResponseAirlineTicketModel? = null,
    val userId: String,
    val checkInDate: Long,
    val checkOutDate: Long,
    val numberOfRooms: Int,
) : Principal



fun ResponseCartModel.toPurchaseModel(createdAt: Long,checkoutId : String): PurchaseModel {
    return PurchaseModel(
        hotelTicketModel = hotelTicketModel,
        returnAirLineModel = returnAirLineTicketModel,
        airLineModel = airLineModel,
        userId = userId,
        createdAt = createdAt,
        checkInDate = checkInDate,
        checkOutDate = checkOutDate,
        checkoutId = checkoutId,
        numberOfRooms = numberOfRooms
    )
}


fun ResponseCartModel.toCartModel(): CartModel {
    return CartModel(
        hotelTicketModel = hotelTicketModel,
        airLineTicketModel = airLineModel,
        returnAirLineTicketModel = returnAirLineTicketModel,
        userId = userId,
        checkInDate = checkInDate,
        checkOutDate = checkOutDate,
        numberOfRooms = numberOfRooms
    )
}