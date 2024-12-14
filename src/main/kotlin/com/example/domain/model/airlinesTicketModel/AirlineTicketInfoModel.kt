package com.example.domain.model.airlinesTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class AirlineTicketInfoModel(
    val travelClass: Int,
    val pricePerSeat: Double,
    val pricePerSeatRoundTrip: Double?=0.0,
    val numberOfSeats: Int ? = 1,
    val numberOfChildren: Int ? = 0,
    val totalAllowances: Int ? = 25,
    val childAge: Int ? = 0,
    val isVisible: Boolean? = true,
) : Principal

