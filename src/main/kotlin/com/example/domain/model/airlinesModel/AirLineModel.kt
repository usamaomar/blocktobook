package com.example.domain.model.airlinesModel


import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class AirLineModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val profiles: List<AirlineProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String,
    val logo: String
) : Principal


fun AirLineModel.toResponseAirLineModel(): ResponseAirLineModel {
    return ResponseAirLineModel(
        id = this.id?.toHexString() ?: "",
        name = this.referenceName,
        logo = this.logo,
        profiles = this.profiles,
        referenceName = this.referenceName,
        cityId = this.cityId,
        city = null,
        code = this.code
    )
}
