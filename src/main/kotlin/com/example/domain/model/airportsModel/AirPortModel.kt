package com.example.domain.model.airportsModel


import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class AirPortModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val profiles: List<AirPortProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String
) : Principal



fun AirPortModel.toResponseAirPortModel(): ResponseAirPortModel {
    return ResponseAirPortModel(
        id = this.id?.toHexString() ?: "",
        name = this.referenceName,
        profiles = this.profiles,
        referenceName = this.referenceName,
        cityId = this.cityId,
        code = this.code
    )
}
