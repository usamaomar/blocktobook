package com.example.domain.model.cartModel

import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateCartModel(
    val hotelTicketId: String?,
    val airLineTripId: String?,
    val returnAirLineTripId: String?,
    val checkInDate: Long,
    val checkOutDate: Long,
    val numberOfRooms: Int) : Principal


fun CreateCartModel.toCartModel(
    userId: String,
    hotelTicketModel: ResponseHotelTicketModel?,
    airLineTicketModel: ResponseAirlineTicketModel?,
    returnAirLineTicketModel: ResponseAirlineTicketModel?
): CartModel {
    return CartModel(
        hotelTicketModel = hotelTicketModel,
        airLineTicketModel = airLineTicketModel,
        returnAirLineTicketModel = returnAirLineTicketModel,
        userId = userId,
        checkInDate = this.checkInDate,
        checkOutDate = this.checkOutDate,
        numberOfRooms = this.numberOfRooms,
    )
}


fun CreateCartModel.toResponseCartModel(
    userId: String,
    hotelTicketModel: ResponseHotelTicketModel?,
    airLineModel: ResponseAirlineTicketModel?,
): ResponseCartModel {
    return ResponseCartModel(
        hotelTicketModel = hotelTicketModel,
        airLineModel = airLineModel,
        userId = userId,
        checkInDate = this.checkInDate,
        checkOutDate = this.checkOutDate,
        numberOfRooms = this.numberOfRooms
    )
}