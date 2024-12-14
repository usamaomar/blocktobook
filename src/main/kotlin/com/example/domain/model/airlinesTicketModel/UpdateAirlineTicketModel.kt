package com.example.domain.model.airlinesTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAirlineTicketModel(
    val id: String,
    val departureCityId: String,
    val arrivalCityId: String,
    val departureAirportId: String,
    val arrivalAirportId: String,
    val airLineId: String,
    val departureDate: String,
    val arrivalDate: String,
    val departureTime: String,
    val arrivalTime: String,
    val travelClass: Int,
    val pricePerSeat: Double,
    val pricePerSeatRoundTrip: Double?=0.0,
    val numberOfSeats: Int ? = 1,
    val numberOfChildren: Int ? = 0,
    val totalAllowances: Int ? = 25,
    val childAge: Int ? = 0,
    val isVisible: Boolean? = true,
): Principal
