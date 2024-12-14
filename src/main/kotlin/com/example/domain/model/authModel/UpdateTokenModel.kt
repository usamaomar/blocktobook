package com.example.domain.model.authModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTokenModel(
    val userId: String?,
    val token: String?,
    val refreshToken: String?
) : Principal
