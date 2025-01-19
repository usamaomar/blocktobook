package com.example.domain.model.airlinesTicketModel

import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.cityModel.ResponseCityModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class AirlineTicketModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val userId: String,
    val departureCityId: String,
    val arrivalCityId: String,
    val departureAirportId: String,
    val arrivalAirportId: String,
    val airLineId: String,
    val roundTripId: String? = null,
    val departureDate: String,
    val arrivalDate: String,
    val departureTime: String,
    val arrivalTime: String,
    val isRoundTrip: Boolean? = false,
    val travelClass: Int,
    val pricePerSeat: Double,
    val pricePerSeatRoundTrip: Double? = 0.0,
    val numberOfSeats: Int? = 1,
    val numberOfChildren: Int? = 0,
    val totalAllowances: Int? = 25,
    val childAge: Int? = 0,
    val isVisible: Boolean? = null,
) : Principal


fun AirlineTicketModel.toResponseAirlineTicketModel(
    _id: String,
    departureCity: ResponseCityModel?,
    arrivalCity: ResponseCityModel?,
    departureAirport: ResponseAirPortModel?,
    arrivalAirport: ResponseAirPortModel?,
    airLine: ResponseAirLineModel?,
    returnAirLine: ResponseAirLineModel?,
    numberOfSeatsLeft: Int? = 0
): ResponseAirlineTicketModel {
    return ResponseAirlineTicketModel(
        id = _id,
        departureCity = departureCity,
        arrivalCity = arrivalCity,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        airLine = airLine,
        userId = userId,
        isVisible = this.isVisible,
        departureDate = this.departureDate,
        arrivalDate = this.arrivalDate,
        departureTime = this.departureTime,
        roundTripId = this.roundTripId,
        arrivalTime = this.arrivalTime,
        isRoundTrip = this.isRoundTrip,
        travelClass = this.travelClass,
        pricePerSeat = this.pricePerSeat,
        pricePerSeatRoundTrip = this.pricePerSeatRoundTrip,
        numberOfSeats = this.numberOfSeats,
        numberOfSeatsLeft =  numberOfSeatsLeft,
        numberOfChildren = this.numberOfChildren,
        totalAllowances = this.totalAllowances,
        childAge = this.childAge
    )
}
