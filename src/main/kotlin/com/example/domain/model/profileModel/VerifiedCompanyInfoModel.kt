package com.example.domain.model.profileModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class VerifiedCompanyInfoModel(
    val userId: String,
    val isCompanyInfoVerified: Boolean,
) : Principal


