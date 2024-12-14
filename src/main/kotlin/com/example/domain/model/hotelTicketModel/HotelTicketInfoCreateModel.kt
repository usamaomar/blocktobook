package com.example.domain.model.hotelTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class HotelTicketInfoCreateModel(
    val roomClass: Int,
    val isVisible: Boolean? = true,
    val numberOfRoomsPerNight: Int ? = 1,
    val transportation: Int ? = null,
    val numberOfAdultsAllowance: Int,
    val numberOfChildrenAllowance: Int,
    val pricePerNight: Double,
    val childrenAge: Int,
) : Principal
