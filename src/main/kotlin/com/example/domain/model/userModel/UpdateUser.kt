package com.example.domain.model.userModel

import com.example.domain.model.profileModel.CompanyInfoModel
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUser(
    val name: String? = null,
    val profilePhoto: String? = null,
    val companyLogo: String? = null,
) : Principal
