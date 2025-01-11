package com.example.domain.model.hotelTicketModel

import com.example.domain.model.hotelModel.ResponseHotelModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class HotelTicketModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val hotelId: String,
    val ticketNumber: String? = null,
    val pricePerNight: Double,
    val fromDate: Long,
    val toDate: Long,
    val roomClass: Int,
    val roomCategory: Int? = 0,
    val transportation: Int? = null,
    val userId: String,
    val isVisible: Boolean? = true,
    val numberOfRoomsPerNight: Int? = 1,
    val numberOfAdultsAllowance: Int,
    val numberOfChildrenAllowance: Int,
    val childrenAge: Int,
) : Principal


fun HotelTicketModel.toResponseHotelTicketModel(_id: String, hotel: ResponseHotelModel? =null): ResponseHotelTicketModel {
    return ResponseHotelTicketModel(
        id = _id,
        pricePerNight = this.pricePerNight,
        fromDate = this.fromDate,
        toDate = this.toDate,
        roomClass = this.roomClass,
        roomCategory = this.roomCategory,
        isVisible = isVisible,
        hotel = hotel,
        userId = this.userId,
        transportation = transportation,
        ticketNumber = ticketNumber,
        numberOfRoomsPerNight = numberOfRoomsPerNight,
        numberOfAdultsAllowance = numberOfAdultsAllowance,
        numberOfChildrenAllowance = numberOfChildrenAllowance,
        childrenAge = childrenAge,

    )
}
fun HotelTicketModel.toResponseHotelTicketWithHotelModel(_id: String, hotel: ResponseHotelModel?): ResponseHotelTicketModel {
    return ResponseHotelTicketModel(
        id = _id,
        userId = this.userId,
        hotel = hotel,
        pricePerNight = this.pricePerNight,
        fromDate = this.fromDate,
        toDate = this.toDate,
        roomClass = this.roomClass,
        roomCategory = this.roomCategory,
        isVisible = isVisible,
        transportation = transportation,
        ticketNumber = ticketNumber,
        numberOfRoomsPerNight = numberOfRoomsPerNight,
        numberOfAdultsAllowance = numberOfAdultsAllowance,
        numberOfChildrenAllowance = numberOfChildrenAllowance,
        childrenAge = childrenAge,
    )
}
