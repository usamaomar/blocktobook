package com.example.domain.model.airlinesModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

@Serializable
data class CreateAirLine(
    val profiles : List<AirlineProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String,
    val logo: String,
) : Principal

fun CreateAirLine.toAirLineModel():  AirLineModel {
    return AirLineModel(
        profiles = this.profiles,
        referenceName = this.referenceName,
        cityId = this.cityId,
        code = this.code,
        logo = this.logo,
    )
}
