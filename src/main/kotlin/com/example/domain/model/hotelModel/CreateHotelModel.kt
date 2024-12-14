package com.example.domain.model.hotelModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

@Serializable
data class CreateHotelModel(
    val profiles : List<HotelProfileModel>,
    val latitude : Double,
    val longitude : Double,
    val stars : Double,
    val cityId : String,
    val logo: String,
) : Principal


fun CreateHotelModel.toHotelModel(): HotelModel {
    return HotelModel(
        profiles = this.profiles,
        latitude = this.latitude,
        longitude = this.longitude,
        cityId = this.cityId,
        stars = this.stars,
        logo = this.logo,
    )
}
