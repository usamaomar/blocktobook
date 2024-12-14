package com.example.domain.model.airlinesTicketModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateAirlineTicketModel(
    val departureCityId: String,
    val arrivalCityId: String,
    val departureAirportId: String,
    val arrivalAirportId: String,
    val airLineId: String,
    val departureDate: String,
    val arrivalDate: String,
    val departureTime: String,
    val arrivalTime: String,
    val isRoundTrip: Boolean? = false,
    val roundTripDepartureDate: String?=null,
    val roundTripArrivalDate: String?=null,
    val roundTripDepartureTime: String?=null,
    val roundTripArrivalTime: String?=null,
    val returnAirLineId: String?=null,
    val airlineTicketInfoList: ArrayList<AirlineTicketInfoModel>

) : Principal


//fun CreateTicketModel.toTicketModel(userId : String): TicketModel {
//    return TicketModel(
//        flightNumber = this.flightNumber,
//        reservationNumber = this.reservationNumber,
//        totalAllowances = this.totalAllowances,
//        pricePerSeat = this.pricePerSeat,
//        departureDate = this.departureDate,
//        arrivalDate = this.arrivalDate,
//        departureAirportId = this.departureAirportId,
//        arrivalAirportId = this.arrivalAirportId,
//        airLineId = this.airLineId,
//        travelClass = this.travelClass,
//        userId = userId,
//        isVisible = this.isVisible ?: true,
//        departureTime = this.departureTime,
//        arrivalTime = this.arrivalTime,
//        numberOfSeats = this.numberOfSeats ?: 1
//    )
//}
