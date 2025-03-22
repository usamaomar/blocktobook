package com.example.domain.model.profileModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCompanyInfoModel(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val facilityNumber: String? = null,
    val tourismLicense: String? = null,
    val commercialRegister: String? = null,
    val isCompanyInfoVerified: Boolean,
    val canCreateTrip: Boolean? = null,
    val blockToBookFees: Double,
    val createdAt: Long? = 0
) : Principal


fun UpdateCompanyInfoModel.toCompanyInfoModel(): CompanyInfoModel {
    return CompanyInfoModel(
        name = name,
        phoneNumber = phoneNumber,
        facilityNumber = facilityNumber,
        tourismLicense = tourismLicense,
        commercialRegister = commercialRegister,
        isCompanyInfoVerified = isCompanyInfoVerified,
        canCreateTrip = canCreateTrip,
        blockToBookFees = blockToBookFees,
        createdAt = createdAt
    )
}


fun UpdateCompanyInfoModel.toCompanyInfoModelUser(blockToBookFees: Double ,isCompanyInfoVerified: Boolean): CompanyInfoModel {
    return CompanyInfoModel(
        name = name,
        phoneNumber = phoneNumber,
        facilityNumber = facilityNumber,
        tourismLicense = tourismLicense,
        commercialRegister = commercialRegister,
        isCompanyInfoVerified = isCompanyInfoVerified,
        canCreateTrip = canCreateTrip,
        blockToBookFees = blockToBookFees,
        createdAt = createdAt
    )
}
