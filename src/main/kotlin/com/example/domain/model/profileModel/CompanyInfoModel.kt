package com.example.domain.model.profileModel

import kotlinx.serialization.Serializable

@Serializable
data class CompanyInfoModel(
    val name: String,
    val phoneNumber: String,
    val facilityNumber: String? = null,
    val tourismLicense: String? = null,
    val commercialRegister: String? = null,
    val isCompanyInfoVerified: Boolean,
    val blockToBookFees: Double,
    val createdAt: Long? = 0
)




