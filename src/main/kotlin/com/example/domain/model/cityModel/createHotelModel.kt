package com.example.domain.model.cityModel


import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

@Serializable
data class CreateCityModel(
    val profiles : List<CityProfileModel>,
    val twoDigitCountryCode: String,
    val threeDigitCountryCode: String
) : Principal


fun CreateCityModel.toCityModel(): CityModel {
    return CityModel(
        profiles = this.profiles,
        twoDigitCountryCode = this.twoDigitCountryCode,
        threeDigitCountryCode = this.threeDigitCountryCode
    )
}
fun CreateCityModel.toRsponceCityModelWithId(): CityModel {
    return CityModel(
        profiles = this.profiles,
        twoDigitCountryCode = this.twoDigitCountryCode,
        threeDigitCountryCode = this.threeDigitCountryCode
    )
}
