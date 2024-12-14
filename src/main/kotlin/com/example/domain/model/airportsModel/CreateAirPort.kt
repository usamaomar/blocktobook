package com.example.domain.model.airportsModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

@Serializable
data class CreateAirPort(
    val profiles : List<AirPortProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String,
) : Principal

fun CreateAirPort.toAirPortModel():  AirPortModel {
    return AirPortModel(
        profiles = this.profiles,
        referenceName = this.referenceName,
        cityId = this.cityId,
        code = this.code,
    )
}
