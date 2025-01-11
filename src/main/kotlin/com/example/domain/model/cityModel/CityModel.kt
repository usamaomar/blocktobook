package com.example.domain.model.cityModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class CityModel(
    @SerialName("_id")
    @Contextual val id: ObjectId? = ObjectId(),
    val profiles: List<CityProfileModel>,
    val twoDigitCountryCode: String,
    val threeDigitCountryCode: String
) : Principal


fun CityModel.toResponseCityModel( xAppLanguageId: Int,_id: String): ResponseCityModel {
    return ResponseCityModel(
        id = id?.toHexString() ?: "",
        name = this.profiles.find { it.languageId == xAppLanguageId }?.name ?: "",
        countryName = this.profiles.find { it.languageId == xAppLanguageId }?.countryName ?: "",
        twoDigitCountryCode = this.twoDigitCountryCode,
        threeDigitCountryCode = this.threeDigitCountryCode,
        profiles = this.profiles
    )
}
