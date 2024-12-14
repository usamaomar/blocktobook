package com.example.domain.model.hotelModel


import com.example.domain.model.cityModel.ResponseCityModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class HotelModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val profiles : List<HotelProfileModel>,
    val latitude : Double,
    val longitude : Double,
    val stars : Double,
    val cityId : String,
    val logo: String,
) : Principal

fun HotelModel.toResponseHotelModel(xAppLanguageId: Int, _id: String,cityModel: ResponseCityModel? = null): ResponseHotelModel {
    return ResponseHotelModel(
        id = _id,
        profiles = this.profiles,
        name = this.profiles.find { it.languageId == xAppLanguageId }?.name ?: "",
        longitude = this.longitude,
        latitude = this.latitude,
        stars = this.stars,
        city = cityModel,
        logo = this.logo,
    )
}