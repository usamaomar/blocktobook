package com.example.domain.model.hotelTicketModel

data class RoomAvailability(
    val date: Long,
    val roomsLeft: Int,
    val hotelTicketId: String,
    val dateString: String ?= null,
)