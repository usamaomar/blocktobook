package com.example.domain.model.airlinesModel

import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.CreateCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

@Serializable
data class UpdateAirLine(
    val id: String,
    val profiles : List<AirlineProfileModel>,
    val referenceName: String,
    val cityId: String,
    val code: String,
    val logo: String,
) : Principal


