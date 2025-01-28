package com.example.domain.model.airlinesTicketModel

import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAirlineTicketModel(
    val id: String? = null,
    val departureCity: ResponseCityModel?,
    val arrivalCity: ResponseCityModel?,
    val departureAirport: ResponseAirPortModel?,
    val arrivalAirport: ResponseAirPortModel?,
    val airLine: ResponseAirLineModel?,
    val roundTripId: String?=null,
    val departureDate: String,
    val arrivalDate: String,
    val departureTime: String,
    val arrivalTime: String,
    val userId: String,
    val ticketNumber: String? = null,
    val isRoundTrip: Boolean? = false,
    val roundTripDepartureDate: String?=null,
    val roundTripArrivalDate: String?=null,
    val roundTripDepartureTime: String?=null,
    val roundTripArrivalTime: String?=null,
    val returnAirLine: ResponseAirlineTicketModel?=null,
    val travelClass: Int,
    val pricePerSeat: Double,
    val pricePerSeatRoundTrip: Double?=0.0,
    val pricePerInfantRoundTrip: Double?=0.0,
    val numberOfSeats: Int ? = 0,
    val numberOfSeatsLeft: Int ? = 0,
    val numberOfChildren: Int ? = 0,
    val totalAllowances: Int ? = 25,
    val childAge: Int ? = 0,
    val isVisible: Boolean? = null,
    val flightNumber: String? = null,
    val pricePerInfant: Double? = 0.0,
) : Principal



