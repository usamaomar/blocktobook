package com.example.domain.model.hotelTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateHotelTicketModel(
    val hotelId: String,
    val fromDate: Long,
    val toDate: Long,
    val hotelTicketInfoList: ArrayList<HotelTicketInfoCreateModel>
) : Principal


//fun CreateHotelTicketModel.toHotelTicketModel(userId: String): HotelTicketModel {
//    return HotelTicketModel(
//        hotelId = this.hotelId,
//        pricePerNight = this.price,
//        fromDate = this.reservationDate,
//        toDate = this.checkOutDate,
//        roomClass = this.roomClass,
//        userId = userId,
//        isVisible = isVisible,
//        numberOfRoomsPerNight = quantity,
//        transportation = transportation,
//        ticketNumber = null,
//        numberOfAdultsAllowance = numberOfAdults,
//        numberOfChildrenAllowance = numberOfChildren,
//        childrenAge = childrenAge,
//    )
//}
