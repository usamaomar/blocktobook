package com.example.domain.model.hotelTicketModel

import com.example.domain.model.hotelModel.ResponseHotelModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
@Serializable
data class ResponseHotelTicketModel(
    val id: String? = null,
    val hotel: ResponseHotelModel? = null,
    val pricePerNight: Double,
    val ticketNumber: String? = null,
    val roomId: String? = null,
    val userId: String? = null,
    val roomCategory: Int? = 0,
    val fromDate: Long,
    val toDate: Long,
    val roomClass: Int,
    val transportation: Int ? = null,
    val numberOfRoomsPerNight: Int ? = null,
    val isVisible: Boolean? = true,
    val numberOfAdultsAllowance: Int,
    val numberOfChildrenAllowance: Int,
    val childrenAge: Int,
): Principal

