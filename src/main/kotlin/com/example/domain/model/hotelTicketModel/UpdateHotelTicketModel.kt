package com.example.domain.model.hotelTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateHotelTicketModel(
    val id: String,
    val hotelId: String?,
    val ticketNumber: String? = null,
    val price: Double,
    val reservationDate: Long,
    val checkOutDate: Long,
    val roomClass: Int,
    val transportation: Int ? = null,
    val isVisible: Boolean? = true,
    val quantity: Int,
    val numberOfAdults: Int,
    val numberOfChildren: Int,
    val childrenAge: Int,
): Principal
