package com.example.domain.model.profileModel

import kotlinx.serialization.Serializable

@Serializable
data class CreateCompanyInfoModel(
    val name: String,
    val phoneNumber: String,
    val facilityNumber: String? = null,
    val tourismLicense: String? = null,
    val commercialRegister: String? = null,
    val createdAt: Long? = 0
)

fun CreateCompanyInfoModel.toCompanyInfoModelUser(blockToBookFees: Double ,isCompanyInfoVerified: Boolean): CompanyInfoModel {
    return CompanyInfoModel(
        name = name,
        phoneNumber = phoneNumber,
        facilityNumber = facilityNumber,
        tourismLicense = tourismLicense,
        commercialRegister = commercialRegister,
        isCompanyInfoVerified = isCompanyInfoVerified,
        blockToBookFees = blockToBookFees,
        createdAt = createdAt
    )
}




