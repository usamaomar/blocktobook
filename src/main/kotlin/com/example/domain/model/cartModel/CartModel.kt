package com.example.domain.model.cartModel

import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class CartModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val hotelTicketModel: ResponseHotelTicketModel?,
    val airLineTicketModel: ResponseAirlineTicketModel?,
    val returnAirLineTicketModel: ResponseAirlineTicketModel?,
    val userId: String,
    val checkInDate: Long,
    val checkOutDate: Long,
    val numberOfRooms: Int,
    val numberOfInfants: Int? = null,
) : Principal



